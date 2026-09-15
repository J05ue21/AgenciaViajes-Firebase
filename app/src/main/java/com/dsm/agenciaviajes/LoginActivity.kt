package com.dsm.agenciaviajes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmailLogin)
        val etPassword = findViewById<EditText>(R.id.etPasswordLogin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvIrARegistro = findViewById<TextView>(R.id.tvIrARegistro)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty())    {
                etEmail.error = "Ingrese su correo electrónico"
                etEmail.requestFocus()  // coloca el cursor en el campo de texto
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Ingrese su contraseña"
                etPassword.requestFocus()
                return@setOnClickListener
            }
            //Autenticacion con Firebase [Email + PAssword]
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Bienvenido/a",
                            Toast.LENGTH_SHORT)
                            .show()
                        irAMainActivity()

                    } else {
                        Toast.makeText(
                            this,
                            "Error en el inicio de sesión",
                            Toast.LENGTH_SHORT)
                            .show()
                    }

            }

    }
        tvIrARegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
    override fun onStart()
    {
        super.onStart()
        // Si el usuario ya está autenticado, se redirige a la actividad principal [MainActivity]
        if (auth.currentUser != null) {
            irAMainActivity()
        }
    }
    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)

        // Para que el ususario silvestre no intente volver a la pantalla de login al presionar "Atrás"
        // limpiamos el Stack
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}