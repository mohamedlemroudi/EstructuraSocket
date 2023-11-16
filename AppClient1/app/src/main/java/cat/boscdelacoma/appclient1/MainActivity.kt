package cat.boscdelacoma.appclient1

import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var myApp: MyApp
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myApp = application as MyApp

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectNetwork()
                .penaltyLog()
                .build()
        )

        val signInButton = findViewById<Button>(R.id.buttonSignIn)
        usernameEditText = findViewById(R.id.editTextUsername)
        passwordEditText = findViewById(R.id.editTextPassword)

        signInButton.setOnClickListener {
            try {
                val username = usernameEditText.text.toString()
                val password = passwordEditText.text.toString()

                // Iniciar una tarea asincrónica para la conexión y el inicio de sesión
                SignInTask().execute(username, password)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // AsyncTask para manejar la conexión y el inicio de sesión en segundo plano
    inner class SignInTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String): String {
            return try {
                val username = params[0]
                val password = params[1]

                myApp.connectToServer()
                myApp.sendMessage("SIGNIN: $username;$password")

                val response = myApp.receiveMessage()

                //myApp.closeConnection()

                response ?: "No response from server"
            } catch (e: IOException) {
                e.printStackTrace()
                "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            handleServerResponse(result)
        }
    }

    private fun handleServerResponse(response: String) {
        when (response) {
            "SIGNIN_FEEDBACK" -> {
                // Crear un Intent para iniciar la actividad de chat
                val chatIntent = Intent(this, MessageActivity::class.java)
                // Puedes pasar información adicional al Intent, por ejemplo, el nombre de usuario
                chatIntent.putExtra("USERNAME", usernameEditText.text.toString())
                // Iniciar la actividad de chat
                startActivity(chatIntent)
            }
            else -> {
                // Maneja otras respuestas según sea necesario
                showAlertDialog("Error", "Failed to sign in. Please try again.")
            }
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog: DialogInterface?, _: Int ->
                dialog?.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}
