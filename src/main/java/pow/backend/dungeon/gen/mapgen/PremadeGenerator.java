package pow.backend.dungeon.gen.mapgen;

import pow.backend.GameMap;
import pow.backend.dungeon.DungeonSquare;
import pow.backend.dungeon.MonsterIdGroup;
import pow.backend.dungeon.gen.*;
import pow.backend.dungeon.gen.worldgen.MapPoint;
import pow.backend.utils.GeneratorUtils;
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
    private final GameMap.Flags flags;

    public PremadeGenerator(PremadeMapData.PremadeMapInfo premadeMapInfo, ProtoTranslator translator,
                            MonsterIdGroup monsterIds, int level, GameMap.Flags flags) {
        this.premadeMapInfo = premadeMapInfo;
        this.translator = translator;
        this.monsterIds = monsterIds;
        this.level = level;
        this.flags = flags;
    }

    @Override
    public GameMap genMap(String name,
                          List<MapConnection> connections,
                          MapPoint.PortalStatus portalStatus,
                          Random rng) {

        DungeonSquare[][] dungeonSquares = GeneratorUtils.convertToDungeonSquares(premadeMapInfo.data, translator);

        // place the exits and get key locations
        GeneratorUtils.CommonIds commonIds = new GeneratorUtils.CommonIds(
                translator.getTerrain(Constants.TERRAIN_FLOOR).id,
                translator.getFeature(Constants.FEATURE_UP_STAIRS).id,
                translator.getFeature(Constants.FEATURE_DOWN_STAIRS).id,
                translator.getFeature(Constants.FEATURE_OPEN_PORTAL).id,
                translator.getFeature(Constants.FEATURE_CLOSED_PORTAL).id);
        // TODO: replace upstairs, downstairs rather than default.
        Map<String, Point> keyLocations = GeneratorUtils.addDefaultExits(
                connections,
                portalStatus,
                dungeonSquares,
                commonIds,
                rng);

        // add items
        int numItems = GeneratorUtils.getDefaultNumItems(premadeMapInfo.data, rng);
        GeneratorUtils.addItems(level, dungeonSquares, numItems, rng);

        return new GameMap(name, level, dungeonSquares, keyLocations, new MonsterIdGroup(monsterIds), flags,null);
    }
}
