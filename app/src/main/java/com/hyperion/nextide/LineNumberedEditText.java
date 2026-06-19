package com.hyperion.nextide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * Super Stable & Smooth LineNumberedEditText for Next-IDE (Fixed UI Freeze).
 */
public class LineNumberedEditText extends AppCompatEditText
{
    private Rect rect;
    private Paint paint;
    private int lastPaddingLeft = -1;
    private final int marginGap = 20; 

    public LineNumberedEditText(Context context)
    {
        super(context);
        init();
    }

    public LineNumberedEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public LineNumberedEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    private void init()
    {
        rect = new Rect();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.MAGENTA); 
        paint.setTextSize(35);
        paint.setTypeface(Typeface.MONOSPACE);
    }

    // UI Freeze မဖြစ်စေရန် Measure လုပ်ချိန်တွင်သာ Padding ကို စနစ်တကျ တွက်ချက်ခြင်း
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (paint != null) {
            int lineCount = Math.max(1, getLineCount());
            String maxLineStr = String.valueOf(lineCount);
            int neededPaddingLeft = (int) paint.measureText(maxLineStr) + marginGap + 25;

            if (lastPaddingLeft != neededPaddingLeft) {
                lastPaddingLeft = neededPaddingLeft;
                setPadding(neededPaddingLeft, getPaddingTop(), getPaddingRight(), getPaddingBottom());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // မူလစာသားများကို အရင်ပေါ်အောင် ဆွဲခိုင်းခြင်း
        super.onDraw(canvas);

        int lineCount = getLineCount();
        Layout layout = getLayout();
        CharSequence text = getText();

        if (layout != null && text != null && paint != null) {
            int baseline;
            int lineNumber = 1;

            for (int i = 0; i < lineCount; ++i) {
                baseline = getLineBounds(i, rect);

                if (i == 0) {
                    canvas.drawText(String.valueOf(lineNumber), 10, baseline, paint);
                    ++lineNumber;
                } else {
                    int lineStart = layout.getLineStart(i);
                    if (lineStart > 0 && lineStart <= text.length() && text.charAt(lineStart - 1) == '\n') {
                        canvas.drawText(String.valueOf(lineNumber), 10, baseline, paint);
                        ++lineNumber;
                    }
                }
            }
        }
    }
}
