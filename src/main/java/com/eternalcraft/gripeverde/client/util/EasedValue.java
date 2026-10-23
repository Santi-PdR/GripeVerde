package com.eternalcraft.gripeverde.client.util;

/**
 * Interpolacion suave 0.0 -> 1.0 con easing.
 * Usar tickRealtime() desde render para mantener la misma velocidad a cualquier FPS,
 * o tick(deltaSeconds) cuando el llamador ya dispone de un delta real.
 */
public class EasedValue {
    private float current = 0f;
    private float target = 0f;
    private final float speed;
    private long lastTickNanos = Long.MIN_VALUE;

    public EasedValue(float speedPerSecond) {
        this.speed = speedPerSecond;
    }

    public void setTarget(boolean on) {
        this.target = on ? 1f : 0f;
    }

    /** Sets the value without interpolation for reduced-motion preferences. */
    public void setImmediately(boolean on) {
        this.current = this.target = on ? 1f : 0f;
        this.lastTickNanos = System.nanoTime();
    }

    public void tick(float deltaSeconds) {
        float diff = target - current;
        float step = speed * deltaSeconds;
        if (Math.abs(diff) <= step) {
            current = target;
        } else {
            current += Math.signum(diff) * step;
        }
    }

    /** Advances using elapsed wall time so render FPS does not change animation speed. */
    public void tickRealtime() {
        long now = System.nanoTime();
        if (lastTickNanos != Long.MIN_VALUE) {
            float elapsedSeconds = Math.max(0f, Math.min(0.1f,
                (now - lastTickNanos) / 1_000_000_000f));
            tick(elapsedSeconds);
        }
        lastTickNanos = now;
    }

    public float get() {
        return current;
    }

    /** Easing cubic out para movimiento con peso. */
    public float getEased() {
        float inverse = 1f - current;
        return 1f - inverse * inverse * inverse;
    }

    /** Lerp entre dos colores ARGB usando el valor actual. */
    public int lerpColor(int colorA, int colorB) {
        float t = getEased();
        int a = lerpChannel((colorA >> 24) & 0xFF, (colorB >> 24) & 0xFF, t);
        int r = lerpChannel((colorA >> 16) & 0xFF, (colorB >> 16) & 0xFF, t);
        int g = lerpChannel((colorA >> 8)  & 0xFF, (colorB >> 8)  & 0xFF, t);
        int b = lerpChannel(colorA & 0xFF, colorB & 0xFF, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(int a, int b, float t) {
        return (int) (a + (b - a) * t);
    }
}
