package pow.frontend.utils.table;

import java.util.ArrayList;
import java.util.List;

public class TableBuilder {
    private List<List<Cell>> cells;
    private List<Integer> colWidths;
    private List<Integer> rowHeights;
    private boolean drawHeaderLine;
    private int specifiedHeight;
    private int spacing;

    public TableBuilder() {
        this.cells = new ArrayList<>();
        this.drawHeaderLine = false;
        this.specifiedHeight = -1;
        this.spacing = 0;
    }

    public void setCells(List<List<Cell>> cells) {
        this.cells = cells;
    }

    public void addRow(List<Cell> row) {
        cells.add(row);
    }

    public void setDrawHeaderLine(boolean drawHeaderLine) {
        this.drawHeaderLine = drawHeaderLine;
    }

    public void setRowHeight(int height) {
        this.specifiedHeight = height;
    }

    public void setColWidths(List<Integer> colWidths) {
        this.colWidths = colWidths;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    private List<Integer> getDefaultHeights() {
        List<Integer> heights = new ArrayList<>();
        for (List<Cell> row : cells) {
            int height = 0;
            for (Cell cell : row) {
                height = Math.max(height, cell.getHeight());
            }
            heights.add(height);
        }
        return heights;
    }

    private List<Integer> getConstantHeights() {
        List<Integer> heights = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            heights.add(specifiedHeight);
        }
        return heights;
    }

    private List<Integer> getDefaultWidths() {
        List<Integer> widths = new ArrayList<>();
        for (int c = 0; c < cells.get(0).size(); c++) {
            int width = 0;
            for (int r = 0; r < cells.size(); r++) {
                width = Math.max(width, cells.get(r).get(c).getWidth());
            }
            widths.add(width);
        }
        return widths;
    }

    public Table build() {
        if (colWidths == null) {
            colWidths = getDefaultWidths();
        }
        if (rowHeights == null) {
            rowHeights = specifiedHeight > 0 ? getConstantHeights() : getDefaultHeights();
        }

        return new Table(cells, colWidths, rowHeights, spacing, drawHeaderLine);
    }
}
