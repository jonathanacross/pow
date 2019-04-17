package pow.frontend.widget;

public class TableCell {
    public enum VertAlign {
        TOP,
        CENTER,
        BOTTOM
    }

    public enum HorizAlign {
        LEFT,
        CENTER,
        RIGHT
    }

    public Widget widget;
    public final VertAlign vAlign;
    public final HorizAlign hAlign;

    public TableCell(Widget widget) {
        this(widget, VertAlign.CENTER, HorizAlign.LEFT);
    }

    public TableCell(Widget widget, VertAlign vAlign, HorizAlign hAlign) {
        this.widget = widget;
        this.vAlign = vAlign;
        this.hAlign = hAlign;
    }
}
