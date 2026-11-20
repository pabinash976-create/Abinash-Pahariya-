package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.WolfThemePreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wolf_study_prefs")

enum class StudyLanguage(val displayName: String, val nativeLabel: String) {
    ENGLISH("English", "English"),
    ASSAMESE("Assamese", "অসমীয়া"),
    HINDI("Hindi", "हिन्दी")
}

data class UserPreferences(
    val studentName: String = "Scholar",
    val academicTrack: String = "Personal Study Hub",
    val preferredLanguage: StudyLanguage = StudyLanguage.ENGLISH,
    val darkThemeEnabled: Boolean = true,
    val themePreset: WolfThemePreset = WolfThemePreset.MIDNIGHT_WOLF,
    val appLockEnabled: Boolean = false,
    val privacyPinHash: String = "",
    val hideProfileInBackup: Boolean = false,
    val lastBackupTimestamp: Long = 0L
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val STUDENT_NAME = stringPreferencesKey("student_name")
        val ACADEMIC_TRACK = stringPreferencesKey("academic_track")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val THEME_PRESET = stringPreferencesKey("theme_preset")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val PRIVACY_PIN_HASH = stringPreferencesKey("privacy_pin_hash")
        val HIDE_PROFILE_IN_BACKUP = booleanPreferencesKey("hide_profile_in_backup")
        val LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val langName = prefs[Keys.PREFERRED_LANGUAGE] ?: StudyLanguage.ENGLISH.name
        val language = StudyLanguage.entries.firstOrNull { it.name == langName } ?: StudyLanguage.ENGLISH
        val presetName = prefs[Keys.THEME_PRESET] ?: WolfThemePreset.MIDNIGHT_WOLF.name
        val preset = WolfThemePreset.entries.firstOrNull { it.name == presetName } ?: WolfThemePreset.MIDNIGHT_WOLF
        UserPreferences(
            studentName = prefs[Keys.STUDENT_NAME] ?: "Scholar",
            academicTrack = prefs[Keys.ACADEMIC_TRACK] ?: "Personal Study Hub",
            preferredLanguage = language,
            darkThemeEnabled = prefs[Keys.DARK_THEME] ?: true,
            themePreset = preset,
            appLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
            privacyPinHash = prefs[Keys.PRIVACY_PIN_HASH] ?: "",
            hideProfileInBackup = prefs[Keys.HIDE_PROFILE_IN_BACKUP] ?: false,
            lastBackupTimestamp = prefs[Keys.LAST_BACKUP_TIMESTAMP] ?: 0L
        )
    }

    suspend fun updateProfile(name: String, track: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STUDENT_NAME] = name.trim().ifEmpty { "Scholar" }
            prefs[Keys.ACADEMIC_TRACK] = track.trim().ifEmpty { "Personal Study Hub" }
        }
    }

    suspend fun setLanguage(language: StudyLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PREFERRED_LANGUAGE] = language.name
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = enabled
        }
    }

    suspend fun setThemePreset(preset: WolfThemePreset) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_PRESET] = preset.name
        }
    }

    suspend fun setPrivacyPin(pin: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            if (pin.isNotBlank()) {
                prefs[Keys.PRIVACY_PIN_HASH] = hashPin(pin)
            }
            prefs[Keys.APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun disableAppLock() {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_LOCK_ENABLED] = false
        }
    }

    suspend fun setHideProfileInBackup(hide: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HIDE_PROFILE_IN_BACKUP] = hide
        }
    }

    suspend fun updateLastBackupTimestamp(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_BACKUP_TIMESTAMP] = timestamp
        }
    }

    fun verifyPin(candidate: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return true
        return hashPin(candidate) == storedHash
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
