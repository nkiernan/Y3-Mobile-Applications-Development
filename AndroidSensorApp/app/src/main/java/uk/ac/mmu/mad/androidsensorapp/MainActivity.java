package uk.ac.mmu.mad.androidsensorapp;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import uk.ac.mmu.mad.androidsensorapp.utils.SensorData;

public class MainActivity extends AppCompatActivity {
    public static String sensorServerURL = "http://10.0.2.2:8080/PhidgetServer/sensorToDB";
    public Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Spinner sensorList = findViewById(R.id.sensorList);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getSensorList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sensorList.setAdapter(adapter);

        SeekBar valueSeekBar = findViewById(R.id.valueSeekbar);
        final TextView currentSeekBarValue = findViewById(R.id.sliderValueLabel);

        valueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentSeekBarValue.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public SensorData getSensorData(SensorData queryData) throws UnsupportedEncodingException {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String sensorJSONStr = gson.toJson(queryData);
        String fullURL = sensorServerURL + "?sensordata="+ URLEncoder.encode(sensorJSONStr, "UTF-8")+"&getdata=true";
        Log.i("GET_DATA", "Sending data to: "+fullURL);
        String line;
        SensorData result = new SensorData();

        try {
            url = new URL(fullURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result = gson.fromJson(line, SensorData.class);
            }
            rd.close();

        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
        return result;
    }

    public String sendSensorData(SensorData sensorData) throws UnsupportedEncodingException {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String sensorJSONStr = gson.toJson(sensorData);
        String fullURL = sensorServerURL + "?sensordata="+URLEncoder.encode(sensorJSONStr, "UTF-8");
        Log.i("SEND_DATA","Sending data to: "+fullURL);
        String line;
        StringBuilder result = new StringBuilder();

        try {
            url = new URL(fullURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public void getData_onClick(View v) throws UnsupportedEncodingException {
        EditText currentUser = findViewById(R.id.userTextBox);
        Spinner sensorList = findViewById(R.id.sensorList);

        TextView userID = findViewById(R.id.userIdValue);
        TextView sensorName = findViewById(R.id.nameValue);
        TextView sensorValue = findViewById(R.id.valueValue);
        TextView timeStamp = findViewById(R.id.timestampValue);

        SensorData sensorData = new SensorData();
        sensorData.setUserID(currentUser.getText().toString());
        sensorData.setSensorName(sensorList.getSelectedItem().toString());
        SensorData result = getSensorData(sensorData);

        userID.setText(result.getUserID());
        sensorName.setText(result.getSensorName());
        sensorValue.setText(result.getSensorValue());
        timeStamp.setText(result.getTimestamp());

        Log.i("FOUND_DATA", "Sensor found: " + result.toString());
    }

    public void sendData_onClick(View v) {
        EditText currentUser = findViewById(R.id.userTextBox);
        Spinner sensorList = findViewById(R.id.sensorList);
        SeekBar valueSeekBar = findViewById(R.id.valueSeekbar);

        SensorData sensorData = new SensorData();
        sensorData.setUserID(currentUser.getText().toString());
        sensorData.setSensorName(sensorList.getSelectedItem().toString());
        sensorData.setSensorValue(String.valueOf(valueSeekBar.getProgress()));

        Log.i("SEND_DATA", "Sending new sensor: " + sensorData.toString());

        try {
            sendSensorData(sensorData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public ArrayList getSensorList() {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String fullURL = sensorServerURL + "?getdata=true&sensordata=allsensors";
        String line;
        ArrayList sensorList = new ArrayList<>();

        try {
            url = new URL(fullURL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null)
                sensorList = gson.fromJson(line, ArrayList.class);
            rd.close();
            Log.i("SENSOR_LIST", "Sensors: " + sensorList.toString() + " obtained");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sensorList;
    }
}