package com.betpro.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.betpro.android.domain.model.AppSettings
import com.betpro.android.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "betpro_settings")

class SettingsRepositoryImpl @Inject constructor(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val IS_SIMULATION_MODE = booleanPreferencesKey("is_simulation_mode")
        val IS_AI_ENGINE_ENABLED = booleanPreferencesKey("is_ai_engine_enabled")
        val BASE_STAKE = doublePreferencesKey("base_stake")
        val STOP_LOSS = doublePreferencesKey("stop_loss")
        val VIRTUAL_BALANCE = doublePreferencesKey("virtual_balance")
    }

    override val appSettingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppSettings(
                isSimulationMode = preferences[PreferencesKeys.IS_SIMULATION_MODE] ?: true,
                isAIEngineEnabled = preferences[PreferencesKeys.IS_AI_ENGINE_ENABLED] ?: true,
                baseStake = preferences[PreferencesKeys.BASE_STAKE] ?: 10.0,
                stopLossThreshold = preferences[PreferencesKeys.STOP_LOSS] ?: 100.0,
                virtualBalance = preferences[PreferencesKeys.VIRTUAL_BALANCE] ?: 1250.0
            )
        }

    override suspend fun updateSimulationMode(isSimulation: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SIMULATION_MODE] = isSimulation
        }
    }

    override suspend fun updateAIEngine(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AI_ENGINE_ENABLED] = isEnabled
        }
    }

    override suspend fun updateBaseStake(stake: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BASE_STAKE] = stake
        }
    }

    override suspend fun updateStopLoss(stopLoss: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STOP_LOSS] = stopLoss
        }
    }

    override suspend fun updateVirtualBalance(newBalance: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIRTUAL_BALANCE] = newBalance
        }
    }
}
