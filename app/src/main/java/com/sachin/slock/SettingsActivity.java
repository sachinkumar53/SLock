package com.sachin.slock;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
    private SwitchPreference mEnabled;
    private ListPreference timePosition;

    public class Keys {
        public static final String R_TO_D = "reset";
        public static final String LOCKER_ENABLED = "enabled";
        public static final String TIME_POSITION="time";
        public static final String OWNER = "owner";
        public static final String NAME = "name";
        public static final String SOUND = "sound";
        public static final String WALLPAPER = "wallpaper";
    }

    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.s_lock_settings);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean val = sharedPreferences.getBoolean(Keys.LOCKER_ENABLED,false);

        if (val == true){
            startService(new Intent(getBaseContext(),SLockService.class));
        }
        if (val == false){
            stopService(new Intent(getBaseContext(),SLockService.class));
        }

        CheckBoxPreference wallpaper = (CheckBoxPreference)findPreference(Keys.WALLPAPER);
        wallpaper.setDefaultValue(true);
        wallpaper.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editor.putBoolean(Keys.WALLPAPER,Boolean.parseBoolean(String.valueOf(newValue)));
                editor.commit();
                return true;
            }
        });

        mEnabled = (SwitchPreference)findPreference(Keys.LOCKER_ENABLED);
        mEnabled.setDefaultValue(false);
        mEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean val = Boolean.parseBoolean(String.valueOf(newValue));
                editor.putBoolean(Keys.LOCKER_ENABLED, val);
                editor.commit();
                if (val == true) {
                    startService(new Intent(getBaseContext(), SLockService.class));
                }
                if (val == false) {
                    stopService(new Intent(getBaseContext(), SLockService.class));
                }
                return true;
            }
        });



        timePosition = (ListPreference)findPreference(Keys.TIME_POSITION);
        timePosition.setDefaultValue("center");
        timePosition.setSummary(sharedPreferences.getString(Keys.TIME_POSITION,"center"));
        timePosition.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editor.putString(Keys.TIME_POSITION, String.valueOf(newValue));
                editor.commit();
                timePosition.setSummary(String.valueOf(newValue));
                return true;
            }
        });

        CheckBoxPreference ownerCheck = (CheckBoxPreference)findPreference(Keys.OWNER);
        ownerCheck.setDefaultValue(false);
        ownerCheck.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isEnabled = Boolean.parseBoolean(String.valueOf(newValue));
                editor.putBoolean(Keys.OWNER, isEnabled);
                editor.commit();
                return true;
            }
        });

        CheckBoxPreference checkBoxPreference = (CheckBoxPreference)findPreference(Keys.SOUND);
        checkBoxPreference.setDefaultValue(true);
        checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                editor.putBoolean(Keys.SOUND,Boolean.parseBoolean(String.valueOf(newValue)));
                editor.commit();
                return true;
            }
        });

        final Preference name = (Preference)findPreference(Keys.NAME);
        name.setSummary(sharedPreferences.getString(Keys.NAME,"Tap to change name"));
        name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
                alertDialog.setTitle("Enter your name");
                final EditText editText = new EditText(SettingsActivity.this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                editText.setLayoutParams(layoutParams);
                alertDialog.setView(editText);
                editText.setText(sharedPreferences.getString(Keys.NAME, "Sachin Kumar"));
                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = editText.getText().toString();
                        editor.putString(Keys.NAME, name);
                        editor.commit();
                        preference.setSummary(name);
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
                return true;
            }
        });

        Preference preference = (Preference)findPreference(Keys.R_TO_D);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                editor.clear();
                editor.commit();
                Toast.makeText(getBaseContext(),"Reset to default",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}
