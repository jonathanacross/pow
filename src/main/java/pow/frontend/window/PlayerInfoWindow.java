package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.action.Attack;
import pow.backend.actors.Actor;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.frontend.Frontend;
import pow.frontend.utils.ImageController;
import pow.util.TextUtils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        lines.add("Speed:     " + player.speed);
        lines.add("");

        Font f = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(lines.get(i), TILE_SIZE + 2 * MARGIN, MARGIN + (i + 1) * FONT_SIZE);
        }

        // draw slot stuff
        Map<DungeonItem.Slot, SlotData> slotData = new HashMap<>();
                slotData.put(DungeonItem.Slot.WEAPON, new SlotData("Weapon", 0));
                slotData.put(DungeonItem.Slot.BOW, new SlotData("Bow", 1));
                slotData.put(DungeonItem.Slot.SHIELD, new SlotData("Shield", 2));
                slotData.put(DungeonItem.Slot.HEADGEAR, new SlotData("Head", 3));
                slotData.put(DungeonItem.Slot.ARMOR, new SlotData("Armor", 4));
                slotData.put(DungeonItem.Slot.CLOAK, new SlotData("Cloak", 5));
                slotData.put(DungeonItem.Slot.RING, new SlotData("Ring", 6));
                slotData.put(DungeonItem.Slot.GLOVES, new SlotData("Gloves", 7));
                slotData.put(DungeonItem.Slot.BOOTS, new SlotData("Boots", 8));
        for (SlotData sd : slotData.values()) {
            graphics.drawString(sd.name, 220, TILE_SIZE * sd.position + 30);
        }
        for (DungeonItem item: player.equipment) {
            int position = slotData.get(item.slot).position;
            ImageController.drawTile(graphics, item.image, 270, TILE_SIZE * position + MARGIN);
            graphics.drawString(TextUtils.singular(item.name), 310, TILE_SIZE * position + 30);
        }
    }

    private static class SlotData {
        public String name;
        public int position;

        public SlotData(String name, int position) {
            this.name = name;
            this.position = position;
        }
    }
}
