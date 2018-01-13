import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import utils.SensorData;

import com.google.gson.Gson;

import com.phidget22.*;

public class SensorToServer {
	final static int IKIT_SERIAL_NUMBER = 457026;
	final static int RFID_SERIAL_NUMBER = 455738;
	static Gson gson = new Gson();
	static String sensorServerURL = "http://localhost:8080/PhidgetServer/sensorToDB?";

	public static void main(String[] args) throws PhidgetException {
		VoltageRatioInput slider = new VoltageRatioInput();
		VoltageRatioInput rotation = new VoltageRatioInput();
		RFID rfid = new RFID();

		while (true) {
			readSensorValues(slider, "slider", 0);
			readSensorValues(rotation, "rotation", 1);
			readRfidValues(rfid);
			pause(500);
		}
	}

	public static String sendToServer(SensorData sensorData) throws UnsupportedEncodingException{
		URL url;
		HttpURLConnection conn;
		BufferedReader rd;
		String sensorJSONStr = gson.toJson(sensorData);
		String fullURL = sensorServerURL + "sensordata="+URLEncoder.encode(sensorJSONStr, "UTF-8");
		System.out.println("Sending data to: "+fullURL);
		String line;
		String result = "";

		try {
			url = new URL(fullURL);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static void pause(int secs){
		try {
			Thread.sleep(secs*1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	private static void readSensorValues(VoltageRatioInput sensorInput, final String sensorName, int channel) throws PhidgetException {
		SensorData sensorData = new SensorData();
		sensorData.setUserID("15088410");
		sensorData.setSensorName(sensorName);
		sensorData.setSensorValue("0");
		sensorInput.setDeviceSerialNumber(IKIT_SERIAL_NUMBER);
		sensorInput.setChannel(channel);
		sensorInput.open();

		System.out.println(sensorData.getSensorName() + " activated and gathering data");

		sensorInput.addVoltageRatioChangeListener(new VoltageRatioInputVoltageRatioChangeListener() {
			public void onVoltageRatioChange(VoltageRatioInputVoltageRatioChangeEvent e) {
				double sensorReading = e.getVoltageRatio();
				int scaledSensorReading = (int) (1000 * sensorReading);
				if (scaledSensorReading != Integer.parseInt(sensorData.getSensorValue())) {
					System.out.println("Sending new " + sensorData.getSensorName() + " value: " + scaledSensorReading);
					sensorData.setSensorValue(Integer.toString(scaledSensorReading));
					try {
						sendToServer(sensorData);
					} catch (UnsupportedEncodingException encEx) {
						encEx.printStackTrace();
					}
				}
			}
		});
	}

	private static void readRfidValues(RFID rfidInput) throws PhidgetException {
		rfidInput.setDeviceSerialNumber(RFID_SERIAL_NUMBER);
		rfidInput.open();
		rfidInput.setAntennaEnabled(true);

		System.out.println("Reading RFID tags");

		rfidInput.addTagListener(new RFIDTagListener() {
			public void onTag(RFIDTagEvent in) {
				String rfidTag = in.getTag();
				System.out.println("Tag read: " + rfidTag);
				SensorData rfidData = new SensorData();
				rfidData.setUserID("15088410");
				rfidData.setSensorName("rfid");
				rfidData.setSensorValue(rfidTag);
				try {
					sendToServer(rfidData);
					System.out.println("RFID tag registered: " + rfidData.getSensorValue());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}				
			}
		});				
	}
}