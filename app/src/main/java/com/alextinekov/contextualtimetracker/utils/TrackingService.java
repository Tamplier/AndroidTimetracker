package com.alextinekov.contextualtimetracker.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
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
import android.os.PowerManager;
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
    private long prevTime = 0;
    private boolean prevScreenState = true;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");

        updateNotification("none");

        applicationsArray = new RunnedApplicationInfo[10];
    }

    private void updateNotification(String foregroundApp){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(String.format(getResources().getString(R.string.foreground_app), foregroundApp))
                .setContentIntent(intent);
        Notification notification;
        if (Build.VERSION.SDK_INT < 16)
            notification = builder.getNotification();
        else
            notification = builder.build();

        startForeground(SERVICE_ID, notification); //не убивать сервис при нехватке памяти и при удалении приложния из недавних.
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
        boolean isScreenOn = isScreenOn();
        if(prevScreenState != isScreenOn){
            prevTime = 0;
        }
        if(!isScreenOn){
            Log.i(TAG, "Screen off, no interaction");
            return;
        }
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        /**
         * Returns a list of RunningAppProcessInfo records, or null if there are no running processes
         * (it will not return an empty list).
         * This list ordering is not specified.
         */
        List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
        ActivityManager.RunningAppProcessInfo info = getForegroundProcess(processes);
        if(prevTime == 0) prevTime = System.currentTimeMillis();

        long deltaTime = System.currentTimeMillis() - prevTime;
        prevTime = System.currentTimeMillis();

        if(info == null || BuildConfig.APPLICATION_ID.equals(info.processName)){
            Log.i(TAG, "There is no foreground processes or not tracked package is found");
            updateNotification("none");
            return;
        }
        PackageManager pm = this.getPackageManager();
        CharSequence label;
        try{
            ApplicationInfo appInfo = pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA);
            label = pm.getApplicationLabel(appInfo);
        }
        catch (PackageManager.NameNotFoundException e){
            label = info.processName;
        }

        updateNotification(label.toString());

        if(applicationsArray[0] != null){
            applicationsArray[0].activeTime += deltaTime;
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

    private ActivityManager.RunningAppProcessInfo getForegroundProcess(List<ActivityManager.RunningAppProcessInfo> processes){
        ActivityManager.RunningAppProcessInfo foreground = null;
        if(processes == null)
            return foreground;
        for(ActivityManager.RunningAppProcessInfo pInfo : processes){
            /*
            * This process is running the foreground UI;
            * that is, it is the thing currently at the top of the screen that the user is interacting with.
            * */
            if(pInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                foreground = pInfo;
                break;
            }
        }
        return foreground;
    }

    private boolean isScreenOn(){
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        boolean result = false;
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.KITKAT_WATCH)
            result = pm.isInteractive();
        else
            result = pm.isScreenOn();
        return result;
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
