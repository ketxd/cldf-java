package de.tuebingen.sfs.cldfjava.io;

import java.util.HashMap;
import java.util.Map;

public class IPAFormCanonization {
    public static Map<Character,String> replacements;

    static {
        replacements = new HashMap<>();
        replacements.put('g', "ɡ"); //more correct way of encoding the IPA symbol (stable glyph properties)
        //various Unicode codepoints that should be canonized as combinations of base glyphs and combining chars
        replacements.put('à', "à");
        replacements.put('á', "á");
        replacements.put('â', "â");
        replacements.put('ã', "ã");
        replacements.put('è', "è");
        replacements.put('é', "é");
        replacements.put('ê', "ê");
        replacements.put('ì', "ì");
        replacements.put('í', "í");
        replacements.put('î', "î");
        replacements.put('ò', "ò");
        replacements.put('ó', "ó");
        replacements.put('ô', "ô");
        replacements.put('õ', "õ");
        replacements.put('ù', "ù");
        replacements.put('ú', "ú");
        replacements.put('û', "û");
        replacements.put('ý', "ý");
        replacements.put('ĩ', "ĩ");
        replacements.put('ũ', "ũ");
        replacements.put('ẽ', "ẽ");
        replacements.put('ỳ', "ỳ");
        replacements.put('ỹ', "y");
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
