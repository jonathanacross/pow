package pow.frontend.utils.table;

import pow.frontend.Style;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class Table {

    final List<List<Cell>> cells;
    final List<Integer> colWidths;
    final List<Integer> rowHeights;
    final boolean drawHeaderLine;
    final int spacing;

    public Table(List<List<Cell>> cells, List<Integer> colWidths, List<Integer> rowHeights, int spacing, boolean drawHeaderLine) {
        this.cells = cells;
        this.colWidths = colWidths;
        this.rowHeights = rowHeights;
        this.spacing = spacing;
        this.drawHeaderLine = drawHeaderLine;
    }

    public int getWidth() {
        int totalWidth = 0;
        for (int cw : colWidths) {
            totalWidth += cw;
        }
        totalWidth += (colWidths.size() - 1) * spacing;
        return totalWidth;
    }

    public int getHeight() {
        int totalHeight = 0;
        for (int rh : rowHeights) {
            totalHeight += rh;
        }
        totalHeight += (rowHeights.size() - 1) * spacing;
        return totalHeight;
    }

    public void draw(Graphics graphics, int x, int y) {
        // draw cell contents
        int yOffset = y;
        for (int r = 0; r < cells.size(); r++) {
            List<Cell> row = cells.get(r);

            int xOffset = x;
            for (int c = 0; c < row.size(); c++) {
//                graphics.setColor(Color.RED);
//                graphics.drawRect(xOffset, yOffset, colWidths.get(c), rowHeights.get(r));
                Cell cell = row.get(c);
                cell.draw(graphics, xOffset, yOffset, colWidths.get(c), rowHeights.get(r));
                xOffset += colWidths.get(c) + spacing;
            }

            yOffset += rowHeights.get(r) + spacing;
        }

        // draw header line
        if (drawHeaderLine) {
            int lineY = y + rowHeights.get(0) + (spacing / 2);
            graphics.setColor(Style.SEPARATOR_LINE_COLOR);
            graphics.drawLine(x, lineY, x + getWidth(), lineY);
        }
    }
}
