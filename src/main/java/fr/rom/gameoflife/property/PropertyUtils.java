package fr.rom.gameoflife.property;

import fr.rom.gameoflife.utils.Strings;

import java.util.List;

public class PropertyUtils {

    private PropertyUtils() {}

    public static String listToString(final List<String> list) {
        if(list.isEmpty()) return Strings.EMPTY;

        StringBuilder builder = new StringBuilder();
        builder.append(list.get(0));
        for(int i = 1; i < list.size(); i++) {
            builder.append(Strings.COMMA).append(list.get(i));
        }
        return builder.toString();
    }
}
