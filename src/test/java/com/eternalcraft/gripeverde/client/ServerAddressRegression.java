package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.render.DiaryChrome;

/**
 * Headless regression checks against the real Minecraft ServerAddress parser.
 * Run via ./gradlew verifyServerAddressRegression (also required by check/build).
 * No DNS requests, sockets, rendering or Minecraft client instance are used.
 */
public final class ServerAddressRegression {
    private static int checks;

    private ServerAddressRegression() {
    }

    private static void valid(String address, boolean expected) {
        boolean actual = DiaryChrome.looksLikeAddress(address);
        if (actual != expected) {
            throw new AssertionError("Validity for " + address + ": expected " + expected + ", got " + actual);
        }
        checks++;
    }

    private static void readout(String address, boolean english, String expected) {
        String actual = DiaryChrome.addressReadout(address, english);
        if (!expected.equals(actual)) {
            throw new AssertionError("Readout for " + address + ": expected " + expected + ", got " + actual);
        }
        checks++;
    }

    public static void main(String[] args) {
        valid(null, false);
        valid("", false);
        valid("   ", false);
        valid("localhost", true);
        valid("127.0.0.1", true);
        valid("play.example.org:25565", true);
        valid("play.example.org:65535", true);
        valid("  play.example.org:25565  ", true);
        valid("münchen.example", true);
        valid("[::1]", true);
        valid("[::1]:25566", true);
        valid("[2001:db8::1234]", true);
        valid("[2001:db8::1234]:25570", true);
        valid("2001:db8::1234", true);
        valid("play.example.org:65536", false);
        valid("play.example.org:-1", false);
        valid("play.example.org:notaport", false);
        valid("[::1]:65536", false);
        valid("[::1", false);
        valid(":25565", false);
        valid("two words.example", false);
        valid("host\tname.example", false);
        valid("host\nname.example", false);
        valid("host\u00a0name.example", false);
        valid("host\u0001name.example", false);
        valid("https://play.example.org", false);

        readout("localhost", true, "host: localhost  ·  port: 25565");
        readout("127.0.0.1:25570", false, "anfitrion: 127.0.0.1  ·  puerto: 25570");
        readout("[::1]", true, "host: ::1  ·  port: 25565");
        readout("[::1]:25566", false, "anfitrion: ::1  ·  puerto: 25566");
        readout("2001:db8::1234", true, "host: 2001:db8::1234  ·  port: 25565");
        readout("[2001:db8::1234]:25570", true, "host: 2001:db8::1234  ·  port: 25570");
        readout("münchen.example", true, "host: xn--mnchen-3ya.example  ·  port: 25565");
        readout("  localhost:25566  ", true, "host: localhost  ·  port: 25566");
        readout(null, true, "");
        readout("", false, "");
        readout("[::1", true, "");
        readout("host:65536", true, "");
        System.out.println("Server address regression: " + checks + " checks passed (no network).");
    }
}
