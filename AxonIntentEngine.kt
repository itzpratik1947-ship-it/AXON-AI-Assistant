package com.pratik.axon.core.engine

import com.pratik.axon.core.model.AxonIntent
import java.util.Locale

class AxonIntentEngine {

    fun normalizeCommand(
        command: String
    ): String {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        when (detectIntent(text)) {

            AxonIntent.BRIGHTNESS_INCREASE -> {
                return "increase brightness"
            }

            AxonIntent.BRIGHTNESS_DECREASE -> {
                return "decrease brightness"
            }

            AxonIntent.VOLUME_INCREASE -> {
                return "increase volume"
            }

            AxonIntent.VOLUME_DECREASE -> {
                return "decrease volume"
            }

            AxonIntent.VOLUME_MUTE -> {
                return "mute"
            }

            AxonIntent.VOLUME_UNMUTE -> {
                return "unmute"
            }

            AxonIntent.FLASHLIGHT_ON -> {
                return "turn on flashlight"
            }

            AxonIntent.FLASHLIGHT_OFF -> {
                return "turn off flashlight"
            }

            AxonIntent.BATTERY_STATUS -> {
                return "battery level"
            }

            AxonIntent.CHARGING_STATUS -> {
                return "am i charging"
            }

            AxonIntent.DEVICE_MODEL -> {
                return "what phone"
            }

            AxonIntent.WIFI_STATUS -> {
                return "wifi status"
            }

            AxonIntent.OPEN_WIFI_SETTINGS -> {
                return "open wifi settings"
            }

            AxonIntent.OPEN_SOUND_SETTINGS -> {
                return "open sound settings"
            }

            AxonIntent.OPEN_DISPLAY_SETTINGS -> {
                return "open display settings"
            }

            AxonIntent.OPEN_ANDROID_SETTINGS -> {
                return "open settings"
            }

            AxonIntent.OPEN_PHONE -> {
                return "open phone"
            }

            AxonIntent.OPEN_CONTACTS -> {
                return "open contacts"
            }

            AxonIntent.OPEN_MESSAGES -> {
                return "open messages"
            }

            AxonIntent.OPEN_EMAIL -> {
                return "open email"
            }

            AxonIntent.OPEN_BROWSER -> {
                return "open browser"
            }

            AxonIntent.CALCULATE -> {
                return command
            }

            AxonIntent.OPEN_WEBSITE -> {
                return command
            }

            AxonIntent.OPEN_APP -> {
                return command
            }

            else -> {
                return command
            }
        }
    }

    fun detectIntent(
        command: String
    ): AxonIntent {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )

        // =========================================================
        // GREETING
        // =========================================================

        if (
            text == "hi" ||
            text == "hello" ||
            text == "hey" ||
            text == "axon" ||
            text == "hi axon" ||
            text == "hey axon" ||
            text == "hello axon" ||
            text.contains("hello axon") ||
            text.contains("hi axon") ||
            text.contains("hey axon")
        ) {
            return AxonIntent.GREETING
        }

        // =========================================================
        // TIME
        // =========================================================

        if (
            text.contains("what time") ||
            text.contains("current time") ||
            text.contains("tell me the time") ||
            text.contains("time right now") ||
            text.contains("what's the time") ||
            text.contains("whats the time") ||
            text == "time"
        ) {
            return AxonIntent.TIME
        }

        // =========================================================
        // DATE
        // =========================================================

        if (
            text.contains("what date") ||
            text.contains("today's date") ||
            text.contains("todays date") ||
            text.contains("tell me the date") ||
            text.contains("what day is it") ||
            text.contains("what is today's date") ||
            text.contains("what is todays date") ||
            text == "date"
        ) {
            return AxonIntent.DATE
        }

        // =========================================================
        // IDENTITY
        // =========================================================

        if (
            text.contains("who are you") ||
            text.contains("what are you") ||
            text.contains("what is axon") ||
            text.contains("who is axon")
        ) {
            return AxonIntent.IDENTITY
        }

        // =========================================================
        // FLASHLIGHT
        // =========================================================

        val flashlight =
            text.contains("flashlight") ||
            text.contains("torch")

        val flashlightOnAction =
            text.contains("turn on") ||
            text.contains("switch on") ||
            text.contains("enable") ||
            text.contains("activate") ||
            text.contains("turn the flashlight on") ||
            text.contains("turn the torch on") ||
            text.contains("switch the flashlight on") ||
            text.contains("switch the torch on") ||
            text.contains("put the flashlight on") ||
            text.contains("put the torch on") ||
            text.contains("flashlight on") ||
            text.contains("torch on")

