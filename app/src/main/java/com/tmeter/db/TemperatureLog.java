package com.tmeter.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "temperature_logs")
public class TemperatureLog {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private long timestamp;
    private float temperature;
    private String source; // "SENSOR" or "BATTERY"

    public TemperatureLog(long timestamp, float temperature, String source) {
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.source = source;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
