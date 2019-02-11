package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.Party;
import pow.backend.SpellParams;
import pow.backend.actors.Player;
import pow.backend.dungeon.DungeonItem;
import pow.backend.utils.AttackUtils;
import pow.frontend.Frontend;
import pow.frontend.Style;
import pow.frontend.WindowDim;
import pow.frontend.utils.ImageController;
import pow.frontend.utils.ImageUtils;
import pow.frontend.utils.KeyInput;
import pow.frontend.utils.KeyUtils;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class PlayerInfoWindow extends AbstractWindow {

    private int viewPane;
    private static final String HELP_STRING = "Press [left]/[right] to change view, c/[esc]/[enter] to close.";

    public PlayerInfoWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        viewPane = 0;
    }

    @Override
    public void processKey(KeyEvent e) {
        KeyInput input = KeyUtils.getKeyInput(e);

        switch (input) {
            case EAST:
                viewPane = (viewPane + 1) % 3;
                frontend.setDirty(true);
                break;
            case WEST:
                viewPane = (viewPane + 3 - 1) % 3;
                frontend.setDirty(true);
                break;
            case OKAY:
            case CANCEL:
            case PLAYER_INFO:
                frontend.close();
                break;
        }
    }

    private String getResistPercent(int bonus) {
        double hitFactor = AttackUtils.getResistance(bonus);
        // Convert to amount resisted, rather than amount hit.
        double resistFactor = (1 - hitFactor);

        DecimalFormat df = new DecimalFormat("##%");
        return df.format(resistFactor);

    }

    private static List<String> getSpellNames(List<SpellParams> spells) {
        List<String> strings = new ArrayList<>(spells.size());
        for (SpellParams spell : spells) {
            strings.add(spell.name);
        }
        return strings;
    }
    private void drawCharInfo(Graphics graphics, Player player, Point where, int width) {
        ImageController.drawTile(graphics, player.image, where.x, where.y);
        int textWidth = width - Style.TILE_SIZE - Style.SMALL_MARGIN;

        graphics.setFont(Style.getDefaultFont());
        FontMetrics textMetrics = graphics.getFontMetrics(Style.getDefaultFont());
        List<String> spellLines = player.spells.isEmpty()
                ? Collections.emptyList()
                : ImageUtils.wrapText("Spells: " + TextUtils.formatList(getSpellNames(player.spells)) + ".", textMetrics, textWidth);

        List<String> lines = new ArrayList<>();
        String winnerString = player.isWinner() ? " (Winner!)" : "";
        lines.add(player.name + winnerString);
        lines.add("");
        lines.add("");
        lines.add("HP:        " + player.getHealth() + "/" + player.getMaxHealth());
        lines.add("MP:        " + player.getMana() + "/" + player.getMaxMana());
        lines.add("");
        lines.add("Exp:       " + player.experience);
        lines.add("Exp next:  " + player.getExpToNextLevel());
        lines.add("Level:     " + player.level);
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
        lines.add("");
        lines.addAll(spellLines);

        graphics.setColor(Color.WHITE);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(lines.get(i),
                    where.x + Style.TILE_SIZE + Style.SMALL_MARGIN,
                    where.y + Style.TILE_SIZE - 5 + i * Style.FONT_SIZE);
        }

        graphics.setColor(Style.SEPARATOR_LINE_COLOR);
        graphics.drawLine(where.x, where.y + Style.TILE_SIZE + 1,
                where.x + width, where.y + Style.TILE_SIZE + 1);
    }

    private void drawMainInfo(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);
        graphics.setFont(Style.getDefaultFont());

        Player player = backend.getGameState().party.player;
        Player pet = backend.getGameState().party.pet;
        int infoWidth = 300;

        drawCharInfo(graphics, player, new Point(2*Style.SMALL_MARGIN, Style.SMALL_MARGIN), infoWidth);
        if (pet != null) {
            drawCharInfo(graphics, pet, new Point(4*Style.SMALL_MARGIN + infoWidth, Style.SMALL_MARGIN), infoWidth);
        }

        // bottom text
        graphics.setColor(Color.WHITE);
        graphics.drawString(HELP_STRING, Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
    }

    private void drawStatsInfo(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Player player = backend.getGameState().party.player;

        graphics.setFont(Style.getDefaultFont());
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

        int dx = Style.TILE_SIZE;
        int dy = Style.TILE_SIZE;
        int numSlots = slotData.size();
        int numBonuses = bonusData.size();

        int gridTop = Style.SMALL_MARGIN + 45;  // top of interior of grid (excluding header)
        int gridLeft = Style.SMALL_MARGIN + 105;  // left of interior of grid (excluding header)

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
            int y = gridTop + dy * sd.position + Style.TILE_SIZE/2 + Style.FONT_SIZE/2;
            graphics.drawString(sd.name, gridLeft - 100, y);
        }

        // grid interior
        for (DungeonItem item: player.equipment.items) {
            int position = slotData.get(item.slot).position;
            int y = gridTop + dy * position + Style.TILE_SIZE/2 + Style.FONT_SIZE/2;

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
        graphics.setColor(Style.SEPARATOR_LINE_COLOR);
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

        // bottom sum
        int[] bonusTotals = new int[DungeonItem.NUM_BONUSES];
        for (DungeonItem item: player.equipment.items) {
            for (int i = 0; i < DungeonItem.NUM_BONUSES; i++) {
                bonusTotals[i] += item.bonuses[i];
            }
        }
        graphics.setColor(Color.WHITE);
        int sumY = gridTop + numSlots*dy + Style.SMALL_MARGIN + Style.FONT_SIZE;
        graphics.drawString("(total)", gridLeft - 100, sumY);
        for (Map.Entry<Integer, StringPosition> entry : bonusData.entrySet()) {
            int bonusIdx = entry.getKey();
            StringPosition bonus = entry.getValue();
            int x = gridLeft + 5 + bonus.position * dx;
            if (bonusTotals[bonusIdx] > 0) {
                graphics.drawString(bonusString(bonusTotals[bonusIdx]), x, sumY);
            }
        }

        // bottom text
        graphics.setColor(Color.WHITE);
        graphics.drawString(HELP_STRING, Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
    }

    private void drawProgressInfo(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Party party = backend.getGameState().party;

        graphics.setFont(Style.getDefaultFont());

        // draw artifacts
        graphics.setColor(Color.WHITE);
        int y1 = Style.SMALL_MARGIN + Style.FONT_SIZE;
        graphics.drawString("Artifacts:", Style.SMALL_MARGIN, y1);
        FontMetrics textMetrics = graphics.getFontMetrics(Style.getDefaultFont());
        graphics.setColor(Style.SEPARATOR_LINE_COLOR);
        graphics.drawLine(Style.SMALL_MARGIN, y1 + 3, dim.width - Style.SMALL_MARGIN, y1 + 3);

        Map<DungeonItem.ArtifactSlot, Point> artifactLocations = new HashMap<>();
        artifactLocations.put(DungeonItem.ArtifactSlot.PETSTATUE, new Point(0,0));
        artifactLocations.put(DungeonItem.ArtifactSlot.LANTERN, new Point(0,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.KEY, new Point(0,2));
        artifactLocations.put(DungeonItem.ArtifactSlot.MAP, new Point(0,3));
        artifactLocations.put(DungeonItem.ArtifactSlot.FLOAT, new Point(0,4));
        artifactLocations.put(DungeonItem.ArtifactSlot.GASMASK, new Point(0,5));
        artifactLocations.put(DungeonItem.ArtifactSlot.PORTALKEY, new Point(0,6));
        artifactLocations.put(DungeonItem.ArtifactSlot.GLASSES, new Point(0,7));
        artifactLocations.put(DungeonItem.ArtifactSlot.PICKAXE, new Point(1,0));
        artifactLocations.put(DungeonItem.ArtifactSlot.BAG, new Point(1,1));
        artifactLocations.put(DungeonItem.ArtifactSlot.TURTLESHELL, new Point(1,2));
        artifactLocations.put(DungeonItem.ArtifactSlot.HEATSUIT, new Point(1,3));
        artifactLocations.put(DungeonItem.ArtifactSlot.XRAYSCOPE, new Point(1,4));
        artifactLocations.put(DungeonItem.ArtifactSlot.LANTERN2, new Point(1,5));
        int dx = 320;
        int dy = Style.TILE_SIZE + Style.FONT_SIZE;
        graphics.setColor(Color.WHITE);
        for (DungeonItem item : party.artifacts.getArtifacts().values()) {
            Point loc = artifactLocations.get(item.artifactSlot);
            int x = Style.MARGIN + loc.x * dx;
            int y = Style.SMALL_MARGIN + 2*Style.FONT_SIZE + loc.y * dy;
            ImageController.drawTile(graphics, item.image, x, y);
            List<String> descLines =
                    ImageUtils.wrapText(item.name + ": " + item.description, textMetrics, dx - (2*Style.SMALL_MARGIN + Style.TILE_SIZE));
            for (int i = 0; i < descLines.size(); i++) {
                graphics.drawString(descLines.get(i), x + Style.TILE_SIZE + Style.SMALL_MARGIN,
                        y + (i+1)*Style.FONT_SIZE);
            }

        }

        // draw returned pearls
        graphics.setColor(Color.WHITE);
        int y2 = 400;
        graphics.drawString("Returned Pearls:", Style.SMALL_MARGIN, y2);
        graphics.setColor(Style.SEPARATOR_LINE_COLOR);
        graphics.drawLine(Style.SMALL_MARGIN, y2 + 3, dim.width - Style.SMALL_MARGIN, y2 + 3);

        Map<String, Point> pearlLocations = new HashMap<>();
        pearlLocations.put("pearl 1", new Point(0, 0));
        pearlLocations.put("pearl 2", new Point(1, 0));
        pearlLocations.put("pearl 3", new Point(2, 0));
        pearlLocations.put("pearl 4", new Point(3, 0));
        pearlLocations.put("pearl 5", new Point(4, 0));
        pearlLocations.put("pearl 6", new Point(5, 0));
        pearlLocations.put("pearl 7", new Point(6, 0));
        pearlLocations.put("pearl 8", new Point(7, 0));

        for (DungeonItem item : party.returnedPearls) {
            if (!pearlLocations.containsKey(item.id)) {
                continue;
            }
            Point loc = pearlLocations.get(item.id);
            int x = Style.MARGIN + (Style.SMALL_MARGIN + Style.TILE_SIZE) * loc.x;
            int y = 410;
            ImageController.drawTile(graphics, item.image, x, y);
        }

        // bottom text
        graphics.setColor(Color.WHITE);
        graphics.drawString(HELP_STRING, Style.SMALL_MARGIN, dim.height - Style.SMALL_MARGIN);
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
        switch (viewPane) {
            case 0: drawMainInfo(graphics); break;
            case 1: drawStatsInfo(graphics); break;
            case 2: drawProgressInfo(graphics); break;
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
