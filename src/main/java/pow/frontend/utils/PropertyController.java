package pow.frontend.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Class to allow reading properties from a file but also allow
// the user to override with -Dproperty=value
public class PropertyController {

    private final Properties properties;

    public PropertyController() throws IOException {
        this.properties = new Properties();
        init();
    }

    private void init() throws IOException {
        try (InputStream input = this.getClass().getResourceAsStream("/application.properties")) {
            properties.load(input);
            for (Object key : this.properties.keySet()) {
                String override = System.getProperty((String) key);
                if (override != null) {
                    properties.put(key, override);
                }
            }
        }
    }

    public String getProperty(String key) {
        return (String) this.properties.get(key);
    }
}
