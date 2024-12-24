package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.uds_improveit.preflistener.PrefListener
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : ComponentActivity() {

    private val TAG: String = "STACKSTACK"
    private val TAGListener: String = "STACKSTACK_TAGListener"
//    private val TAGListener: String = "MainListener"

    var count = AtomicInteger()
    var counter = 0
    var initBoolValue = false

    private var executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope =
        CoroutineScope(SupervisorJob() + executor + CoroutineExceptionHandler { _, exception ->
            println(exception.stackTraceToString())
        })

    private var dataStore: DataStore<Preferences>? =
        null//= createDataStore(this, "filename_data_store")
    private var dataStore1: DataStore<Preferences>? =
        null //= createDataStore(this, "filename_data_store_2")

    var pref: SharedPreferences? = null
    var pref2: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LazyColumn {
                    item {
                        Text(
                            text = "SharedPreferences",
                            modifier = Modifier.padding(start = 32.dp),
                            fontSize = 24.sp
                        )
                    }
                    item {
                        rowItem("Int++", { prefInt() }, "remove Int", { prefIntRemove() })
                    }
                    item {
                        rowItem(
                            "String++",
                            { prefString() },
                            "remove String",
                            { prefStringRemove() })
                    }
                    item {
                        rowItem("Long++", { prefLong() }, "remove Long", { prefLongRemove() })
                    }
                    item {
                        rowItem("Float++", { prefFloat() }, "remove Float", { prefFloatRemove() })
                    }
                    item {
                        rowItem(
                            "Boolean++",
                            { prefBoolean() },
                            "remove Boolean",
                            { prefBooleanRemove() })
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    item {
                        Text(
                            text = "DataStore",
                            modifier = Modifier.padding(start = 32.dp),
                            fontSize = 24.sp
                        )
                    }
                    item {
                        rowItem("Int++", { datastoreInt() }, "remove Int", { datastoreIntRemove() })
                    }
                    item {
                        rowItem(
                            "String++",
                            { datastoreString() },
                            "remove String",
                            { datastoreStringRemove() })
                    }
                    item {
                        rowItem(
                            "Long++",
                            { datastoreLong() },
                            "remove Long",
                            { datastoreLongRemove() })
                    }
                    item {
                        rowItem(
                            "Double++",
                            { datastoreDouble() },
                            "remove Double",
                            { datastoreDoubleRemove() })
                    }
                    item {
                        rowItem(
                            "Boolean++",
                            { datastoreBoolean() },
                            "remove Boolean",
                            { datastoreBooleanRemove() })
                    }
                    item {
                        Text(
                            text = "SharedPreferences 2",
                            modifier = Modifier.padding(start = 32.dp),
                            fontSize = 24.sp
                        )
                    }
                    item {
                        rowItem("Int++", { prefInt2() }, "remove Int", { prefIntRemove2() })
                    }
                    item {
                        rowItem(
                            "String++",
                            { prefString2() },
                            "remove String",
                            { prefStringRemove2() })
                    }
                    item {
                        rowItem("Long++", { prefLong2() }, "remove Long", { prefLongRemove2() })
                    }
                    item {
                        rowItem("Float++", { prefFloat2() }, "remove Float", { prefFloatRemove2() })
                    }
                    item {
                        rowItem(
                            "Boolean++",
                            { prefBoolean2() },
                            "remove Boolean",
                            { prefBooleanRemove2() })
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        pref = getSharedPreferences("filename", MODE_PRIVATE)
        pref2 = getSharedPreferences("filename_2", MODE_PRIVATE)

        PrefListener.addSharedPreferencesSource("filename")
        PrefListener.addSharedPreferencesSource("filename_2")

        dataStore?.let {
            PrefListener.addDatastoreSource(
                dataStore = it,
                dataStoreAliasName = "file_name_data_store"
            )
        }
    }

    private fun addNewDataStore() {
        val filename = "filename_" + count.addAndGet(1)
        val dataStore = createDataStore(this@MainActivity, filename)

        PrefListener.addDatastoreSource(
            dataStore = dataStore,
            dataStoreAliasName = filename
        )
        lifecycleScope.launch {
            dataStore.edit {
                it[intPreferencesKey("filename_KEY")] = count.addAndGet(5)
            }
        }
    }


    //------------------------
    private fun prefInt() {
        lifecycleScope.launch {
            pref?.let {
                Log.d(TAG, prefInt)
                it.edit().putInt(prefInt, count.addAndGet(1)).apply()
            }
        }
    }

    private fun prefIntRemove() {
        pref?.edit()?.remove(prefInt)?.apply()
    }

    private fun prefString() {
        lifecycleScope.launch {
            pref?.let {
                Log.d(TAG, prefString)
                val a = count.addAndGet(1)
                it.edit().putString(prefString, "String ${a}").apply()
            }
        }
    }

    private fun prefStringRemove() {
        pref?.edit()?.remove(prefString)?.apply()
    }

    private fun prefLong() {
        lifecycleScope.launch {
            pref?.let {
                Log.d(TAG, preflong)
                it.edit().putLong(preflong, count.addAndGet(1).toLong()).apply()
            }
        }
    }

    private fun prefLongRemove() {
        pref?.edit()?.remove(preflong)?.apply()
    }

    private fun prefFloat() {
        lifecycleScope.launch {
            pref?.let {
                Log.d(TAG, prefFloat)
                it.edit().putFloat(prefFloat, count.addAndGet(1).toFloat()).apply()
            }
        }
    }

    private fun prefFloatRemove() {
        pref?.edit()?.remove(prefFloat)?.apply()
    }

    private fun prefBoolean() {
        lifecycleScope.launch {
            pref?.let {
                Log.d(TAG, prefBoolean)
                it.edit().putBoolean(prefBoolean, (count.addAndGet(1) % 2 == 0)).apply()
            }
        }
    }

    private fun prefBooleanRemove() {
        pref?.edit()?.remove(prefBoolean)?.apply()
    }

    //------------------------  2
    private fun prefInt2() {
        lifecycleScope.launch {
            pref2?.let {
                Log.d(TAG, prefInt)
                it.edit().putInt(prefInt, count.addAndGet(1)).apply()
            }
        }
    }

    private fun prefIntRemove2() {
        pref2?.edit()?.remove(prefInt)?.apply()
    }

    private fun prefString2() {
        lifecycleScope.launch {
            pref2?.let {
                Log.d(TAG, prefString)
                val a = count.addAndGet(1)
                it.edit().putString(prefString, "String ${a}").apply()
            }
        }
    }

    private fun prefStringRemove2() {
        pref2?.edit()?.remove(prefString)?.apply()
    }

    private fun prefLong2() {
        lifecycleScope.launch {
            pref2?.let {
                Log.d(TAG, preflong)
                it.edit().putLong(preflong, count.addAndGet(1).toLong()).apply()
            }
        }
    }

    private fun prefLongRemove2() {
        pref2?.edit()?.remove(preflong)?.apply()
    }

    private fun prefFloat2() {
        lifecycleScope.launch {
            pref2?.let {
                Log.d(TAG, prefFloat)
                it.edit().putFloat(prefFloat, count.addAndGet(1).toFloat()).apply()
            }
        }
    }

    private fun prefFloatRemove2() {
        pref2?.edit()?.remove(prefFloat)?.apply()
    }

    private fun prefBoolean2() {
        lifecycleScope.launch {
            pref2?.let {
                Log.d(TAG, prefBoolean)
                it.edit().putBoolean(prefBoolean, (count.addAndGet(1) % 2 == 0)).apply()
            }
        }
    }

    private fun prefBooleanRemove2() {
        pref2?.edit()?.remove(prefBoolean)?.apply()
    }

    private fun datastoreInt() {
        if (dataStore == null) {
            dataStore = createDataStore(this, "file_name_data_store")
            PrefListener.addDatastoreSource(
                dataStore = dataStore!!,
                dataStoreAliasName = "file_name_data_store"
            )
            return
        }
        lifecycleScope.launch {
            dataStore?.edit {
                it[datastoreInt] = count.addAndGet(1)
            }
        }
    }

    private fun datastoreIntRemove() {
        lifecycleScope.launch {
            dataStore?.edit {
                it.remove(datastoreInt)
            }
        }
    }

    private fun datastoreString() {
        if (dataStore1 == null) {
            dataStore1 = createDataStore(this, "file_name_data_store1")
            PrefListener.addDatastoreSource(
                dataStore = dataStore1!!,
                dataStoreAliasName = "file_name_data_store1"
            )
            return
        }
        lifecycleScope.launch {
            dataStore1?.edit {
                val a = count.addAndGet(1)
                it[datastoreString] = "String ${a}"
            }
        }
    }

    private fun datastoreStringRemove() {
        lifecycleScope.launch {
            dataStore?.edit {
                it.remove(datastoreString)
            }
        }
    }

    private fun datastoreLong() {
        lifecycleScope.launch {
            dataStore?.edit {
                it[datastoreLong] = count.addAndGet(1).toLong()
            }
        }
    }

    private fun datastoreLongRemove() {
        lifecycleScope.launch {
            dataStore?.edit {
                it.remove(datastoreLong)
            }
        }
    }

    private fun datastoreDouble() {
        lifecycleScope.launch {
            dataStore?.edit {
                it[datastoreFloat] = count.addAndGet(1).toFloat()
            }
        }
    }

    private fun datastoreDoubleRemove() {
        lifecycleScope.launch {
            dataStore?.edit {
                it.remove(datastoreFloat)
            }
        }
    }

    private fun datastoreBoolean() {
        lifecycleScope.launch {
            dataStore?.edit {
                it[datastoreBoolean] = (count.addAndGet(1)) % 2 == 0
            }
        }
    }

    private fun datastoreBooleanRemove() {
        lifecycleScope.launch {
            dataStore?.edit {
                it.remove(datastoreBoolean)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}

//@Preview(showBackground = true)
@Composable
fun mainPreview() {
    MyApplicationTheme {
        mainScreen()
    }
}

private val prefInt = "INT_SHARED"
private val prefString = "STRING_SHARED"
private val prefBoolean = "BOOLEAN_SHARED"
private val preflong = "LONG_SHARED"
private val prefFloat = "DOUBLE_SHARED"

private val datastoreInt = intPreferencesKey("INT_DATASTORE")
private val datastoreString = stringPreferencesKey("STRING_DATASTORE")
private val datastoreBoolean = booleanPreferencesKey("BOOLEAN_DATASTORE")
private val datastoreLong = longPreferencesKey("LONG_DATASTORE")
private val datastoreFloat = floatPreferencesKey("FLOAT_DATASTORE")
private val datastoreDouble = doublePreferencesKey("DOUBLE_DATASTORE")

private fun createDataStore(context: Context, filename: String) =
    PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile(filename)
    }


@Composable
@Preview(showBackground = true)
fun mainScreen() {
    Column {
        LazyColumn {
            item {
                Text(
                    text = "SharedPreferences",
                    modifier = Modifier.padding(start = 32.dp),
                    fontSize = 24.sp
                )
            }
            item {
                rowItem("Int++", {}, "remove Int", {})
            }
            item {
                rowItem("String++", {}, "remove String", {})
            }
            item {
                rowItem("Long++", {}, "remove Long", {})
            }
            item {
                rowItem("Double++", {}, "remove Double", {})
            }
            item {
                rowItem("Boolean++", {}, "remove Boolean", {})
            }
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                Text(
                    text = "DataStore",
                    modifier = Modifier.padding(start = 32.dp),
                    fontSize = 24.sp
                )
            }
            item {
                rowItem("Int++", {}, "remove Int", {})
            }
            item {
                rowItem("String++", {}, "remove String", {})
            }
            item {
                rowItem("Long++", {}, "remove Long", {})
            }
            item {
                rowItem("Double++", {}, "remove Double", {})
            }
            item {
                rowItem("Boolean++", {}, "remove Boolean", {})
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun rowItem(
    leftButtonLabel: String = "left Button",
    leftButtonOnClick: () -> Unit = {},
    rightButtonLabel: String = "right button",
    rightButtonOnClick: () -> Unit = {}
) {
    Column {
        Spacer(modifier = Modifier.height(5.dp))
        Row(modifier = Modifier.padding(all = 8.dp)) {
            Button(onClick = leftButtonOnClick, modifier = Modifier.weight(1f)) {
                Text(text = leftButtonLabel)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = rightButtonOnClick, modifier = Modifier.weight(1f)) {
                Text(text = rightButtonLabel)
            }
        }
    }
}