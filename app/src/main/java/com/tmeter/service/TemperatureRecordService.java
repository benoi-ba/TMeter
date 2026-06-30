package com.tmeter.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.tmeter.MainActivity;
import com.tmeter.db.AppDatabase;
import com.tmeter.db.TemperatureLog;
import com.tmeter.sensor.TemperatureProvider;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TemperatureRecordService extends Service {

    private static final String TAG = "TempRecordService";
    private static final String CHANNEL_ID = "temperature_logger_channel";
    private static final int NOTIFICATION_ID = 1001;

    private TemperatureProvider temperatureProvider;
    private AppDatabase database;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");
        
        temperatureProvider = new TemperatureProvider(this);
        temperatureProvider.startListening((temperature, source) -> {
            // Optional: Handle live changes if needed
        });
        
        database = AppDatabase.getDatabase(this);
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        createNotificationChannel();
        
        // Start foreground service with initial notification
        Notification notification = buildNotification(0.0f, "Initializing...", false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceChangeListener = (sharedPrefs, key) -> {
            if ("recording_frequency_ms".equals(key)) {
                Log.d(TAG, "Frequency changed, rescheduling logger");
                rescheduleLoggingTask();
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        
        rescheduleLoggingTask();
    }

    private void rescheduleLoggingTask() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        
        // Get interval from preferences, default is 60000ms (1 minute)
        long intervalMs = getRecordingInterval();
        Log.d(TAG, "Scheduling temperature logger every " + intervalMs + " ms");
        
        scheduledTask = scheduler.scheduleAtFixedRate(
                this::recordTemperature,
                0, // Start immediately
                intervalMs,
                TimeUnit.MILLISECONDS
        );
    }

    private long getRecordingInterval() {
        // We will store the value as string in PreferenceDialog for compatibility with standard ListPreference,
        // or as long. Let's support both or handle string conversion safely.
        String valStr = sharedPreferences.getString("recording_frequency_ms", "60000");
        try {
            return Long.parseLong(valStr);
        } catch (NumberFormatException e) {
            return 60000L;
        }
    }

    private void recordTemperature() {
        StringBuilder sourceBuilder = new StringBuilder();
        float temp = temperatureProvider.getCurrentReading(sourceBuilder);
        String source = sourceBuilder.toString();
        long now = System.currentTimeMillis();
        
        Log.d(TAG, String.format(Locale.US, "Logging reading: %.1f°C from %s", temp, source));
        
        // Insert to Database
        TemperatureLog logEntry = new TemperatureLog(now, temp, source);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            database.temperatureLogDao().insert(logEntry);
        });
        
        // Update notification status
        updateNotification(temp, source);
    }

    private void updateNotification(float currentTemp, String source) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            Notification notification = buildNotification(currentTemp, source, true);
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Notification buildNotification(float temp, String source, boolean hasReading) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String contentText = hasReading 
                ? String.format(Locale.getDefault(), "Current Temperature: %.1f°C (%s)", temp, source)
                : "Active background logging...";

        // Note: Using system drawable to avoid resource compiler errors on startup
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Thermometer Active Recording")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thermometer Logger Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Shows active background recording status");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Return START_STICKY so it gets restarted if system runs out of memory
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
        }
        scheduler.shutdown();
        
        if (temperatureProvider != null) {
            temperatureProvider.stopListening();
        }
        
        if (sharedPreferences != null && preferenceChangeListener != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
        }
        
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
