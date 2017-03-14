package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.dungeon.gen.*;
import pow.util.Point;

import java.util.List;
import java.util.Map;
import java.util.Random;

// generates various types of test areas.
public class PremadeGenerator implements MapGenerator {

    private final PremadeMapData.PremadeMapInfo premadeMapInfo;
    private final ProtoTranslator translator;
    private final int level;
    private final MonsterIdGroup monsterIds;

    public PremadeGenerator(PremadeMapData.PremadeMapInfo premadeMapInfo, ProtoTranslator translator, MonsterIdGroup monsterIds, int level) {
        this.premadeMapInfo = premadeMapInfo;
        this.translator = translator;
        this.monsterIds = monsterIds;
        this.level = level;
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
        int numItems = GeneratorUtils.getDefaultNumItems(premadeMapInfo.data, rng);
        GeneratorUtils.addItems(level, dungeonSquares, numItems, rng);

        GameMap map = new GameMap(name, level, dungeonSquares, keyLocations, new MonsterIdGroup(monsterIds), null);
        return map;
    }
}
