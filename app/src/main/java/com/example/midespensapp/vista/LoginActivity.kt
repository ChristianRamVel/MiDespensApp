package com.example.midespensapp.vista

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.midespensapp.R
import com.example.midespensapp.controlador.ComprobarCasaExisteCallBack
import com.example.midespensapp.controlador.CrearCasaCallBack
import com.example.midespensapp.controlador.RealTimeManager
import com.example.midespensapp.modelo.Casa
import com.example.midespensapp.modelo.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var realTimeManager: RealTimeManager
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference
    private lateinit var gson: Gson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        realTimeManager = RealTimeManager()
        database = FirebaseDatabase.getInstance()
        gson = Gson()
        usersRef = database.getReference("usuarios")

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
                signIn(etEmail.text.toString(), etPassword.text.toString())
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
                        val idCasa = etCasaId.text.toString().trim()
                        checkIfCasaExists(
                            idCasa,
                            etEmail.text.toString(),
                            etPassword.text.toString()
                        )
                    } else {
                        Toast.makeText(
                            this,
                            "Por favor, rellene todos los campos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        btnCreateAccount.setOnClickListener {
            createAccount(etEmail.text.toString(), etPassword.text.toString())
        }
    }

    private fun signIn(email: String, password: String) {

        if (camposValidos(email, password)) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        Toast.makeText(
                            this,
                            "Error al iniciar sesión, revise los datos introducidos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        } else {
            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun createAccount(email: String, password: String) {
        if (camposValidos(email, password)) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        crearCasaNueva(object : CrearCasaCallBack {
                            override fun onCasaCreada(casa: Casa) {
                                addUserToDatabase(email, casa.id, userId.toString())
                                signIn(email, password)
                            }

                            override fun onError(error: Exception?) {
                                Log.e(TAG, "Error al crear casa", error)
                            }
                        })

                    } else {
                        //si la contraseña es menor de 6 caracteres salta un error
                        if (password.length < 6) {
                            Toast.makeText(
                                this,
                                "La contraseña debe tener al menos 6 caracteres",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(this, "Error al crear usuario", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
        } else {
            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT)
                .show()
        }

    }

    private fun createAccount(email: String, password: String, idCasa: String) {
        if (camposValidos(email, password)) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        addUserToDatabase(email, idCasa, userId.toString())
                        signIn(email, password)
                    } else {
                        Toast.makeText(this, "Error al crear usuario", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        } else {
            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT)
                .show()
        }

    }

    //funcion para crear una casa
    private fun crearCasaNueva(callback: CrearCasaCallBack) {
        realTimeManager.crearCasa(object : CrearCasaCallBack {
            override fun onCasaCreada(casa: Casa) {

                callback.onCasaCreada(casa)
            }

            override fun onError(error: Exception?) {
                Log.e(TAG, "Error al crear casa", error)
                Toast.makeText(this@LoginActivity, "Error al crear casa", Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    private fun addUserToDatabase(email: String, idCasa: String, userId: String) {
        // Crear un nuevo objeto Usuario y agregarlo a la base de datos utilizando el ID de usuario como clave
        val usuario = Usuario(email, idCasa)
        usersRef.child(userId).setValue(usuario)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al agregar usuario a la base de datos", e)
                Toast.makeText(this@LoginActivity, "Error al crear usuario", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun checkIfCasaExists(idCasa: String, email: String, password: String) {

        realTimeManager.comprobarCasaExiste(idCasa, object : ComprobarCasaExisteCallBack {
            override fun onCasaExiste() {
                    createAccount(email, password, idCasa)

            }

            override fun onCasaNoExiste() {
                Toast.makeText(
                    this@LoginActivity,
                    "La casa no existe, por favor, introduzca un ID de casa válido",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onError(error: Exception?) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error al verificar la casa.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    private fun camposValidos(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }


    private fun updateUI(user: Any?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
                .putExtra("user", gson.toJson(user))
            startActivity(intent)
        }
    }
}

