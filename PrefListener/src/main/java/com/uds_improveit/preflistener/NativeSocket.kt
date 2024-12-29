package com.uds_improveit.preflistener

import com.uds_improveit.preflistener.Logger.logD
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.Socket
import kotlin.concurrent.thread

object NativeSocket {
    private var socket: Socket? = null
    private var socketThread: Thread? = null
    private var onSocketCreated = {}

    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var _socketState = MutableStateFlow(SocketState.IDLE)
    val socketState = _socketState.asStateFlow()

    fun createSocket(
        ip: String, port: Int, listener: () -> Unit
    ): Socket? {
        if (_socketState.value in listOf(SocketState.CONNECTING, SocketState.CONNECTED)) {
//            logD("socketState: ${socketState}, return")
            return null
        }

        onSocketCreated = listener
//        logD("start socket creating")
        socketThread = thread {
            try {
                _socketState.tryEmit(SocketState.CONNECTING)
                socket = Socket(ip, port)
                outputStream = socket!!.getOutputStream()
                inputStream = socket!!.getInputStream()
//                logD("Socket created")
                _socketState.tryEmit(SocketState.CONNECTED)
                onSocketCreated.invoke()
                while (true) {
                    val inputStreamReader = InputStreamReader(inputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    val Response = bufferedReader.readLine()

                }
            } catch (e: Exception) {
                socket = null
                _socketState.tryEmit(SocketState.ERROR)
                logD(e.stackTraceToString())
            }
        }
//        logD("end socket creation")
        return socket
    }

    fun stopSocket() {
        try {
            socket?.close()
            socketThread?.interrupt()
            socket = null
            socketThread = null
        } catch (e: Exception) {

//            logD("Stop socket/thread exception")
//            logD("Exception: ${e.stackTraceToString()}")
        } finally {
            _socketState.tryEmit(SocketState.CLOSED)
        }
    }

    fun sendMessage(message: String): Boolean {
        try {
            if (outputStream == null) {
                logD("-----NativeSocket sendMessage outputStream is null-----")
                return false
            }
            outputStream?.let {
                val outputStreamWriter = OutputStreamWriter(outputStream)
                val bufferedWriter = BufferedWriter(outputStreamWriter)
                bufferedWriter.write(message.plus("\n"))
                bufferedWriter.flush()
//                logD("-----NativeSocket sendMessage message sent-----")
                return true
            }
        } catch (e: Exception) {
//            logD(e.stackTraceToString())
        }
        return false
    }
}

enum class SocketState {
    IDLE,
    CONNECTING,
    CONNECTED,
    ERROR,
    CLOSED
}
