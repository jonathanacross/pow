package pow.frontend.window;

import pow.backend.GameBackend;
import pow.backend.GameConstants;
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
import pow.frontend.widget.*;
import pow.util.TextUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

public class PlayerInfoWindow extends AbstractWindow {

    private int viewPane;
    private static final String HELP_STRING = "Press [left]/[right]/[space] to change view, c/[esc]/[enter] to close.";

    public PlayerInfoWindow(WindowDim dim, boolean visible, GameBackend backend, Frontend frontend) {
        super(dim, visible, backend, frontend);
        viewPane = 0;
    }

    @Override
    public void processKey(KeyEvent e) {
        KeyInput input = KeyUtils.getKeyInput(e);

        switch (input) {
            case EAST:
            case CYCLE:
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

    private List<Widget> getRow(String key, String value, Font font) {
        List<Widget> row = new ArrayList<>();
        row.add(new Space());
        row.add(new TextBox(Arrays.asList(key), State.NORMAL, font));
        row.add(new TextBox(Arrays.asList(value), State.NORMAL, font));
        return row;
    }

    private void drawCharInfo(Graphics graphics, Player player, Point where, int width) {
        int textWidth = width - Style.TILE_SIZE - 2*Style.SMALL_MARGIN;

        Font font = Style.getDefaultFont();
        graphics.setFont(font);
        FontMetrics textMetrics = graphics.getFontMetrics(Style.getDefaultFont());

        List<String> spellLines = player.spells.isEmpty()
                ? Collections.emptyList()
                : ImageUtils.wrapText("Spells: " + TextUtils.formatList(getSpellNames(player.spells)) + ".", textMetrics, textWidth);
        String winnerString = player.isWinner() ? " (Winner!)" : "";
        String secondaryAttack = player.hasBowEquipped() ? String.valueOf(player.getSecondaryAttack()) : "N/A";

        Table table = new Table();
        List<Widget> header = new ArrayList<>();
        header.add(new Tile(player.image, State.NORMAL));
        header.add(new TextBox(Arrays.asList(player.name + winnerString), State.NORMAL, font));
        header.add(new Space());
        table.addRow(header);

        table.addRow(getRow("HP:        ", player.getHealth() + "/" + player.getMaxHealth(), font));
        table.addRow(getRow("MP:        ", player.getMana() + "/" + player.getMaxMana(), font));
        table.addRow(getRow("", "", font));
        table.addRow(getRow("Exp:       ", "" + player.experience, font));
        table.addRow(getRow("Exp next:  ", "" + player.getExpToNextLevel(), font));
        table.addRow(getRow("Level:     ", "" + player.level, font));
        table.addRow(getRow("", "", font));
        table.addRow(getRow("Str:       ", "" + player.baseStats.strength, font));
        table.addRow(getRow("Dex:       ", "" + player.baseStats.dexterity, font));
        table.addRow(getRow("Int:       ", "" + player.baseStats.intelligence, font));
        table.addRow(getRow("Con:       ", "" + player.baseStats.constitution, font));
        table.addRow(getRow("", "", font));
        table.addRow(getRow("Attack:    ", "" + player.getPrimaryAttack(), font));
        table.addRow(getRow("Bow:       ", "" + secondaryAttack, font));
        table.addRow(getRow("Defense:   ", "" + player.getDefense(), font));
        table.addRow(getRow("Speed:     ", "" + player.getSpeed(), font));
        table.addRow(getRow("", "", font));
        table.addRow(getRow("rFire:     ", "" + getResistPercent(player.baseStats.resFire), font));
        table.addRow(getRow("rCold:     ", "" + getResistPercent(player.baseStats.resCold), font));
        table.addRow(getRow("rAcid:     ", "" + getResistPercent(player.baseStats.resAcid), font));
        table.addRow(getRow("rElec:     ", "" + getResistPercent(player.baseStats.resElec), font));
        table.addRow(getRow("rPois:     ", "" + getResistPercent(player.baseStats.resPois), font));
        table.addRow(getRow("rDam:      ", "" + getResistPercent(player.baseStats.resDam), font));
        table.addRow(getRow("", "", font));
        for (String line : spellLines) {
            table.addRow(getRow(line, "", font));
        }

        table.setColWidths(Arrays.asList(Style.TILE_SIZE + 2*Style.SMALL_MARGIN, 80, textWidth - 80));
        table.setDrawHeaderLine(true);
        table.autosize();

        table.draw(graphics, where.x, where.y);
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
            int y = gridTop + dy * sd.position + Style.TILE_SIZE/2 + Style.getFontSize()/2;
            graphics.drawString(sd.name, gridLeft - 100, y);
        }

        // grid interior
        for (DungeonItem item: player.equipment.items) {
            int position = slotData.get(item.slot).position;
            int y = gridTop + dy * position + Style.TILE_SIZE/2 + Style.getFontSize()/2;

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
        int sumY = gridTop + numSlots*dy + Style.SMALL_MARGIN + Style.getFontSize();
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

    private Table getArtifactTable(Party party, Graphics graphics, Font font) {
        List<List<Widget>> cells = new ArrayList<>();

        // add the header
        List<Widget> header = new ArrayList<>();
        header.add(new TextBox(Arrays.asList("Artifacts:"), State.NORMAL, font));
        header.add(new Space());
        header.add(new Space());
        header.add(new Space());

        cells.add(header);

        // add blank cells; we'll fill these in later
        for (int i = 1; i <= 8; i++) {
            List<Widget> row = new ArrayList<>();
            row.add(new Space(Style.TILE_SIZE, Style.TILE_SIZE));
            row.add(new Space());
            row.add(new Space(Style.TILE_SIZE, Style.TILE_SIZE));
            row.add(new Space());
            cells.add(row);
        }

        // the point x, y refers to the column and row in the widget.
        Map<DungeonItem.ArtifactSlot, Point> artifactLocations = new HashMap<>();
        artifactLocations.put(DungeonItem.ArtifactSlot.PETSTATUE, new Point(0, 1));
        artifactLocations.put(DungeonItem.ArtifactSlot.LANTERN, new Point(0, 2));
        artifactLocations.put(DungeonItem.ArtifactSlot.KEY, new Point(0, 3));
        artifactLocations.put(DungeonItem.ArtifactSlot.MAP, new Point(0, 4));
        artifactLocations.put(DungeonItem.ArtifactSlot.FLOAT, new Point(0, 5));
        artifactLocations.put(DungeonItem.ArtifactSlot.GASMASK, new Point(0, 6));
        artifactLocations.put(DungeonItem.ArtifactSlot.PORTALKEY, new Point(0, 7));
        artifactLocations.put(DungeonItem.ArtifactSlot.GLASSES, new Point(0, 8));
        artifactLocations.put(DungeonItem.ArtifactSlot.PICKAXE, new Point(2, 1));
        artifactLocations.put(DungeonItem.ArtifactSlot.BAG, new Point(2, 2));
        artifactLocations.put(DungeonItem.ArtifactSlot.TURTLESHELL, new Point(2, 3));
        artifactLocations.put(DungeonItem.ArtifactSlot.HEATSUIT, new Point(2, 4));
        artifactLocations.put(DungeonItem.ArtifactSlot.XRAYSCOPE, new Point(2, 5));
        artifactLocations.put(DungeonItem.ArtifactSlot.LANTERN2, new Point(2, 6));

        int descWidth = 258;
        int imageWidth = Style.TILE_SIZE;

        FontMetrics textMetrics = graphics.getFontMetrics(font);
        for (DungeonItem item : party.artifacts.getArtifacts().values()) {
            Point loc = artifactLocations.get(item.artifactSlot);
            List<String> descLines =
                    ImageUtils.wrapText(item.name + ": " + item.description, textMetrics, descWidth);
            cells.get(loc.y).set(loc.x, new Tile(item.image, State.NORMAL));
            cells.get(loc.y).set(loc.x + 1,
                    new TextBox(descLines, State.NORMAL, font));
        }

        Table artifactTable = new Table();
        artifactTable.setCells(cells);
        artifactTable.setColWidths(Arrays.asList(imageWidth, descWidth, imageWidth, descWidth));
        artifactTable.setDrawHeaderLine(true);
        artifactTable.setHSpacing(Style.SMALL_MARGIN);
        artifactTable.setVSpacing(Style.SMALL_MARGIN);
        artifactTable.autosize();

        return artifactTable;
    }

    private DungeonItem findPearl(Party party, String itemId) {
        for (DungeonItem item : party.returnedPearls) {
            if (item.id.equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    private Table getPearlTable(Party party, Font font) {
        List<Widget> header = new ArrayList<>();
        header.add(new TextBox(Arrays.asList("Pearls:"), State.NORMAL, font));
        for (int c = 1; c < GameConstants.NUM_PEARLS_TO_WIN; c++) {
            header.add(new Space());
        }

        List<Widget> pearlRow = new ArrayList<>();
        for (int c = 0; c < GameConstants.NUM_PEARLS_TO_WIN; c++) {
            String itemId = "pearl " + (c+1);
            DungeonItem pearl = findPearl(party, itemId);
            if (pearl != null) {
                pearlRow.add(new Tile(pearl.image, State.NORMAL));
            } else {
                pearlRow.add(new Space());
            }
        }

        int imageWidth = Style.TILE_SIZE;
        List<Integer> colWidths = new ArrayList<>();
        for (int i = 0; i < GameConstants.NUM_PEARLS_TO_WIN; i++) {
            colWidths.add(imageWidth);
        }
        Table pearlTable = new Table();
        pearlTable.addRow(header);
        pearlTable.addRow(pearlRow);
        pearlTable.setColWidths(colWidths);
        pearlTable.setDrawHeaderLine(true);
        pearlTable.setHSpacing(Style.SMALL_MARGIN);
        pearlTable.setVSpacing(Style.SMALL_MARGIN);
        pearlTable.autosize();

        return pearlTable;
    }

    private void drawProgressInfo(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Party party = backend.getGameState().party;

        Font font = Style.getDefaultFont();
        graphics.setFont(font);

        // draw artifacts
        Table artifactTable = getArtifactTable(party, graphics, font);
        artifactTable.draw(graphics, Style.SMALL_MARGIN, Style.SMALL_MARGIN);

        // draw returned pearls
        Table pearlTable = getPearlTable(party, font);
        pearlTable.draw(graphics, Style.SMALL_MARGIN, 2*Style.SMALL_MARGIN + artifactTable.getHeight());

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
