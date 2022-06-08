package com.example.appportfolio.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("my_data_store")
class UserPreferences(
    context: Context
) {
    private val applicationContext=context.applicationContext
    //

    val authToken: Flow<String?>
    get()= applicationContext.dataStore.data.map{preferences->
        preferences[KEY_AUTH]
    }
    suspend fun saveAuthToken(authToken:String){
        applicationContext.dataStore.edit{ preferences->
            preferences[KEY_AUTH]=authToken
        }
    }
    companion object{
        private val KEY_AUTH= stringPreferencesKey("key_auth")
    }
}

