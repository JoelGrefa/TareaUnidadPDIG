package modelo;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {

    private static ResourceBundle messages;

    // Establece el idioma a utilizar
    public static void setLanguage(Locale locale) {
        // Carga el archivo .properties correspondiente al idioma
        messages = ResourceBundle.getBundle("messages", locale);


    }

    // Devuelve el texto correspondiente a una clave
    public static String getMessage(String key) {
        return messages.getString(key);
    }
}
