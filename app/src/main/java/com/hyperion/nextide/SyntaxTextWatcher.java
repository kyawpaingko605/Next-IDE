package com.hyperion.nextide;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;

public class SyntaxTextWatcher implements TextWatcher, ColorableText {

    private Editable editText = null;
    private String prevText = "";
    private final TextColorizer colorizer = new TextColorizer();
    private Context context = null; 
    
    // ပြင်ဆင်ချက် - setSpan လုပ်ချိန်တွင် afterTextChanged ထပ်ခါထပ်ခါ အလုပ်လုပ်ပြီး Infinite Loop မဖြစ်စေရန် တားဆီးပေးမည့် Flag
    private boolean isHighlighting = false;

    public SyntaxTextWatcher(Context context) {
        this.context = context;
    }

    @Override
    public void afterTextChanged(Editable editText) {
        // အရောင်ဆိုးနေဆဲ ဖြစ်ပါက ကုဒ်ကို ထပ်မပတ်စေဘဲ ကျော်သွားခိုင်းခြင်း
        if (isHighlighting) return;
        
        this.editText = editText;

        String currText = editText.toString();
        if (this.prevText.equals(currText)) {
            return;
        } else {
            this.prevText = currText;
        }

        // ပြင်ဆင်ချက် - အရောင်စဆိုးပြီ ဖြစ်ကြောင်း အမှတ်အသားပြုခြင်း
        isHighlighting = true;

        ForegroundColorSpan[] toRemoveSpans = editText.getSpans(0, currText.length(), ForegroundColorSpan.class);
        if (toRemoveSpans != null && toRemoveSpans.length > 0) {
            for (ForegroundColorSpan span : toRemoveSpans) {
                editText.removeSpan(span);
            }
        }

        try {
            colorizer.processText(this);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // ပြင်ဆင်ချက် - အရောင်ဆိုးပြီးပါက နောက်တစ်ကြိမ် စာရိုက်နိုင်ရန် Flag ကို ပြန်ဖွင့်ပေးခြင်း
            isHighlighting = false;
        }
    }

    public AssetManager getAssets() {
        if (context != null) {
            return context.getAssets();
        }
        return null;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // အသုံးမပြုပါ
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // အသုံးမပြုပါ
    }

    @Override
    public void setColor(int first, int length, int color) {
        if (this.editText == null) return;

        boolean valid = true;
        if (first < 0) {
            first = 0;
            valid = false;
        }
        if (length <= 0) {
            length = 1;
            valid = false;
        }

        int currLen = this.editText.length();
        if (first >= currLen) return; 

        if (first + length > currLen) {
            length = currLen - first;
            valid = false;
        }

        if (!valid) {
            color = Color.MAGENTA;
        }

        try {
            editText.setSpan(new ForegroundColorSpan(color), first, first + length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getText() {
        if (this.editText != null) {
            return this.editText.toString();
        }
        return "";
    }
}
