package pow.frontend.save;

import java.io.File;
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
        Arrays.sort(files);
        return new ArrayList(Arrays.asList(files));
    }

    private static void makeSaveDir() {
        if (!SAVE_DIR.exists()) {
            SAVE_DIR.mkdirs();
        }
    }
}
