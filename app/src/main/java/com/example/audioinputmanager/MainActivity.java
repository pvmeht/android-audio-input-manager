package com.example.audioinputmanager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button; // Can keep this or import MaterialButton
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button setPreferenceButton, revertButton, refreshButton;
    private Spinner deviceSpinner;

    private AudioManager audioManager;
    private List<AudioDeviceInfo> audioDevices = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    // Modern way to handle permission requests
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean recordAudioGranted = result.getOrDefault(Manifest.permission.RECORD_AUDIO, false);
                Boolean bluetoothConnectGranted = true; // Assume true for older APIs

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    bluetoothConnectGranted = result.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false);
                }

                if (recordAudioGranted && bluetoothConnectGranted) {
                    Toast.makeText(this, "Permissions granted. Ready.", Toast.LENGTH_SHORT).show();
                    updateDeviceList();
                } else {
                    Toast.makeText(this, "Permissions are required to list devices and run the service.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install the splash screen. MUST be before super.onCreate() and setContentView()
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Find views by their new IDs
        setPreferenceButton = findViewById(R.id.set_preference_button);
        revertButton = findViewById(R.id.revert_button);
        refreshButton = findViewById(R.id.refresh_button);
        deviceSpinner = findViewById(R.id.device_spinner);

        // Setup the adapter for our spinner
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(spinnerAdapter);

        // Check for permissions on startup
        if (checkPermissions()) {
            updateDeviceList();
        } else {
            requestPermissions();
        }

        refreshButton.setOnClickListener(v -> updateDeviceList());

        setPreferenceButton.setOnClickListener(v -> {
            int selectedPosition = deviceSpinner.getSelectedItemPosition();
            if (selectedPosition < 0 || audioDevices.isEmpty()) {
                Toast.makeText(this, "No device selected or available.", Toast.LENGTH_SHORT).show();
                return;
            }
            AudioDeviceInfo selectedDevice = audioDevices.get(selectedPosition);
            startAudioService(selectedDevice);
        });

        // The "Revert to Default" button simply stops our service.
        // The service's onDestroy() method handles the cleanup.
        revertButton.setOnClickListener(v -> stopAudioService());
    }

    private void updateDeviceList() {
        if (!checkPermissions()) {
            Toast.makeText(this, "Permissions needed to scan for devices.", Toast.LENGTH_SHORT).show();
            return;
        }

        audioDevices.clear();
        List<String> deviceNames = new ArrayList<>();

        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
        if (devices.length == 0) {
            deviceNames.add("No input devices found");
        } else {
            for (AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_BUILTIN_MIC ||
                        device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                        device.getType() == AudioDeviceInfo.TYPE_USB_DEVICE ||
                        device.getType() == AudioDeviceInfo.TYPE_USB_HEADSET) {
                    audioDevices.add(device);
                    String deviceName = device.getProductName().toString() + " (" + getDeviceTypeName(device.getType()) + ")";
                    deviceNames.add(deviceName);
                }
            }
        }
        spinnerAdapter.clear();
        spinnerAdapter.addAll(deviceNames);
        spinnerAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Device list updated.", Toast.LENGTH_SHORT).show();
    }

    private String getDeviceTypeName(int type) {
        switch (type) {
            case AudioDeviceInfo.TYPE_BUILTIN_MIC:
                return "Internal Mic";
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                return "Bluetooth";
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                return "Wired Headset";
            case AudioDeviceInfo.TYPE_USB_DEVICE:
            case AudioDeviceInfo.TYPE_USB_HEADSET:
                return "USB Audio";
            default:
                return "Other";
        }
    }

    private void startAudioService(AudioDeviceInfo device) {
        Intent serviceIntent = new Intent(this, AudioService.class);
        serviceIntent.putExtra("DEVICE_TYPE", device.getType());
        serviceIntent.putExtra("DEVICE_NAME", device.getProductName().toString());

        ContextCompat.startForegroundService(this, serviceIntent);
        Toast.makeText(this, "Preference set to: " + getDeviceTypeName(device.getType()), Toast.LENGTH_SHORT).show();
    }

    private void stopAudioService() {
        Intent serviceIntent = new Intent(this, AudioService.class);
        stopService(serviceIntent);
        Toast.makeText(this, "Audio preference reverted to default", Toast.LENGTH_SHORT).show();
    }

    private boolean checkPermissions() {
        boolean hasRecordAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean hasBluetoothConnect = true; // No permission needed before Android 12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasBluetoothConnect = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        }
        return hasRecordAudio && hasBluetoothConnect;
    }

    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
    }
}
