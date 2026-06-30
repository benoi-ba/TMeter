# TMeter: Android Thermometer & Logger App

A modern, native Android application built in **Java** that displays the ambient temperature from the device's hardware sensors, with an automatic fallback to the battery temperature sensor if ambient sensors are not present.

It logs these readings at a customizable frequency using a foreground service and saves them to a local SQLite database using Google's **Room ORM**. Historical trends are plotted on an interactive chart using **MPAndroidChart**.

---

## Features
- **Real-Time Display**: Gauge showing current temperature, with a dynamic indicator showing whether the source is the ambient `Sensor` or `Battery Fallback`.
- **Background Logging Service**: Robust, persistent Foreground Service that schedules readings, writes them to the DB, and keeps a persistent system notification updated.
- **Customizable Frequencies**: Quick-settings picker to adjust logging frequency dynamically (from 10 seconds up to 1 hour) without restarting the service.
- **Interactive Chart**: Custom-styled line chart showing historical temperature logs, designed with relative Unix timestamps to maintain high floating-point rendering precision.
- **Room SQLite Database**: Reliable local storage for historical logs.
- **Permissions-Safe**: Seamless runtime requests for notification permissions on Android 13+ (API 33/34).

---

## Directory Structure
```text
TMeter/
├── .gitignore
├── settings.gradle
├── build.gradle
├── gradle.properties
└── app/
    ├── build.gradle
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── java/com/tmeter/
            │   ├── MainActivity.java
            │   ├── db/
            │   │   ├── AppDatabase.java
            │   │   ├── TemperatureLog.java
            │   │   └── TemperatureLogDao.java
            │   ├── sensor/
            │   │   └── TemperatureProvider.java
            │   └── service/
            │       └── TemperatureRecordService.java
            └── res/
                ├── layout/
                │   └── activity_main.xml
                ├── values/
                │   ├── colors.xml
                │   ├── strings.xml
                │   └── themes.xml
                └── xml/
                    ├── backup_rules.xml
                    └── data_extraction_rules.xml
```

---

## Getting Started

### Prerequisites
- **Android Studio** (Koala or newer recommended)
- **Android SDK** (Target API Level 34, Min SDK Level 26)
- **JDK 17 or JDK 21** configured in Android Studio

### Importing & Running the App
1. Open **Android Studio**.
2. Select **File > Open** and select the `TMeter` project directory.
3. Android Studio will automatically index the files, download the Gradle wrapper, and resolve dependencies (Room, Preference, and MPAndroidChart).
4. Connect an Android device or start an emulator.
5. Click the **Run** button (green play icon) to install and launch the application.

---

## Design and Styling
The app utilizes a modern slate dark-mode palette (`#0F172A`) with high-contrast indicator highlights (emerald green for sensor readings, amber orange for battery status fallbacks) to provide a premium, clean aesthetic.
