#!/usr/bin/env python3
"""Execute actual memory shutdown/scheduling methods with a guarded real executor.

The write operation is simulated; the executor, monitor and interrupt flag are
real. The companion Gradle regression checks the real library/filesystem path.
No Minecraft client is launched. CI requires JDK 17.
"""
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

from test_language_screen import method

ROOT = Path(__file__).resolve().parents[1]
SOURCE = (ROOT / 'src/main/java/com/eternalcraft/gripeverde/client/lore/ChroniclerMemory.java').read_text()

HARNESS = '''import java.util.*;
import java.util.concurrent.*;
public class ChroniclerMemory {
    static GuardedExecutor WRITER = new GuardedExecutor();
    static boolean savePending, failFirst;
    static int writes;
    static List<String> threads = new ArrayList<>();
    static void check(boolean value, String reason) { if (!value) throw new AssertionError(reason); }
    static synchronized void writeNow() {
        check(Thread.holdsLock(ChroniclerMemory.class), "write must own monitor");
        writes++; threads.add(Thread.currentThread().getName());
        savePending = failFirst && writes == 1;
    }
    static class GuardedExecutor extends AbstractExecutorService {
        ExecutorService delegate = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "writer"); t.setDaemon(true); return t;
        });
        boolean timeout, interrupt;
        int awaits;
        public void shutdown() { delegate.shutdown(); }
        public List<Runnable> shutdownNow() { return delegate.shutdownNow(); }
        public boolean isShutdown() { return delegate.isShutdown(); }
        public boolean isTerminated() { return delegate.isTerminated(); }
        public void execute(Runnable task) { delegate.execute(task); }
        public boolean awaitTermination(long timeoutValue, TimeUnit unit) throws InterruptedException {
            // Deterministic contract check, not an elapsed-time benchmark.
            check(!Thread.holdsLock(ChroniclerMemory.class), "shutdown wait must not own writer monitor");
            check(unit.toMillis(timeoutValue) == 3000, "existing wait budget retained");
            awaits++;
            if (interrupt) throw new InterruptedException("simulated interruption");
            if (timeout) return false;
            return delegate.awaitTermination(timeoutValue, unit);
        }
    }
    __SHUTDOWN__
    __SCHEDULE__
    public static void main(String[] args) throws Exception {
        Thread.currentThread().setName("closer");
        try {
            switch (args[0]) {
                case "idle":
                    flushForShutdown(); check(writes == 0 && WRITER.awaits == 1, "idle shutdown writes nothing"); break;
                case "pending_inline":
                    savePending = true; flushForShutdown();
                    check(writes == 1 && !savePending && threads.equals(List.of("closer")), "pending state flushed inline"); break;
                case "queued_writer":
                    savePending = true; WRITER.execute(ChroniclerMemory::writeNow); flushForShutdown();
                    check(writes == 1 && !savePending, "queued writer finishes without redundant inline write");
                    check(WRITER.isTerminated() && threads.equals(List.of("writer")), "executor drains before return"); break;
                case "worker_failure":
                    savePending = true; failFirst = true; WRITER.execute(ChroniclerMemory::writeNow); flushForShutdown();
                    check(writes == 2 && !savePending && threads.equals(List.of("writer", "closer")), "failed queued write retried inline"); break;
                case "timeout":
                    savePending = true; WRITER.timeout = true; flushForShutdown();
                    check(writes == 1 && !savePending, "timeout retains inline fallback"); break;
                case "interrupted":
                    savePending = true; WRITER.interrupt = true; flushForShutdown();
                    check(writes == 1 && !savePending, "interruption retains inline fallback");
                    check(Thread.currentThread().isInterrupted(), "interrupt flag restored"); break;
                case "rejected_schedule":
                    savePending = true; WRITER.shutdown(); scheduleWrite();
                    check(writes == 1 && !savePending && threads.equals(List.of("closer")), "rejected work saved inline"); break;
                case "repeat_shutdown":
                    savePending = true; WRITER.execute(ChroniclerMemory::writeNow);
                    flushForShutdown(); flushForShutdown();
                    check(writes == 1 && WRITER.awaits == 2, "repeated shutdown is safe and clean"); break;
                default: throw new AssertionError(args[0]);
            }
        } finally {
            Thread.interrupted();
            WRITER.shutdownNow();
            WRITER.delegate.awaitTermination(3, TimeUnit.SECONDS);
        }
    }
}
'''


class Wiring(unittest.TestCase):
    def test_shutdown_is_not_method_synchronized(self):
        self.assertIn('public static void flushForShutdown()', SOURCE)
        self.assertNotIn('public static synchronized void flushForShutdown()', SOURCE)
        actual = method(SOURCE, 'public static void flushForShutdown(')
        self.assertLess(actual.index('WRITER.awaitTermination('), actual.index('synchronized (ChroniclerMemory.class)'))

    def test_real_writer_remains_synchronized(self):
        self.assertIn('private static synchronized void writeNow()', SOURCE)

    def test_ci_requires_execution(self):
        for path in ('.github/workflows/build.yml', 'tools/ci/build.yml'):
            self.assertIn('python3 tools/test_memory_shutdown.py --require-java', (ROOT / path).read_text())


class Executable(unittest.TestCase):
    def test_shutdown_methods(self):
        if not shutil.which('javac') or not shutil.which('java'):
            if '--require-java' in sys.argv:
                self.fail('JDK 17 required')
            self.skipTest('JDK unavailable; CI requires executable checks')
        shutdown = method(SOURCE, 'public static void flushForShutdown(')
        schedule = method(SOURCE, 'private static void scheduleWrite(')
        cases = ('idle', 'pending_inline', 'queued_writer', 'worker_failure', 'timeout',
                 'interrupted', 'rejected_schedule', 'repeat_shutdown')
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / 'ChroniclerMemory.java'
            path.write_text(HARNESS.replace('__SHUTDOWN__', shutdown).replace('__SCHEDULE__', schedule))
            subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', tmp, str(path)], check=True, timeout=60)
            for case in cases:
                with self.subTest(case=case):
                    subprocess.run(['java', '-cp', tmp, 'ChroniclerMemory', case], check=True, timeout=15)
            print(f'Memory shutdown: {len(cases)} executable scenarios passed')


if __name__ == '__main__':
    unittest.main(argv=[arg for arg in sys.argv if arg != '--require-java'])
