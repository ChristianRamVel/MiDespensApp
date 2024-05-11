package com.example.midespensapp.vista.despensa

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.midespensapp.R
import com.example.midespensapp.controlador.ObtenerCasaPorIdUsuarioCallBack
import com.example.midespensapp.controlador.RealTimeManager
import com.example.midespensapp.modelo.Casa
import com.example.midespensapp.modelo.ProductoDespensa
import com.example.midespensapp.modelo.ProductoListaCompra
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AnadirDespensaActivity : AppCompatActivity() {

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

        etNombreProducto = findViewById(R.id.nombreProducto)
        etCantidadMinimaProducto = findViewById(R.id.cantidadMinimaStock)
        etCantidadActualProducto = findViewById(R.id.cantidadActual)
        etCantidadAComprar = findViewById(R.id.cantidadAComprar)
        botonRegistrar = findViewById(R.id.botonRegistrarEnDespensa)
        checkbox = findViewById(R.id.checkBox)

        checkbox.setOnClickListener {
            if (checkbox.isChecked) {
                etCantidadAComprar.visibility = EditText.VISIBLE
            } else {
                etCantidadAComprar.text.clear()
                etCantidadAComprar.visibility = EditText.GONE
            }
        }

        val intent = intent
        if (intent.extras != null) {
            val nombreProducto = intent.getStringExtra("nombreProducto")
            val cantidadActualProducto = intent.getIntExtra("stockActual", 0)

            etNombreProducto.setText(nombreProducto)
            etCantidadMinimaProducto.text.clear()
            etCantidadActualProducto.setText(cantidadActualProducto.toString())

            etNombreProducto.isEnabled = false
            etCantidadActualProducto.isEnabled = false
            checkbox.isEnabled = false
        }


        botonRegistrar.setOnClickListener {
            registrarProducto()

            etNombreProducto.text.clear()
            etCantidadMinimaProducto.text.clear()
            etCantidadActualProducto.text.clear()
            etCantidadAComprar.text.clear()
            checkbox.isChecked = false
        }
    }

    private fun registrarProducto() {
        val nombreProducto = etNombreProducto.text.toString()
        val cantidadMinimaProductoStr = etCantidadMinimaProducto.text.toString()
        val cantidadActualProductoStr = etCantidadActualProducto.text.toString()

        if (!checkbox.isChecked) {
            if (validarNombreProducto(nombreProducto) &&
                validarCantidadMinima(cantidadMinimaProductoStr) &&
                validarCantidadActual(cantidadActualProductoStr)
            ) {
                guardarProductoEnDespensa(
                    nombreProducto,
                    cantidadActualProductoStr.toInt(),
                    cantidadMinimaProductoStr.toInt()

                )
            }
        } else {
            val cantidadMinimaProductoStr = etCantidadMinimaProducto.text.toString()
            val cantidadActualProductoStr = etCantidadActualProducto.text.toString()
            val cantidadAComprarStr = etCantidadAComprar.text.toString()

            if (validarNombreProducto(nombreProducto) &&
                validarCantidadMinima(cantidadMinimaProductoStr) &&
                validarCantidadActual(cantidadActualProductoStr) &&
                validarCantidadMinima(cantidadAComprarStr)
            ) {
                val cantidadMinimaProducto = cantidadMinimaProductoStr.toInt()
                val cantidadActualProducto = cantidadActualProductoStr.toInt()
                val cantidadAComprar = cantidadAComprarStr.toInt()

                Log.d("MainActivity2", "Nombre: $nombreProducto")
                Log.d("MainActivity2", "Cantidad mínima: $cantidadMinimaProducto")
                Log.d("MainActivity2", "Cantidad actual: $cantidadActualProducto")
                Log.d("MainActivity2", "Cantidad a comprar: $cantidadAComprar")

                guardarProductoEnDespensa(
                    nombreProducto,
                    cantidadActualProducto,
                    cantidadMinimaProducto

                )
                guardarProductoEnListaCompra(
                    nombreProducto,
                    cantidadAComprar
                )

                etNombreProducto.text.clear()
                etCantidadMinimaProducto.text.clear()
                etCantidadActualProducto.text.clear()
                etCantidadAComprar.text.clear()
                checkbox.isChecked = false
            }
        }
    }

    private fun validarNombreProducto(nombreProducto: String): Boolean {
        if (nombreProducto.isEmpty()) {
            etNombreProducto.error = "El nombre del producto no puede estar vacío"
            Toast.makeText(this, "El nombre del producto no puede estar vacío", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    /*
    private fun validarCantidadAComprar(cantidadAComprarStr: String): Boolean {
        if (cantidadAComprarStr.isEmpty()) {
            etCantidadAComprar.error = "La cantidad a comprar no puede estar vacía"
            Toast.makeText(this, "La cantidad a comprar no puede estar vacía", Toast.LENGTH_SHORT).show()
            return false
        }else if (cantidadAComprarStr.toInt() <= 0) {
            etCantidadAComprar.error = "La cantidad a comprar no puede ser 0"
            Toast.makeText(this, "La cantidad a comprar no puede ser 0", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
*/
    private fun validarCantidadMinima(cantidadMinimaProductoStr: String): Boolean {
        if (cantidadMinimaProductoStr.isEmpty()) {
            etCantidadMinimaProducto.error = "La cantidad mínima de stock no puede estar vacía"
            Toast.makeText(
                this,
                "La cantidad mínima de stock no puede estar vacía",
                Toast.LENGTH_SHORT
            ).show()
            return false
        } else if (cantidadMinimaProductoStr.toInt() <= 0) {
            etCantidadMinimaProducto.error = "La cantidad mínima de stock no puede ser 0"
            Toast.makeText(this, "La cantidad mínima de stock no puede ser 0", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun validarCantidadActual(cantidadActualProductoStr: String): Boolean {
        if (cantidadActualProductoStr.isEmpty()) {
            etCantidadActualProducto.error = "La cantidad actual de stock no puede estar vacía"
            Toast.makeText(
                this,
                "La cantidad actual de stock no puede estar vacía",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

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
                                    this@AnadirDespensaActivity,
                                    "Producto guardado en la lista de la compra correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@AnadirDespensaActivity,
                                    "Error al guardar el producto en la lista de la compra",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this@AnadirDespensaActivity,
                            "No se encontró la casa para el usuario actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(error: Exception?) {
                    Toast.makeText(
                        this@AnadirDespensaActivity,
                        "Error obteniendo casa: ${error?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(
                this@AnadirDespensaActivity,
                "El usuario no está autenticado",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    //funcion para guardar producto en despensa
    internal fun guardarProductoEnDespensa(
        nombreProducto: String,
        cantidadActualProducto: Int,
        cantidadMinimaProducto: Int

    ) {
        // Obtener referencia a la base de datos Firebase
        val databaseReference = FirebaseDatabase.getInstance().reference

        //Obtener el ID de la casa del usuario actual
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                ObtenerCasaPorIdUsuarioCallBack {
                override fun onCasaObtenida(casa: Casa?) {
                    if (casa != null) {
                        // Crear un objeto ProductoDespensa con los datos proporcionados
                        val productoDespensa =
                            ProductoDespensa(
                                nombreProducto,
                                cantidadActualProducto,
                                cantidadMinimaProducto

                            )
                        // Utilizar el nombre del producto como clave para almacenar el objeto ProductoDespensa en la lista productosDespensa de la casa
                        databaseReference.child("casas").child(casa.id)
                            .child("productosDespensa").child(nombreProducto)
                            .setValue(productoDespensa)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@AnadirDespensaActivity,
                                    "Producto guardado en la despensa correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@AnadirDespensaActivity,
                                    "Error al guardar el producto en la despensa",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this@AnadirDespensaActivity,
                            "No se encontró la casa para el usuario actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(error: Exception?) {
                    Log.e("MainActivity2", "Error obteniendo casa: ${error?.message}")
                    Toast.makeText(
                        this@AnadirDespensaActivity,
                        "Error obteniendo casa: ${error?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            // El usuario no está autenticado
            Log.e("MainActivity2", "El usuario no está autenticado")
            Toast.makeText(
                this@AnadirDespensaActivity,
                "El usuario no está autenticado",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onBackPressedDispatcher.onBackPressed()
        finish()
    }

}