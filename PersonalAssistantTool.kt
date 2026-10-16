
package com.pratik.axon.platform.android.personal

import android.content.Context
import com.pratik.axon.core.capability.AxonTool
import com.pratik.axon.core.model.AxonResult
import com.pratik.axon.core.security.RiskLevel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PersonalAssistantTool(
    context: Context
) : AxonTool {

    private val store = PersonalDataStore(context)
    private val reminderScheduler = ReminderScheduler(context)

    override fun getName(): String = "Personal Assistant"

    override fun getRiskLevel(): RiskLevel = RiskLevel.LOW

    override fun canHandle(command: String): Boolean {
        val text = command.lowercase(Locale.ROOT).trim()

        val noteCommand =
            text.contains("save a note") ||
            text.contains("save note") ||
            text.contains("take a note") ||
            text.contains("write a note") ||
            text.contains("my notes") ||
            text.contains("list notes") ||
            text.contains("show notes") ||
            text.contains("delete latest note") ||
            text.contains("delete last note") ||
            text.contains("delete the latest note") ||
            text.contains("delete the last note")


        val taskCommand =
            text.contains("add a task") ||
            text.contains("add task") ||
            text.contains("add to my tasks") ||
            text.contains("to my to-do list") ||
            text.contains("to my todo list") ||
            text.contains("list tasks") ||
            text.contains("show tasks") ||
            text.contains("my tasks") ||
            text.contains("complete a task") ||
            text.contains("complete the task") ||
            text.contains("mark a task complete") ||
            text.contains("clear completed tasks")

        val reminderCommand =
            text.contains("remind me") ||
            text.contains("set a reminder") ||
            text.contains("set reminder") ||
            text.contains("schedule a reminder")

        return noteCommand || taskCommand || reminderCommand
    }

    override fun execute(command: String): AxonResult {
        val text = command.trim()
        val lower = text.lowercase(Locale.ROOT)

        return try {
            when {
                lower.contains("delete latest note") ||
                    lower.contains("delete last note") ||
lower.contains("delete the latest note") ||
    lower.contains("delete the last note") ||
    lower.contains("delete latest note") ||
    lower.contains("delete last note") -> {
    if (store.deleteLatestNote()) {
        success("Deleted the latest note.")
    } else {
        failure("You don't have any saved notes.")
    }
}


                
lower.contains("list notes") ||
    lower.contains("show notes") ||
    lower.contains("my notes") -> listNotes()


                lower.contains("save a note") ||
                    lower.contains("save note") ||
                    lower.contains("take a note") ||
                    lower.contains("write a note") -> saveNote(text)

                lower.contains("clear completed tasks") ->
                    clearCompletedTasks()

                lower.contains("complete a task") ||
                    lower.contains("complete the task") ||
                    lower.contains("mark a task complete") ->
                    completeTask()

                
lower.contains("list tasks") ||
    lower.contains("show tasks") ||
    lower.contains("my tasks") -> listTasks()


                lower.contains("add a task") ||
                    lower.contains("add task") ||
                    lower.contains("add to my tasks") ||
                    lower.contains("to my to-do list") ||
                    lower.contains("to my todo list") ->
                    addTask(text)

                lower.contains("remind me") ||
                    lower.contains("set a reminder") ||
                    lower.contains("set reminder") ||
                    lower.contains("schedule a reminder") ->
                    createReminder(text)

                else -> failure("I couldn't understand that personal assistant command.")
            }
        } catch (exception: Exception) {
            failure(
                "I couldn't complete that action. Please try again."
            )
        }
    }

    private fun saveNote(command: String): AxonResult {
        val content = removePrefix(
            command,
            listOf(
                "please save a note that",
                "save a note that",
                "save note that",
                "please save a note",
                "save a note",
                "save note",
                "please take a note that",
                "take a note that",
                "take a note",
                "write a note that",
                "write a note"
            )
        )

        if (content.isBlank()) {
            return failure("Tell me what you want me to save in the note.")
        }

        store.addNote(content)
        return success("Note saved.")
    }

   
private fun listNotes(): AxonResult {
    val notes = store.getNotes()

    if (notes.isEmpty()) {
        return success("You don't have any saved notes.")
    }

    val message = buildString {
        append("You have ${notes.size} saved notes. ")

        notes.take(5).forEachIndexed { index, note ->
            append("Note ${index + 1}: ${note.text}. ")
        }

        if (notes.size > 5) {
            append("And ${notes.size - 5} more notes.")
        }
    }

    return success(message)
}


    private fun addTask(command: String): AxonResult {
        val taskText = removePrefix(
            command,
            listOf(
                "please add a task to my to-do list",
                "add a task to my to-do list",
                "add a task to my todo list",
                "add task to my to-do list",
                "add task to my todo list",
                "add to my tasks",
                "add a task",
                "add task",
                "to my to-do list",
                "to my todo list"
            )
        )

        if (taskText.isBlank()) {
            return failure("Tell me what task you want to add.")
        }

        store.addTask(taskText)
        return success("Task added: $taskText.")
    }

    private fun listTasks(): AxonResult {
        val tasks = store.getTasks()

        if (tasks.isEmpty()) {
            return success("Your task list is empty.")
        }

        val pending = tasks.filterNot { it.completed }
        val completed = tasks.count { it.completed }

        if (pending.isEmpty()) {
            return success("You have no pending tasks. $completed tasks completed.")
        }

        val message = buildString {
            append("Your pending tasks. ")
            pending.take(10).forEachIndexed { index, task ->
                append("${index + 1}. ${task.text}. ")
            }
            append("$completed tasks completed.")
        }

        return success(message)
    }

    private fun completeTask(): AxonResult {
        return if (store.completeLatestPendingTask()) {
            success("Marked the latest pending task as completed.")
        } else {
            failure("You don't have any pending tasks.")
        }
    }

    private fun clearCompletedTasks(): AxonResult {
        val count = store.clearCompletedTasks()
        return success("Cleared $count completed tasks.")
    }

    private fun createReminder(command: String): AxonResult {
        val parsed = parseReminder(command)
            ?: return failure(
                "I couldn't understand the reminder time. Try: Remind me at 7 PM to study."
            )

        val scheduled = reminderScheduler.scheduleReminder(
            reminderText = parsed.first,
            triggerAtMillis = parsed.second
        )

        return if (scheduled) {
            val formattedTime = SimpleDateFormat(
                "EEE, d MMM 'at' h:mm a",
                Locale.getDefault()
            ).format(Date(parsed.second))

            success("Reminder scheduled for $formattedTime: ${parsed.first}.")
        } else {
            failure("I couldn't schedule that reminder. Check the time and try again.")
        }
    }

    
private fun parseReminder(command: String): Pair<String, Long>? {
    val timeRegex = Regex(
        """(?i)(\d{1,2})(?:\s*:\s*(\d{2}))?\s*(a\.?m\.?|p\.?m\.?)"""
    )

    val match = timeRegex.find(command) ?: return null

    val hourInput = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].ifBlank { "0" }.toIntOrNull() ?: return null
    val period = match.groupValues[3]
        .lowercase(Locale.ROOT)
        .replace(".", "")

    if (hourInput !in 1..12 || minute !in 0..59) return null

    val hour24 = when {
        period == "am" && hourInput == 12 -> 0
        period == "pm" && hourInput != 12 -> hourInput + 12
        period == "am" -> hourInput
        period == "pm" -> hourInput
        else -> return null
    }

    val taskMatch = Regex(
        """(?i)\bto\s+(.+)$"""
    ).find(command)

    val reminderText = taskMatch?.groupValues
        ?.getOrNull(1)
        ?.trim()
        ?.trimEnd('.', '!', '?')
        .orEmpty()

    if (reminderText.isBlank()) return null

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hour24)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    if (command.contains("tomorrow", ignoreCase = true)) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    } else if (calendar.timeInMillis <= System.currentTimeMillis()) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    return reminderText to calendar.timeInMillis
}


    private fun removePrefix(
        command: String,
        prefixes: List<String>
    ): String {
        val trimmed = command.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        for (prefix in prefixes.sortedByDescending { it.length }) {
            if (lower.startsWith(prefix)) {
                return trimmed.substring(prefix.length)
                    .trim()
                    .trimStart(':', ',', '-', ' ')
                    .trimEnd('.', '!', '?')
                    .trim()
            }
        }

        return ""
    }

    private fun success(message: String) =
        AxonResult(success = true, message = message)

    private fun failure(message: String) =
        AxonResult(success = false, message = message)
}