        val flashlightOffAction =
            text.contains("turn off") ||
            text.contains("switch off") ||
            text.contains("disable") ||
            text.contains("deactivate") ||
            text.contains("turn the flashlight off") ||
            text.contains("turn the torch off") ||
            text.contains("switch the flashlight off") ||
            text.contains("switch the torch off") ||
            text.contains("put the flashlight off") ||
            text.contains("put the torch off") ||
            text.contains("flashlight off") ||
            text.contains("torch off")

        if (
            flashlight &&
            flashlightOnAction
        ) {
            return AxonIntent.FLASHLIGHT_ON
        }

        if (
            flashlight &&
            flashlightOffAction
        ) {
            return AxonIntent.FLASHLIGHT_OFF
        }

        // =========================================================
        // MUTE
        // =========================================================

        if (
            text == "mute" ||
            text.contains("mute the phone") ||
            text.contains("mute my phone") ||
            text.contains("mute sound") ||
            text.contains("mute audio") ||
            text.contains("turn off sound") ||
            text.contains("turn the sound off") ||
            text.contains("turn sound off") ||
            text.contains("silence the phone") ||
            text.contains("silence my phone") ||
            text.contains("make the phone silent") ||
            text.contains("put the phone on silent")
        ) {
            return AxonIntent.VOLUME_MUTE
        }

        // =========================================================
        // UNMUTE
        // =========================================================

        if (
            text == "unmute" ||
            text.contains("unmute the phone") ||
            text.contains("unmute sound") ||
            text.contains("unmute audio") ||
            text.contains("turn on sound") ||
            text.contains("turn the sound on") ||
            text.contains("turn sound on") ||
            text.contains("restore sound") ||
            text.contains("restore audio")
        ) {
            return AxonIntent.VOLUME_UNMUTE
        }

        // =========================================================
        // VOLUME INCREASE
        // =========================================================

        val volumeIncreaseAction =
            text.contains("increase volume") ||
            text.contains("raise volume") ||
            text.contains("turn up volume") ||
            text.contains("volume up") ||
            text.contains("make volume louder") ||
            text.contains("make the volume louder") ||
            text.contains("make it louder") ||
            text.contains("make louder") ||
            text.contains("increase sound") ||
            text.contains("raise sound") ||
            text.contains("turn up sound") ||
            text.contains("sound up") ||
            text.contains("make sound louder") ||
            text.contains("make the sound louder") ||
            text.contains("increase loudness") ||
            text == "louder" ||
            text == "volume up" ||
            text == "sound up"

        if (volumeIncreaseAction) {
            return AxonIntent.VOLUME_INCREASE
        }

        // =========================================================
        // VOLUME DECREASE
        // =========================================================

        val volumeDecreaseAction =
            text.contains("decrease volume") ||
            text.contains("lower volume") ||
            text.contains("turn down volume") ||
            text.contains("volume down") ||
            text.contains("make volume quieter") ||
            text.contains("make the volume quieter") ||
            text.contains("make it quieter") ||
            text.contains("make quieter") ||
            text.contains("decrease sound") ||
            text.contains("lower sound") ||
            text.contains("turn down sound") ||
            text.contains("sound down") ||
            text.contains("make sound quieter") ||
            text.contains("make the sound quieter") ||
            text.contains("decrease loudness") ||
            text == "quieter" ||
            text == "volume down" ||
            text == "sound down"

        if (volumeDecreaseAction) {
            return AxonIntent.VOLUME_DECREASE
        }

        // =========================================================
        // BRIGHTNESS INCREASE
        // =========================================================

        val brightnessIncreaseAction =
            text.contains("increase brightness") ||
            text.contains("raise brightness") ||
            text.contains("turn up brightness") ||
            text.contains("brightness up") ||
            text.contains("make brightness higher") ||
            text.contains("make the brightness higher") ||
            text.contains("make the screen brighter") ||
            text.contains("make screen brighter") ||
            text.contains("brighten the screen") ||
            text.contains("brighten screen") ||
            text.contains("increase screen brightness") ||
            text.contains("raise screen brightness") ||
            text.contains("turn up screen brightness") ||
            text.contains("make display brighter") ||
            text.contains("brighten display") ||
            text.contains("brighten the screen") ||
            text.contains("brighten screen") ||
            text.contains("increase the screen brightness") ||
            text.contains("raise the screen brightness") ||
            text.contains("turn the brightness up") ||
            text == "brighter" ||
            text == "brightness up"

        if (brightnessIncreaseAction) {
            return AxonIntent.BRIGHTNESS_INCREASE
        }

        // =========================================================
        // BRIGHTNESS DECREASE
        // =========================================================

