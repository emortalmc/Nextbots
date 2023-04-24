package dev.emortal.nextbots.util;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

public class LerpUtil {

    public static double lerp(double a, double b, double alpha) {
        return a + alpha * (b - a);
    }

    public static Point lerp(Point a, Point b, double alpha) {
        return new Vec(
                lerp(a.x(), b.x(), alpha),
                lerp(a.y(), b.y(), alpha),
                lerp(a.z(), b.z(), alpha)
        );
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

}
