package com.tmeter.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TemperatureLogDao {
    
    @Insert
    void insert(TemperatureLog log);

    @Query("SELECT * FROM temperature_logs ORDER BY timestamp ASC")
    LiveData<List<TemperatureLog>> getAllLogsLive();

    @Query("SELECT * FROM temperature_logs ORDER BY timestamp DESC LIMIT 500")
    List<TemperatureLog> getRecentLogsDirect();

    @Query("DELETE FROM temperature_logs")
    void clearAllLogs();

    @Query("DELETE FROM temperature_logs WHERE timestamp < :cutoff")
    void deleteOldLogs(long cutoff);
}
