package pow.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Class for reading "block" files, e.g. used for maps.
// Entries are groups of strings, separated by empty lines.
// Lines beginning with // are comments.
public class BlockReader {

    private List<List<String>> data;

    public List<List<String>> getData() {
        return this.data;
    }

    public BlockReader(File file) throws IOException {
        readFile(file);
    }

    public BlockReader(InputStream stream) throws IOException {
        readFile(stream);
    }

    private void readFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        readFile(is);
    }

    private void readFile(InputStream stream) throws IOException {
        boolean betweenGroups = true;

        data = new ArrayList<>();

        List<String> lineGroup = new ArrayList<>();

        try (InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String line;

            while ((line = br.readLine()) != null) {

                // skip comment lines
                if (line.startsWith("//")) continue;
                boolean isEmpty = line.trim().isEmpty();

                if (betweenGroups) {
                    if (!isEmpty) {
                        betweenGroups = false;
                        lineGroup = new ArrayList<>();
                        lineGroup.add(line);
                    }
                } else {
                    if (!isEmpty) {
                        lineGroup.add(line);
                    } else {
                        data.add(lineGroup);
                        betweenGroups = true;
                    }
                }
            }
            // add last entry, if needed
            if (!betweenGroups) {
                data.add(lineGroup);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        InputStream tsvStream = BlockReader.class.getResourceAsStream("/data/rooms.txt");
        BlockReader reader = new BlockReader(tsvStream);

        List<List<String>> data = reader.getData();
        for (List<String> entry : data) {
            System.out.println(entry);
            System.out.println();
        }
    }
}
