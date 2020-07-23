package fr.rom.gameoflife.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

public class PropertyValuesGetter implements Serializable {
    private static final long serialVersionUID = 42L;
    private final InputStream inputStream;
    private Properties properties;



    public PropertyValuesGetter(String configFile) throws IllegalArgumentException, FileNotFoundException {
        checkValidityFilename(configFile);
        inputStream = getClass().getClassLoader().getResourceAsStream(configFile);
        checkValidityFile();
        readValues();
    }


    public String getPropertyValue(String property){
        if(properties.containsKey(property))
            return properties.getProperty(property);

        return null;
    }

    private void readValues() {
        try {
            properties = new Properties();
            properties.load(inputStream);
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkValidityFilename(String filename) throws IllegalArgumentException {
        String[] parsedFilename = filename.split("\\.");
        if(parsedFilename.length > 2 || !parsedFilename[parsedFilename.length - 1].equals("properties"))
            throw new IllegalArgumentException("Nom du fichier de configuration invalide.");
    }

    private void checkValidityFile() throws FileNotFoundException {
        if (inputStream == null) {
            throw new FileNotFoundException("property file not found in the classpath");
        }
    }
}
