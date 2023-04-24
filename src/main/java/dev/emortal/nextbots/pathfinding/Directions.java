package dev.emortal.nextbots.pathfinding;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;

public enum Directions {
    A(0, 0, -1),
    B(0, 0, 1),
    C(-1, 0, 0),
    D(1, 0, 0),

    E(0, 1, -1),
    F(0, 1, 1),
    G(-1, 1, 0),
    H(1, 1, 0),

    I(0, -1, -1),
    J(0, -1, 1),
    K(-1, -1, 0),
    L(1, -1, 0);
    //    C(-1, -1),
//    E(-1, 1),
//    F(1, -1);
//    H(1, 1);

    private int offX;
    private int offY;
    private int offZ;
    Directions(int offX, int offY, int offZ) {
        this.offX = offX;
        this.offY = offY;
        this.offZ = offZ;
    }

    public Point asPoint() {
        return new Vec(this.offX, this.offY, this.offZ);
    }

    public int getOffX() {
        return offX;
    }

    public int getOffZ() {
        return offZ;
    }

    public static final Directions[] VALUES = values();
    public static final int VALUES_SIZE = VALUES.length;
}
