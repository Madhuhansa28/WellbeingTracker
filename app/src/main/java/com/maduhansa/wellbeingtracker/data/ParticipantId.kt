package com.maduhansa.wellbeingtracker.data

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

object ParticipantId {

    private const val PREFS_NAME = "participant"
    private const val KEY_ID = "participant_id"

    // Returns this phone's random ID, creating one the first time (e.g. "P-3F9A1C2B").
    fun get(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(KEY_ID, null)?.let { return it }

        val newId = "P-" + UUID.randomUUID().toString().take(8).uppercase()
        prefs.edit { putString(KEY_ID, newId) }
        return newId
    }

    // Forgets the current ID. A new one is created next time get() is called.
    fun reset(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit { remove(KEY_ID) }
    }
}