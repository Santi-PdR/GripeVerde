package com.eternalcraft.gripeverde.client.integration;

import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * v0.284.0: las ultimas direcciones a las que se entro, guardadas en un fichero
 * de tres lineas bajo la carpeta de configuracion.
 *
 * No toca el servidores.dat del juego ni la lista de puestos: esto es memoria
 * de la casa, para que la entrada directa no obligue a recordar la direccion
 * cada vez que se cae una partida.
 */
public final class RecentAddresses {

    private static final Logger LOGGER = LogManager.getLogger("GripeVerde/RecentAddresses");
    private static final int MAX = 3;
    private static final String FILE = "gripeverde-recents.txt";
    private static List<String> cache = null;

    private RecentAddresses() {
    }

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE);
    }

    /** Instantanea inmutable, de la direccion mas nueva a la mas vieja. */
    public static synchronized List<String> list() {
        if (cache != null) return cache;
        List<String> out = new ArrayList<>();
        try {
            Path f = file();
            if (Files.isRegularFile(f)) {
                // No cargar un archivo entero para conservar solo tres entradas.
                try (BufferedReader reader = Files.newBufferedReader(f, StandardCharsets.UTF_8)) {
                    String line;
                    while (out.size() < MAX && (line = reader.readLine()) != null) {
                        String address = normalize(line);
                        if (!address.isEmpty() && !out.contains(address)) out.add(address);
                    }
                }
            }
        } catch (Exception exception) {
            LOGGER.warn("No pude leer las direcciones recientes: {}", exception.getMessage());
        }
        cache = List.copyOf(out);
        return cache;
    }

    /** Anota una direccion usada: la primera, sin repetir y sin crecer sin fin. */
    public static synchronized void push(String address) {
        String ip = normalize(address);
        if (ip.isEmpty()) return;
        List<String> current = new ArrayList<>(list());
        current.remove(ip);
        current.add(0, ip);
        while (current.size() > MAX) current.remove(current.size() - 1);
        // Conservar la visita en memoria aunque falle el disco. Otro push,
        // incluso de la misma direccion, vuelve a intentar persistir la lista.
        cache = List.copyOf(current);
        Path temporary = null;
        try {
            Path f = file();
            Files.createDirectories(f.getParent());
            temporary = Files.createTempFile(f.getParent(), FILE + ".", ".tmp");
            Files.write(temporary, current, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, f, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, f, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception exception) {
            LOGGER.warn("No pude guardar las direcciones recientes: {}", exception.getMessage());
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (Exception exception) {
                    LOGGER.warn("No pude limpiar {}: {}", temporary, exception.getMessage());
                }
            }
        }
    }

    private static String normalize(String address) {
        if (address == null) return "";
        String trimmed = address.trim();
        for (int i = 0; i < trimmed.length(); i++) {
            // El formato es una direccion por linea; nunca permitir que una
            // entrada inyecte otra linea o controles invisibles en el historial.
            char c = trimmed.charAt(i);
            if (Character.isISOControl(c)) return "";
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= trimmed.length() || !Character.isLowSurrogate(trimmed.charAt(i + 1))) return "";
                i++;
            } else if (Character.isLowSurrogate(c)) {
                return "";
            }
        }
        return trimmed;
    }
}
