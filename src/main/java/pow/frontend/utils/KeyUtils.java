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
            case KeyEvent.VK_SPACE:
                return hasShift(event) ? KeyInput.UNKNOWN : KeyInput.CANCEL;
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
            case KeyEvent.VK_PERIOD:
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
            default:
                return KeyInput.UNKNOWN;
        }
    }
}