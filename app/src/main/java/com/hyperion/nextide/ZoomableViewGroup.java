package com.hyperion.nextide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Advanced ZoomableViewGroup for Next-IDE.
 * Fixed dispatchTouchEvent matrix mapping and removed infinite measurement loops to fix typing lag.
 */
public class ZoomableViewGroup extends ViewGroup {

    private final Matrix matrix = new Matrix();
    private final Matrix matrixInverse = new Matrix();
    private final Matrix savedMatrix = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    private final PointF start = new PointF();
    private final PointF mid = new PointF();
    private float oldDist = 1f;

    private boolean initZoomApplied = false;
    private final float[] mDispatchTouchEventWorkingArray = new float[2];

    public ZoomableViewGroup(Context context) { super(context); init(context); }
    public ZoomableViewGroup(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
    public ZoomableViewGroup(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }

    private void init(Context context) {
        setWillNotDraw(false); 
    }

    private float[] screenPointsToScaledPoints(float[] a) {
        matrixInverse.mapPoints(a);
        return a;
    }

    // ပြင်ဆင်ချက် - Child Views များဆီသို့ Touch Event ကို Matrix အတိုင်း စနစ်တကျ လက်ဆင့်ကမ်းပေးပို့ခြင်း
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mDispatchTouchEventWorkingArray[0] = ev.getX();
        mDispatchTouchEventWorkingArray[1] = ev.getY();
        screenPointsToScaledPoints(mDispatchTouchEventWorkingArray);
        
        float x = mDispatchTouchEventWorkingArray[0];
        float y = mDispatchTouchEventWorkingArray[1];

        // မူရင်း Event အား မထိခိုက်စေဘဲ Event အသစ်ပွား၍ တည်နေရာညှိပြီးမှ Child များထံ ပေးပို့ခြင်း
        MotionEvent transformedEvent = MotionEvent.obtain(ev);
        transformedEvent.setLocation(x, y);
        
        boolean handled = super.dispatchTouchEvent(transformedEvent);
        transformedEvent.recycle();
        
        // အကယ်၍ Child (EditText) မှ Touch အား အသုံးမပြုပါက ၎င်း ViewGroup ကိုယ်တိုင် ပွတ်ဆွဲ/ချုံ့ချဲ့ရန် စီမံခြင်း
        if (!handled) {
            handled = onTouchEvent(ev);
        }
        
        return handled;
    }

    private float spacing(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } catch (Exception e) {
            return 0f;
        }
    }

    private void midPoint(PointF point, MotionEvent event) {
        try {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        } catch (Exception ignored) {}
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    // ပြင်ဆင်ချက် - Infinite Loop ဖြစ်စေသော onMeasure ထဲမှ Zoom တွက်ချက်မှုကို မျက်နှာပြင်အရွယ်အစား အတည်ပြုပြီးချိန် (onSizeChanged) သို့ ပြောင်းရွှေ့ခြင်း
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!initZoomApplied && getChildCount() > 0) {
            View child = getChildAt(0);
            if (child.getMeasuredWidth() > 0) {
                zoomToFit(child.getMeasuredWidth(), child.getMeasuredHeight(), w, h);
            }
        }
    }

    private void zoomToFit(int c_w, int c_h, float container_width, float container_height) {
        if (c_h == 0 || container_height == 0) return;
        
        float proportion_firstChild = (float) c_w / (float) c_h;
        float proportion_container = container_width / container_height;

        if (proportion_container < proportion_firstChild) {
            float initZoom = container_height / c_h;
            matrix.postScale(initZoom, initZoom);
            matrix.postTranslate(-1 * (c_w * initZoom - container_width) / 2, 0);
        } else {
            float initZoom = container_width / c_w;
            matrix.postScale(initZoom, initZoom);
            matrix.postTranslate(0, -1 * (c_h * initZoom - container_height) / 2);
        }
        matrix.invert(matrixInverse);
        initZoomApplied = true;
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.concat(matrix);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            int pointerCount = event.getPointerCount();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    if (pointerCount >= 2) {
                        oldDist = spacing(event);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        float dx = event.getX() - start.x;
                        float dy = event.getY() - start.y;
                        matrix.postTranslate(dx, dy);
                        matrix.invert(matrixInverse);
                        invalidate();
                    } else if (mode == ZOOM && pointerCount >= 2) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = (newDist / oldDist);
                            matrix.postScale(scale, scale, mid.x, mid.y);
                            matrix.invert(matrixInverse);
                            invalidate();
                        }
                    }
                    break;
            }
        } catch (Exception ignored) {}

        return true;
    }
}
