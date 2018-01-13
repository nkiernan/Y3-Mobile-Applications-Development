import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import utils.SensorData;


@WebServlet("/sensorToDB")
public class sensorToDB extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private String lastValidSensorNameStr  = "no sensor";
	private String lastValidSensorValueStr = "invalid";
	private String returnMessage = "";

	Connection conn = null;
	Statement stmt;
	Gson gson = new Gson();

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String user = "kiernann";
		String password = "pilmjepS8";
		String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:3306/"+user;

		try { 
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			conn = DriverManager.getConnection(url, user, password);
			stmt = conn.createStatement();
		} catch (SQLException se) {
			System.out.println(se);
		}
	}

	public void destroy() {
		try {
			conn.close();
		} catch (SQLException se) {
			System.out.println(se);
		}
	}

	public void connectDB() {
		String user = "kiernann";
		String password = "pilmjepS8";
		String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:3306/"+user;

		try {  Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			conn = DriverManager.getConnection(url, user, password);
			stmt = conn.createStatement();
		} catch (SQLException se) {
			System.out.println(se);
		}
	}

	public sensorToDB() {
		super();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {

		response.setStatus(HttpServletResponse.SC_OK);
		String info = request.getParameter("getdata");

		if (info == null){
			String sensorJSONStr = request.getParameter("sensordata");
			SensorData sensorData = gson.fromJson(sensorJSONStr, SensorData.class);

			if (!(sensorJSONStr==null)) {
				returnMessage = updateSensorTable(sensorData);
			}
			else {
				returnMessage = "bad data";
			}

			PrintWriter out = response.getWriter();
			System.out.println("Return response for receiving data: "+ returnMessage);
			out.print(returnMessage);
			out.close();
		}

		else if (request.getParameter("sensordata").equals("allsensors")) {
			ArrayList<String> sensorList = getSensorList();

			PrintWriter out = response.getWriter();
			out.print(gson.toJson(sensorList));
			out.close();
		}

		else {
			String sensorJSONStr = request.getParameter("sensordata");
			String noData = "No sensor data";
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();

			if (!(sensorJSONStr==null)) {
				SensorData sensorData = gson.fromJson(sensorJSONStr, SensorData.class);
				SensorData result = getSensorData(sensorData);
				System.out.println("Sensor found: "+result.toString());
				out.print(gson.toJson(result));
				out.close();
			}
			else {	
				System.out.println(noData);
				out.print(noData);
				out.close();
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private String updateSensorTable(SensorData sensorData){
		try {
			String updateSQL = "INSERT INTO sensorUsage(UserID, SensorName, SensorValue, TimeInserted) "
					+ "VALUES('"+sensorData.getUserID()+"','"+sensorData.getSensorName()+"','"+sensorData.getSensorValue()+"', NOW());";
			System.out.println("Updated value : " + updateSQL);
			connectDB();
			stmt.executeUpdate(updateSQL);
		} catch (SQLException se) {
			System.out.println(se);
			return("Invalid");
		}
		lastValidSensorNameStr = sensorData.getSensorName();
		lastValidSensorValueStr = sensorData.getSensorValue();	
		return "OK";
	}	

	private SensorData getSensorData(SensorData sensorData){
		PreparedStatement pst;
		ResultSet rs;
		SensorData result = new SensorData();
		String noDataAvailable = "No data for sensor: "+sensorData.getSensorName();
		String selectSQL = "SELECT * FROM sensorUsage WHERE "+
				"UserID = '" + sensorData.getUserID() + "' AND " +
				"SensorName = '" + sensorData.getSensorName() + 
				"' ORDER BY TimeInserted DESC LIMIT 1;";

		System.out.println("DEBUG: Sensor retrieval SQL is : "+ selectSQL);

		try { 
			pst = conn.prepareStatement(selectSQL);
			rs = pst.executeQuery();
			while (rs.next()) {
				result.setUserID(rs.getString(1)); 
				result.setSensorName(rs.getString(2));
				result.setSensorValue(rs.getString(3));
				result.setTimestamp(rs.getTimestamp(4).toString());
				if (!result.getUserID().equals("null") || !result.getSensorName().equals("null")) {
					System.out.println("Retrieved data: "+ result.toString());
				}
				else {
					System.out.println(noDataAvailable);
				}
			}
		} catch (SQLException ex) {
			System.out.println("Error in SQL " + ex.getMessage());
		}
		return result;
	}

	private ArrayList<String> getSensorList() {
		PreparedStatement pst;
		ResultSet rs;
		String allSensors = "SELECT DISTINCT SensorName FROM sensorUsage;";
		ArrayList<String> sensors = new ArrayList<String>();

		try {
			pst = conn.prepareStatement(allSensors);
			rs = pst.executeQuery();
			while(rs.next()) {
				sensors.add(rs.getString("SensorName"));
			}
			Collections.sort(sensors);
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("List of stored sensors: " + sensors.toString());
		return sensors;
	}
}