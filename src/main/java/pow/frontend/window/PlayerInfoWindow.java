package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.utils.AttackUtils;
import pow.frontend.Frontend;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class PlayerInfoWindow extends AbstractWindow {

    private boolean mainView;
    private final Map<DungeonItem.Slot, StringPosition> slotData;

    public PlayerInfoWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        mainView = true;

        slotData = new HashMap<>();
        slotData.put(DungeonItem.Slot.WEAPON, new StringPosition("Weapon", 0));
        slotData.put(DungeonItem.Slot.BOW, new StringPosition("Bow", 1));
        slotData.put(DungeonItem.Slot.SHIELD, new StringPosition("Shield", 2));
        slotData.put(DungeonItem.Slot.HEADGEAR, new StringPosition("Head", 3));
        slotData.put(DungeonItem.Slot.ARMOR, new StringPosition("Armor", 4));
        slotData.put(DungeonItem.Slot.CLOAK, new StringPosition("Cloak", 5));
        slotData.put(DungeonItem.Slot.RING, new StringPosition("Ring", 6));
        slotData.put(DungeonItem.Slot.BRACELET, new StringPosition("Bracelet", 7));
        slotData.put(DungeonItem.Slot.AMULET, new StringPosition("Amulet", 8));
        slotData.put(DungeonItem.Slot.GLOVES, new StringPosition("Gloves",9));
        slotData.put(DungeonItem.Slot.BOOTS, new StringPosition("Boots", 10));
    }

    @Override
    public void processKey(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            frontend.close();
            return;
        }

        if (keyCode == KeyEvent.VK_TAB || keyCode == KeyEvent.VK_SPACE) {
            mainView = !mainView;
            frontend.setDirty(true);
        }
    }

    final private int TILE_SIZE = 32;
    final private int MARGIN = 15;
    final private int FONT_SIZE = 12;

    private String getResistPercent(int bonus) {
        double hitFactor = AttackUtils.getResistance(bonus);
        // Convert to amount resisted, rather than amount hit.
        double resistFactor = (1 - hitFactor);

        DecimalFormat df = new DecimalFormat("##%");
        return df.format(resistFactor);

    }

    private void drawMainInfo(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Player player = backend.getGameState().party.player;

        ImageController.drawTile(graphics, player.image, MARGIN, MARGIN);

        List<String> lines = new ArrayList<>();
        String winnerString = player.isWinner() ? " (Winner!)" : "";
        lines.add(player.name + winnerString);
        lines.add("");
        lines.add("HP:        " + player.getHealth() + "/" + player.getMaxHealth());
        lines.add("MP:        " + player.getMana() + "/" + player.getMaxMana());
        lines.add("Exp:       " + player.experience);
        lines.add("Exp next:  " + player.getExpToNextLevel());
        lines.add("Level:     " + player.level);
        lines.add("Gold:      " + player.gold);
        lines.add("");
        lines.add("Str:       " + player.baseStats.strength);
        lines.add("Dex:       " + player.baseStats.dexterity);
        lines.add("Int:       " + player.baseStats.intelligence);
        lines.add("Con:       " + player.baseStats.constitution);
        lines.add("");
        lines.add("Attack:    " + player.getPrimaryAttack());   // 2d4 (+3, +1)
        if (player.hasBowEquipped()) {
            lines.add("Bow:       " + player.getSecondaryAttack());  // 1d2 (+2, +0)
        } else {
            lines.add("Bow:       N/A");
        }
        lines.add("Defense:   " + player.getDefense()); // [19, +5]
        lines.add("Speed:     " + player.getSpeed());
        lines.add("");
        lines.add("rFire:     " + getResistPercent(player.baseStats.resFire));
        lines.add("rCold:     " + getResistPercent(player.baseStats.resCold));
        lines.add("rAcid:     " + getResistPercent(player.baseStats.resAcid));
        lines.add("rElec:     " + getResistPercent(player.baseStats.resElec));
        lines.add("rPois:     " + getResistPercent(player.baseStats.resPois));
        lines.add("rDam:      " + getResistPercent(player.baseStats.resDam));

        Font f = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(lines.get(i), TILE_SIZE + 2 * MARGIN, MARGIN + (i + 1) * FONT_SIZE);
        }

        // draw slot stuff
        for (StringPosition sd : slotData.values()) {
            graphics.drawString(sd.name, 260, MARGIN + TILE_SIZE * sd.position + TILE_SIZE/2 + FONT_SIZE/2);
        }
        for (DungeonItem item: player.equipment.items) {
            int position = slotData.get(item.slot).position;
            int y = TILE_SIZE * position + MARGIN;
            ImageController.drawTile(graphics, item.image, 315, y);
            graphics.drawString(TextUtils.format(item.name, 1, false),  355, y + FONT_SIZE + 2);
            graphics.drawString(item.bonusString(), 355, y + 2*FONT_SIZE + 2);
        }

        // draw artifacts
        Map<DungeonItem.ArtifactSlot, Point> artifactLocations = new HashMap<>();
        artifactLocations.put(DungeonItem.ArtifactSlot.PETSTATUE, new Point(0,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.LANTERN, new Point(1,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.KEY, new Point(2,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.MAP, new Point(3,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.FLOAT, new Point(4,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.GASMASK, new Point(5,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.PORTALKEY, new Point(6,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.GLASSES, new Point(7,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.PICKAXE, new Point(8,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.BAG, new Point(9,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.TURTLESHELL, new Point(10,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.HEATSUIT, new Point(11,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.XRAYSCOPE, new Point(12,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.LANTERN2, new Point(13,1));

        for (DungeonItem item : player.party.artifacts.getArtifacts().values()) {
            Point loc = artifactLocations.get(item.artifactSlot);
            int x = 15 + TILE_SIZE * loc.x;
            int y = 370 + TILE_SIZE * loc.y;
            ImageController.drawTile(graphics, item.image, x, y);
        }

        // bottom text
        graphics.setColor(Color.WHITE);
        graphics.drawString("Press [space]/[tab] to change view, [esc] to close.", MARGIN, dim.height - MARGIN);
    }

    private void drawStatsInfo(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Player player = backend.getGameState().party.player;
        ImageController.drawTile(graphics, player.image, MARGIN, MARGIN);

        Font f = new Font("Courier", Font.PLAIN, FONT_SIZE);
        graphics.setFont(f);
        graphics.setColor(Color.WHITE);

        // draw slot stuff
        Map<DungeonItem.Slot, StringPosition> slotData = new HashMap<>();
        slotData.put(DungeonItem.Slot.WEAPON, new StringPosition("Weapon", 0));
        slotData.put(DungeonItem.Slot.BOW, new StringPosition("Bow", 1));
        slotData.put(DungeonItem.Slot.SHIELD, new StringPosition("Shield", 2));
        slotData.put(DungeonItem.Slot.HEADGEAR, new StringPosition("Head", 3));
        slotData.put(DungeonItem.Slot.ARMOR, new StringPosition("Armor", 4));
        slotData.put(DungeonItem.Slot.CLOAK, new StringPosition("Cloak", 5));
        slotData.put(DungeonItem.Slot.RING, new StringPosition("Ring", 6));
        slotData.put(DungeonItem.Slot.BRACELET, new StringPosition("Bracelet", 7));
        slotData.put(DungeonItem.Slot.AMULET, new StringPosition("Amulet", 8));
        slotData.put(DungeonItem.Slot.GLOVES, new StringPosition("Gloves",9));
        slotData.put(DungeonItem.Slot.BOOTS, new StringPosition("Boots", 10));

        Map<Integer, StringPosition> bonusData = new HashMap<>();
        bonusData.put(DungeonItem.TO_HIT_IDX, new StringPosition("hit", 0));
        bonusData.put(DungeonItem.TO_DAM_IDX, new StringPosition("dam", 1));
        bonusData.put(DungeonItem.DEF_IDX, new StringPosition("def", 2));
        bonusData.put(DungeonItem.STR_IDX, new StringPosition("str", 3));
        bonusData.put(DungeonItem.DEX_IDX, new StringPosition("dex", 4));
        bonusData.put(DungeonItem.INT_IDX, new StringPosition("int", 5));
        bonusData.put(DungeonItem.CON_IDX, new StringPosition("con", 6));
        bonusData.put(DungeonItem.RES_FIRE_IDX, new StringPosition("rFire", 7));
        bonusData.put(DungeonItem.RES_COLD_IDX, new StringPosition("rCold", 8));
        bonusData.put(DungeonItem.RES_ACID_IDX, new StringPosition("rAcid", 9));
        bonusData.put(DungeonItem.RES_ELEC_IDX, new StringPosition("rElec", 10));
        bonusData.put(DungeonItem.RES_POIS_IDX, new StringPosition("rPois", 11));
        bonusData.put(DungeonItem.RES_DAM_IDX, new StringPosition("rDam", 12));
        bonusData.put(DungeonItem.SPEED_IDX, new StringPosition("speed", 13));
        bonusData.put(DungeonItem.WEALTH_IDX, new StringPosition("wealth", 14));
        bonusData.put(DungeonItem.SOCKETS_IDX, new StringPosition("sockets", 15));

        int dx = TILE_SIZE;
        int dy = TILE_SIZE;
        int numSlots = slotData.size();
        int numBonuses = bonusData.size();

        int gridTop = MARGIN + 50;  // top of interior of grid (excluding header)
        int gridLeft = MARGIN + 105;  // left of interior of grid (excluding header)

        // header on top
        for (StringPosition bonus : bonusData.values()) {
            int x = gridLeft + 15 + bonus.position * dx;
            int y = gridTop - 5;
            drawRotated(graphics, bonus.name, x, y, -45);
        }

        // left icons
        for (DungeonItem item: player.equipment.items) {
            int position = slotData.get(item.slot).position;
            int x = gridLeft - 40;
            int y = gridTop + dy * position;
            ImageController.drawTile(graphics, item.image, x, y);
        }

        // left item types
        for (StringPosition sd : slotData.values()) {
            int y = gridTop + dy * sd.position + TILE_SIZE/2 + FONT_SIZE/2;
            graphics.drawString(sd.name, gridLeft - 100, y);
        }

        // grid interior
        for (DungeonItem item: player.equipment.items) {
            int position = slotData.get(item.slot).position;
            int y = gridTop + dy * position + TILE_SIZE/2 + FONT_SIZE/2;

            for (Map.Entry<Integer, StringPosition> entry : bonusData.entrySet()) {
                int bonusIdx = entry.getKey();
                StringPosition bonus = entry.getValue();
                int x = gridLeft + 5 + bonus.position * dx;
                if (item.bonuses[bonusIdx] > 0) {
                    graphics.drawString(bonusString(item.bonuses[bonusIdx]), x, y);
                }
            }
        }

        // grid interior lines
        graphics.setColor(Color.DARK_GRAY);
        for (int bonusIdx = 1; bonusIdx < numBonuses; bonusIdx++) {
            graphics.drawLine(gridLeft + dy*bonusIdx, gridTop, gridLeft + dy*bonusIdx, gridTop + dy*numSlots);
        }
        for (int slotIdx = 1; slotIdx < numSlots; slotIdx++) {
            graphics.drawLine(gridLeft, gridTop + slotIdx*dy, gridLeft + numBonuses*dx, gridTop + slotIdx*dy);
        }

        // grid border
        graphics.setColor(Color.GRAY);
        graphics.drawLine(gridLeft, gridTop, gridLeft, gridTop + dy*numSlots);
        graphics.drawLine(gridLeft, gridTop, gridLeft + numBonuses*dx, gridTop);
        graphics.drawLine(gridLeft + dy*numBonuses, gridTop, gridLeft + dy*numBonuses, gridTop + dy*numSlots);
        graphics.drawLine(gridLeft, gridTop + numSlots*dy, gridLeft + numBonuses*dx, gridTop + numSlots*dy);

        // bottom text
        graphics.setColor(Color.WHITE);
        graphics.drawString("Press [space]/[tab] to change view, [esc] to close.", MARGIN, dim.height - MARGIN);
    }

    // right justifies string, assumes bonus is <= 999.
    private static String bonusString(int bonus) {
        return String.format("%3s", bonus);
    }

    private static void drawRotated(Graphics g, String text, double x, double y, int angle) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate((float)x,(float)y);
        g2d.rotate(Math.toRadians(angle));
        g2d.drawString(text,0,0);
        g2d.rotate(-Math.toRadians(angle));
        g2d.translate(-(float)x,-(float)y);
    }

    @Override
    public void drawContents(Graphics graphics) {
        if (mainView) {
            drawMainInfo(graphics);
        } else {
            drawStatsInfo(graphics);
        }
    }

    private static class StringPosition {
        final String name;
        final int position;

        StringPosition(String name, int position) {
            this.name = name;
            this.position = position;
        }
    }
}
