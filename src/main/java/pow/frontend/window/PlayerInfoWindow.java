package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.action.Attack;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.frontend.Frontend;
import pow.frontend.utils.ImageController;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class PlayerInfoWindow extends AbstractWindow {

    public PlayerInfoWindow(int x, int y, int width, int height, boolean visible, GameBackend backend, Frontend frontend) {
        super(x, y, width, height, visible, backend, frontend);
    }

    @Override
    public void processKey(KeyEvent e) {
        frontend.close();
    }

    final private int TILE_SIZE = 32;
    final private int MARGIN = 10;
    final private int FONT_SIZE = 12;

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        Player player = backend.getGameState().player;

        ImageController.drawTile(graphics, player.image, MARGIN, MARGIN);

        List<String> lines = new ArrayList<>();
        lines.add(player.name);
        lines.add("");
        lines.add("HP:       " + player.health + "/" + player.maxHealth);
        lines.add("MP:       ");
        lines.add("Exp:      ");
        lines.add("Exp next: ");
        lines.add("Level:    ");
        lines.add("Gold:     ");
        lines.add("");
        lines.add("Str:       ");
        lines.add("Dex:       ");
        lines.add("Int:       ");
        lines.add("Con:       ");
        lines.add("");
        lines.add("Attack:    " + player.attackDamage);
        lines.add("Defense:   " + player.defense);
        lines.add("");

        Font f = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(lines.get(i), TILE_SIZE + 2*MARGIN, MARGIN + (i+1)*FONT_SIZE);
        }
    }
}
