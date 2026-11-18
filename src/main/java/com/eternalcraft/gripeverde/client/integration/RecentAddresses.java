package com.eternalcraft.gripeverde.client.integration;

import net.minecraft.client.Minecraft;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final int MAX = 3;
    private static final String FILE = "gripeverde-recents.txt";
    private static List<String> cache = null;

    private RecentAddresses() {
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(FILE);
    }

    /** Las direcciones recordadas, de la mas nueva a la mas vieja. */
    public static synchronized List<String> list() {
        if (cache != null) return cache;
        List<String> out = new ArrayList<>();
        try {
            Path f = file();
            if (Files.isRegularFile(f)) {
                for (String line : Files.readAllLines(f, StandardCharsets.UTF_8)) {
                    String s = line == null ? "" : line.trim();
                    if (s.isEmpty() || out.contains(s)) continue;
                    out.add(s);
                    if (out.size() >= MAX) break;
                }
            }
        } catch (Throwable ignored) {
        }
        cache = out;
        return out;
    }

    /** Anota una direccion usada: la primera, sin repetir y sin crecer sin fin. */
    public static synchronized void push(String address) {
        try {
            String ip = address == null ? "" : address.trim();
            if (ip.isEmpty()) return;
            List<String> current = new ArrayList<>(list());
            current.remove(ip);
            current.add(0, ip);
            while (current.size() > MAX) current.remove(current.size() - 1);
            cache = current;
            Path f = file();
            try {
                Files.createDirectories(f.getParent());
            } catch (Throwable ignored) {
            }
            Files.write(f, current, StandardCharsets.UTF_8);
        } catch (Throwable ignored) {
        }
    }
}
