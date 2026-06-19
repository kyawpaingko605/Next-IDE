package com.hyperion.nextide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * The optimized implementation of an EditText where each line is numbered on the left.
 */
public class LineNumberEditText extends AppCompatEditText {

    private boolean lineNumberVisible = true;
    private Rect rect;
    private Paint paint;
    private int lineNumberMarginGap = 16; 
    protected int LINE_NUMBER_TEXTSIZE_GAP = 4;
    
    private int lastPaddingLeft = -1; 

    public LineNumberEditText(Context context) {
        super(context);
        init();
    }

    public LineNumberEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineNumberEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        rect = new Rect();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GRAY); 
        paint.setTextSize(getTextSize() - LINE_NUMBER_TEXTSIZE_GAP);
    }

    public void setLineNumberMarginGap(int lineNumberMarginGap) {
        this.lineNumberMarginGap = lineNumberMarginGap;
        updatePadding();
    }

    public int getLineNumberMarginGap() {
        return lineNumberMarginGap;
    }

    public void setLineNumberVisible(boolean lineNumberVisible) {
        this.lineNumberVisible = lineNumberVisible;
        updatePadding();
    }

    public boolean isLineNumberVisible() {
        return lineNumberVisible;
    }

    public void setLineNumberTextColor(int textColor) {
        paint.setColor(textColor);
        invalidate();
    }

    public int getLineNumberTextColor() {
        return paint.getColor();
    }

    // ပြင်ဆင်ချက် - စာသားပြောင်းလဲချိန် (စာကြောင်းရေ တိုး/လျော့ချိန်) တွင်သာ Padding ကို တွက်ချက်ခြင်းဖြင့် onDraw တွင် တုန်ခါခြင်းကို လုံးဝပျောက်ကင်းစေသည်
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        updatePadding();
    }

    private void updatePadding() {
        if (lineNumberVisible) {
            paint.setTextSize(getTextSize() - LINE_NUMBER_TEXTSIZE_GAP);
            int lineCount = Math.max(1, getLineCount());
            String maxLineStr = String.valueOf(lineCount);
            int neededPaddingLeft = (int) paint.measureText(maxLineStr) + lineNumberMarginGap + 20;

            if (lastPaddingLeft != neededPaddingLeft) {
                lastPaddingLeft = neededPaddingLeft;
                setPadding(neededPaddingLeft, getPaddingTop(), getPaddingRight(), getPaddingBottom());
            }
        } else {
            if (lastPaddingLeft != 16) {
                lastPaddingLeft = 16;
                setPadding(16, getPaddingTop(), getPaddingRight(), getPaddingBottom());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (lineNumberVisible) {
            int lineCount = getLineCount();
            int baseline;
            for (int i = 0; i < lineCount; i++) {
                baseline = getLineBounds(i, rect);
                String lineNumberText = String.valueOf(i + 1);
                canvas.drawText(lineNumberText, 10, baseline, paint);
            }
        }
        super.onDraw(canvas);
    }
}
