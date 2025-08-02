<p align="center">
  # 🎙️ Android Audio Input Manager

A simple yet powerful Android utility to help you manage your device's audio input source. Perfect for gamers, streamers, and anyone who wants more control over whether their phone uses the built-in mic or a connected Bluetooth headset.

</p>



---

## 🚀 Key Features

*   **🎧 Device Discovery:** Automatically scans and lists all available audio inputs, including the internal microphone, wired headsets, and Bluetooth devices.
*   **✅ Set Preference:** Choose an input device from the list. The app will then run a background service to strongly suggest to the Android system that your chosen device should be used.
*   **🔄 Revert to Default:** A one-tap button to stop the service and instantly return all audio control to the standard Android OS behavior.
*   **✨ Modern UI:** Built with a clean splash screen and modern Material Design components for a great user experience.

---

## 🔧 How It Works (An Easy Explanation)

This app uses official Android APIs to influence, but not force, the microphone selection.

#### What it Does
When you select a Bluetooth device and tap "Set," the app starts a background service. This service constantly tells the Android `AudioManager`: *"Hey, a Bluetooth audio connection is active and important!"*. Most apps (like games or voice recorders) will listen to this system-level hint and automatically use your Bluetooth mic.

> **Important Limitation:** This app cannot *force* another app to use a specific microphone. Android's security model prevents one app from hijacking another's audio stream. Our method works for the vast majority of apps, but some may be hard-coded to ignore system suggestions.

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed and set up:

*   **Android Studio**: Version `Electric Eel` or newer.
*   **JDK 11**: The project is configured to use Java Development Kit version 11 for Gradle.
*   **Android Device**: A physical device running Android 10 (API 29) or higher.
*   **Bluetooth Headset**: A headset with a microphone is required to test the core functionality.

---

## ⚙️ Setup and Installation

You can build and run this project from the source code.

1.  **Clone the Repository**
    Open your terminal or command prompt and run:
    ```
    git clone https://github.com/pvmeht/android-audio-input-manager.git
    ```

2.  **Open in Android Studio**
    *   Launch Android Studio.
    *   Go to `File > Open` and select the cloned project folder.
    *   Allow Gradle to sync and download all the necessary project dependencies.

3.  **Build and Run**
    *   Connect your Android device that meets the prerequisites.
    *   Click the **Run 'app'** button (the green play icon ▶️).

4.  **Grant Permissions**
    *   On first launch, the app will ask for **Audio** and **Nearby Devices (Bluetooth)** permissions. You must grant these for the app to function correctly.

---

## 📸 Screenshot

<p align="center">
  <img src="https://github.com/user-attachments/assets/18e7de7b-c1f0-4c2f-a74d-2363ca456098" width="250"/>
</p>


