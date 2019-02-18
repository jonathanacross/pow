package pow.frontend.widget;

public class TableCell {
    public enum VertAlign {
        TOP,
        CENTER,
        BOTTOM
    }

    public Widget widget;
    public VertAlign vAlign;

    public TableCell(Widget widget) {
        this.widget = widget;
        this.vAlign = VertAlign.CENTER;
    }

    public TableCell(Widget widget, VertAlign vAlign) {
        this.widget = widget;
        this.vAlign = vAlign;
    }
}
