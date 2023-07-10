package fr.rom.gameoflife.utils;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;



public final class Message {

    private static final ObjectProperty<Locale> locale;
    static {
        locale = new SimpleObjectProperty<>(getSystemLocale());
        locale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
    }

    private Message() {}

    public static String get(final String key, final Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("messages/message", getLocale());
        return MessageFormat.format(bundle.getString(key), args);
    }

    public static StringBinding createStringBinding(final String key, Object... args) {
        return Bindings.createStringBinding(() -> get(key, args), locale);
    }

    public static List<Locale> getSupportedLocales() {
        return new ArrayList<>(Arrays.asList(Locale.ENGLISH, Locale.FRENCH));
    }

    public static Locale getSystemLocale() {
        Locale sysDefault = Locale.getDefault();
        return getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.FRENCH;
    }

    public static Locale getLocaleFromString(String localeString) {
        Locale l = Locale.of(localeString);
        if(getSupportedLocales().contains(l)) {
            return l;
        } else {
            return Locale.FRENCH;
        }
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static void setLocale(Locale newLocale) {
        locale.set(newLocale);
        Locale.setDefault(newLocale);
    }

}