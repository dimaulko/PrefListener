package com.uds_improveit.preflistener

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.uds_improveit.preflistener.Logger.logD
import com.uds_improveit.preflistener.Logger.logW
import com.uds_improveit.preflistener.datasource_processor.DataSourceProcessor
import com.uds_improveit.preflistener.datasource_processor.SharedPreferencesProcessor
import com.uds_improveit.preflistener.sql.DataUpdateEvent
import com.uds_improveit.preflistener.sql.PrefLoaderDatabase
import com.uds_improveit.preflistener.sql.toDTO
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors

const val PORT = 55690
const val development = true

object PrefListener {

    //--- manageSDK
    private var sdkInited = false
    private var deviceID: String = ""
    private var connectionIP: String = ""

    private var prefUtil: PrefUtil? = null

    private var networkStateHolder: NetworkMonitoringUtil? = null
    private var executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope =
        CoroutineScope(SupervisorJob() + executor + CoroutineExceptionHandler { _, exception ->
            println(exception.stackTraceToString())
        })

    //--- App var's
    private var appContext: Context? = null
    val isDebuggable: Boolean
        get() {
            return appContext?.let {
                0 != it.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
            }?.run {
                false
            }!!
        }


    // database
    private var database: PrefLoaderDatabase? = null

    fun init(
        _context: Context
    ) {

        if (sdkInited) {
            logD("You can't reInit PrefListener")
            return
        }

        this.appContext = _context.applicationContext
        this.prefUtil = PrefUtil(_context)

        database = PrefLoaderDatabase.getDatabase(_context)

        networkStateHolder = NetworkMonitoringUtil(appContext!!).apply {
            registerNetworkCallbackEvents()
            checkNetworkState()
        }

        networkStateHolder?.checkNetworkState()

        scope.launch {
            networkStateHolder?.networkState?.collectLatest { isConnected ->
                logW("Network state ${isConnected}")
                if (isConnected) {
                    connectFromNetworkStateChange()
                }
            }
        }

        connectionIP = prefUtil?.ip.orEmpty()
        deviceID = prefUtil?.deviceId.orEmpty()

        SharedPreferencesProcessor.setWorkerScope(scope)
        DataSourceProcessor.setWorkerScope(scope)
        SharedPreferencesProcessor.setUpdateDataListener(::saveNewEvent)
        DataSourceProcessor.setUpdateDataListener(::saveNewEvent)

        scope.launch {

            database?.let { _database ->
                combine(
                    _database.userDao().getLastEvent().distinctUntilChanged(),
                    NativeSocket.socketState
                ) { dataList, networkState ->
                    dataList to networkState
                }.collect { data ->
                    println("distinctUntilChanged:  ${data}")
                    data.first?.let { it ->
                        if (sendMessageNativeThread(it.toDTO())) {
                            deleteEventFromDB(it)
                        }
                    }
                }
            }
        }

        sdkInited = true
    }



    private fun deleteEventFromDB(event: DataUpdateEvent) {
        scope.launch {
            delay(1500)
            database?.userDao()?.deleteEvent(event)
        }
    }

    fun connectFromReceiver(deviceID: String, ip: String? = "") {

        this.deviceID = deviceID
        prefUtil?.deviceId = deviceID

        ip?.trim()!!.let {
            connectionIP = it
            prefUtil?.ip = it
        }

        runSocket()
    }

    private fun connectFromNetworkStateChange() {
        runSocket()
    }

    fun addSharedPreferencesSource(fileName: String) {
        SharedPreferencesProcessor.setSourceFileName(context = appContext!!, filename = fileName)
    }

    fun addDatastoreSource(dataStoreAliasName: String, dataStore: DataStore<Preferences>) {
        DataSourceProcessor.setDataStore(
            datastore = dataStore,
            aliasSourceName = dataStoreAliasName
        )
    }

    private fun runSocket() {

        if (deviceID.isEmpty()) {
            logW("connectFromNetworkStateChange deviceID is empty")
            return
        }

        if (connectionIP.isEmpty()) {
            logW("IP is empty, can't run socket")
            return
        }

        if (networkStateHolder?.checkNetworkState() == false) {
            Logger.logD("startSocket: networkStatus ${networkStateHolder?.networkState?.value} return")
            return
        }

        NativeSocket.createSocket(connectionIP, PORT, ::logW) {
            logD("startSocket: Socket is starting")
        }
    }

    private fun sendMessageNativeThread(message: Builder.UpdatesObject): Boolean {
        if (NativeSocket.socketState.value != SocketState.CONNECTED) {
            logD("-----socketClient not connected -----")
            return false
        }

        val isSuccess = NativeSocket.sendMessage(
            Json.encodeToString(
                message.copy(
                    deviceId = deviceID,
                    project = appContext?.packageName.orEmpty()
                )
            )
        )

        return isSuccess
    }

    private fun saveNewEvent(message: Builder.UpdatesObject) {

        if (message.data.isEmpty()) {
            return
        }

        scope.launch {
//            (0..5).forEach {
                database?.userDao()?.insertEvent(
                    DataUpdateEvent(
                        id = null,
                        sourceName = message.sourceName,
                        sourceType = message.source,
                        data = message.data.toString(),
                        timestamp = message.timestamp,
                        reConnection = false
                    )
                )
            }
//        }
    }


}
