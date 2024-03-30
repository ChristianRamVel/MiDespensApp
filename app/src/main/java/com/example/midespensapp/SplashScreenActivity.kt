package com.example.midespensapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT = 2000 // Tiempo en milisegundos (2 segundos en este ejemplo)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            // Comprobar si el usuario está autenticado
            if (FirebaseAuth.getInstance().currentUser != null) {
                // El usuario está autenticado, iniciar MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // El usuario no está autenticado, iniciar LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
            }
            // Finalizar la actividad actual
            finish()
        }, SPLASH_TIME_OUT.toLong())
    }
}
