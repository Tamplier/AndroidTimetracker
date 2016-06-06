package com.alextinekov.contextualtimetracker.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alextinekov.contextualtimetracker.BuildConfig;
import com.alextinekov.contextualtimetracker.MainActivity;
import com.alextinekov.contextualtimetracker.R;
import com.alextinekov.contextualtimetracker.data.DBQueryWrapper;
import com.alextinekov.contextualtimetracker.data.RunnedApplicationInfo;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Alex Tinekov on 25.05.2016.
 */
public class TrackingService extends Service {
    private Thread workerThread;
    private static final int SERVICE_ID = 777;
    private static final String TAG = TrackingService.class.getSimpleName();
    private static final int TRACK_PERIOD_IN_MILLIS = 5000;
    public static final int MSG_SAVE_STATE = 1;
    private static final String SYSTEM_PACKAGE_NAME = "android";
    private RunnedApplicationInfo applicationsArray[];
    private long prevTime;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher);
        Notification notification;
        if (Build.VERSION.SDK_INT < 16)
            notification = builder.getNotification();
        else
            notification = builder.build();

        startForeground(SERVICE_ID, notification); //не убивать сервис при нехватке памяти и при удалении приложния из недавних.

        applicationsArray = new RunnedApplicationInfo[10];
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Command started");
        //если поток работает, то игнорим старт
        if(workerThread == null || !workerThread.isAlive())
            testTask();
        return START_REDELIVER_INTENT; //после остановки перезапустить и повторно запустить не завершенную задачу
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(workerThread != null)
            workerThread.interrupt();
        Log.i(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Messenger(interactionHandler).getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    private void testTask(){
        workerThread= new Thread(new Runnable() {
            @Override
            public void run() {
                boolean interrupted = false;
                while (!Thread.interrupted() && !interrupted){
                    updateAppsList();
                    try{
                        Thread.sleep(TRACK_PERIOD_IN_MILLIS);
                    }
                    catch (InterruptedException e){
                        interrupted = true;
                        Log.i(TAG, "Thread interrupted");
                    }
                }
                stopSelf();
            }
        });
        workerThread.start();
    }

    private void updateAppsList(){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        ActivityManager.RunningAppProcessInfo info = processes.get(0);
        PackageManager pm = this.getPackageManager();
        CharSequence label;
        try{
            if(!isForegroundProcess(info) || BuildConfig.APPLICATION_ID.equals(info.processName)) {
                Log.i(TAG, "Not tracked package is found: " + info.processName);
                return;
            }
            ApplicationInfo appInfo = pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA);
            label = pm.getApplicationLabel(appInfo);
        }
        catch (PackageManager.NameNotFoundException e){
            label = info.processName;
        }

        if(applicationsArray[0] != null){
            applicationsArray[0].activeTime += TRACK_PERIOD_IN_MILLIS;
        }
        if(applicationsArray[0] == null || !info.processName.equals(applicationsArray[0].packageName)){
            RunnedApplicationInfo rai = new RunnedApplicationInfo();
            rai.activeTime = 0;
            rai.packageName = info.processName;
            rai.name = label.toString();
            System.arraycopy(applicationsArray, 0, applicationsArray, 1, applicationsArray.length-1);
            applicationsArray[0] = rai;
        }

        Log.i(TAG, String.format("Top process %s. Time: %d", label, applicationsArray[0].activeTime));
    }

    private boolean isForegroundProcess(ActivityManager.RunningAppProcessInfo processInfo){
        return processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
    }

    private Handler interactionHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SAVE_STATE:
                    SQLiteDatabase db = DBQueryWrapper.getInstance(getApplicationContext()).openDatabase();
                    DBQueryWrapper.updateInfo(db, applicationsArray);
                    DBQueryWrapper.getInstance(getApplicationContext()).closeDatabase();
                    Message m = Message.obtain();
                    m.what = MainActivity.MSG_SAVE_DONE;
                    try {
                        msg.replyTo.send(m);
                    }
                    catch (RemoteException e){}
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };
}
