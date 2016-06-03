package com.alextinekov.contextualtimetracker.data;

import android.graphics.drawable.Drawable;

/**
 * Created by Alex Tinekov on 28.05.2016.
 */
public class RunnedApplicationInfo {
    public String name;
    public long activeTime;
    public String packageName;
    //public Drawable icon; //не оптимально хранить все в памяти
}
