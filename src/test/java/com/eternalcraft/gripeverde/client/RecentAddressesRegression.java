package com.eternalcraft.gripeverde.client;

import com.eternalcraft.gripeverde.client.integration.RecentAddresses;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Real history/UTF-8/filesystem checks in isolated Forge config directories. */
public final class RecentAddressesRegression {
    private static int checks;

    private static void check(boolean value, String reason) {
        checks++;
        if (!value) throw new AssertionError(reason);
    }

    private static Path newConfig(Path game) throws Exception {
        FMLPaths.loadAbsolutePaths(game);
        var cache = RecentAddresses.class.getDeclaredField("cache");
        cache.setAccessible(true);
        cache.set(null, null);
        return FMLPaths.CONFIGDIR.get().resolve("gripeverde-recents.txt");
    }

    private static void history(String... expected) {
        check(RecentAddresses.list().equals(List.of(expected)), "history order/content: " + List.of(expected));
    }

    private static void noTemporaryFiles(Path config) throws Exception {
        try (var files = Files.list(config)) {
            check(files.noneMatch(path -> path.getFileName().toString().endsWith(".tmp")), "temporary files cleaned");
        }
    }

    private static void immutable(Runnable mutation) {
        try {
            mutation.run();
            throw new AssertionError("history must not be mutable by a caller");
        } catch (UnsupportedOperationException expected) {
            checks++;
        }
    }

    private static void ordering(Path root) throws Exception {
        Path file = newConfig(root.resolve("order"));
        history();
        check(!Files.exists(file), "reading absent history creates no file");
        immutable(() -> RecentAddresses.list().add("injected.test"));
        RecentAddresses.push(" first.test ");
        history("first.test");
        List<String> firstSnapshot = RecentAddresses.list();
        RecentAddresses.push("second.test");
        RecentAddresses.push("third.test");
        RecentAddresses.push("fourth.test");
        history("fourth.test", "third.test", "second.test");
        check(firstSnapshot.equals(List.of("first.test")), "older snapshots remain stable");
        RecentAddresses.push(" second.test ");
        history("second.test", "fourth.test", "third.test");
        check(Files.readAllLines(file).equals(RecentAddresses.list()), "file order equals memory order");
        List<String> snapshot = RecentAddresses.list();
        immutable(() -> snapshot.set(0, "injected.test"));
        immutable(snapshot::clear);
        immutable(() -> snapshot.remove("second.test"));
        var iterator = snapshot.iterator();
        iterator.next();
        immutable(iterator::remove);
        noTemporaryFiles(file.getParent());
        newConfig(root.resolve("order"));
        history("second.test", "fourth.test", "third.test");
    }

    private static void loading(Path root) throws Exception {
        Path file = newConfig(root.resolve("loading"));
        String source = "\n  first.test  \nfirst.test\n\t\nsecond.test\nbad\u0000entry\nthird.test\nfourth.test\n";
        Files.writeString(file, source);
        history("first.test", "second.test", "third.test");
        check(Files.readString(file).equals(source), "load does not rewrite source");
        Files.writeString(file, "external-change.test\n");
        history("first.test", "second.test", "third.test");
        check(RecentAddresses.list().size() == 3, "cached snapshots do not change after external file writes");

        file = newConfig(root.resolve("bounded-read"));
        byte[] prefix = ("one.test\ntwo.test\nthree.test\n" + "unused.test\n".repeat(4096)).getBytes(StandardCharsets.UTF_8);
        byte[] input = Arrays.copyOf(prefix, prefix.length + 1);
        input[prefix.length] = (byte) 0xFF;
        Files.write(file, input);
        history("one.test", "two.test", "three.test");
        check(Arrays.equals(Files.readAllBytes(file), input), "read stops after three unique entries without decoding damaged tail");
    }

    private static void invalidInputsAndUnicode(Path root) throws Exception {
        Path file = newConfig(root.resolve("inputs"));
        RecentAddresses.push("original.test");
        String original = Files.readString(file);
        for (String input : new String[] {null, "", "   ", "\t\r\n", "one.test\ntwo.test", "one.test\rtwo.test",
                "one\ttest", "one\u0000test", "one\u007Ftest", "one\uD83Dtest", "one\uDE42test"}) {
            RecentAddresses.push(input);
            history("original.test");
            check(Files.readString(file).equals(original), "invalid input does not rewrite history");
        }
        RecentAddresses.push("[2001:db8::1]:25566");
        RecentAddresses.push("mañana.example");
        RecentAddresses.push("\uD83D\uDE42.example");
        history("\uD83D\uDE42.example", "mañana.example", "[2001:db8::1]:25566");
        check(Files.readAllLines(file, StandardCharsets.UTF_8).equals(RecentAddresses.list()), "Unicode and IPv6 round trip unchanged");
        newConfig(root.resolve("inputs"));
        history("\uD83D\uDE42.example", "mañana.example", "[2001:db8::1]:25566");
        noTemporaryFiles(file.getParent());
    }

    private static void failedReplacement(Path root) throws Exception {
        Path file = newConfig(root.resolve("failure"));
        RecentAddresses.push("older.test");
        List<String> previous = RecentAddresses.list();
        Path backup = file.resolveSibling("previous.txt");
        Files.move(file, backup);
        String original = Files.readString(backup);
        Files.createDirectory(file);
        Path marker = file.resolve("keep.txt");
        Files.writeString(marker, "Keep destination");
        RecentAddresses.push("new.test");
        history("new.test", "older.test");
        check(previous.equals(List.of("older.test")), "failure does not mutate previous snapshot");
        check(Files.readString(marker).equals("Keep destination"), "failed replacement leaves destination intact");
        check(Files.readString(backup).equals(original), "previous file remains intact");
        noTemporaryFiles(file.getParent());
        Files.delete(marker);
        Files.delete(file);
        Files.move(backup, file);
        RecentAddresses.push("new.test");
        history("new.test", "older.test");
        check(Files.readAllLines(file).equals(RecentAddresses.list()), "same-address push retries failed persistence");
        noTemporaryFiles(file.getParent());
    }

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("gripeverde-recents-regression-");
        try {
            ordering(root);
            loading(root);
            invalidInputsAndUnicode(root);
            failedReplacement(root);
            System.out.println("Recent addresses: " + checks + " checks passed");
        } finally {
            try (var files = Files.walk(root)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }
}
