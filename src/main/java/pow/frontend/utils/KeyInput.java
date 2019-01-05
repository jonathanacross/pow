package pow.frontend.utils;

// common key inputs
public enum KeyInput {
    OKAY,
    CANCEL,
    CYCLE,

    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    SOUTH_WEST,
    WEST,
    NORTH_WEST,

    RUN_NORTH,
    RUN_NORTH_EAST,
    RUN_EAST,
    RUN_SOUTH_EAST,
    RUN_SOUTH,
    RUN_SOUTH_WEST,
    RUN_WEST,
    RUN_NORTH_WEST,

    SAVE,
    PLAYER_INFO,
    LOOK,
    REST,
    FIRE,
    CLOSE_DOOR,
    GET,
    DROP,
    HELP,
    QUAFF,
    WEAR,
    TAKE_OFF,
    MAGIC,
    PET,
    AUTO_PLAY,
    KNOWLEDGE,
    INVENTORY,
    GROUND,
    EQUIPMENT,
    TARGET,
    TARGET_FLOOR,
    SHOW_WORLD_MAP,
    OPTIMIZE_EQUIPMENT,

    DEBUG_INCR_CHAR_LEVEL, // debugging keys, remove at some point
    DEBUG_HEAL_CHAR,
    DEBUG_SHOW_PET_AI,
    DEBUG_SHOW_PLAYER_AI,

    UNKNOWN // user types something unknown
}
