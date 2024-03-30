package com.example.midespensapp

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.midespensapp.DB.RealTimeManager

class MainActivity2 : AppCompatActivity() {

    private val realTimeManager = RealTimeManager()
    private lateinit var etNombreProducto: EditText
    private lateinit var etCantidadMinimaProducto: EditText
    private lateinit var etCantidadActualProducto: EditText
    private lateinit var etCantidadAComprar: EditText
    private lateinit var botonRegistrar: Button
    private lateinit var checkbox: CheckBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

         etNombreProducto = findViewById<EditText>(R.id.nombreProducto)
         etCantidadMinimaProducto = findViewById<EditText>(R.id.cantidadMinimaStock)
         etCantidadActualProducto = findViewById<EditText>(R.id.cantidadActual)
         etCantidadAComprar = findViewById<EditText>(R.id.cantidadAComprar)
         botonRegistrar = findViewById<Button>(R.id.botonRegistrarEnDespensa)
         checkbox = findViewById<CheckBox>(R.id.checkBox)

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
            val cantidadAComprarStr = etCantidadAComprar.text.toString()

            if (!checkbox.isChecked) {
                if (MainActivity3().validarNombreProducto(nombreProducto) && MainActivity3().validarCantidadAComprar(
                        cantidadAComprarStr
                    )
                ) {
                    val cantidadAComprar = cantidadAComprarStr.toInt()
                    MainActivity3().guardarProductoEnListaCompra(nombreProducto, cantidadAComprar)
                }
            } else {
                val cantidadMinimaProductoStr = etCantidadMinimaProducto.text.toString()
                val cantidadActualProductoStr = etCantidadActualProducto.text.toString()

                if (MainActivity3().validarNombreProducto(nombreProducto) && MainActivity3().validarCantidadMinima(
                        cantidadMinimaProductoStr
                    ) &&
                    MainActivity3().validarCantidadActual(cantidadActualProductoStr) && MainActivity3().validarCantidadAComprar(
                        cantidadAComprarStr
                    )
                ) {

                    val cantidadMinimaProducto = cantidadMinimaProductoStr.toInt()
                    val cantidadActualProducto = cantidadActualProductoStr.toInt()
                    val cantidadAComprar = cantidadAComprarStr.toInt()

                    MainActivity3().guardarProductoEnDespensa(
                        nombreProducto,
                        cantidadMinimaProducto,
                        cantidadActualProducto
                    )
                    MainActivity3().guardarProductoEnListaCompra(nombreProducto, cantidadAComprar)
                }
            }
        }
    }

}