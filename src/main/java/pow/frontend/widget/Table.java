package pow.frontend.widget;

import pow.frontend.Style;

import java.awt.Graphics;
import java.util.List;

public class Table implements Widget {

    // rather than list of cells, use tableCells:
    // class TableCell { Widget cell, Alignment alignment, Color background }

    final List<List<Widget>> cells;
    final List<Integer> colWidths;
    final List<Integer> rowHeights;
    final boolean drawHeaderLine;
    final int hSpacing;
    final int vSpacing;

    public Table(List<List<Widget>> cells, List<Integer> colWidths, List<Integer> rowHeights, int hSpacing, int vSpacing, boolean drawHeaderLine) {
        this.cells = cells;
        this.colWidths = colWidths;
        this.rowHeights = rowHeights;
        this.hSpacing = hSpacing;
        this.vSpacing = vSpacing;
        this.drawHeaderLine = drawHeaderLine;
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
            List<Widget> row = cells.get(r);

            int xOffset = x;
            for (int c = 0; c < row.size(); c++) {
                // Show borders of cells for debugging
                // graphics.setColor(Color.RED);
                // graphics.drawRect(xOffset, yOffset, colWidths.get(c), rowHeights.get(r));
                Widget widget = row.get(c);
                // draw left aligned, vertically centered.
                int dy = (rowHeights.get(r) - widget.getHeight()) / 2;
                widget.draw(graphics, xOffset, yOffset + dy);
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
