package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.frontend.Frontend;
import pow.frontend.utils.ImageController;

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
        lines.add("HP:        " + player.health + "/" + player.maxHealth);
        lines.add("MP:        " + player.mana + "/" + player.maxMana);
        lines.add("Exp:       " + player.experience);
        lines.add("Exp next:  " + player.getExpToNextLevel());
        lines.add("Level:     " + player.level);
        lines.add("Gold:      " + player.gold);
        lines.add("");
        lines.add("Str:       " + player.currStats.strength);
        lines.add("Dex:       " + player.currStats.dexterity);
        lines.add("Int:       " + player.currStats.intelligence);
        lines.add("Con:       " + player.currStats.constitution);
        lines.add("");
        lines.add("Attack:    " + player.attack);   // 2d4 (+3, +1)
        lines.add("Bow:       " + player.bowAttack);  // 1d2 (+2, +0)
        lines.add("Defense:   " + player.defense); // [19, +5]
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
                slotData.put(DungeonItem.Slot.BRACELET, new SlotData("Bracelet", 7));
                slotData.put(DungeonItem.Slot.AMULET, new SlotData("Amulet", 8));
                slotData.put(DungeonItem.Slot.GLOVES, new SlotData("Gloves",9));
                slotData.put(DungeonItem.Slot.BOOTS, new SlotData("Boots", 10));
        for (SlotData sd : slotData.values()) {
            graphics.drawString(sd.name, 245, TILE_SIZE * sd.position + 30);
        }
        for (DungeonItem item: player.equipment) {
            int position = slotData.get(item.slot).position;
            ImageController.drawTile(graphics, item.image, 295, TILE_SIZE * position + MARGIN);
            graphics.drawString(item.stringWithInfo(), 335, TILE_SIZE * position + 30);
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
