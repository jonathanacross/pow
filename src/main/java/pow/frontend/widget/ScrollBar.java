package pow.frontend.widget;

import pow.frontend.Style;

import java.awt.*;

public class ScrollBar implements Widget {

    private final int scrollBarHeight;
    private final int viewSize;
    private final int contentSize;
    private final int scrollStep;  // How much to scroll if up/down is pressed.
    private final boolean everythingVisible;
    private int position;

    public ScrollBar(int scrollBarHeight, int viewSize, int contentSize, int scrollStep) {
        this.scrollBarHeight = scrollBarHeight;
        this.viewSize = viewSize;
        this.contentSize = contentSize;
        this.scrollStep = scrollStep;
        this.everythingVisible = viewSize >= contentSize;
        this.position = 0;
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

        int sbLeft = x;
        int sbTop = y;

        int viewTop = position;
        int viewBottom = Math.min(position + viewSize, contentSize);

        int centerTop = scrollBarHeight * viewTop / contentSize;
        int centerBottom = scrollBarHeight * viewBottom / contentSize;
        graphics.setColor(Style.SEPARATOR_LINE_COLOR);
        graphics.drawLine(sbLeft, sbTop, sbLeft, sbTop + scrollBarHeight);
        graphics.drawRect(sbLeft - 3, centerTop + sbTop, 6, centerBottom - centerTop);
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
