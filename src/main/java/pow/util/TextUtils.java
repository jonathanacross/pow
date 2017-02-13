package pow.util;

import java.util.regex.Pattern;

public class TextUtils {

    // formats a raw string --
    // ! at the beginning means don't modify; useful for unique things, e.g., "Blarbak the king"
    // & indicates a position to substitute a/an, e.g., "& potion" as opposed to "water"
    // ~ indicates where to pluralize, e.g., "potion~ of heath"
    // |x|y| indicates a custom singular/pluralization, e.g., "dwar|f|ves|"
    public static String format(String name, int count, boolean definite) {
        // if starts with !, then don't change anything.  Useful for unique things.
        if (name.startsWith("!")) {
            return name.substring(1);
        }

        // add a/an/the/<count>
        String prefixed = replacePrefix(name, count, definite);

        // fix pluralization
        String result = count == 1 ? replaceSingular(prefixed) : replacePlural(prefixed);

        return result;
    }

    public static String plural(String name) {
        return replacePlural(removePrefix(name));
    }
    public static String singular(String name) {
        return replaceSingular(removePrefix(name));
    }

    private static Pattern customPluralRegex = Pattern.compile("(.*)\\|(.*)\\|(.*)\\|(.*)");

    private static boolean startsWithVowel(String s) {
        // skip over any '& '
        int idx = 0;
        while (idx < s.length() && s.charAt(idx) == ' ' || s.charAt(idx) == '&') {
            idx++;
        }
        if (idx >= s.length()) return false;

        char c = s.charAt(idx);
        return (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u');
    }

    private static String removePrefix(String s) {
        return s.replaceAll("& ", "");
    }

    private static String replacePrefix(String s, int count, boolean definite) {
        if (count == 0) {
            return "no " + s.replaceAll("& ", "");
        } else if (count == 1) {
            if (definite) {
                return "the " + s.replaceAll("& ", "");
            } else {
                String indefArticle = startsWithVowel(s) ? "an" : "a";
                return s.replaceAll("&", indefArticle);
            }
        } else {
            return count + " " + s.replaceAll("& ", "");
        }

    }

    private static String replaceCustomSingular(String s) {
        int bar1 = s.indexOf('|');
        if (bar1 < 0) return s;

        return customPluralRegex.matcher(s).replaceAll("$1$2$4");
    }

    private static String replaceCustomPlural(String s) {
        int bar1 = s.indexOf('|');
        if (bar1 < 0) return s;

        return customPluralRegex.matcher(s).replaceAll("$1$3$4");
    }

    private static String replaceSingular(String s) {
        String simpleSingle = s.replaceAll("~", "");
        return replaceCustomSingular(simpleSingle);
    }

    private static String replacePlural(String s) {
        String simplePlural = s
                .replaceAll("ch~", "ches")
                .replaceAll("sh~", "shes")
                .replaceAll("s~", "ses")
                .replaceAll("x~", "xes")
                .replaceAll("z~", "zes")
                .replaceAll("~", "s");
        return replaceCustomPlural(simplePlural);
    }

    public static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

}
