package pow.frontend.utils;

import java.awt.event.KeyEvent;

import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;

public class KeyUtils {
    public static boolean hasShift(KeyEvent event) {
        int onMask = SHIFT_DOWN_MASK;
        return (event.getModifiersEx() & onMask) == onMask;
    }

    public static KeyInput getKeyInput(KeyEvent event) {
            switch (event.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.OKAY;
            case KeyEvent.VK_ESCAPE:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.CANCEL;
            case KeyEvent.VK_SPACE:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.CYCLE;
            case KeyEvent.VK_SLASH:
                return KeyInput.HELP;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_L:
            case KeyEvent.VK_NUMPAD6:
                return hasShift(event) ? KeyInput.RUN_EAST : KeyInput.EAST;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_H:
            case KeyEvent.VK_NUMPAD4:
                return hasShift(event) ? KeyInput.RUN_WEST : KeyInput.WEST;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_J:
            case KeyEvent.VK_NUMPAD2:
                return hasShift(event) ? KeyInput.RUN_SOUTH : KeyInput.SOUTH;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_K:
            case KeyEvent.VK_NUMPAD8:
                return hasShift(event) ? KeyInput.RUN_NORTH : KeyInput.NORTH;
            case KeyEvent.VK_Y:
            case KeyEvent.VK_NUMPAD7:
                return hasShift(event) ? KeyInput.RUN_NORTH_WEST : KeyInput.NORTH_WEST;
            case KeyEvent.VK_U:
            case KeyEvent.VK_NUMPAD9:
                return hasShift(event) ? KeyInput.RUN_NORTH_EAST : KeyInput.NORTH_EAST;
            case KeyEvent.VK_B:
            case KeyEvent.VK_NUMPAD1:
                return hasShift(event) ? KeyInput.RUN_SOUTH_WEST : KeyInput.SOUTH_WEST;
            case KeyEvent.VK_N:
            case KeyEvent.VK_NUMPAD3:
                return hasShift(event) ? KeyInput.RUN_SOUTH_EAST : KeyInput.SOUTH_EAST;
            case KeyEvent.VK_COMMA:
                return hasShift(event) ? KeyInput.UP_STAIRS : KeyInput.UNKNOWN;
            case KeyEvent.VK_PERIOD:
                return hasShift(event) ? KeyInput.DOWN_STAIRS : KeyInput.REST;
            case KeyEvent.VK_NUMPAD5:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.REST;
            case KeyEvent.VK_F:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.FIRE;
            case KeyEvent.VK_S:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.SAVE;
            case KeyEvent.VK_X:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.LOOK;
            case KeyEvent.VK_C:
                return hasShift(event) ? KeyInput.PLAYER_INFO : KeyInput.CLOSE_DOOR;
            case KeyEvent.VK_G:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.GET;
            case KeyEvent.VK_D:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.DROP;
            case KeyEvent.VK_I:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.INVENTORY;
            case KeyEvent.VK_Q:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.QUAFF;
            case KeyEvent.VK_W:
                return hasShift(event) ? KeyInput.TAKE_OFF : KeyInput.WEAR;
            case KeyEvent.VK_M:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.MAGIC;
            case KeyEvent.VK_T:
                return hasShift(event) ? KeyInput.TARGET_FLOOR : KeyInput.TARGET;
            case KeyEvent.VK_2:
                return hasShift(event) ? KeyInput.SHOW_WORLD_MAP : KeyInput.UNKNOWN;

            // debugging commands
            case KeyEvent.VK_EQUALS:
                return hasShift(event) ? KeyInput.DEBUG_INCR_CHAR_LEVEL : KeyInput.UNKNOWN;
            case KeyEvent.VK_1:
                return hasShift(event) ? KeyInput.DEBUG_HEAL_CHAR : KeyInput.UNKNOWN;

            default:
                return KeyInput.UNKNOWN;
        }
    }
}
