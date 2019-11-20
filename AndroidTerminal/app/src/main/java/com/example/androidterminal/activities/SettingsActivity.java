package com.example.androidterminal.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.androidterminal.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
//        ((TextView) findViewById(R.id.exitButton)).setTextColor(Color.RED);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        SharedPreferences prefs;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {     // settings' input checks go here
            super.onCreate(savedInstanceState);
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final int maxRuntime = Integer.parseInt(prefs.getString("maxRuntime", "0"));
            this.findPreference("runtime").setTitle("Runtime (max " + maxRuntime + ")");
            this.findPreference("runtime").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        if (val >= 0 && val <= maxRuntime) {
                            return true;
                        }
                        Toast.makeText(getActivity(), "Maximum possible runtime is " + maxRuntime, Toast.LENGTH_LONG).show();
                        return false;
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity(), "Runtime must be an integer", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            });

            this.findPreference("port").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    try {
                        int val = Integer.parseInt(newValue.toString());
                        if (val >= 0 && val <= 65535) {
                            return true;
                        }
                        Toast.makeText(getActivity(), "Not a valid port number", Toast.LENGTH_LONG).show();
                        return false;
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity(), "Port number must be an integer", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            });

            this.findPreference("ipAddr").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String ipv4AddrRegex = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
                    String ipv6AddrRegex = "(([0-9a-fA-F]{0,4}:){1,7}[0-9a-fA-F]{0,4})";
                    if (newValue.toString().matches(ipv4AddrRegex) || newValue.toString().matches(ipv6AddrRegex)) {
                        return true;
                    }
                    Toast.makeText(getActivity(), "Not a valid IP address", Toast.LENGTH_LONG).show();
                    return false;
                }
            });
        }

        public AlertDialog buildExitAlert() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle("Confirm Exit");
            builder.setMessage("Are you sure you want to exit?\nAny active transmission will terminate!");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        getActivity().finishAffinity();
                    } else{
                        getActivity().finish();
                    }
                    System.exit( 0 );
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("exitButton")) {
                Log.i("exit button", "clicked");
                buildExitAlert().show();
            }
            return super.onPreferenceTreeClick(preference);
        }
    }

}