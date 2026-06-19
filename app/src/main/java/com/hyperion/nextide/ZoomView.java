package com.hyperion.nextide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Optimized Zooming view for Next-IDE.
 * Fixed non-stop invalidation to improve battery life and prevent device heating.
 */
public class ZoomView extends FrameLayout {

    public interface ZoomViewListener {
        void onZoomStarted(float zoom, float zoomx, float zoomy);
        void onZooming(float zoom, float zoomx, float zoomy);
        void onZoomEnded(float zoom, float zoomx, float zoomy);
    }

    float zoom = 1.0f; 
    float maxZoom = 4.0f; 
    float smoothZoom = 1.0f;
    float zoomX, zoomY;
    float smoothZoomX, smoothZoomY;
    private boolean scrolling;

    private boolean showMinimap = false;
    private int miniMapColor = Color.BLACK;
    private int miniMapHeight = -1;
    private String miniMapCaption;
    private float miniMapCaptionSize = 20.0f; 
    private int miniMapCaptionColor = Color.WHITE;

    private long lastTapTime;
    private float touchStartX, touchStartY;
    private float touchLastX, touchLastY;
    private float startd;
    private boolean pinching;
    private float lastd;
    private float lastdx1, lastdy1;
    private float lastdx2, lastdy2;

    private final Matrix m = new Matrix();
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); 

    ZoomViewListener listener;

    public ZoomView(final Context context) {
        super(context);
        setWillNotDraw(false); 
    }

    public ZoomView(final Context context, AttributeSet attributes) {
        super(context, attributes);
        setWillNotDraw(false);
    }

    public float getZoom() { return zoom; }
    public float getMaxZoom() { return maxZoom; }

    public void setMaxZoom(final float maxZoom) {
        if (maxZoom < 1.0f) return;
        this.maxZoom = maxZoom;
    }

    public void setMiniMapEnabled(final boolean showMiniMap) { this.showMinimap = showMiniMap; }
    public boolean isMiniMapEnabled() { return showMinimap; }

    public void setMiniMapHeight(final int miniMapHeight) {
        if (miniMapHeight < 0) return;
        this.miniMapHeight = miniMapHeight;
    }

    public int getMiniMapHeight() { return miniMapHeight; }
    public void setMiniMapColor(final int color) { miniMapColor = color; }
    public int getMiniMapColor() { return miniMapColor; }
    public String getMiniMapCaption() { return miniMapCaption; }
    public void setMiniMapCaption(final String miniMapCaption) { this.miniMapCaption = miniMapCaption; }

    public void zoomTo(final float zoom, final float x, final float y) {
        this.zoom = Math.min(zoom, maxZoom);
        zoomX = x;
        zoomY = y;
        smoothZoomTo(this.zoom, x, y);
    }

    public void smoothZoomTo(final float zoom, final float x, final float y) {
        smoothZoom = clamp(1.0f, zoom, maxZoom);
        smoothZoomX = x;
        smoothZoomY = y;
        if (listener != null) {
            listener.onZoomStarted(smoothZoom, x, y);
        }
        invalidate(); 
    }

    public void setListener(final ZoomViewListener listener) { this.listener = listener; }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        int pointerCount = ev.getPointerCount();

        if (pointerCount == 1) {
            processSingleTouchEvent(ev, action);
        }
        else if (pointerCount == 2) {
            processDoubleTouchEvent(ev, action);
        }

        return true;
    }

    private void processSingleTouchEvent(final MotionEvent ev, int action) {
        final float x = ev.getX();
        final float y = ev.getY();

        final float w = miniMapHeight * (float) getWidth() / getHeight();
        final float h = miniMapHeight;
        final boolean touchingMiniMap = x >= 10.0f && x <= 10.0f + w && y >= 10.0f && y <= 10.0f + h;

        if (showMinimap && smoothZoom > 1.0f && touchingMiniMap) {
            processSingleTouchOnMinimap(ev);
        } else {
            processSingleTouchOutsideMinimap(ev, action);
        }
    }

    private void processSingleTouchOnMinimap(final MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        final float w = miniMapHeight * (float) getWidth() / getHeight();
        final float h = miniMapHeight;
        final float zx = (x - 10.0f) / w * getWidth();
        final float zy = (y - 10.0f) / h * getHeight();
        smoothZoomTo(smoothZoom, zx, zy);
    }

    private void processSingleTouchOutsideMinimap(final MotionEvent ev, int action) {
        final float x = ev.getX();
        final float y = ev.getY();
        float lx = x - touchStartX;
        float ly = y - touchStartY;
        final float l = (float) Math.hypot(lx, ly);
        float dx = x - touchLastX;
        float dy = y - touchLastY;
        touchLastX = x;
        touchLastY = y;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = x;
                touchStartY = y;
                scrolling = false;
                break;

            case MotionEvent.ACTION_MOVE:
                if (scrolling || (smoothZoom > 1.0f && l > 30.0f)) {
                    if (!scrolling) {
                        scrolling = true;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                    }
                    smoothZoomX -= dx / zoom;
                    smoothZoomY -= dy / zoom;
                    invalidate(); 
                    return;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (l < 30.0f) {
                    if (System.currentTimeMillis() - lastTapTime < 500) {
                        if (smoothZoom == 1.0f) {
                            smoothZoomTo(maxZoom, x, y);
                        } else {
                            smoothZoomTo(1.0f, getWidth() / 2.0f, getHeight() / 2.0f);
                        }
                        lastTapTime = 0;
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.dispatchTouchEvent(ev);
                        return;
                    }
                    lastTapTime = System.currentTimeMillis();
                    performClick();
                }
                if (listener != null) {
                    listener.onZoomEnded(zoom, zoomX, zoomY);
                }
                break;
        }

        ev.setLocation(zoomX + (x - 0.5f * getWidth()) / zoom, zoomY + (y - 0.5f * getHeight()) / zoom);
        super.dispatchTouchEvent(ev);
    }

    private void processDoubleTouchEvent(final MotionEvent ev, int action) {
        try {
            final float x1 = ev.getX(0);
            final float y1 = ev.getY(0);
            final float x2 = ev.getX(1);
            final float y2 = ev.getY(1);

            final float d = (float) Math.hypot(x2 - x1, y2 - y1);
            final float dd = d - lastd;
            lastd = d;
            final float ld = Math.abs(d - startd);

            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    startd = d;
                    pinching = false;
                    lastdx1 = x1; lastdy1 = y1;
                    lastdx2 = x2; lastdy2 = y2;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (pinching || ld > 30.0f) {
                        pinching = true;
                        final float dxk = 0.5f * ((x1 - lastdx1) + (x2 - lastdx2));
                        final float dyk = 0.5f * ((y1 - lastdy1) + (y2 - lastdy2));
                        smoothZoomTo(Math.max(1.0f, zoom * d / (d - dd)), zoomX - dxk / zoom, zoomY - dyk / zoom);
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_UP:
                    pinching = false;
                    break;
            }
            
            lastdx1 = x1; lastdy1 = y1;
            lastdx2 = x2; lastdy2 = y2;

        } catch (Exception ignored) {}

        ev.setAction(MotionEvent.ACTION_CANCEL);
        super.dispatchTouchEvent(ev);
    }

    private float clamp(final float min, final float value, final float max) {
        return Math.max(min, Math.min(value, max));
    }

    private float lerp(final float a, final float b, final float k) {
        return a + (b - a) * k;
    }

    private float bias(final float a, final float b, final float k) {
        return Math.abs(b - a) >= k ? a + k * Math.signum(b - a) : b;
    }

    @Override
    protected void dispatchDraw(final Canvas canvas) {
        zoom = lerp(bias(zoom, smoothZoom, 0.05f), smoothZoom, 0.2f);
        smoothZoomX = clamp(0.5f * getWidth() / smoothZoom, smoothZoomX, getWidth() - 0.5f * getWidth() / smoothZoom);
        smoothZoomY = clamp(0.5f * getHeight() / smoothZoom, smoothZoomY, getHeight() - 0.5f * getHeight() / smoothZoom);

        zoomX = lerp(bias(zoomX, smoothZoomX, 0.1f), smoothZoomX, 0.35f);
        zoomY = lerp(bias(zoomY, smoothZoomY, 0.1f), smoothZoomY, 0.35f);
        
        if (zoom != smoothZoom && listener != null) {
            listener.onZooming(zoom, zoomX, zoomY);
        }

        // ပြင်ဆင်ချက် - Float တန်ဖိုး ကွာဟချက်ကို ပိုမိုကျယ်ပြန့်စွာ တိုင်းတာပြီး ကာတွန်းပြီးဆုံးက ငြိမ်သက်သွားစေခြင်း
        final boolean animating = Math.abs(zoom - smoothZoom) > 0.01f
                || Math.abs(zoomX - smoothZoomX) > 0.1f || Math.abs(zoomY - smoothZoomY) > 0.1f;

        // တန်ဖိုး တအားနီးကပ်သွားပါက မူရင်းတန်ဖိုးအတိုင်း တိုက်ရိုက်သတ်မှတ်၍ Redraw ရပ်တန့်ခြင်း
        if (!animating) {
            zoom = smoothZoom;
            zoomX = smoothZoomX;
            zoomY = smoothZoomY;
        }

        if (getChildCount() == 0) return;

        m.reset();
        m.setTranslate(0.5f * getWidth(), 0.5f * getHeight());
        m.preScale(zoom, zoom);
        m.preTranslate(-clamp(0.5f * getWidth() / zoom, zoomX, getWidth() - 0.5f * getWidth() / zoom),
                       -clamp(0.5f * getHeight() / zoom, zoomY, getHeight() - 0.5f * getHeight() / zoom));

        final View v = getChildAt(0);
        m.preTranslate(v.getLeft(), v.getTop());

        canvas.save();
        canvas.concat(m);
        v.draw(canvas);
        canvas.restore();

        if (showMinimap) {
            if (miniMapHeight < 0) {
                miniMapHeight = getHeight() / 4;
            }

            canvas.save();
            canvas.translate(10.0f, 10.0f);

            p.setColor(0x80000000 | (0x00ffffff & miniMapColor));
            final float w = miniMapHeight * (float) getWidth() / getHeight();
            final float h = miniMapHeight;
            canvas.drawRect(0.0f, 0.0f, w, h, p);

            if (miniMapCaption != null && miniMapCaption.length() > 0) {
                p.setTextSize(miniMapCaptionSize);
                p.setColor(miniMapCaptionColor);
                canvas.drawText(miniMapCaption, 10.0f, 10.0f + miniMapCaptionSize, p);
            }

            p.setColor(0x40ffffff & miniMapColor);
            final float dx = w * zoomX / getWidth();
            final float dy = h * zoomY / getHeight();
            canvas.drawRect(dx - 0.5f * w / zoom, dy - 0.5f * h / zoom, dx + 0.5f * w / zoom, dy + 0.5f * h / zoom, p);

            canvas.restore();
        }

        if (animating) {
            postInvalidateOnAnimation();
        }
    }
}
