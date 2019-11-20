package com.example.androidterminal.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.os.Message;
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

import com.example.androidterminal.utils.AsyncConnect;
import com.example.androidterminal.utils.CSVFileReader;
import com.example.androidterminal.utils.ItemArrayAdapter;
import com.example.androidterminal.R;
import com.example.androidterminal.network.TerminalPublisher;
import com.example.androidterminal.utils.Utilities;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private final int terminalId = 1;   // !! Change this to 0/1 for vehicle26/vehicle27 !!

    private SharedPreferences prefs;
    private TextView runtimeText;
    private ListView listView;
    private ItemArrayAdapter itemArrayAdapter;
    private ProgressBar dataProgressBar;
    private ProgressBar connectionProgressBar;
    private Button startButton;
    private Button stopButton;

    private AsyncConnect asyncConnect;
    private TerminalPublisher esp = null;
    private Runnable connectedRunnable;
    private Handler outterHandler;
    private Handler innerHandler = new Handler();

    private final String[] dataHeaders =
            {"Timestep", "Vehicle ID", "Latitude", "Longitude", "Angle", "Speed", "RSSI", "Throughput"};
    private List<String[]> dataList;
    private int runtime;
    private boolean runningFlag = false;
    private int maxRuntime = -1;
    private int timerInt = 0;
    private final int delay = 1000;     // milliseconds
    private final int onlineCheckFrequency = 5;     // seconds

    private final String terminalName = (terminalId == 0) ? "v26Terminal" : "v27Terminal";
    private final String terminal2esTopic = (terminalId == 0) ? "v26_ES/topic" : "v27_ES/topic";
    private final String es2terminalTopic = (terminalId == 0) ? "ES_v26/topic" : "ES_v27/topic";        // unused for now

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
        stopButton = findViewById(R.id.stopButton);
        startButton = findViewById(R.id.startButton);

        Utilities.toggleButtonActive(stopButton);
        connectionProgressBar.setVisibility(View.INVISIBLE);

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
//        editor.clear();           // uncomment to reset preferences
        editor.apply();

        itemArrayAdapter = new ItemArrayAdapter(getApplicationContext(), R.layout.item_layout);
        Parcelable state = listView.onSaveInstanceState();
        listView.setAdapter(itemArrayAdapter);
        listView.onRestoreInstanceState(state);
        Utilities.resetItemArrayList(itemArrayAdapter, dataHeaders);
    }

    public void startTransmission(View view) {
        if (timerInt > 0) {
            Toast.makeText(this, "Already transmitting", Toast.LENGTH_SHORT).show();
            return;
        }
        final Context con = this;
        if (! Utilities.isDeviceOnline(con)) {
            Toast.makeText(this, "No internet Connection", Toast.LENGTH_SHORT).show();
            return;
        }
        Utilities.toggleButtonActive(startButton);
        runningFlag = true;

        connectedRunnable = new Runnable() {
            @Override
            public void run() {
                if (esp == null) {
                    Log.i(terminalName, "Failed to connect");
                    Toast.makeText(getBaseContext(), "Could not connect to server\nMaybe it's offline?", Toast.LENGTH_LONG).show();
                    Utilities.toggleButtonActive(startButton);
                    runningFlag = false;
                    return;
                }
                Log.i(terminalName, "Connected successfully!");
                Toast.makeText(getBaseContext(), "Connected to server!\nBeginning transmission ...", Toast.LENGTH_LONG).show();
                esp.publishMessage("'" + terminalName + "' connected successfully; starting transmission of " + runtime + " datapoints");
                Utilities.toggleButtonActive(stopButton);

                runtime = Integer.parseInt(prefs.getString("runtime", maxRuntime+""));
                runtimeText.setText("Transmitting datapoints...    |    " + timerInt + "/" + runtime);
                dataProgressBar.setMax(runtime);
                dataProgressBar.setProgress(0);
                Utilities.resetItemArrayList(itemArrayAdapter, dataHeaders);

                innerHandler.postDelayed(new Runnable(){        // every second send a datapoint
                    public void run(){
                        if (timerInt % onlineCheckFrequency == 0) Utilities.isDeviceOnline(con);
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
                            innerHandler.postDelayed(this, delay);      // reinstate the handler for the next datapoint
                        } else {
                            runtimeText.setText("Transmission complete!\nAll " + runtime + " datapoints were sent successfully!");
                            esp.publishMessage("Transmission of " + runtime + " datapoints from '" + terminalName + "' completed successfully!");
                            Utilities.toggleButtonActive(stopButton);
                            Utilities.toggleButtonActive(startButton);
                            runningFlag = false;
                            timerInt = 0;
                        }
                    }
                }, delay);
            }
        };

        Handler outterHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {    // used to be notified of AsyncConnect's completion
                switch (msg.what) {
                    case 0:
                        removeCallbacksAndMessages(null);
                        connectedRunnable.run();
                        break;
                    default:
                        break;
                }
            }
        };

        Log.i(terminalName, "Trying to connect ...");
        asyncConnect = new AsyncConnect(this, connectionProgressBar, outterHandler, terminalName, prefs.getString("ipAddr", null), Integer.parseInt(prefs.getString("port", 1883+"")), terminal2esTopic);
        asyncConnect.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new Thread(new Runnable() {
            public void run() {
                try {
                    esp = asyncConnect.get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        outterHandler.postDelayed(connectedRunnable, 10000);    // will be resumed early if connected
    }

    public void stopTransmission(View view) {
        if (timerInt > 0) {
            innerHandler.removeCallbacksAndMessages(null);
            runtimeText.setText("Transmission was stopped    |    " + timerInt + "/" + runtime);
            timerInt = 0;
            esp.publishMessage("Transmission from '" + terminalName + "' was stopped");
            Utilities.toggleButtonActive(stopButton);
            Utilities.toggleButtonActive(startButton);
            runningFlag = false;
        } else {
            Toast.makeText(this, "No active transmission to be stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("TIMER_INT", timerInt);
        outState.putBoolean("RUNNING_BOOL", runningFlag);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        timerInt = savedInstanceState.getInt("TIMER_INT");
        runningFlag = savedInstanceState.getBoolean("RUNNING_BOOL", runningFlag);
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
            if (runningFlag) {      // prevent access to settings if transmitting
                Toast.makeText(this, "Settings cannot be changed while transmitting!", Toast.LENGTH_SHORT).show();
                return false;
            }
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
