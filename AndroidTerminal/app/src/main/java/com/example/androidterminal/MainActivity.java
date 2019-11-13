package com.example.androidterminal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.example.androidterminal.edge_server.ESPublisher;
import com.example.androidterminal.edge_server.EdgeServer;

import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView runtimeText;

    ESPublisher esp = null;
    Handler handler = new Handler();
    List<String[]> dataList;
    int timerInt = 0;
    int runtime = -1;
    int maxRuntime = -1;
    final int delay = 1000;     // milliseconds
    final int onlineCheckFrequency = 5;     // seconds

    private final int terminalId = 1;
    private final String terminalName = (terminalId == 0) ? "v26Terminal" : "v27Terminal";

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
        if (runtime < 0 || runtime > maxRuntime) {
            runtime = maxRuntime;
        }
        runtimeText.setText(Integer.toString(runtime));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void startTransmission(View view) {
        runtimeText.setText(Integer.toString(runtime));
        try {
            Log.i(terminalName, "Trying to connect ...");
            esp = new ESPublisher(terminalName, EdgeServer.getVehicleTopic(0));
            Log.i(terminalName, "Connected successfully!");
        } catch (MqttException e) {
            e.printStackTrace();
        }
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
        handler.removeCallbacksAndMessages(null);
        runtimeText.setText("Transmission was stopped!");
        esp.publishMessage("Transmission from '" + terminalName + "' was stopped");
        timerInt = 0;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
