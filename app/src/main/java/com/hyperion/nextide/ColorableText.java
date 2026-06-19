package com.hyperion.nextide;

/**
 * Interface representing a colorable and stylable text document for Next-IDE.
 */
public interface ColorableText {
    
    /**
     * Sets the text color for a given region of our document.
     * * @param first  The position of the first character to color (if negative, treated as 0)
     * @param length The number of characters to color
     * @param color  The color value (e.g., Color.GREEN) to apply
     */
    void setColor(int first, int length, int color);
    
    /**
     * Get the object's text.
     * * @return The text associated with the object's document.
     */
    String getText();

    /* 💡 နောက်ပိုင်းတွင် Editor ၌ Keyword များကို Bold (စာလုံးအထူ) ပြုလုပ်လိုပါက 
         ဤအောက်ပါ Method အား Interface တွင် အလွယ်တကူ ထပ်မံဖြည့်စွက်အသုံးပြုနိုင်ပါသည်:
         
         void setStyle(int first, int length, int typefaceStyle); 
    */
}
