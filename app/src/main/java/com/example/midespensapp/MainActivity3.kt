package com.example.midespensapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.midespensapp.DB.ObtenerCasaPorIdUsuarioCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity3 : AppCompatActivity() {

    private val realTimeManager = RealTimeManager()
    private lateinit var etNombreProducto: EditText
    private lateinit var etCantidadMinimaProducto: EditText
    private lateinit var etCantidadActualProducto: EditText
    private lateinit var etCantidadAComprar: EditText
    private lateinit var botonRegistrar: Button
    private lateinit var checkbox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main3)

        etNombreProducto = findViewById<EditText>(R.id.nombreProducto)
        etCantidadMinimaProducto = findViewById<EditText>(R.id.cantidadMinimaStock)
        etCantidadActualProducto = findViewById<EditText>(R.id.cantidadActual)
        etCantidadAComprar = findViewById<EditText>(R.id.cantidadAComprar)
        botonRegistrar = findViewById<Button>(R.id.botonRegistrarEnDespensa)
        checkbox = findViewById<CheckBox>(R.id.checkBox)

        checkbox.setOnClickListener {
            if (checkbox.isChecked) {
                etCantidadMinimaProducto.visibility = EditText.VISIBLE
                etCantidadActualProducto.visibility = EditText.VISIBLE
            } else {
                etCantidadMinimaProducto.text.clear()
                etCantidadActualProducto.text.clear()
                etCantidadMinimaProducto.visibility = EditText.GONE
                etCantidadActualProducto.visibility = EditText.GONE
            }
        }

        botonRegistrar.setOnClickListener {
            val nombreProducto = etNombreProducto.text.toString()
            val cantidadAComprarStr = etCantidadAComprar.text.toString()

            if (!checkbox.isChecked) {
                if (validarNombreProducto(nombreProducto) && validarCantidadAComprar(
                        cantidadAComprarStr
                    )
                ) {
                    val cantidadAComprar = cantidadAComprarStr.toInt()
                    guardarProductoEnListaCompra(nombreProducto, cantidadAComprar)
                }
            } else {
                val cantidadMinimaProductoStr = etCantidadMinimaProducto.text.toString()
                val cantidadActualProductoStr = etCantidadActualProducto.text.toString()

                if (validarNombreProducto(nombreProducto) && validarCantidadMinima(
                        cantidadMinimaProductoStr
                    ) &&
                    validarCantidadActual(cantidadActualProductoStr) && validarCantidadAComprar(
                        cantidadAComprarStr
                    )
                ) {

                    val cantidadMinimaProducto = cantidadMinimaProductoStr.toInt()
                    val cantidadActualProducto = cantidadActualProductoStr.toInt()
                    val cantidadAComprar = cantidadAComprarStr.toInt()

                    guardarProductoEnDespensa(
                        nombreProducto,
                        cantidadMinimaProducto,
                        cantidadActualProducto
                    )
                    guardarProductoEnListaCompra(nombreProducto, cantidadAComprar)
                }
            }
        }


    }
    internal fun validarNombreProducto(nombreProducto: String): Boolean {
        return if (nombreProducto.isEmpty()) {
            etNombreProducto.error = "El nombre del producto no puede estar vacío"
            Toast.makeText(
                this,
                "El nombre del producto no puede estar vacío",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else {
            true
        }
    }

    internal fun validarCantidadAComprar(cantidadAComprarStr: String): Boolean {
        return if (cantidadAComprarStr.isEmpty()) {
            etCantidadAComprar.error = "La cantidad a comprar no puede estar vacía"
            Toast.makeText(
                this,
                "La cantidad a comprar no puede estar vacía",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else {
            val cantidadAComprar = cantidadAComprarStr.toInt()
            if (cantidadAComprar <= 0) {
                etCantidadAComprar.error = "La cantidad a comprar no puede ser 0 o negativa"
                Toast.makeText(
                    this,
                    "La cantidad a comprar no puede ser 0 o negativa",
                    Toast.LENGTH_SHORT
                ).show()
                false
            } else {
                true
            }
        }
    }

    internal fun validarCantidadMinima(cantidadMinimaProductoStr: String): Boolean {
        return if (cantidadMinimaProductoStr.isEmpty()) {
            etCantidadMinimaProducto.error = "La cantidad mínima de stock no puede estar vacía"
            Toast.makeText(
                this,
                "La cantidad mínima de stock no puede estar vacía",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else {
            val cantidadMinimaProducto = cantidadMinimaProductoStr.toInt()
            if (cantidadMinimaProducto <= 0) {
                etCantidadMinimaProducto.error =
                    "La cantidad mínima de stock no puede ser 0 o negativa"
                Toast.makeText(
                    this,
                    "La cantidad mínima de stock no puede ser 0 o negativa",
                    Toast.LENGTH_SHORT
                ).show()
                false
            } else {
                true
            }
        }
    }

    internal fun validarCantidadActual(cantidadActualProductoStr: String): Boolean {
        return if (cantidadActualProductoStr.isEmpty()) {
            etCantidadActualProducto.error = "La cantidad actual de stock no puede estar vacía"
            Toast.makeText(
                this,
                "La cantidad actual de stock no puede estar vacía",
                Toast.LENGTH_SHORT
            ).show()
            false
        } else {
            true
        }
    }

    //funcion para guardar producto en productosListaCompra
    internal fun guardarProductoEnListaCompra(nombreProducto: String, cantidadAComprar: Int) {
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
                        val productoCompra =
                            ProductoListaCompra(nombreProducto, cantidadAComprar, false)
                        // 4. Utilizar el nombre del producto como clave para almacenar el objeto ProductoDespensa en la lista productosDespensa de la casa
                        databaseReference.child("casas").child(casa.id)
                            .child("productosListaCompra").child(nombreProducto)
                            .setValue(productoCompra)
                            .addOnSuccessListener {
                                // Éxito al guardar el producto en la despensa
                                Toast.makeText(
                                    this@MainActivity3,
                                    "Producto guardado en la lista de la compra correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@MainActivity3,
                                    "Error al guardar el producto en la lista de la compra",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this@MainActivity3,
                            "No se encontró la casa para el usuario actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(error: Exception?) {
                    Log.e("MainActivity2", "Error obteniendo casa: ${error?.message}")
                    Toast.makeText(
                        this@MainActivity3,
                        "Error obteniendo casa: ${error?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            // El usuario no está autenticado
            Log.e("MainActivity2", "El usuario no está autenticado")
            Toast.makeText(this@MainActivity3, "El usuario no está autenticado", Toast.LENGTH_SHORT)
                .show()
        }
    }

    //funcion para guardar producto en despensa
    internal fun guardarProductoEnDespensa(
        nombreProducto: String,
        cantidadMinimaProducto: Int,
        cantidadActualProducto: Int
    ) {
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
                        val productoDespensa =
                            ProductoDespensa(
                                nombreProducto,
                                cantidadMinimaProducto,
                                cantidadActualProducto
                            )
                        // 4. Utilizar el nombre del producto como clave para almacenar el objeto ProductoDespensa en la lista productosDespensa de la casa
                        databaseReference.child("casas").child(casa.id)
                            .child("productosDespensa").child(nombreProducto)
                            .setValue(productoDespensa)
                            .addOnSuccessListener {
                                // Éxito al guardar el producto en la despensa
                                Toast.makeText(
                                    this@MainActivity3,
                                    "Producto guardado en la despensa correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@MainActivity3,
                                    "Error al guardar el producto en la despensa",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this@MainActivity3,
                            "No se encontró la casa para el usuario actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(error: Exception?) {
                    Log.e("MainActivity2", "Error obteniendo casa: ${error?.message}")
                    Toast.makeText(
                        this@MainActivity3,
                        "Error obteniendo casa: ${error?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            // El usuario no está autenticado
            Log.e("MainActivity2", "El usuario no está autenticado")
            Toast.makeText(this@MainActivity3, "El usuario no está autenticado", Toast.LENGTH_SHORT)
                .show()
        }
    }

}