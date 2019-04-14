package pow.frontend.utils;

import pow.frontend.Style;
import pow.frontend.widget.Table;
import pow.frontend.widget.TableCell;
import pow.util.DebugLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelpController {
    private static final HelpController instance;

    static {
        try {
            instance = new HelpController();
        } catch (Exception e) {
            DebugLogger.fatal(e);
            throw new RuntimeException(e); // so intellij won't complain
        }
    }

    private List<MarkdownReader.MarkdownElement> elements;

    private HelpController() throws IOException {
        InputStream mdFileStream = this.getClass().getResourceAsStream("/help.md");
        List<String> mdLines = readLines(mdFileStream);
        this.elements = MarkdownReader.parseText(mdLines);
    }

    private List<String> readLines(InputStream stream) throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static Table getHelpTable(int width) {
        Table table = new Table();
        for (MarkdownReader.MarkdownElement element : instance.elements) {
            table.addRow(Collections.singletonList(
                    new TableCell(element.convertToWidget(width))
            ));
        }
        table.setVSpacing(Style.MARGIN);
        table.autosize();
        return table;
    }
}
