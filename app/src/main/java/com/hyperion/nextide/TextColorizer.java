package com.hyperion.nextide;

import java.util.HashSet;
import java.util.Set;
import android.graphics.Color;

/**
 * Highly Optimized TextColorizer Engine for Next-IDE.
 * Uses Single-Pass Tokenization to prevent typing lag and ANR crashes on large files.
 */
public class TextColorizer {

    private static final String[] reservedWords = {
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", 
        "const", "continue", "default", "do", "double", "else", "enum", "extends", "false", 
        "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", 
        "int", "interface", "long", "native", "new", "null", "package", "private", "protected", 
        "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", 
        "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"
    };

    private final Set<String> reservedWordsHashSet = new HashSet<>();

    public TextColorizer() {
        for (String word : reservedWords) {
            reservedWordsHashSet.add(word);
        }
    }

    /**
     * Color's the text in the object with high-performance single-pass tokenization.
     */
    public void processText(ColorableText colorableObject) {
        if (colorableObject == null) return;

        String theText = colorableObject.getText();
        int length = theText.length();
        if (length == 0) return;

        // ၁။ စာသားအားလုံးကို ပုံမှန် အမည်းရောင် အရင်သတ်မှတ်
        colorableObject.setColor(0, length, Color.BLACK);

        int keywordColor = Color.parseColor("#007ACC"); // အပြာရောင်
        int commentColor = Color.parseColor("#4EC9B0"); // အစိမ်းရောင်

        int index = 0;
        
        // အမြန်နှုန်း အမြင့်ဆုံးရရှိရန် စာသားတစ်ခုလုံးကို Single Pass ဖြင့် တစ်ခေါက်တည်းသာ ပတ်ခြင်း
        while (index < length) {
            char ch = theText.charAt(index);

            // ၂။ Single-line သို့မဟုတ် Multi-line Comment စစ်ဆေးခြင်း
            if (ch == '/' && index + 1 < length) {
                char nextCh = theText.charAt(index + 1);
                if (nextCh == '/') {
                    // Single Line Comment (//)
                    int endComment = theText.indexOf("\n", index + 2);
                    if (endComment == -1) endComment = length;
                    colorableObject.setColor(index, endComment - index, commentColor);
                    index = endComment;
                    continue;
                } else if (nextCh == '*') {
                    // Multi-line Comment (/*)
                    int endComment = theText.indexOf("*/", index + 2);
                    if (endComment == -1) {
                        colorableObject.setColor(index, length - index, commentColor);
                        index = length;
                    } else {
                        int commentLength = (endComment + 2) - index;
                        colorableObject.setColor(index, commentLength, commentColor);
                        index = endComment + 2;
                    }
                    continue;
                }
            }

            // ၃။ Java Keywords များကို စကားလုံးအလိုက် ခွဲထုတ်ပြီး သက်သာစွာ အရောင်ခြယ်ခြင်း
            if (Character.isJavaIdentifierStart(ch)) {
                int startWord = index;
                index++;
                while (index < length && Character.isJavaIdentifierPart(theText.charAt(index))) {
                    index++;
                }
                
                String word = theText.substring(startWord, index);
                if (reservedWordsHashSet.contains(word)) {
                    colorableObject.setColor(startWord, index - startWord, keywordColor);
                }
                continue;
            }

            index++;
        }
    }
}
