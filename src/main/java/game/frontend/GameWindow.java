package game.frontend;

import game.GameBackend;
import game.GameState;
import game.backend.command.Move;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class GameWindow extends AbstractWindow {

    public GameWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT)
            backend.commandQueue.add(new Move(1, 0));
        else if (e.getKeyCode() == KeyEvent.VK_LEFT)
            backend.commandQueue.add(new Move(-1, 0));
        else if (e.getKeyCode() == KeyEvent.VK_DOWN)
            backend.commandQueue.add(new Move(0, 1));
        else if (e.getKeyCode() == KeyEvent.VK_UP)
            backend.commandQueue.add(new Move(0, -1));
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
   }
}
