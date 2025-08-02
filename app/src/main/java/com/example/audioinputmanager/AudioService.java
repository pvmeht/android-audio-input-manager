package com.example.audioinputmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AudioService extends Service {

    private static final String TAG = "AudioService";
    private static final String CHANNEL_ID = "AudioServiceChannel";
    private AudioManager audioManager;

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        createNotificationChannel();
        Log.d(TAG, "Service Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get the device info from the intent
        int deviceType = intent.getIntExtra("DEVICE_TYPE", -1);
        String deviceName = intent.getStringExtra("DEVICE_NAME");
        if (deviceName == null) deviceName = "Unknown Device";

        String notificationText = "Managing audio preference for: " + deviceName;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audio Manager Active")
                .setContentText(notificationText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);

        if (audioManager == null) {
            Log.e(TAG, "AudioManager not available.");
            return START_NOT_STICKY; // Don't restart if something is wrong
        }

        // The core logic: Act based on the selected device type
        switch (deviceType) {
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                Log.d(TAG, "Preference set to Bluetooth. Activating SCO.");
                audioManager.startBluetoothSco();
                audioManager.setBluetoothScoOn(true);
                break;

            case AudioDeviceInfo.TYPE_BUILTIN_MIC:
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
            case AudioDeviceInfo.TYPE_USB_DEVICE:
            case AudioDeviceInfo.TYPE_USB_HEADSET:
            default:
                // For any non-Bluetooth device, we ensure Bluetooth SCO is OFF,
                // allowing the system to fall back to its default (wired > internal).
                Log.d(TAG, "Preference set to non-Bluetooth. Deactivating SCO.");
                if (audioManager.isBluetoothScoOn()) {
                    audioManager.setBluetoothScoOn(false);
                    audioManager.stopBluetoothSco();
                }
                break;
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed. Reverting all audio preferences.");
        if (audioManager != null && audioManager.isBluetoothScoOn()) {
            audioManager.setBluetoothScoOn(false);
            audioManager.stopBluetoothSco();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Audio Service Channel",
                    NotificationManager.IMPORTANCE_LOW // Low importance to be less intrusive
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
