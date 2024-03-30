package com.example.midespensapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.midespensapp.DB.ObtenerCasaPorIdUsuarioCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity2 : AppCompatActivity() {

    private val realTimeManager = RealTimeManager()
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
            val cantidadMinimaProductoStr = etCantidadMinimaProducto.text.toString()
            val cantidadActualProductoStr = etCantidadActualProducto.text.toString()

            if (cantidadMinimaProductoStr.isNullOrEmpty() || cantidadActualProductoStr.isNullOrEmpty()) {
                // Mostrar un mensaje de error o manner la situación de que algún campo esté vacío
                return@setOnClickListener
            }

            val cantidadMinimaProducto = cantidadMinimaProductoStr.toInt()
            val cantidadActualProducto = cantidadActualProductoStr.toInt()

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
                else -> {
                    guardarProductoEnDespensa(nombreProducto, cantidadMinimaProducto, cantidadActualProducto)
                }
            }

            finish()
        }
    }

    //metodo para guardar los datos en la base de datos de firebase database, en la casa correspondiente al usuario
    //en la lista de productosDespensa de la casa
    private fun guardarProductoEnDespensa(nombreProducto: String, cantidadMinimaProducto: Int, cantidadActualProducto: Int) {
        // 1. Obtener referencia a la base de datos Firebase
        val databaseReference = FirebaseDatabase.getInstance().reference

        // 2. Obtener el ID de la casa del usuario actual (asumiendo que ya tienes implementada la obtención del ID del usuario)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                ObtenerCasaPorIdUsuarioCallBack {
                override fun onCasaObtenida(casa: Casa?) {
                    if (casa != null) {
                        // 3. Crear un objeto ProductoDespensa con los datos proporcionados
                        val productoDespensa = ProductoDespensa(nombreProducto, cantidadActualProducto, cantidadMinimaProducto)

                        //comprobamos si el producto ya existe en la despensa
                        if(casa.productosDespensa.containsKey(nombreProducto)){
                            //informar al usuario de que el producto ya existe en la despensa
                            Toast.makeText(this@MainActivity2, "El producto ya existe en la despensa", Toast.LENGTH_SHORT).show()
                        }

                        // 4. Utilizar el nombre del producto como clave para almacenar el objeto ProductoDespensa en la lista productosDespensa de la casa
                        databaseReference.child("casas").child(casa.id).child("productosDespensa").child(nombreProducto).setValue(productoDespensa)
                            .addOnSuccessListener {
                                Log.d("MainActivity2", "Producto $nombreProducto guardado en la despensa correctamente")
                                // Éxito al guardar el producto en la despensa
                                Toast.makeText(this@MainActivity2, "Producto guardado en la despensa correctamente", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                // Error al guardar el producto en la despensa
                                Log.e("MainActivity2", "Error al guardar el producto en la despensa", e)
                                Toast.makeText(this@MainActivity2, "Error al guardar el producto en la despensa", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.e("MainActivity2", "No se encontró la casa para el usuario actual")
                        Toast.makeText(this@MainActivity2, "No se encontró la casa para el usuario actual", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(error: Exception?) {
                    Log.e("MainActivity2", "Error obteniendo casa: ${error?.message}")
                    Toast.makeText(this@MainActivity2, "Error obteniendo casa: ${error?.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // El usuario no está autenticado
            Log.e("MainActivity2", "El usuario no está autenticado")
            Toast.makeText(this@MainActivity2, "El usuario no está autenticado", Toast.LENGTH_SHORT).show()
        }
    }


}