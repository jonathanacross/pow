package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameState;
import pow.backend.command.FireRocket;
import pow.backend.command.Move;
import pow.backend.command.Save;
import pow.frontend.Frontend;
import pow.frontend.effect.GlyphLoc;
import pow.frontend.save.SaveUtils;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GameWindow extends AbstractWindow {

    public GameWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_L:
                backend.commandQueue.add(new Move(1, 0));
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_H:
                backend.commandQueue.add(new Move(-1, 0));
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_J:
                backend.commandQueue.add(new Move(0, 1));
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_K:
                backend.commandQueue.add(new Move(0, -1));
                break;
            case KeyEvent.VK_F:
                backend.commandQueue.add(new FireRocket());
                break;
            case KeyEvent.VK_S:
                backend.commandQueue.add(new Save());
                break;
        }
    }

    @Override
    public void drawContents(Graphics graphics) {
        GameState gs = backend.getGameState();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.WHITE);

        int squareSize = 18;
        Font f = new Font("Courier New", Font.PLAIN, squareSize);
        graphics.setFont(f);
        for (int r = 0; r < gs.map.height; r++) {
            for (int c = 0; c < gs.map.width; c++) {
                graphics.drawString(Character.toString(gs.map.map[r][c]), c*squareSize, (r+1)*squareSize);
            }
        }
        graphics.drawString("@", gs.x*squareSize, (gs.y+1)*squareSize);

        if (! frontend.getEffects().isEmpty()) {
            for (GlyphLoc glyphLoc : frontend.getEffects().get(0).render()) {
                graphics.drawString(Character.toString(glyphLoc.getC()), glyphLoc.getX() * squareSize, (glyphLoc.getY() + 1)* squareSize);
            }
        }
   }
}
