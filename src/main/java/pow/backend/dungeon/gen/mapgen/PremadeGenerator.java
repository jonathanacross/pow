package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.gen.*;
import pow.util.Array2D;
import pow.util.Point;

import java.util.List;
import java.util.Map;
import java.util.Random;

// generates various types of test areas.
public class PremadeGenerator implements MapGenerator {

    PremadeMapData.PremadeMapInfo premadeMapInfo;
    private int level;
    private ProtoTranslator translator;
    private List<String> monsterIds;

    public PremadeGenerator(PremadeMapData.PremadeMapInfo premadeMapInfo, int level, ProtoTranslator translator, List<String> monsterIds) {
        this.premadeMapInfo = premadeMapInfo;
        this.level = level;
        this.translator = translator;
        this.monsterIds = monsterIds;
    }

    @Override
    public GameMap genMap(String name,
                          List<MapConnection> connections,
                          Random rng) {

        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(premadeMapInfo.data, translator);

        // place the exits and get key locations
        String upstairsFeatureId = translator.getFeature(Constants.FEATURE_UP_STAIRS).id;
        String downstairsFeatureId =  translator.getFeature(Constants.FEATURE_DOWN_STAIRS).id;
        String floorTerrainId = translator.getTerrain(Constants.TERRAIN_FLOOR).id;

        // TODO: replace upstairs, downstairs rather than default.
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                dungeonSquares,
                floorTerrainId,
                upstairsFeatureId,
                downstairsFeatureId,
                rng);

        // add items
        int width = Array2D.width(premadeMapInfo.data);
        int height = Array2D.height(premadeMapInfo.data);
        int numItems = (width - 1) * (height - 1) / 100;
        GeneratorUtils.addItems(level, dungeonSquares, numItems, rng);

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, monsterIds, null);
        return map;
    }
}
