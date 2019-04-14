package pow.frontend.widget;

import pow.frontend.Style;

import java.awt.*;

public class ScrollBar implements Widget {

    private int scrollBarHeight;
    private int viewSize;
    private int contentSize;
    private final int scrollStep;  // How much to scroll if up/down is pressed.
    private boolean everythingVisible;
    private int position;

    public ScrollBar(int scrollBarHeight, int viewSize, int contentSize, int scrollStep) {
        this.scrollStep = scrollStep;
        this.position = 0;
        adjustDimensions(scrollBarHeight, viewSize, contentSize);
    }

    public void adjustDimensions(int scrollBarHeight, int viewSize, int contentSize) {
        this.scrollBarHeight = scrollBarHeight;
        this.viewSize = viewSize;
        this.contentSize = contentSize;
        this.everythingVisible = viewSize >= contentSize;
    }

    public void scrollUp() {
        if (!everythingVisible) {
            position -= scrollStep;
            position = Math.max(0, position);
        }
    }

    public void scrollDown() {
        if (!everythingVisible) {
            position += scrollStep;
            position = Math.min(contentSize - viewSize, position);
        }
    }

    public int getPosition() {
        return position;
    }

    @Override
    public void draw(Graphics graphics, int x, int y) {
        if (contentSize == 0) {
            return;
        }

        int viewTop = position;
        int viewBottom = Math.min(position + viewSize, contentSize);

        int centerTop = scrollBarHeight * viewTop / contentSize;
        int centerBottom = scrollBarHeight * viewBottom / contentSize;
        graphics.setColor(Style.SEPARATOR_LINE_COLOR);
        graphics.drawLine(x, y, x, y + scrollBarHeight);
        graphics.drawRect(x - 3, centerTop + y, 6, centerBottom - centerTop);
    }

    @Override
    public int getHeight() {
        return scrollBarHeight;
    }

    @Override
    public int getWidth() {
        return 6;
    }
}
