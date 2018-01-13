import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.Gson;
import com.phidget22.*;

import utils.SensorData;

public class MotorServoGetFromServer {

	final static int SERVO_SERIAL_NUMBER = 305459;
	public static String sensorServerURL = "http://localhost:8080/PhidgetServer/sensorToDB";
	static Gson gson = new Gson();

	public static void main(String[] args) throws Exception {
		RCServo servo = new RCServo();
		servo.setDeviceSerialNumber(SERVO_SERIAL_NUMBER);
		servo.open(5000);
		servo.setTargetPosition(0);
		servo.setEngaged(true);
		SensorData search = new SensorData();
		search.setUserID("15088410");
		search.setSensorName("slider");
		SensorData result;
		int lastValue = 0;

		while (true) {
			result = getSensorFromServer(search);
			if (Integer.parseInt(result.getSensorValue()) != lastValue) {
				Double position = Double.parseDouble(result.getSensorValue()) / 5.55;			
				servo.setTargetPosition(position);
				System.out.println("Scaling " + result.getSensorName() + " value of " + result.getSensorValue() + " down to servo position: " + position);
				lastValue = Integer.parseInt(result.getSensorValue());
			} else {
				Thread.sleep(1000);
			}
		}
	}

	public static SensorData getSensorFromServer(SensorData queryData) throws UnsupportedEncodingException {
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String sensorJSONStr = gson.toJson(queryData);
		String fullURL = sensorServerURL + "?sensordata="+URLEncoder.encode(sensorJSONStr, "UTF-8")+"&getdata=true";
		System.out.println("Sending data to: "+fullURL);
		String line;
		SensorData result = new SensorData();

		try {
			url = new URL(fullURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result = gson.fromJson(line, SensorData.class);
				System.out.println("Found sensor: " + result.toString());
			}
			rd.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}