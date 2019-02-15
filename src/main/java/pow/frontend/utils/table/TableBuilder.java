package pow.frontend.utils.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableBuilder {
    private List<List<Cell>> cells;
    private List<Integer> colWidths;
    private List<Integer> rowHeights;
    private boolean drawHeaderLine;
    private int hSpacing;
    private int vSpacing;

    public TableBuilder() {
        this.cells = new ArrayList<>();
        this.drawHeaderLine = false;
        this.hSpacing = 0;
        this.vSpacing = 0;
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
        for (List<Cell> row : cells) {
            int height = 0;
            for (Cell cell : row) {
                height = Math.max(height, cell.getHeight());
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
                Cell cell = cells.get(r).get(c);
                int cellAutoWidth = cell.getWidth();
                width = Math.max(width, cellAutoWidth);
            }
            widths.add(width);
        }
        return widths;
    }

    public Table build() {
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

        return new Table(cells, colWidths, rowHeights, hSpacing, vSpacing, drawHeaderLine);
    }
}
