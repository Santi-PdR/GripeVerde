#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Gripe Verde — regeneracion de los loops de ambiente del gabinete (v0.24.0).

POR QUE EXISTE ESTE SCRIPT
--------------------------
Diagnostico del reporte "no se escucha el ambiente" (v0.23.x): el motor,
las instancias, el slider y los eventos estaban SANOS; el contenido de los
archivos estaba casi en silencio. Medicion (soundfile, RMS mono):

    wind_urban_loop.ogg        RMS 0.0006 (-64 dB)  -> inaudible
    rain_cobblestone_loop.ogg  RMS 0.0259 (-32 dB)  -> apenas audible
    city_ambient_loop.ogg      RMS 0.0342 (-29 dB)  -> muy bajo
    (referencia: music_title_1 RMS 0.160 (-16 dB),_UI clicks RMS ~0.13)

Este script regenera viento y lluvia con sintesis procedural (ruido coloreado
con envolventes lentas de rafaga, silbido de viento y gotas) y refuerza la
capa de ciudad y dos eventos discretos, cruzando inicio/fin para que el loop
de OpenAL no chasquee. Todo queda en RMS ~0.10-0.13, pico <= 0.95.

Uso:  python3 tools/audio/regenerate_menu_ambience.py
      (idempotente; sobrescribe los OGG in-place)
