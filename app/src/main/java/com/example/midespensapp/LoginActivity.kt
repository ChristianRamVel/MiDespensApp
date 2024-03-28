package com.example.midespensapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.midespensapp.DB.ComprobarSiCasaExisteCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.clases.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseError
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var realTimeManager = RealTimeManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        super.onStart()
        auth = Firebase.auth

        val logout =
            intent.getBooleanExtra("logout", false) // Verificamos si venimos de cerrar sesión

        // Limpiar el historial de actividades
        if (logout) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Verificar si hay un usuario autenticado
        val currentUser = auth.currentUser
        if (currentUser != null && !logout) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        val etEmail = findViewById<EditText>(R.id.email_edit_text)
        val etPassword = findViewById<EditText>(R.id.password_edit_text)
        val btnSingIn = findViewById<Button>(R.id.email_sign_in_button)
        val btnCreateAccount = findViewById<Button>(R.id.btnRegistrarse)
        val btnRegistroEnCasaExistente = findViewById<Button>(R.id.btnRegistrarseEnCasaExistente)

        btnSingIn.setOnClickListener {
            if (etEmail.text.isNotEmpty() && etPassword.text.isNotEmpty()) {
                singIn(etEmail.text.toString(), etPassword.text.toString())
            } else {
                Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        btnRegistroEnCasaExistente.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Introduce el ID de la casa")
                .setView(R.layout.dialog_insert_casa_id)
                .setPositiveButton("Aceptar") { dialog, _ ->
                    val etCasaId = (dialog as AlertDialog).findViewById<EditText>(R.id.etCasaId)
                    if (etCasaId != null && etCasaId.text.isNotEmpty()) {
                        val casaId = etCasaId.text.toString()
                        realTimeManager.comprobarSiCasaExistePorIDCasa(
                            casaId,
                            object : ComprobarSiCasaExisteCallBack {
                                override fun onCasaExiste(existe: Boolean) {
                                    if (existe) {
                                        if (camposValidos(
                                                etEmail.text.toString(),
                                                etPassword.text.toString()
                                            )
                                        ) {
                                            createAccount(
                                                etEmail.text.toString(),
                                                etPassword.text.toString(),
                                                casaId
                                            ) { success ->
                                                if (success) {
                                                    // Creación de cuenta exitosa, ahora guardar usuario
                                                    realTimeManager.guardarUsuario(
                                                        etEmail.text.toString(),
                                                        casaId
                                                    )
                                                    // Actualizar la interfaz de usuario
                                                    updateUI(Firebase.auth.currentUser)
                                                } else {
                                                    // Manejar caso de fallo en la creación de cuenta
                                                    mostrarMensaje("Error al crear la cuenta.")
                                                }
                                            }

                                        } else {
                                            mostrarMensaje("Por favor, rellene todos los campos.")
                                        }
                                    } else {
                                        mostrarMensaje("La casa no existe.")
                                    }
                                }

                                override fun onCasaNoExiste(error: Boolean) {
                                    mostrarMensaje("La casa no existe.")
                                }

                                override fun onError(error: DatabaseError) {
                                    mostrarMensaje("Error al comprobar si la casa existe.")
                                }
                            })

                    } else {
                        mostrarMensaje("Por favor, rellene todos los campos.")
                    }
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            updateUI(Firebase.auth.currentUser)
        }



        btnCreateAccount.setOnClickListener {
            if (etEmail.text.isNotEmpty() && etPassword.text.isNotEmpty()) {
                createAccount(etEmail.text.toString(), etPassword.text.toString()) { success ->
                    if (success) {
                        // Creación de cuenta exitosa, ahora guardar usuario
                        realTimeManager.guardarUsuario(
                            etEmail.text.toString(),
                            realTimeManager.crearCasaNueva().id!!
                        )
                        // Actualizar la interfaz de usuario
                        updateUI(Firebase.auth.currentUser)
                    } else {
                        // Manejar caso de fallo en la creación de cuenta
                        mostrarMensaje("Error al crear la cuenta.")
                    }
                }

                updateUI(Firebase.auth.currentUser)
            } else {
                Toast.makeText(
                    this,
                    "Por favor, rellene todos los campos",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }


    // Función para validar los campos de correo electrónico y contraseña
    private fun camposValidos(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    // Función para mostrar mensajes Toast
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this@LoginActivity, mensaje, Toast.LENGTH_SHORT).show()
    }


    fun createAccount(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val casaNueva = realTimeManager.crearCasaNueva()

                    realTimeManager.guardarUsuario(email, casaNueva.id!!)

                    callback(true) // Notificar éxito al callback
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "short password or email already in use.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    callback(false) // Notificar fallo al callback
                }
            }
    }

    fun createAccount(
        email: String,
        password: String,
        casaExistente: String,
        callback: (Boolean) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")

                    realTimeManager.guardarUsuario(email, casaExistente)

                    callback(true) // Notificar éxito al callback
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "short password or email already in use.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    callback(false) // Notificar fallo al callback
                }
            }
    }

    fun singIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }


    fun updateUI(user: Any?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}

