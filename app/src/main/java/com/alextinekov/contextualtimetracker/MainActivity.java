package com.alextinekov.contextualtimetracker;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.alextinekov.contextualtimetracker.data.ApplicationsListAdapter;
import com.alextinekov.contextualtimetracker.data.DBQueryWrapper;
import com.alextinekov.contextualtimetracker.data.RunnedApplicationInfo;
import com.alextinekov.contextualtimetracker.utils.TrackingService;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static final int MSG_SAVE_DONE = 1;
    private Button stopServiceButton;
    private ListView appList;
    private ServiceConnection serviceConnection;
    private Messenger serviceMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    private void init(){
        stopServiceButton = (Button)findViewById(R.id.stop_service);
        appList = (ListView)findViewById(R.id.app_list);
        stopServiceButton.setOnClickListener(this);

        startTrackingService();
    }

    private void startTrackingService(){
        Intent i = new Intent(this, TrackingService.class);
        startService(i);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceMessenger = new Messenger(service);
                Message m = Message.obtain();
                m.what = TrackingService.MSG_SAVE_STATE;
                m.replyTo = new Messenger(serviceInteraction);
                try {
                    serviceMessenger.send(m);
                }
                catch (RemoteException e){}
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(i, serviceConnection, 0);
    }

    @Override
    public void onClick(View v) {
        if(v == stopServiceButton){
            stopService(new Intent(this, TrackingService.class));
        }
    }

    private Handler serviceInteraction = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SAVE_DONE:
                    if(serviceConnection != null){
                        unbindService(serviceConnection);
                        serviceConnection = null;
                    }
                    showLastApplications();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private void showLastApplications(){
        SQLiteDatabase db = DBQueryWrapper.getInstance(this).openDatabase();
        List<RunnedApplicationInfo> apps = DBQueryWrapper.getAppInfo(db);
        DBQueryWrapper.getInstance(this).closeDatabase();
        appList.setAdapter(new ApplicationsListAdapter(apps, getPackageManager()));
    }
}
