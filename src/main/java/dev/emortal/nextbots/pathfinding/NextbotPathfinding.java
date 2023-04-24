package dev.emortal.nextbots.pathfinding;

import dev.emortal.nextbots.PathNode;
import dev.emortal.nextbots.util.LerpUtil;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

import java.util.*;
import java.util.function.Predicate;

public class NextbotPathfinding {

    public static int WALKABLE_RESOLUTION = 2;

//    public static double heuristic(Point a, Point b) {
//        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y()) + Math.abs(a.z() - b.z());
//    }
    private static double heuristic(Point a, Point b) {
        return a.distanceSquared(b);
    }


    private static List<PathNode> getNeighbours(Instance instance, Point point) {
        List<PathNode> points = new ArrayList<>(Directions.VALUES_SIZE);

        for (Directions value : Directions.VALUES) {
            Point newPoint = point.add(value.asPoint());

            if (isWalkable(instance, newPoint)) {
                points.add(new PathNode(newPoint));
            }
        }

        return points;
    }

    /**
     * Returns the path from target-start (you might want to reverse the list)
     * otherwise returns an empty list if no path found
     */
    public static List<Point> pathfind(Instance instance, Point start, Point target, Predicate<Point> endEarlyPredicate) {
        PathNode startNode = new PathNode(start, 0, heuristic(start, target), null);

        PriorityQueue<PathNode> openList = new PriorityQueue<>((a, b) -> (int) (a.getF() - b.getF()));
        Set<Point> closedList = new HashSet<>(150);

        List<Point> path = new ArrayList<>(100);

        openList.add(startNode);

        int iter = 0;


        while (openList.size() != 0 && iter < 20000) {
            iter++;

            PathNode current = openList.peek();
            openList.remove(current);
            closedList.add(current.getPos());

            if (current.getPos().sameBlock(target) || endEarlyPredicate.test(current.getPos())) {
//            if (current.getPos().sameBlock(target)) {
                // found target!
                PathNode currentPathNode = current;



                while (!currentPathNode.getPos().sameBlock(start)) {
                    path.add(currentPathNode.getPos());

                    if (currentPathNode.getParent() == null) {
                        System.out.println("parent is null");
                    }
                    currentPathNode = currentPathNode.getParent();
                }

                if (path.size() > 1) path.set(path.size() - 1, start);
//                path.set(0, target);

                return path;
            }

            for (PathNode neighbour : getNeighbours(instance, current.getPos())) {
                if (closedList.contains(neighbour.getPos())) continue;

                boolean inSearch = false;
                for (PathNode pathNode : openList) {
                    if (pathNode.getPos().sameBlock(neighbour.getPos())) {
                        inSearch = true;
                        break;
                    }
                }

                double gCostToNeighbour = current.getG() + heuristic(current.getPos(), neighbour.getPos());

                if (!inSearch || gCostToNeighbour < neighbour.getG()) {
                    neighbour.setG(gCostToNeighbour);
                    neighbour.setParent(current);

                    if (!inSearch) {
                        neighbour.setH(heuristic(neighbour.getPos(), target));
                        openList.add(neighbour);
                    }
                }
            }
        }

        return path;
    }


    public static boolean isWalkable(Instance instance, Point point) {
        Block block;
        Block blockUnder;
        try {
            block = instance.getBlock(point);
            blockUnder = instance.getBlock(point.sub(0, 1, 0));
        } catch (NullPointerException e) {
            return false;
        }

        return (!block.isSolid() || Objects.equals(block.getProperty("open"), "true")) && blockUnder.isSolid();
    }

    public static boolean isWalkableDetailed(Instance instance, Point point) {
        Block block;
        Block blockUnder;
        Block blockNorth;
        Block blockEast;
        Block blockSouth;
        Block blockWest;
        try {
            block = instance.getBlock(point);
            blockUnder = instance.getBlock(point.sub(0, 1, 0));
            blockNorth = instance.getBlock(point.sub(0, 1, 0));
            blockEast = instance.getBlock(point.sub(0, 1, 0));
            blockSouth = instance.getBlock(point.sub(0, 1, 0));
            blockWest = instance.getBlock(point.sub(0, 1, 0));
        } catch (NullPointerException e) {
            return false;
        }

        // fix for diagonals
        if ((blockNorth.isSolid() && blockEast.isSolid()) || (blockEast.isSolid() && blockSouth.isSolid()) || (blockSouth.isSolid() && blockWest.isSolid()) || (blockWest.isSolid() && blockNorth.isSolid()))
            return false;

        return (!block.isSolid() || Objects.equals(block.getProperty("open"), "true")) && blockUnder.isSolid();
    }

    public static List<Point> smoothPath(Instance instance, List<Point> currentPath) {
//        ArrayList<Point> smoothedPath = new ArrayList<>(currentPath);

        for (int pointI = 0; pointI < currentPath.size(); pointI++) {
            for (int pointI2 = currentPath.size() - 1; pointI2 >= 0; pointI2--) {
                if (pointI == pointI2) continue;

                pointI = LerpUtil.clamp(pointI, 0, currentPath.size() - 1);
                pointI2 = LerpUtil.clamp(pointI2, 0, currentPath.size() - 1);

                Point point = currentPath.get(pointI);
                Point point2 = currentPath.get(pointI2);

                boolean isWalkable = isWalkableBetweenDetailed(instance, point, point2, WALKABLE_RESOLUTION);
                if (!isWalkable) continue;

                // simplify path
                int smallestIndex = Math.min(pointI, pointI2);
                int largestIndex = Math.max(pointI, pointI2);
                currentPath.subList(smallestIndex + 1, largestIndex).clear();
            }
        }

        return currentPath;
    }

    public static List<Point> splitPath(List<Point> path, int splits) {
        if (splits == 0) return path;
        List<Point> splitPath = new ArrayList<>();

        for (int i = 0; i < path.size() - 1; i++) {
            Point bottom = path.get(i);
            Point top = path.get(i + 1);
            double distance = bottom.distance(top) * splits;

            for (int split = 0; split <= distance; ++split) {
                splitPath.add(LerpUtil.lerp(bottom, top, split / distance));
            }
        }

        return splitPath;
    }

    public static boolean isWalkableBetween(Instance instance, Point a, Point b, int resolution) {
        boolean isWalkable = true;
        double walkdistance = a.distance(b);
        for (double alpha = 0; alpha <= 1.0; alpha += 1.0 / (walkdistance * resolution)) {
            Point inbetweenPoint = LerpUtil.lerp(a, b, alpha);

            if (!isWalkable(instance, inbetweenPoint)) {
                isWalkable = false;
                break;
            }
        }

        return isWalkable;
    }

    public static boolean isWalkableBetweenDetailed(Instance instance, Point a, Point b, int resolution) {
        boolean isWalkable = true;
        double walkdistance = a.distance(b);
        for (double alpha = 0; alpha <= 1.0; alpha += 1.0 / (walkdistance * resolution)) {
            Point inbetweenPoint = LerpUtil.lerp(a, b, alpha);

            if (!isWalkableDetailed(instance, inbetweenPoint)) {
                isWalkable = false;
                break;
            }
        }

        return isWalkable;
    }

}
