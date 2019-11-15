package com.example.androidterminal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private TextView runtimeText;
    private ListView listView;
    private ItemArrayAdapter itemArrayAdapter;
    private ProgressBar dataProgressBar;
    private ProgressBar connectionProgressBar;

    private TerminalPublisher esp = null;
    private Handler handler = new Handler();
    private final String[] dataHeaders =
            {"Timestep", "Vehicle ID", "Latitude", "Longitude", "Angle", "Speed", "RSSI", "Throughput"};
    private List<String[]> dataList;
    private int runtime;
    private int maxRuntime = -1;
    private int timerInt = 0;
    private final int delay = 1000;     // milliseconds
    private final int onlineCheckFrequency = 5;     // seconds

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
        dataProgressBar = findViewById(R.id.dataProgressBar);
        connectionProgressBar = findViewById(R.id.connectionProgressBar);
        listView = findViewById(R.id.listView);

        connectionProgressBar.setVisibility(View.INVISIBLE);
//        connectionProgressBar.setVisibility(View.VISIBLE);

        InputStream inputStream = (terminalId == 0) ? getResources().openRawResource(R.raw.vehicle_26) :
                getResources().openRawResource(R.raw.vehicle_27);
        CSVFileReader csvFile = new CSVFileReader(inputStream);
        dataList = csvFile.read();
        maxRuntime = dataList.size() - 1;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.getString("runtime", null) == null) {
            editor.putString("runtime", Integer.toString(maxRuntime));
        }
        editor.putString("maxRuntime", Integer.toString(maxRuntime));
//        editor.clear();
        editor.apply();

        itemArrayAdapter = new ItemArrayAdapter(getApplicationContext(), R.layout.item_layout);
        Parcelable state = listView.onSaveInstanceState();
        listView.setAdapter(itemArrayAdapter);
        listView.onRestoreInstanceState(state);
        resetItemArrayList();
    }

    public void resetItemArrayList() {
        itemArrayAdapter.clear();
        for (String header : dataHeaders) {
            String[] row = {header, "-"};
            itemArrayAdapter.add(row);
        }
    }

    public void startTransmission(View view) {
        if (timerInt > 0) {
            Toast.makeText(this, "Already transmitting", Toast.LENGTH_SHORT).show();
            return;
        }
        final Context con = this;
        if (! isDeviceOnline(con)) {
            Toast.makeText(this, "No internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (prefs.getString("ipAddr", null) == null) {
//            Toast.makeText(this, "Check app settings:\nIP Address not set", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (prefs.getString("port", null) == null) {
//            Toast.makeText(this, "Check app settings:\nPort not set", Toast.LENGTH_SHORT).show();
//            return;
//        }

//        connectionProgressBar.getHandler().post(new Runnable() {
//            public void run() {
//                connectionProgressBar.setVisibility(View.VISIBLE);
//            }
//        });
//        connectionProgressBar.setVisibility(View.VISIBLE);



//        Log.i(terminalName, "Trying to connect ...");
//        AsyncConnect asyncConnect = new AsyncConnect(this, connectionProgressBar, terminalName, prefs.getString("ipAddr", null), Integer.parseInt(prefs.getString("port", 1883+"")), terminalTopic);
//        asyncConnect.execute();
//        try {
//            esp = asyncConnect.get();
//        } catch (ExecutionException | InterruptedException e) {
//            e.printStackTrace();
//            return;
//        }
//        connectionProgressBar.setVisibility(View.INVISIBLE);
//        Log.i(terminalName, "Connected successfully!");


        try {
            Log.i(terminalName, "Trying to connect ...");
            esp = new TerminalPublisher(terminalName, prefs.getString("ipAddr", null), Integer.parseInt(prefs.getString("port", 1883+"")), terminalTopic);
            Log.i(terminalName, "Connected successfully!");
        } catch (MqttException e) {
            e.printStackTrace();
        }


        if (esp == null) {
            Toast.makeText(this, "Could not connect to server\nMaybe it's offline?", Toast.LENGTH_SHORT).show();
            return;
        }
        esp.publishMessage("'" + terminalName + "' connected successfully; starting transmission of " + runtime + " datapoints");

        runtime = Integer.parseInt(prefs.getString("runtime", maxRuntime+""));
        runtimeText.setText("Transmitting datapoints...    |    " + timerInt + "/" + runtime);
        dataProgressBar.setMax(runtime);
        dataProgressBar.setProgress(0);
        resetItemArrayList();

        handler.postDelayed(new Runnable(){
            public void run(){
                if (timerInt % onlineCheckFrequency == 0) isDeviceOnline(con);
                esp.publishMessage(TextUtils.join(",", dataList.get(timerInt)));
                itemArrayAdapter.clear();
                for (int i = 0; i < dataHeaders.length; i++ ) {
                    String[] row = {dataHeaders[i], dataList.get(timerInt)[i]};
                    itemArrayAdapter.add(row);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    dataProgressBar.setProgress(timerInt + 1, true);
                } else{
                    dataProgressBar.setProgress(timerInt + 1);
                }
                if (timerInt < runtime - 1) {
                    timerInt++;
                    runtimeText.setText("Transmitting datapoints...    |    " + timerInt + "/" + runtime);
                    handler.postDelayed(this, delay);
                } else {
                    runtimeText.setText("Transmission complete!\nAll " + runtime + " datapoints were sent successfully!");
                    esp.publishMessage("Transmission of " + runtime + " datapoints from '" + terminalName + "' completed successfully!");
                    timerInt = 0;
                }
            }
        }, delay);
    }

    public void stopTransmission(View view) {
        if (timerInt > 0) {
            handler.removeCallbacksAndMessages(null);
            runtimeText.setText("Transmission was stopped    |    " + timerInt + "/" + runtime);
            timerInt = 0;
            esp.publishMessage("Transmission from '" + terminalName + "' was stopped");
        } else {
            Toast.makeText(this, "No active transmission to be stopped", Toast.LENGTH_SHORT).show();
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
