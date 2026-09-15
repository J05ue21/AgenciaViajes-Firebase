package com.dsm.agenciaviajes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmailRegister)
        val etPassword = findViewById<EditText>(R.id.etPasswordRegister)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvIrALogin = findViewById<TextView>(R.id.tvIrALogin)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Ingrese su correo electrónico"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Ingrese su contraseña"
                etPassword.requestFocus()
                return@setOnClickListener
                }

            if (password.length < 6) {
                etPassword.error = "La contraseña debe tener al menos 6 caracteres"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Las contraseñas no coinciden"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            // Registro con Firebase Authentication [Email + Password]
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Registro exitoso",
                            Toast.LENGTH_SHORT
                        ).show()

                        irAMainActivity()

                    } else {
                        Toast.makeText(
                            this,
                            "Error en el registro",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }

        tvIrALogin.setOnClickListener {
        finish()    // Regresa a la actividad anterior [LoginActivity]
                    /** startActivity(Intent(this, LoginActivity::class.java))*/
        }

    }
    private fun irAMainActivity() {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
    }
}