package com.github.leadpogrommer.waker

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

class WakeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return
        val wp = WidgetPrefs.loadWidgetPref(context, id) ?: return

        (@SuppressLint("StaticFieldLeak")
        object : AsyncTask<String, Unit, Boolean>() {
            override fun doInBackground(vararg p0: String): Boolean {
                val mac = stringToMAC(p0[0])
                val broadcastAddresses = getBroadcastAddresses()
                val ff: Byte = 0xff.toByte()
                val payload = ByteArray(6 * 17) { ff }
                for (i in 1..16) {
                    mac.copyInto(payload, 6 * i)
                }

                val socket = DatagramSocket()
                var success = true
                for (dst in broadcastAddresses) {
                    val packet = DatagramPacket(payload, payload.size, dst, 9)
                    Thread.sleep(333)
                    try {
                        socket.send(packet)
                    } catch (e: IOException) {
                        Log.e(TAG, "Error waking ${wp.name} (${wp.mac})", e)
                        success = false
                    }
                }
                socket.close()
                return success
            }

            override fun onPostExecute(success: Boolean) {
                super.onPostExecute(success)
                Toast.makeText(
                    context,
                    context.getString(if (success) R.string.packet_sent else R.string.packet_error, wp.mac),
                    Toast.LENGTH_SHORT
                ).show()
            }

        }).execute(wp.mac!!)

    }

    companion object {
        private const val ACTION_WAKE = "com.github.leadpogrommer.waker.WAKE"
        private const val EXTRA_WIDGET_ID = "wid"
        private const val TAG = "WakeReceiver"

        fun getBroadcastAddresses(): MutableList<InetAddress> {
            val out = mutableListOf<InetAddress>()
            for (ni in NetworkInterface.getNetworkInterfaces()) {
                for (address in ni.interfaceAddresses) {
                    address.broadcast ?: continue
                    out.add(address.broadcast)
                }
            }
            return out
        }

        fun stringToMAC(str: String): ByteArray {
            val temp = str.toUpperCase().replace('-', ':').split(':')

            val out = ByteArray(6)
            for (i in 0 until 6) {
                out[i] = Integer.decode('#' + temp[i]).toByte()

            }
            return out
        }

        fun newIntent(ctx: Context, wid: Int): Intent {
            val i = Intent(ctx, WakeReceiver::class.java)
            i.action = ACTION_WAKE
            i.putExtra(EXTRA_WIDGET_ID, wid)
            return i
        }
    }

}
