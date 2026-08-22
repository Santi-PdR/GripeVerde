package com.eternalcraft.gripeverde.client.util;

/**
 * Interpolacion suave 0.0 -> 1.0 con easing.
 * Usar tick() cada frame con deltaSeconds.
 */
public class EasedValue {
    private float current = 0f;
    private float target = 0f;
    private final float speed;

    public EasedValue(float speedPerSecond) {
        this.speed = speedPerSecond;
    }

    public void setTarget(boolean on) {
        this.target = on ? 1f : 0f;
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

    public float get() {
        return current;
    }

    /** Easing cubic out para movimiento con peso. */
    public float getEased() {
        float t = current;
        return 1f - (float) Math.pow(1f - t, 3);
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
