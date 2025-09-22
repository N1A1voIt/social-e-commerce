package com.itu.socialcom.demo.moneytransactions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    public static Properties load(String filename) throws IOException {
        Properties props = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                throw new IOException("Configuration file not found: " + filename);
            }
            props.load(input);
        }
        return props;
    }

}
