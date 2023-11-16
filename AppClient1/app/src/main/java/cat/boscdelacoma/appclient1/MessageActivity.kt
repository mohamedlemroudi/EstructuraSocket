package cat.boscdelacoma.appclient1

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class MessageActivity : AppCompatActivity() {
    private lateinit var server: Socket
    private lateinit var reader: BufferedReader
    private lateinit var writer: BufferedWriter
    private lateinit var messageEditText: EditText
    private lateinit var chatTextView: TextView
    private lateinit var myApp: MyApp
    // En tu actividad principal (MessageActivity)
    private lateinit var responseThread: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectNetwork()
                .penaltyLog()
                .build()
        )

        myApp = application as MyApp

        val sendButton = findViewById<Button>(R.id.sendButton)
        messageEditText = findViewById(R.id.messageEditText)
        chatTextView = findViewById(R.id.chatTextView)

        sendButton.setOnClickListener {
            SendMessageTask().execute()
        }

        // Inicia un hilo para escuchar las respuestas del servidor en segundo plano
        responseThread = Thread {
            while (true) {
                try {
                    val response = myApp.receiveMessage()

                    if (response != null) {

                        try {
                            val jsonObject = JSONObject(response)
                            val content = jsonObject.getString("content")

                            // Dividir la cadena de contenido utilizando el delimitador ";"
                            val parts = content.split(";")

                            if (parts.size == 3) {
                                val sender = parts[0]
                                val receiver = parts[1]
                                val message = parts[2]

                                // Procesa la respuesta y actualiza la interfaz de usuario según sea necesario
                                runOnUiThread {
                                    updateChatView(message)
                                }

                                // Aquí tienes las partes individuales del mensaje
                                println("Sender: $sender, Receiver: $receiver, Message: $message")
                            } else {
                                // La cadena de contenido no tiene el formato esperado
                                println("Formato de contenido incorrecto.")
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            // Error al analizar JSON
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        responseThread.start()
    }

    private fun updateChatView(message: String) {
        val currentText = chatTextView.text.toString()
        val newText = "$currentText\n$message"
        chatTextView.text = newText
    }

    inner class SendMessageTask : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String {
            return try {
                val message = messageEditText.text.toString()
                myApp.sendMessage("MESSAGE:moha;ayoub;$message")

                //val response = myApp.receiveMessage()

                //response ?: "No response from server"

                "Message sent"
            } catch (e: IOException) {
                e.printStackTrace()
                "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            updateChatView(result)
            messageEditText.text.clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detén el hilo de respuesta al destruir la actividad
        responseThread.interrupt()
    }
}