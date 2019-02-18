package pow.frontend.widget;

import pow.frontend.Style;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Table implements Widget {

    List<List<TableCell>> cells;
    List<Integer> colWidths;
    List<Integer> rowHeights;
    boolean drawHeaderLine;
    int hSpacing;
    int vSpacing;

    public Table() {
        this.cells = new ArrayList<>();
        this.drawHeaderLine = false;
        this.hSpacing = 0;
        this.vSpacing = 0;
    }

    public void setCells(List<List<TableCell>> cells) {
        this.cells = cells;
    }

    public void addRow(List<TableCell> row) {
        cells.add(row);
    }

    // adds to the end
    public void addColumn(List<TableCell> col) {
        // make sure we have enough rows
        for (int i = cells.size(); i < col.size(); i++) {
            cells.add(new ArrayList<>());
        }

        for (int i = 0; i < col.size(); i++) {
            cells.get(i).add(col.get(i));
        }
    }

    public void setDrawHeaderLine(boolean drawHeaderLine) {
        this.drawHeaderLine = drawHeaderLine;
    }

    public void setColWidths(List<Integer> colWidths) {
        this.colWidths = colWidths;
    }

    public void setHSpacing(int hSpacing) {
        this.hSpacing = hSpacing;
    }

    public void setVSpacing(int vSpacing) {
        this.vSpacing = vSpacing;
    }

    private List<Integer> getDefaultHeights() {
        List<Integer> heights = new ArrayList<>();
        for (List<TableCell> row : cells) {
            int height = 0;
            for (TableCell cell : row) {
                height = Math.max(height, cell.widget.getHeight());
            }
            heights.add(height);
        }
        return heights;
    }

    private List<Integer> getDefaultWidths() {
        List<Integer> widths = new ArrayList<>();
        for (int c = 0; c < cells.get(0).size(); c++) {
            int width = 0;
            for (int r = 0; r < cells.size(); r++) {
                TableCell cell = cells.get(r).get(c);
                int cellAutoWidth = cell.widget.getWidth();
                width = Math.max(width, cellAutoWidth);
            }
            widths.add(width);
        }
        return widths;
    }

    public void autosize() {
        if (cells.isEmpty()) {
            colWidths = Arrays.asList();
            rowHeights = Arrays.asList();
        } else {
            if (colWidths == null) {
                colWidths = getDefaultWidths();
            }
            if (rowHeights == null) {
                rowHeights = getDefaultHeights();
            }
        }
    }

    public int getWidth() {
        int totalWidth = 0;
        for (int cw : colWidths) {
            totalWidth += cw;
        }
        totalWidth += (colWidths.size() - 1) * hSpacing;
        return totalWidth;
    }

    public int getHeight() {
        int totalHeight = 0;
        for (int rh : rowHeights) {
            totalHeight += rh;
        }
        totalHeight += (rowHeights.size() - 1) * vSpacing;
        return totalHeight;
    }

    public void draw(Graphics graphics, int x, int y) {
        // draw cell contents
        int yOffset = y;
        for (int r = 0; r < cells.size(); r++) {
            List<TableCell> row = cells.get(r);

            int xOffset = x;
            for (int c = 0; c < row.size(); c++) {
                // Show borders of cells for debugging
                // graphics.setColor(Color.RED);
                // graphics.drawRect(xOffset, yOffset, colWidths.get(c), rowHeights.get(r));
                TableCell cell = row.get(c);
                // Adjust for vertical alignment.  By default, everything is left aligned.
                int dy = 0;
                switch (cell.vAlign) {
                    case TOP: dy = 0; break;
                    case CENTER: dy = (rowHeights.get(r) - cell.widget.getHeight()) / 2; break;
                    case BOTTOM: dy = rowHeights.get(r) - cell.widget.getHeight(); break;
                }
                cell.widget.draw(graphics, xOffset, yOffset + dy);
                xOffset += colWidths.get(c) + hSpacing;
            }

            yOffset += rowHeights.get(r) + vSpacing;
        }

        // draw header line
        if (drawHeaderLine) {
            int lineY = y + rowHeights.get(0) + (vSpacing / 2);
            graphics.setColor(Style.SEPARATOR_LINE_COLOR);
            graphics.drawLine(x, lineY, x + getWidth(), lineY);
        }
    }
}
