package cat.boscdelacoma.appclient1

import android.app.Application
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class MyApp : Application() {
    private var server: Socket? = null
    private var writer: BufferedWriter? = null
    private lateinit var reader: BufferedReader

    fun connectToServer() {
        // Cambia la dirección IP y el puerto según tu configuración del servidor
        server = Socket("172.18.208.1", 2009)
        reader = BufferedReader(InputStreamReader(server!!.getInputStream()))
        writer = BufferedWriter(OutputStreamWriter(server!!.getOutputStream()))
    }

    fun sendMessage(message: String) {
        writer?.write("$message\r\n")
        writer?.flush()
    }

    fun receiveMessage(): String? {
        return reader?.readLine()
    }

    fun closeConnection() {
        server?.close()
    }
}
