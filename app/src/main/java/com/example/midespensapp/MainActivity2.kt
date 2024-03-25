package com.example.midespensapp

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
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
        val etCantidadMinimaProducto = findViewById<EditText>(R.id.cantidadMinimaStock)
        val etCantidadActualProducto = findViewById<EditText>(R.id.cantidadActual)
        val etCantidadAComprar = findViewById<EditText>(R.id.cantidadAComprar)
        val botonRegistrar = findViewById<Button>(R.id.botonRegistrarEnDespensa)
        val checkbox = findViewById<CheckBox>(R.id.checkBox)

        checkbox.setOnClickListener {
            if (checkbox.isChecked) {
                etCantidadAComprar.visibility = EditText.VISIBLE
            } else {
                etCantidadAComprar.text.clear()
                etCantidadAComprar.visibility = EditText.GONE
            }
        }

        botonRegistrar.setOnClickListener {
            val nombreProducto = etNombreProducto.text.toString()
            val cantidadMinimaProducto = etCantidadMinimaProducto.text.toString().toInt()
            val cantidadActualProducto = etCantidadActualProducto.text.toString().toInt()
            val cantidadAComprar = etCantidadAComprar.text.toString().toInt()

            when {
                nombreProducto.isEmpty() ->{
                    etNombreProducto.error = "El nombre del producto no puede estar vacío"
                    Toast.makeText(this, "El nombre del producto no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
                cantidadMinimaProducto == 0 -> {
                    etCantidadMinimaProducto.error = "La cantidad mínima no puede ser 0"
                    Toast.makeText(this, "La cantidad mínima no puede ser 0", Toast.LENGTH_SHORT).show()
                }
                cantidadActualProducto == 0 -> {
                    etCantidadActualProducto.error = "La cantidad actual no puede ser 0"
                    Toast.makeText(this, "La cantidad actual no puede ser 0", Toast.LENGTH_SHORT).show()
                }
                cantidadAComprar == 0 && checkbox.isChecked -> {
                    etCantidadAComprar.error = "La cantidad a comprar no puede ser 0"
                    Toast.makeText(this, "La cantidad a comprar no puede ser 0", Toast.LENGTH_SHORT).show()
                }
                cantidadMinimaProducto > (cantidadActualProducto + cantidadAComprar) -> {
                    etCantidadMinimaProducto.error = "La cantidad mínima no puede ser mayor que la cantidad actual y la cantidad a comprar"
                    Toast.makeText(this, "La cantidad mínima no puede ser mayor que la cantidad actual y la cantidad a comprar", Toast.LENGTH_SHORT).show()
                }
            }
            val dbHelper = DbHelper(this)
            val producto = Producto(0, nombreProducto, cantidadMinimaProducto, cantidadActualProducto, cantidadAComprar)
            dbHelper.addProducto(producto)

            finish()
        }
    }

}