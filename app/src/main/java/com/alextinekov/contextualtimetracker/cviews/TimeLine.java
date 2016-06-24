package com.alextinekov.contextualtimetracker.cviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.alextinekov.contextualtimetracker.R;

/**
 * Created by Alex Tinekov on 13.06.2016.
 */
public class TimeLine extends ViewGroup {
    private static final String TAG = TimeLine.class.getSimpleName();
    private TimeLineAdapter adapter;
    private int yPositions[];
    private int xPositions[];
    private float strokeSize;
    private int strokeColor;
    private int mainColor;
    private float itemSize;
    private float smallCircleSize;
    private float distanceBetweenItems;
    private float offsetFromCenter;
    private int layoutWidth;
    private Paint paint;
    LayoutParams lp;

    public TimeLine(Context context) {
        super(context);
    }

    public TimeLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TimeLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public TimeLine(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs){
        TypedArray attributes = ctx.obtainStyledAttributes(attrs, R.styleable.TimeLine);
        strokeSize = attributes.getDimension(R.styleable.TimeLine_tl_strokeSize, 5);
        strokeColor = attributes.getColor(R.styleable.TimeLine_tl_strokeColor, 0x7d7d7d);
        mainColor = attributes.getColor(R.styleable.TimeLine_tl_mainColor, Color.WHITE);
        itemSize = attributes.getDimension(R.styleable.TimeLine_tl_itemSize, 60);
        distanceBetweenItems = attributes.getDimension(R.styleable.TimeLine_tl_distanceBetweenItems, 80);
        offsetFromCenter = attributes.getDimension(R.styleable.TimeLine_tl_offsetFromTheCenter, 60);
        smallCircleSize = attributes.getDimension(R.styleable.TimeLine_tl_smallCircleSize, 10);

        Log.d(TAG, "item size: " + itemSize);
        lp = new LayoutParams((int) itemSize, (int)itemSize);
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        setBackgroundColor(0x00000000);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //на сколько понимаю, l,t,r,b относительно parent'а
        Log.d(TAG, "layout top: " + t);
        int count = getChildCount();
        int top = getPaddingTop();
        layoutWidth = r - l;
        int center = (layoutWidth - getPaddingLeft() - getPaddingRight())/2 + getPaddingLeft();

        if(yPositions == null || yPositions.length != count) {
            yPositions = new int[count];
            xPositions = new int[count];
        }


        for(int i = 0; i < count; i++){
            CircleElementView child = (CircleElementView) getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            int left = center;
            if(child.isPositiveApplication()){
                left = left + (int)offsetFromCenter;
            }
            else {
                left = left - width - (int) offsetFromCenter;
            }

            int right = left + width;
            int bottom = top + height;
            //тут l,t,r,b относительно текущего layout
            Log.d(TAG, "l, t, r, b : " + left + ", " + top + ", " + right + "," + bottom);
            yPositions[i] = top + height/2;
            xPositions[i] = left + width/2;
            child.layout(left,top,right,bottom);
            top += height + distanceBetweenItems;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Взять все дочерние компоненты и оценить высоту, ширину и позицию границы
        if(adapter == null){
            setMeasuredDimension(0, 0);
            return;
        }
        int childCount = adapter.getCount();
        int height = getPaddingTop() + getPaddingBottom();
        int childState = 0;

        for(int i = 0; i < childCount; i++){
            CircleElementView v = adapter.getItem(i);
            v.setStrokeColor(strokeColor);
            v.setMainColor(mainColor);
            v.setStrokeSize(strokeSize);
            v.setLayoutParams(lp);
            addView(v);
            Log.d(TAG, " added child");
            measureChild(v, widthMeasureSpec, heightMeasureSpec);
            height += v.getMeasuredHeight();
            Log.d(TAG, "height: " + height);
            childState = combineMeasuredStates(childState, v.getMeasuredState());
        }
        height += (childCount - 1) * distanceBetweenItems;
        height = resolveSizeAndState(height, heightMeasureSpec, childState);
        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(Color.BLACK);
        int hCenter = getWidth()/2;
        canvas.drawRect(0, 0, hCenter, getHeight(), paint);
        paint.setColor(strokeColor);
        paint.setStrokeWidth(strokeSize);
        canvas.drawLine(hCenter, 0, hCenter, getHeight(), paint);
        for(int i = 0; i < yPositions.length; i++){
            paint.setColor(strokeColor);
            canvas.drawLine(hCenter, yPositions[i], xPositions[i], yPositions[i], paint);
            canvas.drawCircle(hCenter, yPositions[i], smallCircleSize, paint);
            paint.setColor(mainColor);
            canvas.drawCircle(hCenter, yPositions[i], smallCircleSize - strokeSize, paint);
        }
        //Don't forget to draw children
        super.onDraw(canvas);
    }

    public void setAdapter(TimeLineAdapter adapter)
    {
        this.adapter = adapter;
        removeAllViews();
        invalidate();
        requestLayout();
    }
}
