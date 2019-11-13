package com.example.androidterminal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    SharedPreferences prefs;
    private TextView runtimeText;

    TerminalPublisher esp = null;
    Handler handler = new Handler();
    List<String[]> dataList;
    int runtime;
    int maxRuntime = -1;
    int timerInt = 0;
    final int delay = 1000;     // milliseconds
    final int onlineCheckFrequency = 5;     // seconds

    private final int terminalId = 0;
    private final String terminalName = (terminalId == 0) ? "v26Terminal" : "v27Terminal";
    private final String terminalTopic = (terminalId == 0) ? "v26/topic" : "v27/topic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        runtimeText = findViewById(R.id.runtimeText);

        InputStream inputStream = (terminalId == 0) ? getResources().openRawResource(R.raw.vehicle_26) :
                getResources().openRawResource(R.raw.vehicle_27);
        CSVFileReader csvFile = new CSVFileReader(inputStream);
        dataList = csvFile.read();
        maxRuntime = dataList.size() - 1;
        runtimeText.setText(Integer.toString(runtime));

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("runtime", Integer.toString(maxRuntime));
//        editor.clear();
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        runtimeText.setText(Integer.toString(runtime - timerInt));
    }

    public void startTransmission(View view) {
        if (timerInt > 0) {
            Toast.makeText(this, "Already transmitting", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Log.i(terminalName, "Trying to connect ...");
            esp = new TerminalPublisher(terminalName, prefs.getString("ipAddr", null), Integer.parseInt(prefs.getString("port", 1883+"")), terminalTopic);
            Log.i(terminalName, "Connected successfully!");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        runtime = Integer.parseInt(prefs.getString("runtime", maxRuntime+""));
        // TODO: transfer check
        if (runtime < 0 || runtime > maxRuntime) {
            runtime = maxRuntime;
        }
        runtimeText.setText(Integer.toString(runtime));

        esp.publishMessage("'" + terminalName + "' connected successfully; starting transmission of " + runtime + " datapoints");
        if (esp == null) {
            Toast.makeText(this, "Check app settings:\nIP and/or Port not set", Toast.LENGTH_SHORT).show();
            return;
        }
        final Context con = this;
        if (! isDeviceOnline(con)) {
            Toast.makeText(this, "No internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }

        handler.postDelayed(new Runnable(){
            public void run(){
                if (timerInt % onlineCheckFrequency == 0) isDeviceOnline(con);
                esp.publishMessage(TextUtils.join(",", dataList.get(timerInt)));
                if (timerInt < runtime) {
                    timerInt++;
                    runtimeText.setText(Integer.toString(runtime - timerInt));
                    handler.postDelayed(this, delay);
                } else {
                    runtimeText.setText("Transmission complete!\n" + runtime + " datapoints were sent successfully!");
                    esp.publishMessage("Transmission of " + runtime + " datapoints from '" + terminalName + "' completed successfully!");
                    timerInt = 0;
                }
            }
        }, delay);
    }


    public void stopTransmission(View view) {
        if (timerInt > 0) {
            handler.removeCallbacksAndMessages(null);
            runtimeText.setText("Transmission was stopped!");
            esp.publishMessage("Transmission from '" + terminalName + "' was stopped");
            timerInt = 0;
        }
    }

    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isOnline = (networkInfo != null && networkInfo.isConnected());
        if(!isOnline)
            Toast.makeText(context, "No internet Connection", Toast.LENGTH_SHORT).show();
        return isOnline;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("TIMER_INT", timerInt);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        timerInt = savedInstanceState.getInt("TIMER_INT");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
