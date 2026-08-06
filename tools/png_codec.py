"""Pure-Python PNG Decoder and Encoder for Minecraft Texture Generation.
Supports RGB (Type 2), RGBA (Type 6), Indexed (Type 3 with PLTE/tRNS), Grayscale (Type 0).
Always converts to 32-bit RGBA bytearray.
"""
import zlib
import struct

def decode_png(data):
    """Decodes PNG binary data into (width, height, bytearray_of_rgba)."""
    if data[:8] != b'\x89PNG\r\n\x1a\n':
        raise ValueError('Not a valid PNG file signature')
    pos = 8
    width = height = bit_depth = color_type = None
    palette = []
    trns = []
    idat = []
    while pos < len(data):
        length, chunk_type = struct.unpack('>I4s', data[pos:pos+8])
        chunk_data = data[pos+8:pos+8+length]
        pos += 12 + length
        if chunk_type == b'IHDR':
            width, height, bit_depth, color_type, comp, filt, inter = struct.unpack('>IIBBBBB', chunk_data)
        elif chunk_type == b'PLTE':
            palette = [chunk_data[i:i+3] for i in range(0, len(chunk_data), 3)]
        elif chunk_type == b'tRNS':
            trns = list(chunk_data)
        elif chunk_type == b'IDAT':
            idat.append(chunk_data)
    
    decompressed = zlib.decompress(b''.join(idat))
    
    out = bytearray(width * height * 4)
    
    if color_type == 6: # RGBA
        bpp = 4
    elif color_type == 2: # RGB
        bpp = 3
    elif color_type == 3: # Indexed
        bpp = 1
    elif color_type == 0: # Grayscale
        bpp = 1
    else:
        raise ValueError(f'Unsupported color_type: {color_type}')
        
    stride = width * bpp
    raw_pos = 0
    prev_row = bytearray(stride)
    curr_row = bytearray(stride)
    
    def paeth_predictor(a, b, c):
        p = a + b - c
        pa = abs(p - a)
        pb = abs(p - b)
        pc = abs(p - c)
        if pa <= pb and pa <= pc: return a
        elif pb <= pc: return b
        else: return c

    for y in range(height):
        filter_type = decompressed[raw_pos]
        raw_pos += 1
        scanline = decompressed[raw_pos:raw_pos+stride]
        raw_pos += stride
        
        for i in range(stride):
            x_val = scanline[i]
            a = curr_row[i - bpp] if i >= bpp else 0
            b = prev_row[i]
            c = prev_row[i - bpp] if i >= bpp else 0
            
            if filter_type == 0: val = x_val
            elif filter_type == 1: val = (x_val + a) & 0xFF
            elif filter_type == 2: val = (x_val + b) & 0xFF
            elif filter_type == 3: val = (x_val + ((a + b) >> 1)) & 0xFF
            elif filter_type == 4: val = (x_val + paeth_predictor(a, b, c)) & 0xFF
            else: val = x_val
            curr_row[i] = val
        
        for x in range(width):
            out_idx = (y * width + x) * 4
            if color_type == 6:
                out[out_idx:out_idx+4] = curr_row[x*4:x*4+4]
            elif color_type == 2:
                out[out_idx:out_idx+3] = curr_row[x*3:x*3+3]
                out[out_idx+3] = 255
            elif color_type == 3:
                idx = curr_row[x]
                if idx < len(palette):
                    r, g, b = palette[idx]
                else:
                    r, g, b = (0, 0, 0)
                a = trns[idx] if idx < len(trns) else 255
                out[out_idx] = r
                out[out_idx+1] = g
                out[out_idx+2] = b
                out[out_idx+3] = a
            elif color_type == 0:
                g = curr_row[x]
                out[out_idx] = g
                out[out_idx+1] = g
                out[out_idx+2] = g
                out[out_idx+3] = 255
        prev_row[:] = curr_row[:]
        
    return width, height, out

def encode_png_rgba(width, height, rgba_data):
    """Encodes a 32-bit RGBA bytearray to a standard PNG file binary."""
    raw = bytearray()
    stride = width * 4
    for y in range(height):
        raw.append(0) # Filter 0 (None)
        raw.extend(rgba_data[y*stride:(y+1)*stride])
    compressed = zlib.compress(bytes(raw), level=9)
    
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    ihdr_crc = zlib.crc32(b'IHDR' + ihdr_data)
    ihdr_chunk = struct.pack('>I4s', 13, b'IHDR') + ihdr_data + struct.pack('>I', ihdr_crc)
    
    idat_crc = zlib.crc32(b'IDAT' + compressed)
    idat_chunk = struct.pack('>I4s', len(compressed), b'IDAT') + compressed + struct.pack('>I', idat_crc)
    
    iend_crc = zlib.crc32(b'IEND')
    iend_chunk = struct.pack('>I4s', 0, b'IEND') + struct.pack('>I', iend_crc)
    
    return b'\x89PNG\r\n\x1a\n' + ihdr_chunk + idat_chunk + iend_chunk
