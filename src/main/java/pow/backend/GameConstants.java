package pow.backend;

public class GameConstants {
    // --------------- world construction -----------------

    // Use test world (for debugging).
    public final static boolean USE_TEST_WORLD = false;

    // How many monsters per square there should be, on average.
    public final static double MONSTER_DENSITY = 1.0 / 100.0;

    // How many items per square there should be, on average.
    public final static double ITEM_DENSITY = 1.0 / 400.0;

    // Probability that adjacent areas will be connected during
    // world creation.  (Note that at least one connection is
    // guaranteed--setting this to 0 will result in a world that
    // is a tree structure.)
    public final static double PROB_CONNECT_ADJ_AREAS = 0.33;

    // How many new areas the player must go to before monsters
    // will be regenerated in the current area.
    public final static int NUM_RECENT_LOCS_BEFORE_REGEN = 2;

    // How big (maximally) levels will be created.  Note that
    // using a value smaller than 60 may cause some problems
    // for rogue-generated levels.
    public final static int DEFAULT_AREA_SIZE = 60;

    // The size of the base pattern created for outside levels (i.e.,
    // ones made using RecursiveInterpolation).  Larger values will
    // allow more islands of blocked terrain and more complex/but random
    // areas. Final size will be
    // OUTSIDE_AREA_SOURCE_SIZE * 2^OUTSIDE_AREA_NUM_INTERPOLATION_STEPS
    public final static int OUTSIDE_AREA_SOURCE_SIZE = 6;

    // Number of steps to iterate for RecursiveInterpolation.  Works
    // with conjunction with OUTSIDE_AREA_SOURCE_SIZE to determine
    // the actual level size.  Making this higher will increase the
    // fractal nature of the level, which tends to look more realistic.
    public final static int OUTSIDE_AREA_NUM_INTERPOLATION_STEPS = 3;

    // Number of steps to iterate to make mountain/island levels.
    // Level size is approximately 2^ISLAND_AREA_NUM_ITERATIONS.
    public final static int ISLAND_AREA_NUM_ITERATIONS = 6;

    // How big levels will using delve generation.  These are
    // slightly smaller since they are slightly annoying to
    // navigate.
    public final static int DELVE_AREA_SIZE = 50;

    // Number of cells to use to create a radial area (is roughly
    // equal to the area of the level).
    public final static int RADIAL_NUM_CELLS = 4000;

    // How often, when creating  a radial area, a cell will match
    // the one next to it.
    public final static int RADIAL_MATCH_PERCENT = 75;

    // --------------- player/monster constants -------------

    // How many different things an actor (typically the player)
    // can hold.
    public final static int ACTOR_ITEM_LIST_SIZE = 20;

    // How many of the same things an actor can carry (e.g.,
    // arrows, or potions of the same type).
    public final static int ACTOR_DEFAULT_ITEMS_PER_SLOT = 100;

    // After a magic bag upgrade, how many things the player
    // can carry per slot.
    public final static int PLAYER_EXPANDED_ITEMS_PER_SLOT = 250;

    // How far arrows travel
    public final static int ACTOR_ARROW_FIRE_RANGE = 6;

    // Radius of light for a candle
    public final static int CANDLE_LIGHT_RADIUS = 3;

    // Light radius for player at beginning of game
    public final static int PLAYER_SMALL_LIGHT_RADIUS = 4;

    // Light radius for player after getting the lantern.
    public final static int PLAYER_MED_LIGHT_RADIUS = 8;

    // Light radius for player after getting the bright lantern.
    public final static int PLAYER_LARGE_LIGHT_RADIUS = 11;

    // View radius for monsters.  Farther than this and they can't see you.
    public final static int MONSTER_VIEW_RADIUS = 11;

    // Chance a monster will drop an item (for each drop attempt).
    public final static double MONSTER_DROP_CHANCE = 0.5;

    // Probability that monsters will be generated in a group.
    public final static double MONSTER_GROUP_PROBABILITY = 0.05;

    // Chance a monster will drop an item when player has an
    // amulet of wealth.
    public final static double BONUS_MONSTER_DROP_CHANCE = 0.75;

    // Chance a monster will drop some gold (for each drop
    // attempt + 1).  Should be between 0 and 1.  Larger values
    // correspond to higher chance of dropping gold.
    public final static double MONSTER_GOLD_DROP_CHANCE = 0.15;

    // Multiplier for increasing the chance of monsters dropping
    // gold when the player has an amulet of wealth.  Should be
    // between 0 and 1.  Value of 1 will make amulets of wealth have
    // no effect; value of 0 will make monsters always drop things.
    public final static double BONUS_GOLD_DROP_RATE_MULTIPLIER = 0.7;

    // Probability that an item will have a socket.  Probability
    // of 2 sockets will be this squared, etc.
    public final static double PROB_GEN_SOCKET = 0.224;

    // If player doesn't have gas mask, they lose this much
    // health in certain areas.
    public final static int POISON_DAMAGE_PER_TURN = 1;

    // If player doesn't have heat suit, they lose this much
    // health in certain areas.
    public final static int HEAT_DAMAGE_PER_TURN = 5;

    // Amount of damage a trap inflicts.
    public final static int TRAP_DAMAGE_PER_TURN = 40;

    // ------------------- other game constants ---------------

    public final static int MESSAGE_LOG_SIZE = 50;

    // In the world map, can players see unvisited areas?
    public final static boolean PLAYER_CAN_SEE_UNKNOWN_AREAS = false;
}
