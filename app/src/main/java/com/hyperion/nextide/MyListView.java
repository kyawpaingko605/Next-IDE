package com.hyperion.nextide;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Custom ListView to fully expand its height inside ScrollView / BottomSheet.
 */
public class MyListView extends ListView {

    public MyListView(Context context) {
        super(context);
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // [Updated] MeasureSpec ကို AT_MOST အမြင့်ဆုံး ပမာဏအဖြစ် သတ်မှတ်ပြီး တိုင်းတာခြင်း
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        
        // ပြင်ဆင်ချက် - params.height ကို ဤနေရာတွင် တိုက်ရိုက်ပြင်ပါက Infinite Loop ဖြစ်တတ်သဖြင့် 
        // super.onMeasure ထဲသို့ expandSpec ကိုသာ ပေးပို့ပြီး စနစ်တကျ တိုင်းတာခိုင်းခြင်း
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
