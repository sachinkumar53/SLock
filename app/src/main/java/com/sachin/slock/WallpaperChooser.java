package com.sachin.slock;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class WallpaperChooser extends Activity {

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.wallpaper_chooser);

        ViewPager viewPager = (ViewPager)findViewById(R.id.viewpager);
    }
}
