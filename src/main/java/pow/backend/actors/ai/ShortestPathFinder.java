package pow.backend.actors.ai;

import pow.backend.GameConstants;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.util.Direction;
import pow.util.MathUtils;
import pow.util.Point;

import java.util.*;

// Class to do path-finding for Player/Pet AI.
// This computes the optimal path and cost for all points nearby to the given actor.
// Assumes normal step movement.
public class ShortestPathFinder {

    public final AiMap aiMap;
    public Map<Point, Double> cost;
    private Map<Point, Point> cameFrom;

    public ShortestPathFinder(Actor actor, GameState gs) {
        this.aiMap = new AiMap(actor, gs, GameConstants.MONSTER_VIEW_RADIUS);
        this.cost = new HashMap<>();
        this.cameFrom = new HashMap<>();

        findPaths();  // initializes cost and cameFrom
    }

    public List<Point> reconstructPath(Point current) {
        List<Point> path = new ArrayList<>();
        path.add(current);

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }

    private static Point getLowestCostPoint(Set<Point> openSet, Map<Point, Double> cost) {
        Point result = null;
        double bestScore = Double.MAX_VALUE;
        for (Point p : openSet) {
            if (cost.containsKey(p) && cost.get(p) < bestScore) {
                result = p;
                bestScore = cost.get(p);
            }
        }
        return result;
    }

    // Assumes start and goal are only 1 apart.
    // Distance is chosen so that moving in cardinal directions are slightly
    // preferred to moving diagonally, to avoid moving in a zig zag when
    // a straight path would work just as well.
    private static double weightedManhattanDist(Point start, Point goal) {
        double d = MathUtils.dist2(start, goal);
        return (d > 1) ? 1.1 : 1.0;
    }

    // Finds the lowest-cost path to every reachable point from the actor location.
    // Uses a slightly modified version of Dijkstra's algorithm.
    private void findPaths() {
        Point start = aiMap.actorLoc;
        // The set of nodes already evaluated.
        Set<Point> closedSet = new HashSet<>();

        // The set of currently discovered nodes that are not evaluated yet.
        Set<Point> openSet = new HashSet<>();
        openSet.add(start);

        // For each node, which node it can most efficiently be reached from.
        // If a node can be reached from many nodes, cameFrom will eventually contain the
        // most efficient previous step.
        cameFrom = new HashMap<>();

        // For each node, the cost of getting from the start node to that node.
        cost = new HashMap<>();
        cost.put(start, 0.0);

        while (!openSet.isEmpty()) {
            // current = the point in openSet having the lowest cost
            Point current = getLowestCostPoint(openSet, cost);

            openSet.remove(current);
            closedSet.add(current);

            List<Point> neighbors = new ArrayList<>();
            for (Direction dir : Direction.ALL) {
                Point p = new Point(current.x + dir.dx, current.y + dir.dy);
                if (aiMap.canMoveTo(p)) {
                    neighbors.add(p);
                }
            }

            for (Point neighbor : neighbors) {
                // Ignore locations which are already evaluated.
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                // The distance from start to a neighbor
                double distToNeighbor =
                        weightedManhattanDist(current, neighbor)  // cost of moving between nodes
                                + aiMap.squareWeights[neighbor.x][neighbor.y]; // cost of the new node

                double tentative_gScore = cost.get(current) + distToNeighbor;

                if (!openSet.contains(neighbor)) {
                    // Discover a new location
                    openSet.add(neighbor);
                } else if (tentative_gScore >= cost.get(neighbor)) {
                    continue;
                }

                // This path is the best until now. Record it.
                cameFrom.put(neighbor, current);
                cost.put(neighbor, tentative_gScore);
            }
        }
    }
}
