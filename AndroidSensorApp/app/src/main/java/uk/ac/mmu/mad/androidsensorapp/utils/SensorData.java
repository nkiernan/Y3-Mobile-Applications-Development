package uk.ac.mmu.mad.androidsensorapp.utils;

public class SensorData {
    private String userID;
    private String sensorName;
    private String sensorValue;
    private String timestamp;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(String sensorValue) {
        this.sensorValue = sensorValue;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Sensor [userID=" + userID + ", sensorName=" + sensorName + ", sensorValue=" + sensorValue + ", timestamp=" + timestamp + "]";
    }
}