        val brightnessDecreaseAction =
            text.contains("decrease brightness") ||
            text.contains("lower brightness") ||
            text.contains("turn down brightness") ||
            text.contains("brightness down") ||
            text.contains("make brightness lower") ||
            text.contains("make the brightness lower") ||
            text.contains("make the screen darker") ||
            text.contains("make screen darker") ||
            text.contains("darken the screen") ||
            text.contains("darken screen") ||
            text.contains("decrease screen brightness") ||
            text.contains("lower screen brightness") ||
            text.contains("turn down screen brightness") ||
            text.contains("make display darker") ||
            text.contains("darken display") ||
            text.contains("dim the screen") ||
            text.contains("dim screen") ||
            text.contains("lower the screen brightness") ||
            text.contains("reduce the screen brightness") ||
            text.contains("decrease the screen brightness") ||
            text == "darker" ||
            text == "brightness down"

        if (brightnessDecreaseAction) {
            return AxonIntent.BRIGHTNESS_DECREASE
        }

        // =========================================================
        // BATTERY
        // =========================================================

        if (
            text.contains("battery") &&
            !text.contains("charging")
        ) {
            return AxonIntent.BATTERY_STATUS
        }

        // =========================================================
        // CHARGING
        // =========================================================

        if (
            text.contains("charging") ||
            text.contains("am i charging") ||
            text.contains("is my phone charging") ||
            text.contains("is the phone charging") ||
            text.contains("is my device charging")
        ) {
            return AxonIntent.CHARGING_STATUS
        }

        // =========================================================
        // DEVICE MODEL
        // =========================================================

        if (
            text.contains("device model") ||
            text.contains("phone model") ||
            text.contains("what phone") ||
            text.contains("which phone") ||
            text.contains("what device") ||
            text.contains("which device") ||
            text.contains("what is my phone") ||
            text.contains("what's my phone") ||
            text.contains("whats my phone") ||
            text.contains("which phone am i using") ||
            text.contains("what phone am i using")
        ) {
            return AxonIntent.DEVICE_MODEL
        }

        // =========================================================
        // WIFI ON / OFF
        // =========================================================

        val wifi =
            text.contains("wifi") ||
            text.contains("wi-fi") ||
            text.contains("wi fi") ||
            text.contains("why fi") ||
            text.contains("whyfy")

        val wifiOnAction =
            text.contains("turn on wifi") ||
            text.contains("turn on wi-fi") ||
            text.contains("turn on wi fi") ||
            text.contains("switch wifi on") ||
            text.contains("switch wi-fi on") ||
            text.contains("switch wi fi on") ||
            text.contains("enable wifi") ||
            text.contains("enable wi-fi") ||
            text.contains("enable wi fi") ||
            text.contains("activate wifi") ||
            text.contains("activate wi-fi") ||
            text.contains("wifi on") ||
            text.contains("wi-fi on") ||
            text.contains("wi fi on") ||
            text.contains("turn the wifi on") ||
            text.contains("turn the wi-fi on") ||
            text.contains("turn the wi fi on")

        val wifiOffAction =
            text.contains("turn off wifi") ||
            text.contains("turn off wi-fi") ||
            text.contains("turn off wi fi") ||
            text.contains("switch wifi off") ||
            text.contains("switch wi-fi off") ||
            text.contains("switch wi fi off") ||
            text.contains("disable wifi") ||
            text.contains("disable wi-fi") ||
            text.contains("disable wi fi") ||
            text.contains("deactivate wifi") ||
            text.contains("deactivate wi-fi") ||
            text.contains("wifi off") ||
            text.contains("wi-fi off") ||
            text.contains("wi fi off") ||
            text.contains("turn the wifi off") ||
            text.contains("turn the wi-fi off") ||
            text.contains("turn the wi fi off")

        if (
            wifi &&
            (
                wifiOnAction ||
                wifiOffAction
            )
        ) {
            return AxonIntent.OPEN_WIFI_SETTINGS
        }

        // =========================================================
        // WIFI SETTINGS
        // =========================================================

        if (
            text.contains("wifi settings") ||
            text.contains("wi-fi settings") ||
            text.contains("wi fi settings") ||
            text.contains("open wifi settings") ||
            text.contains("open wi-fi settings") ||
            text.contains("open wi fi settings") ||
            text.contains("wifi setting")
        ) {
            return AxonIntent.OPEN_WIFI_SETTINGS
        }

        // =========================================================
        // WIFI STATUS
        // =========================================================

        if (
            wifi &&
            !text.contains("settings")
        ) {
            return AxonIntent.WIFI_STATUS
        }

        // =========================================================
        // SOUND SETTINGS
        // =========================================================

