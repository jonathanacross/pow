package pow.frontend.utils;

import pow.frontend.Style;
import pow.frontend.widget.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Reads a *very* limited set of markdown.  Supported features:
// - paragraphs
// - tables (requiring |s at beginning and end of each row).  Allows tables without a header row, as well)
// - section headers with # and ##
// - one level of unordered lists, using asterisks *
public class MarkdownReader {

    public abstract static class MarkdownElement {
        public abstract Widget convertToWidget(int width);
    }

    public static class MarkdownParagraph extends MarkdownElement {
        final String text;

        public MarkdownParagraph(List<String> lines) {
            List<String> trimmedLines = new ArrayList<>();
            for (String line : lines) {
                trimmedLines.add(line.trim());
            }

            this.text = String.join(" ", trimmedLines);
        }

        @Override
        public Widget convertToWidget(int width) {
            return new TextBox(Collections.singletonList(text), State.NORMAL, Style.getDefaultFont(), width);
        }
    }

    public static class MarkdownHeader extends MarkdownElement {
        final int level;
        final String text;

        public MarkdownHeader(List<String> lines) {
            if (lines.get(0).startsWith("##")) {
                this.level = 2;
            } else {
                // starts with "#"
                this.level = 1;
            }
            // Remove #'s from the beginning and trim.
            List<String> trimmedLines = new ArrayList<>();
            for (String line : lines) {
                String tLine = line.replaceAll("^#+", "").trim();
                trimmedLines.add(tLine);
            }
            this.text = String.join(" ", trimmedLines);
        }

        public Widget convertToWidget(int width) {
            State state = this.level == 1 ? State.HEADER1 : State.HEADER2;
            return new TextBox(Collections.singletonList(text), state, Style.getDefaultFont(), width);
        }
    }

    public static class MarkdownList extends MarkdownElement {
        final String text;

        public MarkdownList(List<String> lines) {
            // Remove #'s from the beginning and trim.
            List<String> trimmedLines = new ArrayList<>();
            for (String line : lines) {
                String tLine = line.replaceAll("^\\*", "").trim();
                trimmedLines.add(tLine);
            }

            this.text = String.join(" ", trimmedLines);
        }

        @Override
        public Widget convertToWidget(int width) {
            Table table = new Table();
            table.addRow(Arrays.asList(
                    // unicode 2022 is a bullet
                    new TableCell(new TextBox(Collections.singletonList("\u2022"), State.NORMAL, Style.getDefaultFont()),
                            TableCell.VertAlign.TOP, TableCell.HorizAlign.LEFT),
                    new TableCell(new TextBox(Collections.singletonList(this.text), State.NORMAL, Style.getDefaultFont(),
                            width - 12 - Style.MARGIN))
            ));
            table.autosize();
            table.setHSpacing(Style.MARGIN);
            return table;
        }
    }

    public static class MarkdownTable extends MarkdownElement {
        final boolean hasHeader;
        final List<List<String>> data;

        // true if the line has the format "| ----- | ---- | ------ |"
        private boolean isSepLine(String line) {
            // see if the only characters are {|- }
            String nonHeaderChars = line.replaceAll("[-| ]", "");
            return nonHeaderChars.isEmpty();
        }

        public MarkdownTable(List<String> lines) {
            int numColumns = lines.get(0).split("\\|", -1).length;
            this.hasHeader = lines.size() > 1 && isSepLine(lines.get(1));
            this.data = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                // skip any header separation line
                if (i == 1 && hasHeader) {
                    continue;
                }
                String[] fields = lines.get(i).split("\\|", numColumns);
                List<String> tableLine = new ArrayList<>();
                for (int j = 1; j < numColumns - 1; j++) {
                    tableLine.add(fields[j].trim());
                }
                this.data.add(tableLine);
            }
        }

        @Override
        public Widget convertToWidget(int width) {
            Table table = new Table();
            for (List<String> row : data) {
                List<TableCell> cells = new ArrayList<>();
                for (String entry : row) {
                    cells.add(new TableCell(new TextBox(Collections.singletonList(entry), State.NORMAL, Style.getDefaultFont())));
                }
                table.addRow(cells);
            }
            table.setDrawHeaderLine(hasHeader);
            table.autosize();
            table.setHSpacing(Style.MARGIN);
            return table;
        }
    }

    private static List<List<String>> splitIntoParagraphs(List<String> lines) {
        List<List<String>> paragraphs = new ArrayList<>();
        List<String> currParagraph = new ArrayList<>();
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                // end current paragraph
                if (!currParagraph.isEmpty()) {
                    paragraphs.add(currParagraph);
                    currParagraph = new ArrayList<>();
                }
            } else {
                currParagraph.add(trimmedLine);
            }
        }
        if (!currParagraph.isEmpty()) {
            paragraphs.add(currParagraph);
        }

        return paragraphs;
    }

    private static MarkdownElement parseParagraph(List<String> lines) {
        String firstLine = lines.get(0);
        // use beginning of first line to determine type.
        if (firstLine.startsWith("|")) {
            return new MarkdownTable(lines);
        } else if (firstLine.startsWith("#")) {
            return new MarkdownHeader(lines);
        } else if (firstLine.startsWith("*")) {
            return new MarkdownList(lines);
        } else {
            return new MarkdownParagraph(lines);
        }
    }

    public static List<MarkdownElement> parseText(List<String> lines) {
        List<List<String>> paragraphs = splitIntoParagraphs(lines);

        List<MarkdownElement> markdownElements = new ArrayList<>();
        for (List<String> pLines : paragraphs) {
            markdownElements.add(parseParagraph(pLines));
        }

        return markdownElements;
    }
}
