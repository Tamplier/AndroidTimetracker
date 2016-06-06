package com.alextinekov.contextualtimetracker.data;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alextinekov.contextualtimetracker.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Alex Tinekov on 03.06.2016.
 */
public class ApplicationsListAdapter extends BaseAdapter {
    private List<RunnedApplicationInfo> appsInfo;
    private PackageManager packageManager;

    public ApplicationsListAdapter(List<RunnedApplicationInfo> appsInfo, PackageManager pm){
        this.appsInfo = appsInfo;
        packageManager =  pm;
    }

    @Override
    public int getCount() {
        return appsInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return appsInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.application_in_list_layout, parent, false);
            view.setTag(new ViewHolder(view));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        RunnedApplicationInfo info = appsInfo.get(position);
        try {
            Drawable icon = packageManager.getApplicationIcon(info.packageName);
            holder.icon.setImageDrawable(icon);
        }
        catch (PackageManager.NameNotFoundException e){}

        holder.name.setText(info.name);

        holder.time.setText(millisToString(info.activeTime));
        return view;
    }

    private String millisToString(long millis){
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.HOURS.toSeconds(TimeUnit.MILLISECONDS.toHours(millis)) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)))    );
    }

    class ViewHolder {
        public ImageView icon;
        public TextView name;
        public TextView time;
        public ViewHolder(View v){
            icon = (ImageView)v.findViewById(R.id.icon);
            name = (TextView)v.findViewById(R.id.app_name);
            time = (TextView)v.findViewById(R.id.time);
        }
    }
}
