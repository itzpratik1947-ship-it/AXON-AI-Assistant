package com.pratik.axon.core.model

enum class AxonIntent {

    NONE,

    // 👋 Basic
    GREETING,
    TIME,
    DATE,
    IDENTITY,

    // 🔦 Device controls
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,

    VOLUME_INCREASE,
    VOLUME_DECREASE,
    VOLUME_MUTE,
    VOLUME_UNMUTE,

    BRIGHTNESS_INCREASE,
    BRIGHTNESS_DECREASE,

    // 📱 Device information
    BATTERY_STATUS,
    CHARGING_STATUS,
    DEVICE_MODEL,
    WIFI_STATUS,

    // 📱 Android actions
    OPEN_WIFI_SETTINGS,
    OPEN_SOUND_SETTINGS,
    OPEN_DISPLAY_SETTINGS,
    OPEN_ANDROID_SETTINGS,

    // 📞 Communication
    OPEN_PHONE,
    OPEN_CONTACTS,
    OPEN_MESSAGES,
    OPEN_EMAIL,
    OPEN_BROWSER,

    // 🌐 Web
    OPEN_WEBSITE,

    // 🚀 Apps
    OPEN_APP,

    // 🧮 Calculation
    CALCULATE
}