package com.swordfish.lemuroid.app.utils

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

object RemoteLogger {
    private const val TAG = "RemoteLogger"
    private const val TARGET_IP = "192.168.68.212"
    private const val TARGET_PORT = 5005
    private var isStarted = false

    fun start() {
        if (isStarted) return
        isStarted = true

        thread(isDaemon = true) {
            try {
                val socket = DatagramSocket()
                val address = InetAddress.getByName(TARGET_IP)
                
                Runtime.getRuntime().exec("logcat -c").waitFor()
                val process = Runtime.getRuntime().exec("logcat -v time")
                val reader = process.inputStream.bufferedReader()
                
                Log.d(TAG, "RemoteLogger starting to stream logcat to $TARGET_IP:$TARGET_PORT")
                
                while (true) {
                    val line = reader.readLine() ?: break
                    val packetData = (line + "\n").toByteArray(Charsets.UTF_8)
                    val packet = DatagramPacket(packetData, packetData.size, address, TARGET_PORT)
                    socket.send(packet)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in RemoteLogger", e)
            }
        }
    }
}
