package com.sachin.slock;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class SLockService extends Service {

    KeyguardManager.KeyguardLock keyguardLock;
    boolean withSound;

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        withSound = sharedPreferences.getBoolean(SettingsActivity.Keys.SOUND,true);
        keyguardLock = ((KeyguardManager) getSystemService(KEYGUARD_SERVICE)).newKeyguardLock("newLock");
        keyguardLock.disableKeyguard();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(broadcastReceiver, intentFilter);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        keyguardLock.reenableKeyguard();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
                    Intent i = new Intent(context,MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(i);
                }
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
                    if (withSound == true) {
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.s_lock);
                        mediaPlayer.start();
                    }else{
                        return;
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    };
}
