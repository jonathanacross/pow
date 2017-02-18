package pow.backend.dungeon.gen.worldgen;

import pow.util.Direction;
import pow.util.TsvReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapGenData {
    public String id;
    public int level;
    public int group;
    public List<Direction> fromDirs;
    public Set<Integer> fromGroups;
    public Set<String> fromIds;

    public MapGenData(String id,
                      int level,
                      int group,
                      List<Direction> fromDirs,
                      Set<Integer> fromGroups,
                      Set<String> fromIds) {
        this.id = id;
        this.level = level;
        this.group = group;
        this.fromDirs = fromDirs;
        this.fromGroups = fromGroups;
        this.fromIds = fromIds;
    }

    public static MapGenData parseMapLinkData(String[] line) {
        String id = line[0];

        int level = Integer.parseInt(line[1]);

        String groupStr = line[2];
        int group = groupStr.isEmpty() ? Integer.MIN_VALUE : Integer.parseInt(groupStr);

        List<Direction> directions = new ArrayList<>();
        if (!line[3].isEmpty()) {
            String[] dirs = line[3].split(",");
            for (String d: dirs) {
                directions.add(Direction.valueOf(d));
            }
        }

        Set<Integer> fromGroups = new HashSet<>();
        if (!line[4].isEmpty()) {
            String[] groups = line[4].split(",");
            for (String g: groups) {
                fromGroups.add(Integer.parseInt(g));
            }
        }

        Set<String> fromIds = new HashSet<>();
        if (!line[5].isEmpty()) {
            String[] fids = line[5].split(",");
            for (String fid: fids) {
                fromIds.add(fid);
            }
        }

        return new MapGenData(id, level, group, directions, fromGroups, fromIds);
    }

    // TODO: add consistency checks to the input file:
    // - first room isn't connected to anything
    // - every other room connects either to an id or a group, but not both
    public static List<MapGenData> readLinkData() throws IOException {
        InputStream tsvStream = GenTopoTest.class.getResourceAsStream("/data/levels.tsv");
        TsvReader reader = new TsvReader(tsvStream);

        List<MapGenData> roomLinkDataList = new ArrayList<>();
        for (String[] line : reader.getData()) {
            MapGenData roomLinkData = parseMapLinkData(line);
            roomLinkDataList.add(roomLinkData);
        }

        return roomLinkDataList;
    }
}
