package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameConstants;
import pow.backend.GameState;
import pow.backend.dungeon.gen.worldgen.MapTopologySummary;
import pow.backend.dungeon.gen.worldgen.SpacialConnection;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.util.Point3D;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorldMapWindow extends AbstractWindow {

    private static final Color UNSEEN_FILL_COLOR = Color.BLACK;
    private static final Color UNSEEN_EDGE_COLOR =
            GameConstants.PLAYER_CAN_SEE_UNKNOWN_AREAS ? Color.DARK_GRAY : Color.BLACK;
    private static final Color VISITED_FILL_COLOR = Color.DARK_GRAY;
    private static final Color VISITED_EDGE_COLOR = Color.GRAY;
    private static final Color ACTIVE_FILL_COLOR = Color.ORANGE;

    // stuff we extract out of the map topology
    private int xmin;
    private int ymin;
    private int mapWidth;
    private int mapHeight;
    private Map<Point, RoomStatus> mainLayerStatus;
    private Map<Point, RoomStatus> otherLayerStatus;


    public WorldMapWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.close();
    }

    private enum RoomStatus {
        UNSEEN,
        VISITED,
        ACTIVE
    }

    @Override
    public void drawContents(Graphics graphics) {
        extractDimensions();
        extractRoomStatuses();

        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        GameState gs = backend.getGameState();
        MapTopologySummary topology = gs.world.topologySummary;
        Map<Point3D, String> roomLocs = topology.getRoomLocs();

        int roomSize = 20;  // these may have to be scaled to fit in the display
        int roomSpacing = 20;
        int roomGridSize = roomSize + roomSpacing;

        int marginX = (dim.width - (roomGridSize * mapWidth)) / 2;
        int marginY = (dim.height - (roomGridSize * mapHeight)) / 2;

        // draw rooms
        for (Map.Entry<Point3D, String> entry : roomLocs.entrySet()) {
            Point3D p = entry.getKey();
            if (p.z != 0) continue;

            int x = p.x - xmin;
            int y = p.y - ymin;
            int baseX = marginX + x * roomGridSize;
            int baseY = marginY + y * roomGridSize;

            Point location = new Point(p.x, p.y);
            // draw outer
            RoomStatus mainStatus = mainLayerStatus.get(location);
            drawRoom(graphics, baseX, baseY, roomSize, roomSize, mainStatus);

            // draw inner
            RoomStatus otherStatus = otherLayerStatus.get(location);
            drawRoom(graphics, baseX + roomSize / 4,
                    baseY + roomSize / 4,
                    roomSize / 2, roomSize / 2, otherStatus);
       }

        // draw connections
        Set<SpacialConnection> connections = topology.getConnections();
        for (SpacialConnection spacialConnection : connections) {
            Point3D p = spacialConnection.fromLoc;

            if (p.z != 0) continue;
            int x = p.x - xmin;
            int y = p.y - ymin;
            int baseX = marginX + x * roomGridSize;
            int baseY = marginY + y * roomGridSize;

            String roomId = roomLocs.get(p);
            boolean visited = gs.world.world.get(roomId).visited;
            graphics.setColor(visited ? VISITED_EDGE_COLOR : UNSEEN_EDGE_COLOR);

            switch (spacialConnection.dir) {
                case N:
                    graphics.drawLine(baseX + roomSize / 2, baseY,
                            baseX + roomSize / 2, baseY - roomSpacing / 2);
                    break;
                case S:
                    graphics.drawLine(baseX + roomSize / 2, baseY + roomSize,
                            baseX + roomSize / 2, baseY + roomSize + roomSpacing / 2);
                    break;
                case E:
                    graphics.drawLine(baseX + roomSize, baseY + roomSize / 2,
                            baseX + roomSize + roomSpacing / 2, baseY + roomSize / 2);
                    break;
                case W:
                    graphics.drawLine(baseX, baseY + roomSize / 2,
                            baseX - roomSpacing / 2, baseY + roomSize / 2);
                    break;
               default:
                    break;
            }
        }
    }

    // Side effects: sets xmin, ymin, mapWidth, mapHeight
    private void extractDimensions() {
        GameState gs = backend.getGameState();
        MapTopologySummary topology = gs.world.topologySummary;
        Map<Point3D, String> roomLocs = topology.getRoomLocs();

        // Find the bounds of what to print.
        xmin = Integer.MAX_VALUE;
        ymin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymax = Integer.MIN_VALUE;
        for (Point3D p : roomLocs.keySet()) {
            if (p.x < xmin) xmin = p.x;
            if (p.x > xmax) xmax = p.x;
            if (p.y < ymin) ymin = p.y;
            if (p.y > ymax) ymax = p.y;
        }

        mapWidth = xmax - xmin + 1;
        mapHeight = ymax - ymin + 1;
    }

    private void updateStatus(Map<Point, RoomStatus> statusMap, Point p, boolean visited, boolean active) {
        // active supersedes everything else
        if (active) {
            statusMap.put(p, RoomStatus.ACTIVE);
            return;
        }

        // upgrade to visited status if not already active
        if (visited) {
            if (!statusMap.containsKey(p) || statusMap.get(p) != RoomStatus.ACTIVE) {
                statusMap.put(p, RoomStatus.VISITED);
                return;
            }
        }

        // if we're hitting this point at all, the room must exist, but not yet visited
        if (!statusMap.containsKey(p)) {
            statusMap.put(p, RoomStatus.UNSEEN);
        }
    }


    // Sets mainLayerStatus, otherLayerStatus.
    private void extractRoomStatuses() {
        mainLayerStatus = new HashMap<>();
        otherLayerStatus = new HashMap<>();

        GameState gs = backend.getGameState();
        MapTopologySummary topology = gs.world.topologySummary;
        Map<Point3D, String> roomLocs = topology.getRoomLocs();

        for (Map.Entry<Point3D, String> entry : roomLocs.entrySet()) {
            String roomId = entry.getValue();
            Point3D p3d = entry.getKey();
            Point p2d = new Point(p3d.x, p3d.y);
            boolean visited = gs.world.world.get(roomId).visited;
            boolean active = roomId.equals(gs.getCurrentMap().id);

            if (p3d.z != 0) {
                updateStatus(otherLayerStatus, p2d, visited, active);
            } else {
                updateStatus(mainLayerStatus, p2d, visited, active);
            }
        }
    }

    private void drawRoom(Graphics graphics, int left, int top, int width, int height, RoomStatus status) {
        if (status == null) {
            return;
        }
        switch (status) {
            case UNSEEN: graphics.setColor(UNSEEN_FILL_COLOR); break;
            case VISITED: graphics.setColor(VISITED_FILL_COLOR); break;
            case ACTIVE: graphics.setColor(ACTIVE_FILL_COLOR); break;
        }
        graphics.fillRect(left, top, width, height);
        switch (status) {
            case UNSEEN: graphics.setColor(UNSEEN_EDGE_COLOR); break;
            case VISITED: graphics.setColor(VISITED_EDGE_COLOR); break;
            case ACTIVE: graphics.setColor(VISITED_EDGE_COLOR); break;
        }
        graphics.drawRect(left, top, width, height);
    }
}
