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
import pow.frontend.WindowLayout;
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

    public PlayerInfoWindow(boolean visible, GameBackend backend, Frontend frontend) {
        super(new WindowDim(0,0,0,0), visible, backend, frontend);
        resize(getWindowDim(this.frontend.layout));
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

    private List<TableCell> getRow(String key, String value, Font font) {
        List<TableCell> row = new ArrayList<>();
        row.add(new TableCell(new Space()));
        row.add(new TableCell(new TextBox(Collections.singletonList(key), State.NORMAL, font)));
        row.add(new TableCell(new TextBox(Collections.singletonList(value), State.NORMAL, font)));
        return row;
    }

    private Table getCharInfoTable(Player player, int width) {
        int textWidth = width - Style.TILE_SIZE - 2*Style.SMALL_MARGIN;
        String winnerString = player.isWinner() ? " (Winner!)" : "";
        String secondaryAttack = player.hasBowEquipped() ? String.valueOf(player.getSecondaryAttack()) : "N/A";
        Font font = Style.getDefaultFont();

        Table table = new Table();
        table.addRow(Arrays.asList(
                new TableCell(new Tile(player.image, State.NORMAL)),
                new TableCell(new TextBox(Collections.singletonList(player.name + winnerString), State.NORMAL, font)),
                new TableCell(new Space())
        ));

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
        table.addRow(getRow("rFire:     ", "" + getResistPercent(player.getResFire()), font));
        table.addRow(getRow("rCold:     ", "" + getResistPercent(player.getResCold()), font));
        table.addRow(getRow("rAcid:     ", "" + getResistPercent(player.getResAcid()), font));
        table.addRow(getRow("rElec:     ", "" + getResistPercent(player.getResElec()), font));
        table.addRow(getRow("rPois:     ", "" + getResistPercent(player.getResPois()), font));
        table.addRow(getRow("rDam:      ", "" + getResistPercent(player.getResDam()), font));
        table.addRow(getRow("", "", font));
        if (!player.spells.isEmpty()) {
            String spellsString = "Spells: " + TextUtils.formatList(getSpellNames(player.spells)) + ".";
            table.addRow(Arrays.asList(
                    new TableCell(new Space()),
                    new TableCell(new TextBox(Collections.singletonList(spellsString), State.NORMAL, font, textWidth)),
                    new TableCell(new Space())
            ));
        }

        table.setColWidths(Arrays.asList(Style.TILE_SIZE + 2*Style.SMALL_MARGIN, 80, textWidth - 80));
        table.setDrawHeaderLine(true);
        table.autosize();

        return table;
    }

    private Table getMainInfoTable() {
        Player player = backend.getGameState().party.player;
        Player pet = backend.getGameState().party.pet;
        int infoWidth = 300;

        Table table = new Table();
        List<TableCell> row = new ArrayList<>();
        row.add(new TableCell(getCharInfoTable(player, infoWidth), TableCell.VertAlign.TOP, TableCell.HorizAlign.LEFT));
        if (pet != null) {
            row.add(new TableCell(getCharInfoTable(pet, infoWidth), TableCell.VertAlign.TOP, TableCell.HorizAlign.LEFT));
        }
        table.addRow(row);
        table.setHSpacing(Style.MARGIN);
        table.autosize();

        return table;
    }

    private Table getStatsTable() {
        Player player = backend.getGameState().party.player;
        Font font = Style.getDefaultFont();

        Map<DungeonItem.Slot, StringPosition> slotData = new HashMap<>();
        slotData.put(DungeonItem.Slot.WEAPON, new StringPosition("Weapon", 1));
        slotData.put(DungeonItem.Slot.BOW, new StringPosition("Bow", 2));
        slotData.put(DungeonItem.Slot.SHIELD, new StringPosition("Shield", 3));
        slotData.put(DungeonItem.Slot.HEADGEAR, new StringPosition("Head", 4));
        slotData.put(DungeonItem.Slot.ARMOR, new StringPosition("Armor", 5));
        slotData.put(DungeonItem.Slot.CLOAK, new StringPosition("Cloak", 6));
        slotData.put(DungeonItem.Slot.RING, new StringPosition("Ring", 7));
        slotData.put(DungeonItem.Slot.BRACELET, new StringPosition("Bracelet", 8));
        slotData.put(DungeonItem.Slot.AMULET, new StringPosition("Amulet", 9));
        slotData.put(DungeonItem.Slot.GLOVES, new StringPosition("Gloves",10));
        slotData.put(DungeonItem.Slot.BOOTS, new StringPosition("Boots", 11));

        Map<Integer, StringPosition> bonusData = new HashMap<>();
        bonusData.put(DungeonItem.TO_HIT_IDX, new StringPosition("hit", 4));
        bonusData.put(DungeonItem.TO_DAM_IDX, new StringPosition("dam", 5));
        bonusData.put(DungeonItem.DEF_IDX, new StringPosition("def", 6));
        bonusData.put(DungeonItem.STR_IDX, new StringPosition("str", 7));
        bonusData.put(DungeonItem.DEX_IDX, new StringPosition("dex", 8));
        bonusData.put(DungeonItem.INT_IDX, new StringPosition("int", 9));
        bonusData.put(DungeonItem.CON_IDX, new StringPosition("con", 10));
        bonusData.put(DungeonItem.RES_FIRE_IDX, new StringPosition("rFire", 11));
        bonusData.put(DungeonItem.RES_COLD_IDX, new StringPosition("rCold", 12));
        bonusData.put(DungeonItem.RES_ACID_IDX, new StringPosition("rAcid", 13));
        bonusData.put(DungeonItem.RES_ELEC_IDX, new StringPosition("rElec", 14));
        bonusData.put(DungeonItem.RES_POIS_IDX, new StringPosition("rPois", 15));
        bonusData.put(DungeonItem.RES_DAM_IDX, new StringPosition("rDam", 16));
        bonusData.put(DungeonItem.SPEED_IDX, new StringPosition("speed", 17));
        bonusData.put(DungeonItem.WEALTH_IDX, new StringPosition("wealth", 18));
        bonusData.put(DungeonItem.SOCKETS_IDX, new StringPosition("sockets", 19));

        int numRows = slotData.size() + 2; // number of slots + 2 for header and total
        int numCols = 20;

        // Make a grid of blank cells; we'll replace some as necessary below.
        List<List<TableCell>> cells = new ArrayList<>();
        for (int r = 0; r < numRows; r++) {
            List<TableCell> blankRow = new ArrayList<>();
            for (int c = 0; c < numCols; c++) {
                blankRow.add(new TableCell(new Space(Style.TILE_SIZE, Style.TILE_SIZE)));
            }
            cells.add(blankRow);
        }

        // header on top
        for (StringPosition bonus : bonusData.values()) {
            int col = bonus.position;
            cells.get(0).set(col, new TableCell(
                    new RotatedText(Style.TILE_SIZE, Style.TILE_SIZE + Style.MARGIN, bonus.name, font)));
        }

        // left icons
        for (DungeonItem item: player.equipment.items) {
            int row = slotData.get(item.slot).position;
            cells.get(row).set(2, new TableCell(new Tile(item.image, State.NORMAL)));
        }

        // left item types
        for (StringPosition sd : slotData.values()) {
            int row = sd.position;
            cells.get(row).set(0, new TableCell(new TextBox(Collections.singletonList(sd.name), State.NORMAL, font)));
        }

        // spacing around icons
        for (int r = 0; r < numRows; r++) {
            cells.get(r).set(1, new TableCell(new Space(Style.MARGIN, Style.TILE_SIZE)));
            cells.get(r).set(3, new TableCell(new Space(Style.MARGIN, Style.TILE_SIZE)));
        }

        // grid interior
        for (DungeonItem item: player.equipment.items) {
            int row = slotData.get(item.slot).position;

            for (Map.Entry<Integer, StringPosition> entry : bonusData.entrySet()) {
                int bonusIdx = entry.getKey();
                StringPosition bonus = entry.getValue();
                int col = bonus.position;
                if (item.bonuses[bonusIdx] > 0) {
                    cells.get(row).set(col, new TableCell(
                            new TextBox(Collections.singletonList(bonusString(item.bonuses[bonusIdx])), State.NORMAL, font),
                            TableCell.VertAlign.CENTER, TableCell.HorizAlign.CENTER));
                }
            }
        }

        // bottom sum
        int[] bonusTotals = new int[DungeonItem.NUM_BONUSES];
        for (DungeonItem item: player.equipment.items) {
            for (int i = 0; i < DungeonItem.NUM_BONUSES; i++) {
                bonusTotals[i] += item.bonuses[i];
            }
        }
        cells.get(numRows-1).set(0, new TableCell(new TextBox(Collections.singletonList("(total)"), State.NORMAL, font)));
        for (Map.Entry<Integer, StringPosition> entry : bonusData.entrySet()) {
            int bonusIdx = entry.getKey();
            StringPosition bonus = entry.getValue();
            int col = bonus.position;
            if (bonusTotals[bonusIdx] > 0) {
                cells.get(numRows-1).set(col, new TableCell(
                            new TextBox(Collections.singletonList(bonusString(bonusTotals[bonusIdx])), State.NORMAL, font),
                        TableCell.VertAlign.CENTER, TableCell.HorizAlign.CENTER));
            }
        }

        Table table = new Table();
        table.setCells(cells);
        table.setGrid(4, 1, numCols, numRows-1);
        table.autosize();

        return table;
    }

    private Table getArtifactTable(Party party, Font font) {
        List<List<TableCell>> cells = new ArrayList<>();

        // add the header
        cells.add(Arrays.asList(
                new TableCell(new TextBox(Collections.singletonList("Artifacts:"), State.NORMAL, font)),
                new TableCell(new Space()),
                new TableCell(new Space()),
                new TableCell(new Space())
        ));

        // add blank cells; we'll fill these in later
        for (int i = 1; i <= 8; i++) {
            cells.add(Arrays.asList(
                    new TableCell(new Space(Style.TILE_SIZE, Style.TILE_SIZE)),
                    new TableCell(new Space()),
                    new TableCell(new Space(Style.TILE_SIZE, Style.TILE_SIZE)),
                    new TableCell(new Space())
            ));
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

        int descWidth = 265;
        int imageWidth = Style.TILE_SIZE;

        for (DungeonItem item : party.artifacts.getArtifacts().values()) {
            Point loc = artifactLocations.get(item.artifactSlot);
            cells.get(loc.y).get(loc.x).widget = new Tile(item.image, State.NORMAL);
            cells.get(loc.y).get(loc.x + 1).widget = new TextBox(Collections.singletonList(item.name + ": " + item.description), State.NORMAL, font, descWidth);
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
        List<TableCell> header = new ArrayList<>();
        header.add(new TableCell(new TextBox(Collections.singletonList("Returned Pearls:"), State.NORMAL, font)));
        for (int c = 1; c < GameConstants.NUM_PEARLS_TO_WIN; c++) {
            header.add(new TableCell(new Space()));
        }

        List<TableCell> pearlRow = new ArrayList<>();
        for (int c = 0; c < GameConstants.NUM_PEARLS_TO_WIN; c++) {
            String itemId = "pearl " + (c+1);
            DungeonItem pearl = findPearl(party, itemId);
            if (pearl != null) {
                pearlRow.add(new TableCell(new Tile(pearl.image, State.NORMAL)));
            } else {
                pearlRow.add(new TableCell(new Space()));
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


    private Table getProgressTable() {
        Party party = backend.getGameState().party;
        Font font = Style.getDefaultFont();

        Table artifactTable = getArtifactTable(party, font);
        Table pearlTable = getPearlTable(party, font);

        Table progressTable = new Table();
        progressTable.addColumn(Arrays.asList(
                new TableCell(artifactTable), new TableCell(pearlTable)
        ));

        progressTable.setVSpacing(Style.MARGIN);
        progressTable.autosize();

        return progressTable;
    }

    // right justifies string, assumes bonus is <= 999.
    private static String bonusString(int bonus) {
        return String.format("%3s", bonus);
    }

    private WindowDim getWindowDim(WindowLayout layout) {
        // compute the maximum size needed to draw all three views.
        int maxWidth = 0;
        int maxHeight = 0;

        // main view
        Table mainInfoTable = getMainInfoTable();
        maxWidth = Math.max(maxWidth, mainInfoTable.getWidth());
        maxHeight = Math.max(maxHeight, mainInfoTable.getHeight());

        // stats view
        Table statsTable = getStatsTable();
        maxWidth = Math.max(maxWidth, statsTable.getWidth());
        maxHeight = Math.max(maxHeight, statsTable.getHeight());

        // progress view
        Table progressTable = getProgressTable();
        maxWidth = Math.max(maxWidth, progressTable.getWidth());
        maxHeight = Math.max(maxHeight, progressTable.getHeight());

        // finally, increase width and height to include margin and bottom text.
        maxWidth += 2*Style.MARGIN;
        maxHeight += 3*Style.MARGIN + Style.getFontSize();

        return layout.center(maxWidth, maxHeight);
    }

    @Override
    public void drawContents(Graphics graphics) {
        graphics.setColor(Style.BACKGROUND_COLOR);
        graphics.fillRect(0, 0, dim.width, dim.height);

        Table table = null;
        switch (viewPane) {
            case 0: table = getMainInfoTable(); break;
            case 1: table = getStatsTable(); break;
            case 2: table = getProgressTable(); break;
        }
        if (table != null) {
            table.draw(graphics, Style.MARGIN, Style.MARGIN);
        }

        graphics.setColor(Color.WHITE);
        graphics.drawString("Press [left]/[right]/[space] to change view, c/[esc]/[enter] to close.",
                Style.MARGIN, dim.height - Style.MARGIN);
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
