
package com.pratik.axon.platform.android.personal

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class AxonNote(
    val id: Long,
    val text: String,
    val createdAt: Long
)

data class AxonTask(
    val id: Long,
    val text: String,
    val completed: Boolean,
    val createdAt: Long
)

class PersonalDataStore(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "axon_personal_data",
            Context.MODE_PRIVATE
        )

    @Synchronized
    fun addNote(text: String): AxonNote {
        val note = AxonNote(
            id = System.currentTimeMillis(),
            text = text.trim(),
            createdAt = System.currentTimeMillis()
        )

        val notes = getNotes().toMutableList()
        notes.add(note)

        val array = JSONArray()
        notes.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("text", it.text)
                    .put("createdAt", it.createdAt)
            )
        }

        preferences.edit()
            .putString("notes", array.toString())
            .apply()

        return note
    }

    @Synchronized
    fun getNotes(): List<AxonNote> {
        val raw = preferences.getString("notes", "[]") ?: "[]"

        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<AxonNote>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                result.add(
                    AxonNote(
                        id = item.optLong("id"),
                        text = item.optString("text"),
                        createdAt = item.optLong("createdAt")
                    )
                )
            }

            result.sortedByDescending { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun deleteLatestNote(): Boolean {
        val notes = getNotes().toMutableList()
        if (notes.isEmpty()) return false

        notes.removeAt(0)
        saveNotes(notes)
        return true
    }

    @Synchronized
    private fun saveNotes(notes: List<AxonNote>) {
        val array = JSONArray()

        notes.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("text", it.text)
                    .put("createdAt", it.createdAt)
            )
        }

        preferences.edit()
            .putString("notes", array.toString())
            .apply()
    }

    @Synchronized
    fun addTask(text: String): AxonTask {
        val task = AxonTask(
            id = System.currentTimeMillis(),
            text = text.trim(),
            completed = false,
            createdAt = System.currentTimeMillis()
        )

        val tasks = getTasks().toMutableList()
        tasks.add(task)
        saveTasks(tasks)

        return task
    }

    @Synchronized
    fun getTasks(): List<AxonTask> {
        val raw = preferences.getString("tasks", "[]") ?: "[]"

        return try {
            val array = JSONArray(raw)
            val result = mutableListOf<AxonTask>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                result.add(
                    AxonTask(
                        id = item.optLong("id"),
                        text = item.optString("text"),
                        completed = item.optBoolean("completed"),
                        createdAt = item.optLong("createdAt")
                    )
                )
            }

            result.sortedBy { it.createdAt }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun completeLatestPendingTask(): Boolean {
        val tasks = getTasks().toMutableList()
        val index = tasks.indexOfLast { !it.completed }

        if (index < 0) return false

        tasks[index] = tasks[index].copy(completed = true)
        saveTasks(tasks)
        return true
    }

    @Synchronized
    fun clearCompletedTasks(): Int {
        val tasks = getTasks().toMutableList()
        val oldSize = tasks.size
        val remaining = tasks.filterNot { it.completed }

        saveTasks(remaining)
        return oldSize - remaining.size
    }

    @Synchronized
    private fun saveTasks(tasks: List<AxonTask>) {
        val array = JSONArray()

        tasks.forEach {
            array.put(
                JSONObject()
                    .put("id", it.id)
                    .put("text", it.text)
                    .put("completed", it.completed)
                    .put("createdAt", it.createdAt)
            )
        }

        preferences.edit()
            .putString("tasks", array.toString())
            .apply()
    }
}
