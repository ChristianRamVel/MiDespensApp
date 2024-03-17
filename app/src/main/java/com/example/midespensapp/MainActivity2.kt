package com.example.midespensapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.examenrecuperacion_crv.DB.DbHelper
import com.example.midespensapp.clases.Producto

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val etNombreProducto = findViewById<EditText>(R.id.nombreProducto)
        val etCantidadMinimaProducto = findViewById<EditText>(R.id.cantidadMinimaProducto)
        val etCantidadAComprar = findViewById<EditText>(R.id.cantidadAComprar)
        val botonRegistrar = findViewById<Button>(R.id.botonRegistrarEnDespensa)

        botonRegistrar.setOnClickListener {
            val nombreProducto = etNombreProducto.text.toString()
            val cantidadMinimaProducto = etCantidadMinimaProducto.text.toString().toInt()
            val cantidadAComprar = etCantidadAComprar.text.toString().toInt()

            val dbHelper = DbHelper(this)
            val producto = Producto(0, nombreProducto, cantidadMinimaProducto, 0, cantidadAComprar)
            dbHelper.addProducto(producto)

            finish()
        }
    }

}