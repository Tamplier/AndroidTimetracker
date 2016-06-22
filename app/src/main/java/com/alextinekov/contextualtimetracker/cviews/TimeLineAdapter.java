package com.alextinekov.contextualtimetracker.cviews;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ArrayAdapter;

import com.alextinekov.contextualtimetracker.R;
import com.alextinekov.contextualtimetracker.data.RunnedApplicationInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex Tinekov on 13.06.2016.
 */
public class TimeLineAdapter{
    private Context context;
    private List<RunnedApplicationInfo> data;
    private ArrayList<CircleElementView> viewPool;
    private PackageManager packageManager;
    private static final int MAX_POOL_SIZE = 5;
    /*
        По образу и подобию ListView. Удобнее, чтобы отдельный класс создавал child элементы из массива и добавлял в ViewGroup.
    * */
    public TimeLineAdapter(Context ctx, List<RunnedApplicationInfo> data, PackageManager pm){
        context = ctx;
        this.data = data;
        packageManager = pm;
        viewPool = new ArrayList<CircleElementView>();
    }

    public int getCount(){
        return data.size();
    }

    public RunnedApplicationInfo getInfo(int i){
        return data.get(i);
    }


    private CircleElementView getViewForItem(int position, CircleElementView view){
        if(view == null){
            view = new CircleElementView(context);
        }
        RunnedApplicationInfo info = getInfo(position);
        if(info.icon == null) {
            try {
                Drawable icon = packageManager.getApplicationIcon(info.packageName);
                if(icon == null) icon = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
                view.setIcon(icon);
            } catch (PackageManager.NameNotFoundException e) {
                view.setIcon(ContextCompat.getDrawable(context, R.mipmap.ic_launcher));
            }
        }
        else{
            view.setIcon(info.icon);
        }
        return view;
    }

    public CircleElementView getItem(int i){
        CircleElementView v = null;
        if(viewPool.size() > 0){
            v = viewPool.get(viewPool.size() - 1);
            viewPool.remove(viewPool.size() - 1);
        }
        return getViewForItem(i, v);
    }

    public void letOutView(CircleElementView v){
        if(viewPool.size() < MAX_POOL_SIZE)
            viewPool.add(v);
    }

}
