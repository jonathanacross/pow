package pow.backend.dungeon.gen;

import pow.backend.ActionParams;
import pow.backend.GameMap;
import pow.backend.actors.Actor;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.DungeonTerrain;
import pow.util.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MapGenerator {
    // expand/modify this class to make richer areas
    public static class MapStyle {
        public String borderTerrainId;
        public String interiorTerrainId;
        public List<String> monsterIds;

        public MapStyle(String borderTerrainId, String interiorTerrainId, List<String> monsterIds) {
            this.borderTerrainId = borderTerrainId;
            this.interiorTerrainId = interiorTerrainId;
            this.monsterIds = monsterIds;
        }
    }

    private static DungeonSquare buildTeleportTile(String terrainTemplateName, String target) {
        DungeonTerrain terrainTemplate = TerrainData.getTerrain(terrainTemplateName);

        ActionParams params = new ActionParams();
        // TODO: pull out magic strings somewhere
        params.actionName = "gotoArea";
        params.name = target;
        DungeonTerrain.Flags flags = new DungeonTerrain.Flags(false, false, false, true);
        DungeonTerrain terrain = new DungeonTerrain(
                terrainTemplate.id,
                terrainTemplate.name,
                terrainTemplate.image,
                flags,
                params);

        DungeonSquare square = new DungeonSquare(terrain, null);
        return square;
    }

    public static GameMap genMap(
            String name,
            int width,
            int height,
            MapStyle style,
            Map<String, String> exits,  // name of this exit -> otherAreaId@otherAreaLocName
            Random rng) {

        // fill in a simple area
        DungeonSquare[][] squares = new DungeonSquare[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                DungeonTerrain terrain =
                        (x == 0 | x == width - 1 || y == 0 || y == height - 1) ?
                                TerrainData.getTerrain(style.borderTerrainId) :
                                TerrainData.getTerrain(style.interiorTerrainId);
                squares[x][y] = new DungeonSquare(terrain, null);
            }
        }

        // place the exits
        int w2 = width / 2;
        int h2 = height / 2;
        Map<String, Point> keyLocations = new HashMap<>();
        for (Map.Entry<String, String> entry : exits.entrySet()) {
            String exitName = entry.getKey();
            String target = entry.getValue();
            DungeonSquare square = buildTeleportTile(style.interiorTerrainId, target);

            Point loc = new Point(-1,-1);

            switch (exitName) {
                case "north": loc.x = w2; loc.y = 0; break;
                case "south": loc.x = w2; loc.y = height - 1; break;
                case "west": loc.x = 0; loc.y = h2; break;
                case "east": loc.x = width - 1; loc.y = h2; break;
                default: throw new RuntimeException("unknown exit name " + exitName);
            }

            squares[loc.x][loc.y] = square;
            keyLocations.put(exitName, loc);
        }

        int numMonsters = (width - 1)*(height - 1) / 50;
        List<Actor> monsters = DungeonGenerator.createMonsters(squares, numMonsters, style.monsterIds, rng);
        GameMap map = new GameMap(name, squares, keyLocations, monsters);
        return map;
    }
}
