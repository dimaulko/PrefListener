package com.uds_improveit.preflistener.datasource_processor

import android.content.Context
import android.content.SharedPreferences
import com.uds_improveit.preflistener.Builder
import kotlinx.coroutines.CoroutineScope

object SharedPreferencesProcessor : DataHandlerProcessor {

    private var onDataUpdate: ((data: Builder.UpdatesObject) -> Unit)? = null
    private val mapSharedPreferences = mutableMapOf<String, SharedPreferences>()
    private var listListener = mutableListOf<SharedPreferencesListener>()

    fun setSourceFileName(context: Context, filename: String) {

        if (filename.isBlank()) {
            println("Filename can't be empty")
            return
        }

        if (mapSharedPreferences.contains(filename)) {
            println("Always listened")
            return
        }

        val prefListener = SharedPreferencesListener(filename)
        listListener.add(prefListener)

        context.getSharedPreferences(filename, Context.MODE_PRIVATE)?.let {
            it.registerOnSharedPreferenceChangeListener(
                prefListener
            )
            println("register listener for filename: $filename")
            mapSharedPreferences[filename] = it
        }
    }

    override fun setUpdateDataListener(listener: (data: Builder.UpdatesObject) -> Unit) {
        onDataUpdate = listener
    }

    override fun setWorkerScope(scope: CoroutineScope) {
        // no need implementation
    }

    private fun buildAndSendPrefData(
        pref: SharedPreferences, key: String?, sourceName: String, isReconnection: Boolean = false
    ) {
        val message = Builder(
            source = Builder.SOURCE.SHARED_PREFERENCES,
            sourceName = sourceName,
            reConnection = isReconnection
        ).also { builder ->
            if (isReconnection) {
                pref.all.forEach { entry -> builder.putValue(entry.key, entry.value) }
            } else {
                key?.let {
                    if (pref.contains(key)) {
                        builder.putValue(key, pref.all[key])
                    } else {
                        builder.putValue(key, null)
                    }
                }
            }
        }.build()
        onDataUpdate?.invoke(message)
    }

    private class SharedPreferencesListener(private val sourceName: String) :
        SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences, key: String?
        ) {
            println("PrefLoaderSharedPreferencesListener: onSharedPreferenceChanged")
            sharedPreferences?.let {
                buildAndSendPrefData(
                    pref = it, key = key, sourceName = sourceName, false
                )
            }
        }
    }
}