        if (
            text.contains("sound settings") ||
            text.contains("audio settings") ||
            text.contains("volume settings") ||
            text.contains("open sound settings") ||
            text.contains("open audio settings")
        ) {
            return AxonIntent.OPEN_SOUND_SETTINGS
        }

        // =========================================================
        // DISPLAY SETTINGS
        // =========================================================

        if (
            text.contains("brightness settings") ||
            text.contains("display settings") ||
            text.contains("screen settings") ||
            text.contains("open brightness settings") ||
            text.contains("open display settings") ||
            text.contains("open screen settings")
        ) {
            return AxonIntent.OPEN_DISPLAY_SETTINGS
        }

        // =========================================================
        // ANDROID SETTINGS
        // =========================================================

        if (
            text == "open settings" ||
            text == "open phone settings" ||
            text == "open android settings" ||
            text == "open device settings" ||
            text == "go to settings" ||
            text == "go to phone settings" ||
            text == "show settings"
        ) {
            return AxonIntent.OPEN_ANDROID_SETTINGS
        }

        // =========================================================
        // PHONE / DIALER
        // =========================================================

        if (
            text.contains("open phone") ||
            text.contains("open dialer") ||
            text.contains("open calling") ||
            text.contains("open calls") ||
            text.contains("open phone app") ||
            text.contains("launch phone") ||
            text.contains("launch dialer")
        ) {
            return AxonIntent.OPEN_PHONE
        }

        // =========================================================
        // CONTACTS
        // =========================================================

        if (
            text.contains("open contacts") ||
            text.contains("open contact") ||
            text.contains("show contacts") ||
            text.contains("show my contacts") ||
            text.contains("launch contacts") ||
            text.contains("launch contact")
        ) {
            return AxonIntent.OPEN_CONTACTS
        }

        // =========================================================
        // MESSAGES
        // =========================================================

        if (
            text.contains("open messages") ||
            text.contains("open messaging") ||
            text.contains("open sms") ||
            text.contains("open message app") ||
            text.contains("launch messages") ||
            text.contains("launch messaging")
        ) {
            return AxonIntent.OPEN_MESSAGES
        }

        // =========================================================
        // EMAIL
        // =========================================================

        if (
            text.contains("open email") ||
            text.contains("open mail") ||
            text.contains("launch email") ||
            text.contains("launch mail")
        ) {
            return AxonIntent.OPEN_EMAIL
        }

        // =========================================================
        // BROWSER
        // =========================================================

        if (
            text.contains("open browser") ||
            text.contains("open web browser") ||
            text.contains("launch browser") ||
            text.contains("launch web browser")
        ) {
            return AxonIntent.OPEN_BROWSER
        }

        // =========================================================
        // CALCULATOR
        // =========================================================

        if (
            text.contains("calculate") ||
            text.contains("what is") && (
                text.contains("plus") ||
                text.contains("minus") ||
                text.contains("times") ||
                text.contains("multiplied by") ||
                text.contains("divided by") ||
                text.contains("divide by")
            ) ||
            text.contains("plus") ||
            text.contains("minus") ||
            text.contains("times") ||
            text.contains("multiplied by") ||
            text.contains("multiply by") ||
            text.contains("divided by") ||
            text.contains("divide by") ||
            text.contains("add") && text.any { it.isDigit() } ||
            text.contains("subtract") && text.any { it.isDigit() } ||
            text.contains("multiply") && text.any { it.isDigit() } ||
            text.contains("divide") && text.any { it.isDigit() }
        ) {
            return AxonIntent.CALCULATE
        }

        // =========================================================
        // WEBSITE
        // =========================================================

        val looksLikeWebsite =
            text.contains(".com") ||
            text.contains(".in") ||
            text.contains(".org") ||
            text.contains(".net") ||
            text.contains(".io") ||
            text.contains(" dot com") ||
            text.contains(" dot in") ||
            text.contains(" dot org") ||
            text.contains(" dot net") ||
            text.contains(" dot io") ||
            text.contains("http://") ||
            text.contains("https://")

        if (
            (
                text.startsWith("open ") ||
                text.startsWith("go to ") ||
                text.startsWith("visit ") ||
                text.startsWith("open website ") ||
                text.startsWith("visit website ")
            ) &&
            looksLikeWebsite
        ) {
            return AxonIntent.OPEN_WEBSITE
        }

        // =========================================================
        // APP
        // =========================================================

        if (
            text.startsWith("open ") ||
            text.startsWith("launch ") ||
            text.startsWith("start ") ||
            text.startsWith("run ")
        ) {
            return AxonIntent.OPEN_APP
        }

        // =========================================================
        // NO MATCH
        // =========================================================

        return AxonIntent.NONE
    }
}