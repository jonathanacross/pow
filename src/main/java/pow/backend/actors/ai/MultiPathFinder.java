package pow.backend.actors.ai;

import pow.backend.GameConstants;
import pow.backend.GameState;
import pow.backend.actors.Actor;
import pow.util.Direction;
import pow.util.MathUtils;
import pow.util.Point;

import java.util.*;

// Class to do general pathfinding for Player/Pet AI.
// This computes the path and score for all points nearby to the given actor.
// Assumes normal step movement.
public class MultiPathFinder {

//    private Actor actor;
//    private GameState gs;
    public AiMap aiMap;
    public Map<Point, Double> gScore; // TODO: rename
    public Map<Point, Point> cameFrom;

    public MultiPathFinder(Actor actor, GameState gs) {
//        this.actor = actor;
//        this.gs = gs;
        this.aiMap = new AiMap(actor, gs, GameConstants.MONSTER_VIEW_RADIUS);

        this.gScore = new HashMap<>();
        this.cameFrom = new HashMap<>();

        findPaths();  // initializes gScore and cameFrom
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

    public double maxDanger(List<Point> path) {
        double danger = 0;
        for (int i = 1; i < path.size() - 1; i++) {
            Point p = path.get(i);
            danger = Math.max(danger, aiMap.squareWeights[p.x][p.y]);
        }
        return danger;
    }

    private static Point getBestCandidatePoint(Set<Point> openSet, Map<Point, Double> fScores) {
        Point result = null;
        double bestScore = Double.MAX_VALUE;
        for (Point p : openSet) {
            if (fScores.containsKey(p) && fScores.get(p) < bestScore) {
                result = p;
                bestScore = fScores.get(p);
            }
        }
        return result;
    }

    // assumes start and goal are only 1 apart
    private static double weightedManhattanDist(Point start, Point goal) {
        double d = MathUtils.dist2(start, goal);
        return (d > 1) ? 1.1 : 1.0;
    }

    public void findPaths() {
        Point start = aiMap.actorLoc;
        // The set of nodes already evaluated
        Set<Point> closedSet = new HashSet<>();

        // The set of currently discovered nodes that are not evaluated yet.
        // Initially, only the start node is known.
        Set<Point> openSet = new HashSet<>();
        openSet.add(start);

        // For each node, which node it can most efficiently be reached from.
        // If a node can be reached from many nodes, cameFrom will eventually contain the
        // most efficient previous step.
        cameFrom = new HashMap<>();

        // For each node, the cost of getting from the start node to that node.
        gScore = new HashMap<>();

        // The cost of going from start to start is zero.
        gScore.put(start, 0.0);

        // For each node, the total cost of getting from the start node to the goal
        // by passing by that node. That value is partly known, partly heuristic.
        // TODO: can this be removed?
        Map<Point, Double> fScore = new HashMap<>();
        fScore.put(start, 0.0);

        while (!openSet.isEmpty()) {
            // current = the node in openSet having the lowest fScore value
            Point current = getBestCandidatePoint(openSet, fScore);

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
                // Ignore the neighbor which is already evaluated.
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                // The distance from start to a neighbor
                double distToNeighbor =
                        weightedManhattanDist(current, neighbor)  // cost of moving between nodes
                 + aiMap.squareWeights[neighbor.x][neighbor.y]; // cost of the new node

                double tentative_gScore = gScore.get(current) + distToNeighbor;

                if (!openSet.contains(neighbor)) {
                    // Discover a new node
                    openSet.add(neighbor);
                } else if (tentative_gScore >= gScore.get(neighbor)) {
                    continue;
                }

                // This path is the best until now. Record it.
                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentative_gScore);
                fScore.put(neighbor, gScore.get(neighbor));
            }
        }
    }
}
