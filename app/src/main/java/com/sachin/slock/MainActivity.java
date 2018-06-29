package com.sachin.slock;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.plattysoft.leonids.ParticleSystem;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener {
    private GestureDetector gestureDetector;
    private static final int SWIPE_SENSITIVITY = 150;
    private TextView timeDisplay;
    private String time;
    private SimpleDateFormat simpleDateFormat;
    private TextView textView;
    private TextView batteryText;
    private String chargingText;
    private ImageButton phone;
    private ImageButton camera;
    private Handler handler;
    private TextView amPm;
    private String amPmText;
    private boolean withSound;
    private TextView owner;
    private MediaPlayer mediaPlayer;
    private ParticleSystem particleSystem;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 19){
            super.setTheme(R.style.LockTheme_Trans);

            try {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        boolean wallVal = sharedPreferences.getBoolean(SettingsActivity.Keys.WALLPAPER,true);
        if (wallVal ){
            mainLayout.setBackgroundResource(R.drawable.default_lock_bg);

        }else
            mainLayout.setBackgroundDrawable(getWallpaper());

        withSound = sharedPreferences.getBoolean(SettingsActivity.Keys.SOUND, true);
        relativeLayout = (RelativeLayout)findViewById(R.id.time_layout);
        //set gravity using preference

        String val = sharedPreferences.getString(SettingsActivity.Keys.TIME_POSITION,"center");
        if (val.equals("center")) {
            relativeLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        if (val.equals("left")){
            relativeLayout.setGravity(Gravity.LEFT);
        }
        if (val.equals("right")){
            relativeLayout.setGravity(Gravity.RIGHT);
        }

        gestureDetector = new GestureDetector(this, this);

        timeDisplay = (TextView) findViewById(R.id.time);
        timeDisplay.setTextSize(90);
        timeDisplay.setTextColor(Color.WHITE);

        amPm = (TextView) findViewById(R.id.am_pm);
        amPm.setTextColor(Color.WHITE);
        batteryText = (TextView)findViewById(R.id.charging_text);
        batteryText.setTextColor(Color.WHITE);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/AryanFontThin.ttf");
        timeDisplay.setTypeface(typeface);
        amPm.setTypeface(typeface);

        textView = (TextView) findViewById(R.id.date);
        textView.setTextColor(Color.WHITE);
        String date = new SimpleDateFormat("E, MMM dd").format(Calendar.getInstance().getTime());
        textView.setText(date);

        owner = (TextView)findViewById(R.id.owner_name);
        boolean isEnabled = sharedPreferences.getBoolean(SettingsActivity.Keys.OWNER,false);
        String name = sharedPreferences.getString(SettingsActivity.Keys.NAME,"Sachin Kumar");
        owner.setText(name);
        if (isEnabled){
            owner.setVisibility(View.VISIBLE);
        }else {
            owner.setVisibility(View.GONE);
        }

        handler = new Handler();
        updateTime();

        mediaPlayer = MediaPlayer.create(this,R.raw.s_unlock);

        phone = (ImageButton) findViewById(R.id.ib_phone);
        phone.setBackgroundResource(R.drawable.phone);
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        camera = (ImageButton) findViewById(R.id.ib_camera);
        camera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Canvas canvas = new Canvas();
                Paint paint = new Paint();
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(50, 50, 20, paint);
                return true;
            }
        });

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
                    updateTime();
                }
                if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                    updateTime();
                }
                if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    chargingText = "Charging (" + String.valueOf(level) + "%)";
                    batteryText.setText(chargingText);

                    if (level == BatteryManager.BATTERY_STATUS_FULL) {
                        chargingText = "Charged! Unplug your charger";
                        batteryText.setText(chargingText);
                    }
                }

                if (intent.getAction().equals(BatteryManager.BATTERY_STATUS_FULL)){
                    chargingText = "Charged! Unplug your charger";
                    batteryText.setText(chargingText);
                }

                if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)){
                    batteryText.setVisibility(View.VISIBLE);
                }
                else {
                    batteryText.setVisibility(View.GONE);

                    if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)){
                    chargingText = "Connect your charger";
                    batteryText.setText(chargingText);
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case  MotionEvent.ACTION_DOWN:
                // Create a particle system and start emiting
                particleSystem = new ParticleSystem(this, 100, R.drawable.particle, 500);
                particleSystem.setScaleRange(0.2f, 1.2f);
                particleSystem.setSpeedRange(0.05f, 0.1f);
                particleSystem.setRotationSpeedRange(90, 180);
                particleSystem.setFadeOut(800, new AccelerateInterpolator());
                particleSystem.emit((int) event.getX(), (int) event.getY(), 40);
                break;
            case MotionEvent.ACTION_MOVE:
                particleSystem.updateEmitPoint((int) event.getX(), (int) event.getY());
                break;
            case MotionEvent.ACTION_UP:
                particleSystem.stopEmitting();
                break;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            MediaPlayer mediaPlayer  = MediaPlayer.create(this,R.raw.s_lock_touch);
            mediaPlayer.start();
        }
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float SWIPE_UP = Math.abs(e1.getY() - e2.getY());
        float SWIPE_DOWN = Math.abs(e2.getY() - e1.getY());
        float SWIPE_LEFT = Math.abs(e2.getX() - e1.getX());
        float SWIPE_RIGHT = Math.abs(e1.getX() - e2.getX());
        if (SWIPE_UP > SWIPE_SENSITIVITY) {
            unLock();
        } else if (SWIPE_DOWN > SWIPE_SENSITIVITY) {
            unLock();
        } else if (SWIPE_LEFT > SWIPE_SENSITIVITY) {
            unLock();
        } else if (SWIPE_RIGHT > SWIPE_SENSITIVITY) {
            unLock();
        }
        return true;
    }

    private void unLock() {
        if (withSound) {
            mediaPlayer.start();
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_BACK;
    }

    public void updateTime() {
        if (DateFormat.is24HourFormat(this)) {
            amPm.setVisibility(View.GONE);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    time = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
                    timeDisplay.setText(time);
                    handler.postDelayed(this, 1000);
                }
            });
        } else {
            amPmText = new SimpleDateFormat("a").format(Calendar.getInstance().getTime());
            amPm.setText(amPmText);
            amPm.setVisibility(View.VISIBLE);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    time = new SimpleDateFormat("hh:mm").format(Calendar.getInstance().getTime());
                    timeDisplay.setText(time);
                    handler.postDelayed(this, 1000);
                }
            });
        }

    }
}
