package com.pratik.axon.platform.android.communication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import java.util.Locale
import kotlin.math.max
import com.pratik.axon.core.capability.Capability
import com.pratik.axon.core.security.RiskLevel

class CommunicationTool(
    private val context: Context
) : AxonTool {

    override fun getName(): String {
        return "Communication"
    }
    
    override fun getRiskLevel(): RiskLevel {
    return RiskLevel.MEDIUM
}

override fun getCapability(): Capability {
    return Capability(
        id = getName(),
        name = getName(),
        description = "Handles communication and contact-related actions.",
        requiredPermissions = listOf(Manifest.permission.READ_CONTACTS),
        riskLevel = RiskLevel.MEDIUM
    )
}

    override fun canHandle(command: String): Boolean {

        val text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        return text.startsWith("call ") ||
                text.startsWith("phone ") ||
                text.contains("make a call") ||
                text.startsWith("message ") ||
                text.startsWith("text ") ||
                text.startsWith("send a message to ")
    }

    override fun execute(command: String): AxonResult {

        var text =
            command
                .lowercase(Locale.getDefault())
                .trim()

        /*
         * ============================
         * WHATSAPP MESSAGE COMMAND
         * ============================
         */

        val isMessageCommand =
            text.startsWith("message ") ||
                    text.startsWith("text ") ||
                    text.startsWith("send a message to ")

        if (isMessageCommand) {

            text =
                text
                    .replaceFirst(
                        Regex("^message\\s+"),
                        ""
                    )
                    .replaceFirst(
                        Regex("^text\\s+"),
                        ""
                    )
                    .replaceFirst(
                        Regex("^send a message to\\s+"),
                        ""
                    )
                    .trim()

            if (text.isEmpty()) {
                return AxonResult(
                    success = false,
                    message = "Please tell me who you want to message."
                )
            }

            if (
                context is Activity &&
                context.checkSelfPermission(
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                context.requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_CONTACTS
                    ),
                    2001
                )

                return AxonResult(
                    success = false,
                    message = "I need permission to access your contacts."
                )
            }

            return findContactAndOpenWhatsApp(text)
        }

        /*
         * ============================
         * PHONE CALL COMMAND
         * ============================
         */

        text =
            text
                .replaceFirst(
                    Regex("^call\\s+"),
                    ""
                )
                .replaceFirst(
                    Regex("^phone\\s+"),
                    ""
                )
                .replaceFirst(
                    Regex("^make a call to\\s+"),
                    ""
                )
                .trim()

        if (text.isEmpty()) {
            return AxonResult(
                success = false,
                message = "Please tell me who you want to call."
            )
        }

        if (
            context is Activity &&
            context.checkSelfPermission(
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            context.requestPermissions(
                arrayOf(
                    Manifest.permission.READ_CONTACTS
                ),
                2001
            )

            return AxonResult(
                success = false,
                message = "I need permission to access your contacts."
            )
        }

        return findContactAndOpenDialer(text)
    }

    /*
     * ============================
     * FIND CONTACT + OPEN DIALER
     * ============================
     */

    private fun findContactAndOpenDialer(
        contactName: String
    ): AxonResult {

        val resolver =
            context.contentResolver

        val projection =
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
            )

        return try {

            val cursor =
                resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )

            if (cursor == null) {
                return AxonResult(
                    success = false,
                    message = "I couldn't access your contacts."
                )
            }

            val matches =
                mutableListOf<ContactMatch>()

            cursor.use {

                while (it.moveToNext()) {

                    val name =
                        it.getString(0)
                            ?: continue

                    val number =
                        it.getString(1)
                            ?: continue

                    val isPrimary =
                        it.getInt(2) == 1

                    val score =
                        getContactMatchScore(
                            contactName,
                            name
                        )

                    if (score > 0) {

                        matches.add(
                            ContactMatch(
                                name = name,
                                number = number,
                                isPrimary = isPrimary,
                                score = score
                            )
                        )
                    }
                }
            }

            if (matches.isEmpty()) {

                return AxonResult(
                    success = false,
                    message = "I couldn't find a contact named $contactName."
                )
            }

            matches.sortWith(
                compareByDescending<ContactMatch> {
                    it.score
                }.thenByDescending {
                    it.isPrimary
                }
            )

            val best =
                matches.first()

            val sameScore =
                matches.filter {
                    it.score == best.score
                }

            val differentContacts =
                sameScore
                    .map {
                        normalizeContactName(it.name)
                    }
                    .distinct()

            if (differentContacts.size > 1) {

                return AxonResult(
                    success = false,
                    message = "I found more than one contact matching $contactName."
                )
            }

            val dialIntent =
                Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse(
                        "tel:" +
                                Uri.encode(best.number)
                    )
                )

            dialIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(
                dialIntent
            )

            AxonResult(
                success = true,
                message = "Opening the dialer for ${best.name}."
            )

        } catch (e: Exception) {

            AxonResult(
                success = false,
                message = "I couldn't access your contacts."
            )
        }
    }

    /*
     * ============================
     * FIND CONTACT + OPEN WHATSAPP
     * ============================
     */

    private fun findContactAndOpenWhatsApp(
        contactName: String
    ): AxonResult {

        val resolver =
            context.contentResolver

        val projection =
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
            )

        return try {

            val cursor =
                resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )

            if (cursor == null) {
                return AxonResult(
                    success = false,
                    message = "I couldn't access your contacts."
                )
            }

            val matches =
                mutableListOf<ContactMatch>()

            cursor.use {

                while (it.moveToNext()) {

                    val name =
                        it.getString(0)
                            ?: continue

                    val number =
                        it.getString(1)
                            ?: continue

                    val isPrimary =
                        it.getInt(2) == 1

                    val score =
                        getContactMatchScore(
                            contactName,
                            name
                        )

                    if (score > 0) {

                        matches.add(
                            ContactMatch(
                                name = name,
                                number = number,
                                isPrimary = isPrimary,
                                score = score
                            )
                        )
                    }
                }
            }

            if (matches.isEmpty()) {

                return AxonResult(
                    success = false,
                    message = "I couldn't find a contact named $contactName."
                )
            }

            matches.sortWith(
                compareByDescending<ContactMatch> {
                    it.score
                }.thenByDescending {
                    it.isPrimary
                }
            )

            val best =
                matches.first()

            /*
             * If two DIFFERENT contacts have
             * exactly the same best score,
             * don't guess.
             */
            val sameScore =
                matches.filter {
                    it.score == best.score
                }

            val differentContacts =
                sameScore
                    .map {
                        normalizeContactName(it.name)
                    }
                    .distinct()

            if (differentContacts.size > 1) {

                return AxonResult(
                    success = false,
                    message = "I found more than one contact matching $contactName."
                )
            }

            /*
             * Convert the contact number into
             * WhatsApp international format.
             */
            val whatsappNumber =
                normalizeIndianPhoneNumber(
                    best.number
                )

            if (whatsappNumber == null) {

                return AxonResult(
                    success = false,
                    message = "I couldn't use that phone number for WhatsApp."
                )
            }

            /*
             * WhatsApp Click-to-Chat URL.
             *
             * Example:
             * 9876543210
             * ↓
             * 919876543210
             */
            val whatsappUri =
                Uri.parse(
                    "https://wa.me/$whatsappNumber"
                )

            val whatsappIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    whatsappUri
                )

            /*
             * Force the link to WhatsApp.
             */
            whatsappIntent.setPackage(
                "com.whatsapp"
            )

            whatsappIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            try {

                context.startActivity(
                    whatsappIntent
                )

            } catch (e: Exception) {

                return AxonResult(
                    success = false,
                    message = "WhatsApp is not installed on this phone."
                )
            }

            AxonResult(
                success = true,
                message = "Opening WhatsApp for ${best.name}."
            )

        } catch (e: Exception) {

            AxonResult(
                success = false,
                message = "I couldn't open WhatsApp."
            )
        }
    }

    /*
     * ============================
     * INDIAN PHONE NUMBER NORMALIZER
     * ============================
     */

    private fun normalizeIndianPhoneNumber(
        number: String
    ): String? {

        var digits =
            number.filter {
                it.isDigit()
            }

        if (digits.isEmpty()) {
            return null
        }

        /*
         * +91XXXXXXXXXX
         * becomes:
         * 91XXXXXXXXXX
         */
        if (
            digits.startsWith("91") &&
            digits.length == 12
        ) {
            return digits
        }

        /*
         * 0XXXXXXXXXX
         * becomes:
         * 91XXXXXXXXXX
         */
        if (
            digits.startsWith("0") &&
            digits.length == 11
        ) {

            digits =
                digits.substring(1)

            return "91$digits"
        }

        /*
         * XXXXXXXXXX
         * becomes:
         * 91XXXXXXXXXX
         */
        if (digits.length == 10) {

            return "91$digits"
        }

        return null
    }

    /*
     * ============================
     * CONTACT MATCHING
     * ============================
     */

    private fun getContactMatchScore(
        spokenName: String,
        contactName: String
    ): Int {

        val spoken =
            normalizeContactName(spokenName)

        val contact =
            normalizeContactName(contactName)

        if (
            spoken.isEmpty() ||
            contact.isEmpty()
        ) {
            return 0
        }

        /*
         * LEVEL 1
         * Exact complete match.
         */
        if (spoken == contact) {
            return 100
        }

        val spokenWords =
            spoken.split(" ")

        val contactWords =
            contact.split(" ")

        /*
         * LEVEL 2
         * Every spoken word exactly matches
         * one contact word.
         */
        if (
            spokenWords.all { spokenWord ->

                contactWords.any { contactWord ->
                    contactWord == spokenWord
                }
            }
        ) {
            return 90
        }

        /*
         * LEVEL 3
         * Contact begins with spoken name.
         */
        if (contact.startsWith(spoken)) {
            return 80
        }

        /*
         * LEVEL 4
         * Every spoken word matches the beginning
         * of a contact word.
         */
        if (
            spokenWords.all { spokenWord ->

                contactWords.any { contactWord ->

                    contactWord.startsWith(
                        spokenWord
                    )
                }
            }
        ) {
            return 70
        }

        /*
         * LEVEL 5
         * Complete spoken name appears inside
         * contact name.
         */
        if (contact.contains(spoken)) {
            return 60
        }

        /*
         * LEVEL 6
         * Fuzzy matching for a single spoken name.
         *
         * Examples:
         *
         * pappa  → papa
         * shubam → shubham
         * desmuk → deshmukh
         */
        if (spokenWords.size == 1) {

            val spokenWord =
                spokenWords.first()

            for (contactWord in contactWords) {

                val similarity =
                    calculateSimilarity(
                        spokenWord,
                        contactWord
                    )

                if (
                    similarity >=
                    getRequiredSimilarity(
                        spokenWord,
                        contactWord
                    )
                ) {
                    return 55
                }
            }
        }

        return 0
    }

    /*
     * ============================
     * FUZZY MATCH THRESHOLD
     * ============================
     */

    private fun getRequiredSimilarity(
        first: String,
        second: String
    ): Double {

        val length =
            max(
                first.length,
                second.length
            )

        return when {

            length <= 3 ->
                0.90

            length <= 4 ->
                0.85

            length <= 6 ->
                0.80

            else ->
                0.75
        }
    }

    /*
     * ============================
     * STRING SIMILARITY
     * ============================
     */

    private fun calculateSimilarity(
        first: String,
        second: String
    ): Double {

        if (first == second) {
            return 1.0
        }

        if (
            first.isEmpty() ||
            second.isEmpty()
        ) {
            return 0.0
        }

        val distance =
            levenshteinDistance(
                first,
                second
            )

        val maxLength =
            max(
                first.length,
                second.length
            )

        return 1.0 -
                (
                    distance.toDouble() /
                            maxLength.toDouble()
                )
    }

    /*
     * ============================
     * LEVENSHTEIN DISTANCE
     * ============================
     */

    private fun levenshteinDistance(
        first: String,
        second: String
    ): Int {

        val dp =
            Array(
                first.length + 1
            ) {
                IntArray(
                    second.length + 1
                )
            }

        for (i in 0..first.length) {
            dp[i][0] = i
        }

        for (j in 0..second.length) {
            dp[0][j] = j
        }

        for (i in 1..first.length) {

            for (j in 1..second.length) {

                val cost =
                    if (
                        first[i - 1] ==
                        second[j - 1]
                    ) {
                        0
                    } else {
                        1
                    }

                dp[i][j] =
                    minOf(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1,
                        dp[i - 1][j - 1] + cost
                    )
            }
        }

        return dp[first.length][second.length]
    }

    /*
     * ============================
     * CONTACT NAME NORMALIZATION
     * ============================
     */

    private fun normalizeContactName(
        name: String
    ): String {

        return name
            .lowercase(Locale.getDefault())
            .replace(
                Regex("[^\\p{L}\\p{N}\\s]"),
                " "
            )
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()
    }

    /*
     * ============================
     * CONTACT MATCH DATA
     * ============================
     */

    private data class ContactMatch(
        val name: String,
        val number: String,
        val isPrimary: Boolean,
        val score: Int
    )
}