package com.alextinekov.contextualtimetracker.cviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.alextinekov.contextualtimetracker.R;

/**
 * Created by Alex Tinekov on 10.06.2016.
 */
public class CircleElementView extends View {
    private static final String TAG = CircleElementView.class.getSimpleName();
    private Paint paint;
    private int x, y;
    private int radius;
    private int iconLeft, iconTop, iconRight, iconBottom;
    private float strokeSize;
    private int strokeColor;
    private int mainColor;
    private Drawable icon;
    public CircleElementView(Context context) {
        super(context);
        init();
    }

    public CircleElementView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleElementView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public CircleElementView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(){
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.timeline_stroke_color));
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    private void init(Context context, AttributeSet attrs){
        init();
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircleElementView);
        try {
            strokeSize = attributes.getDimension(R.styleable.CircleElementView_strokeSize, 0);
            strokeColor = attributes.getColor(R.styleable.CircleElementView_strokeColor, Color.BLACK);
            mainColor = attributes.getColor(R.styleable.CircleElementView_mainColor, Color.BLACK);
            icon = attributes.getDrawable(R.styleable.CircleElementView_tlElementIcon);
        } finally {
            attributes.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(strokeColor);
        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(mainColor);
        canvas.drawCircle(x, y, radius-strokeSize, paint);
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        icon.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //Calculate positions, dimensions, and any other values related to your view's size
        int maxR1 = (h - getPaddingTop() - getPaddingBottom()) / 2;
        int maxR2 = (w - getPaddingLeft() - getPaddingRight()) / 2;
        if(maxR1 > maxR2){
            radius = maxR2;
            x = getPaddingLeft() + radius;
            y = (h - getPaddingTop() - getPaddingBottom())/2 + getPaddingTop();
        }
        else{
            radius = maxR1;
            x = (w - getPaddingLeft() - getPaddingRight())/2 + getPaddingLeft();
            y = getPaddingTop() + radius;
        }
        //radius = Math.min(maxR1, maxR2);
        int maxIconSize = (int) ((radius - strokeSize) * 2 / Math.sqrt(2));
        iconLeft = getPaddingLeft() + (w - getPaddingLeft() - getPaddingRight() - maxIconSize) / 2;
        iconTop = getPaddingTop() + (h - getPaddingTop() - getPaddingBottom() - maxIconSize) / 2;
        iconRight = iconLeft + maxIconSize;
        iconBottom = iconTop + maxIconSize;
        //icon = resize(icon, maxIconSize, maxIconSize);
        //Log.d(TAG, "padding left: " + getPaddingLeft());

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //this method's parameters are View.MeasureSpec values that tell you how big your view's parent wants your view to be, and whether that size is a hard maximum or just a suggestion.
        //resolveSizeAndState -
        Log.d(TAG, "suggested minimum: " + getSuggestedMinimumWidth());
        int minWidth = getPaddingLeft() + getPaddingRight() + radius * 2;
        int minHeight = getPaddingTop() + getPaddingBottom() + radius * 2;
        setMeasuredDimension(resolveSizeAndState(minWidth, widthMeasureSpec, 1), resolveSizeAndState(minHeight, heightMeasureSpec, 1));
    }

    private Drawable resize(Drawable image, int newWidth, int newHeight) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 50, 50, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }
}