"""
import os
import numpy as np
import soundfile as sf

SR = 44100
DUR = 30.0
N = int(SR * DUR)
ROOT = os.path.join(os.path.dirname(__file__), "..", "..",
                    "src", "main", "resources", "assets", "gripeverde", "sounds")
LOOP_DIR = os.path.join(ROOT, "menu", "loops")
EVENT_DIR = os.path.join(ROOT, "menu", "events")

# ---------------------------------------------------------------- utilidades

def shaped_noise(rs, n, alpha):
    """Ruido con espectro 1/f^(alpha/2) de amplitud (alpha=2 -> brown)."""
    w = rs.normal(0.0, 1.0, n)
    W = np.fft.rfft(w)
    f = np.fft.rfftfreq(n, 1.0 / SR)
    f[0] = f[1]
    W = W / np.power(f, alpha / 2.0)
    return np.fft.irfft(W, n)

def band(y, lo, hi):
    """Banda con bordes suaves (1/x^4 por lado), dominio frecuencia."""
    Y = np.fft.rfft(y)
    f = np.fft.rfftfreq(len(y), 1.0 / SR)
    f[0] = max(f[1], 1e-6)
    m = 1.0 / (1.0 + (lo / f) ** 4)
    m *= 1.0 / (1.0 + (f / hi) ** 4)
    return np.fft.irfft(Y * m, len(y))

def slow_env(rs, n, hz, lo_v, hi_v):
    """Envolvente aleatoria suave (turbulencia), rango [lo_v, hi_v]."""
    c = rs.normal(0.0, 1.0, n)
    C = np.fft.rfft(c)
    f = np.fft.rfftfreq(n, 1.0 / SR)
    C = C * np.exp(-np.power(f / hz, 2.0))
    s = np.fft.irfft(C, n)
    s -= s.min()
    s /= (s.max() + 1e-9)
    return lo_v + (hi_v - lo_v) * s

def loopify(y, sec=2.5):
    """Funde el final con el principio y recorta: loop perfecto para OpenAL."""
    m = int(sec * SR)
    head = y[:m].copy()
    tail = y[-m:]
    t = np.linspace(0.0, 1.0, m)
    out = y[:-m].copy()
    out[:m] = tail * (1.0 - t) + head * t
    return out

def normalize(y, target_rms, peak_cap=0.90):
    y = y * (target_rms / (np.sqrt(np.mean(y ** 2)) + 1e-12))
    p = np.max(np.abs(y))
    if p > peak_cap:
        # satura solo los picos, sin mover el cuerpo
        y = np.tanh(y / peak_cap) * peak_cap
        y *= target_rms / (np.sqrt(np.mean(y ** 2)) + 1e-12)
    return np.clip(y, -peak_cap, peak_cap)

def soft_limit(y, ceiling=0.80):
    """Limitador suave que solo actua por encima de la mitad del techo:
    el cuerpo queda intacto y los picos se comprimen hacia `ceiling`."""
    knee = ceiling * 0.55
    out = np.copy(y)
    hot = np.abs(out) > knee
    out[hot] = np.sign(out[hot]) * (
        knee + (ceiling - knee) * np.tanh((np.abs(out[hot]) - knee) / (ceiling - knee)))
    return out

def measure(y):
    return float(np.sqrt(np.mean(y ** 2))), float(np.max(np.abs(y)))

# ------------------------------------------------------------------ viento

def synth_wind(seed=1897):
    rs = np.random.RandomState(seed)
    bed = band(shaped_noise(rs, N, 1.7), 60.0, 850.0)
    gust = np.power(slow_env(rs, N, 0.045, 0.45, 1.0), 2.0)   # rafagas de ~10-30 s
    swirl = slow_env(rs, N, 0.5, 0.75, 1.0)                    # remolinos de 1-2 s
    rumble = band(shaped_noise(rs, N, 2.0), 28.0, 110.0) \
             * slow_env(rs, N, 0.08, 0.30, 1.0) * 0.55
    # silbido que solo aparece en las rafagas (porteo por edificios)
    wf = 340.0 + 210.0 * slow_env(rs, N, 0.05, 0.0, 1.0) \
              + 45.0 * slow_env(rs, N, 0.35, 0.0, 1.0)
    phase = 2.0 * np.pi * np.cumsum(wf) / SR
    wamp = np.power(np.clip(slow_env(rs, N, 0.035, -0.4, 1.0), 0.0, 1.0), 3.0) * 0.16
    whistle = np.sin(phase) * wamp
    y = bed * gust * swirl + rumble + whistle
    y = loopify(y)
    return normalize(y, 0.13)

# ------------------------------------------------------------------ lluvia

def synth_rain(seed=1918):
    rs = np.random.RandomState(seed)
    hiss = band(shaped_noise(rs, N, 1.0), 350.0, 5200.0) \
           * slow_env(rs, N, 0.7, 0.82, 1.0)                  # siseo con oleaje
    body = band(shaped_noise(rs, N, 1.3), 120.0, 900.0) \
           * 0.5 * slow_env(rs, N, 0.25, 0.6, 1.0)            # masa grave
    low = band(shaped_noise(rs, N, 2.2), 40.0, 140.0) * 0.12  # trueno lejano sordo
    # gotas discretas: picos exponenciales de banda alta
    drops = np.zeros(N)
    n_drops = 1400
    pos = np.sort(rs.randint(0, N - 4000, n_drops))
    for p in pos:
        L = rs.randint(int(0.012 * SR), int(0.030 * SR))
        tau = L / 3.5
        t = np.arange(L)
        env = np.exp(-t / tau)
        burst = rs.normal(0.0, 1.0, L)
        burst = band(burst, 1200.0, 5200.0)
        amp = 0.05 + 0.37 * (rs.rand() ** 3.0)                # pocas bien audibles
        drops[p:p + L] += env * burst * amp
    y = soft_limit(hiss + body + low + drops, 0.68)
    y = loopify(y)
    # Tope 0.72: el codificador Vorbis sobresale ~15% en transientes de gota.
    return normalize(y, 0.12, peak_cap=0.72)

# ------------------------------------------------------- aplicacion a disco

def write(path, y, label):
    rms, peak = measure(y)
    sf.write(path, y, SR, format="OGG", subtype="VORBIS")
    print(f"  {label:28s} RMS={rms:.4f} peak={peak:.3f} -> {os.path.basename(path)}")

def boost(path, gain, cap=0.95, skip_if_rms_above=None):
    """Gain sobre un archivo ya grabado. Idempotente: si el RMS actual
    sugiere que el refuerzo ya se aplico (o el original era fuerte), se salta."""
    d, rate = sf.read(path)
    rms_now, _ = measure(d)
    if skip_if_rms_above is not None and rms_now > skip_if_rms_above:
        print(f"  boost x{gain:<4} {os.path.basename(path):28s} OMITIDO (RMS {rms_now:.4f} ya alto)")
        return
    y = np.clip(d * gain, -cap, cap)
    sf.write(path, y, rate, format="OGG", subtype="VORBIS")
    rms, peak = measure(y)
    print(f"  boost x{gain:<4} {os.path.basename(path):28s} RMS={rms:.4f} peak={peak:.3f}")

def main():
    print("Regenerando loops del gabinete (v0.24.0)...")
    write(os.path.join(LOOP_DIR, "wind_urban_loop.ogg"), synth_wind(), "wind (sintetizado)")
    write(os.path.join(LOOP_DIR, "rain_cobblestone_loop.ogg"), synth_rain(), "rain (sintetizado)")
    boost(os.path.join(LOOP_DIR, "city_ambient_loop.ogg"), 3.0, skip_if_rms_above=0.06)
    # Los susurros y el primer paso tambien estaban ahogados (v0.24.0):
    boost(os.path.join(EVENT_DIR, "ambient_occasional.ogg"), 3.0, skip_if_rms_above=0.012)
    boost(os.path.join(EVENT_DIR, "footsteps_distant_1.ogg"), 3.0, skip_if_rms_above=0.018)
    print("Hecho.")

if __name__ == "__main__":
    main()
