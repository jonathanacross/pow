package pow.frontend.utils;

import pow.backend.GameState;
import pow.util.DebugLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SaveUtils {

    private static final File SAVE_DIR;

    static {
        String home = System.getProperty("user.home");
        String sep = System.getProperty("file.separator");
        SAVE_DIR = new File(home, ".pearls_of_wisdom" + sep + "save");
    }

    public static List<File> findSaveFiles() {
        makeSaveDir();
        File[] files = SAVE_DIR.listFiles();
        if (files == null) {
            RuntimeException e = new RuntimeException("Error: save directory is a file!?  Please remove " + SAVE_DIR + ".");
            DebugLogger.fatal(e);
            throw e;
        }
        Arrays.sort(files);
        return new ArrayList<>(Arrays.asList(files));
    }

    private static void makeSaveDir() {
        if (!SAVE_DIR.exists()) {
            if (!SAVE_DIR.mkdirs()) {
                RuntimeException e = new RuntimeException("Error: could not create save directory.  You will not be able to save your games.");
                DebugLogger.fatal(e);
                throw e;
            }
        }
    }

    public static GameState readFromFile(File file) {
        // File file = new File(SAVE_DIR, characterName);
        GameState state = null;
        try (
                InputStream fis = new FileInputStream(file);
                InputStream bis = new BufferedInputStream(fis);
                ObjectInput input = new ObjectInputStream(bis)
        ) {
            state = (GameState) input.readObject();
        } catch (Exception ex) {
            DebugLogger.fatal(ex);
        }
        return state;
    }

    public static void saveToFile(GameState state) {
        String characterName = state.party.player.name;
        File file = new File(SAVE_DIR, characterName);
        try (
                OutputStream fos = new FileOutputStream(file);
                OutputStream bos = new BufferedOutputStream(fos);
                ObjectOutput output = new ObjectOutputStream(bos)
        ) {
            output.writeObject(state);
        } catch (IOException ex) {
            DebugLogger.fatal(ex);
        }
    }
}
