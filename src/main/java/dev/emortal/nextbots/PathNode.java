package dev.emortal.nextbots;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PathNode implements Comparable<PathNode> {

    private Point pos;
    private double g;
    private double h;
    private @Nullable PathNode parent = null;

    public PathNode(Point pos, double g, double h, @Nullable PathNode parent) {
        this.pos = pos;
        this.g = g;
        this.h = h;
        this.parent = parent;
    }

    public PathNode(Point pos) {
        this.pos = pos;
        this.g = Double.MAX_VALUE;
        this.h = Double.MAX_VALUE;
    }

    public Point getPos() {
        return pos;
    }

    public double getG() {
        return g;
    }

    public double getH() {
        return h;
    }

    public double getF() {
        return g + h;
    }

    public @Nullable PathNode getParent() {
        return parent;
    }

    public void setG(double g) {
        this.g = g;
    }

    public void setH(double h) {
        this.h = h;
    }

    public void setParent(@Nullable PathNode parent) {
        this.parent = parent;
    }

    @Override
    public int compareTo(@NotNull PathNode pathNode) {
        return (int) (this.getF() - pathNode.getF());
    }
}
