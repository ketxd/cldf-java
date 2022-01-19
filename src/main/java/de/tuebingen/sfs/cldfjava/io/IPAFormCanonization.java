package de.tuebingen.sfs.cldfjava.io;

import java.util.HashMap;
import java.util.Map;

public class IPAFormCanonization {
    public static Map<Character,String> replacements;

    static {
        replacements = new HashMap<>();
        replacements.put('g', "ɡ"); //more correct way of encoding the IPA symbol (stable glyph properties)
        //various Unicode codepoints that should be canonized as combinations of base glyphs and combining chars
        // acute accent
        replacements.put('á', "á");
        replacements.put('é', "é");
        replacements.put('í', "í");
        replacements.put('ó', "ó");
        replacements.put('ú', "ú");
        replacements.put('ý', "ý");
        // grave accent
        replacements.put('à', "à");
        replacements.put('è', "è");
        replacements.put('ì', "ì");
        replacements.put('ò', "ò");
        replacements.put('ù', "ù");
        replacements.put('ỳ', "ỳ");
        // double acute
        replacements.put('ő', "ő");
        replacements.put('ű', "ű");
        // double grave
        replacements.put('ȁ', "ȁ");
        replacements.put('ȅ', "ȅ");
        replacements.put('ȉ', "ȉ");
        replacements.put('ȍ', "ȍ");
        replacements.put('ȕ', "ȕ");
        // circumflex
        replacements.put('â', "â"); 
        replacements.put('ê', "ê");
        replacements.put('î', "î");
        replacements.put('ô', "ô");
        replacements.put('û', "û");
        replacements.put('ŷ', "ŷ");
        // caron
        replacements.put('ǎ', "ǎ");
        replacements.put('ě', "ě");
        replacements.put('ǐ', "ǐ");
        replacements.put('ǒ', "ǒ");
        replacements.put('ǔ', "ǔ");
        // trema
        replacements.put('ä', "ä");
        replacements.put('ë', "ë");
        replacements.put('ï', "ï");
        replacements.put('ö', "ö");
        replacements.put('ü', "ü");
        replacements.put('ÿ', "ÿ");
        // tilde
        replacements.put('ã', "ã");
        replacements.put('ẽ', "ẽ");
        replacements.put('ĩ', "ĩ");
        replacements.put('õ', "õ");
        replacements.put('ũ', "ũ");
        replacements.put('ỹ', "ỹ");
        // macron
        replacements.put('ā', "ā");
        replacements.put('ē', "ē");
        replacements.put('ī', "ī");
        replacements.put('ō', "ō");
        replacements.put('ū', "ū");
        replacements.put('ȳ', "ȳ");
        // breve
        replacements.put('ă', "");
        replacements.put('ĕ', "");
        replacements.put('ĭ', "");
        replacements.put('ŏ', "");
        replacements.put('ŭ', "");
    }

    public static String process(String rawForm) {
        boolean inParentheses = false;
        StringBuilder canonized = new StringBuilder();
        for (int i = 0; i < rawForm.length(); i++) {
            char c = rawForm.charAt(i);
            if (inParentheses) {
                if (c == ')') inParentheses = false;
                continue;
            }
            String replacement = replacements.get(c);
            if (replacement != null) {
                canonized.append(replacement);
            }
            else {
                if (c == '(') inParentheses = true;
                else canonized.append(c);
            }
        }
        return canonized.toString();
    }
}
