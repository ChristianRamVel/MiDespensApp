package com.example.midespensapp.DB

import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra
import com.example.midespensapp.clases.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class RealTimeManager {
    private val databaseReference =
        FirebaseDatabase.getInstance("https://midespensaapp-ddc2e-default-rtdb.europe-west1.firebasedatabase.app").reference
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()


    fun obtenerUsuariosPorIdCasa(casaId: String, callback: ObtenerUsuariosPorIdCasaCallBack) {
        val query = database.reference.child("usuarios").orderByChild("idCasa").equalTo(casaId)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val usuarios = snapshot.children.mapNotNull {
                        it.getValue(Usuario::class.java)
                    }
                    callback.onUsuariosObtenidos(usuarios.map { it.idCasa })
                } else {
                    callback.onError(DatabaseError("No se encontraron usuarios en la casa."))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.toException())
            }
        })
    }
    fun borrarCasa(casaId: String, callback: BorrarCasaCallBack) {
        val query = database.reference.child("casas").child(casaId)

        query.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onCasaBorrada()
                } else {
                    callback.onError(task.exception)
                }
            }
    }

    fun borrarUsuario(userKey: String, callback: BorrarUsuarioCallBack) {
        val query = database.reference.child("usuarios").child(userKey)

        query.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onUsuarioBorrado()
                } else {
                    callback.onError(task.exception)
                }
            }
    }

    fun obtenerCasaPorIdUsuario(userKey: String, callback: ObtenerCasaPorIdUsuarioCallBack) {
        val usuariosReference = database.getReference("usuarios")
        usuariosReference.child(userKey).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result
                if (snapshot.exists()) {
                    val idCasa = snapshot.child("idCasa").value.toString()
                    val casasReference = database.getReference("casas")
                    casasReference.child(idCasa).get().addOnCompleteListener { innerTask ->
                        if (innerTask.isSuccessful) {
                            val innerSnapshot = innerTask.result
                            if (innerSnapshot.exists()) {
                                val casa = Casa(idCasa)
                                callback.onCasaObtenida(casa)
                            } else {
                                callback.onError(DatabaseError("No se encontró la casa asociada al usuario."))
                            }
                        } else {
                            callback.onError(innerTask.exception)
                        }
                    }
                } else {
                    callback.onError(DatabaseError("No se encontró el usuario."))
                }
            } else {
                callback.onError(task.exception)
            }
        }
    }

    fun obtenerProductosDespensa(casaId: String, callback: ObtenerProductosDespensaCallBack) {
        val query = database.reference.child("casas").child(casaId).child("productosDespensa")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val productosDespensa = mutableListOf<ProductoDespensa>()

                    for (productoSnapshot in snapshot.children) {
                        val nombre = productoSnapshot.child("nombre").getValue(String::class.java)
                        val stockActual =
                            productoSnapshot.child("stockActual").getValue(Int::class.java)
                        val stockMinimo =
                            productoSnapshot.child("stockMinimo").getValue(Int::class.java)

                        if (nombre != null && stockActual != null && stockMinimo != null) {
                            val producto = ProductoDespensa(nombre, stockActual, stockMinimo)
                            productosDespensa.add(producto)
                        }
                    }

                    callback.onProductosObtenidos(productosDespensa)
                } else {
                    callback.onError(DatabaseError("No se encontraron productos en la despensa."))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.toException())
            }
        })
    }

    //obtenerProductosListaCompra
    fun obtenerProductosListaCompra(casaId: String, callback: ObtenerProductosListaCompraCallBack) {
        val query = database.reference.child("casas").child(casaId).child("productosListaCompra")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val productosListaCompra = mutableListOf<ProductoListaCompra>()

                    for (productoSnapshot in snapshot.children) {
                        val nombre = productoSnapshot.child("nombre").getValue(String::class.java)
                        val cantidad =
                            productoSnapshot.child("cantidadAComprar").getValue(Int::class.java)
                        val comprado =
                            productoSnapshot.child("comprado").getValue(Boolean::class.java)

                        if (nombre != null && cantidad != null && comprado != null) {
                            val producto = ProductoListaCompra(nombre, cantidad, comprado)
                            productosListaCompra.add(producto)
                        }
                    }

                    callback.onProductosObtenidos(productosListaCompra)
                } else {
                    callback.onError(DatabaseError("No se encontraron productos en la lista de la compra."))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.toException())
            }
        })
    }

    fun comprobarCasaExiste(casaId: String, callback: ComprobarCasaExisteCallBack) {
        val query = database.reference.child("casas").child(casaId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    callback.onCasaExiste(true)
                } else {
                    callback.onCasaExiste(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.toException())
            }
        })
    }

    //funcion para crear una nueva casa y añadirla a la lista de casas de la base de datos, tendra su id, una lista de productosDespensa y una lista de productosListaCompra
    //la lista de productosDespensa y productosListaCompra tendran un producto por defecto (pan,2,3) y (leche,1,2) respectivamente
    fun crearCasa(callback: CrearCasaCallBack) {
        val casaRef = databaseReference.child("casas").push() // Genera una nueva clave única
        val casaId = casaRef.key // Obtiene la clave generada
        val casa = Casa(casaId!!, mapOf(), mapOf()) // Crea una nueva casa con la clave generada

        val productosDespensa = mutableMapOf("pan" to ProductoDespensa("pan", 2, 3))
        val productosListaCompra = mutableMapOf("leche" to ProductoListaCompra("leche", 1, false))

        casaRef.setValue(casa)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val productosRef = databaseReference.child("casas")
                        .child(casaId) // Referencia a la casa recién creada

                    // Añade productosDespensa a la casa
                    productosRef.child("productosDespensa").setValue(productosDespensa)
                        .addOnCompleteListener { innerTask ->
                            if (innerTask.isSuccessful) {
                                // Añade productosListaCompra a la casa
                                productosRef.child("productosListaCompra")
                                    .setValue(productosListaCompra)
                                    .addOnCompleteListener { innerInnerTask ->
                                        if (innerInnerTask.isSuccessful) {
                                            callback.onCasaCreada(casa)
                                        } else {
                                            callback.onError(innerInnerTask.exception)
                                        }
                                    }
                            } else {
                                callback.onError(innerTask.exception)
                            }
                        }
                } else {
                    callback.onError(task.exception)
                }
            }
    }

    fun borrarListaCompra(casaId: String, callback: BorrarProductoDespensaCallBack) {
        val query = database.reference.child("casas").child(casaId).child("productosListaCompra")

        query.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onProductoBorrado()
                } else {
                    callback.onError(task.exception)
                }
            }
    }


    fun borrarProductoListaCompra(
        casaId: String,
        producto: ProductoListaCompra,
        callback: BorrarProductoListaCompraCallBack
    ) {
        val query = database.reference.child("casas").child(casaId).child("productosListaCompra")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val productosListaCompra = snapshot.children.mapNotNull {
                        it.getValue(ProductoListaCompra::class.java)
                    }

                    val productoAEliminar =
                        productosListaCompra.find { it.nombre == producto.nombre }

                    if (productoAEliminar != null) {
                        val productoRef = query.child(productoAEliminar.nombre)
                        productoRef.removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    callback.onProductoBorrado()
                                } else {
                                    callback.onError(task.exception)
                                }
                            }
                    } else {
                        callback.onError(DatabaseError("No se encontró el producto en la lista de la compra."))
                    }
                } else {
                    callback.onError(DatabaseError("No se encontraron productos en la lista de la compra."))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.toException())
            }
        })
    }

    //funcion para borrar la lista de productosDespensa de una casa
    fun borrarDespensa(casaId: String, callback: BorrarProductoDespensaCallBack) {
        val query = database.reference.child("casas").child(casaId).child("productosDespensa")

        query.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onProductoBorrado()
                } else {
                    callback.onError(task.exception)
                }
            }


    }

    //funcion para borrar un producto de la lista de productosDespensa de una casa
    fun borrarProductoDespensa(
        casaId: String,
        producto: ProductoDespensa,
        callback: BorrarProductoDespensaCallBack
    ) {
        val query = database.reference.child("casas").child(casaId).child("productosDespensa")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val productosListaDespensa = snapshot.children.mapNotNull {
                        it.getValue(ProductoDespensa::class.java)
                    }

                    val productoAEliminar =
                        productosListaDespensa.find { it.nombre == producto.nombre }

                    if (productoAEliminar != null) {
                        val productoRef = query.child(productoAEliminar.nombre)
                        productoRef.removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    callback.onProductoBorrado()
                                } else {
                                    callback.onError(task.exception)
                                }
                            }
                    } else {
                        callback.onError(DatabaseError("No se encontró el producto en la despensa."))
                    }
                } else {
                    callback.onError(DatabaseError("No se encontraron productos en la despensa."))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.toException())
            }
        })
    }



    fun disminuirCantidadAComprar(
        casaId: String,
        producto: ProductoListaCompra,
        callback: DisminuirCantidadAComprarCallBack
    ) {
        val query = database.reference.child("casas").child(casaId).child("productosListaCompra")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val productosListaCompra = snapshot.children.mapNotNull {
                        it.getValue(ProductoListaCompra::class.java)
                    }

                    val productoAModificar =
                        productosListaCompra.find { it.nombre == producto.nombre }

                    if (productoAModificar != null) {
                        val cantidad = productoAModificar.cantidadAComprar
                        if (cantidad > 1) {
                            val productoRef = query.child(productoAModificar.nombre)
                            productoRef.child("cantidadAComprar").setValue(cantidad - 1)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        callback.onCantidadDisminuida()
                                    } else {
                                        callback.onError(task.exception)
                                    }
                                }
                        } else {
                            callback.onError(DatabaseError("La cantidad a comprar no puede ser menor que 1."))
                        }
                    } else {
                        callback.onError(DatabaseError("No se encontró el producto en la lista de la compra."))
                    }
                } else {
                    callback.onError(DatabaseError("No se encontraron productos en la lista de la compra."))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.toException())
            }
        })
    }


    fun aumentarCantidadAComprar(
        casaId: String,
        producto: ProductoListaCompra,
        callback: AumentarCantidadAComprarCallBack
    ) {
        val query = database.reference.child("casas").child(casaId).child("productosListaCompra")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val productosListaCompra = snapshot.children.mapNotNull {
                        it.getValue(ProductoListaCompra::class.java)
                    }

                    val productoAModificar =
                        productosListaCompra.find { it.nombre == producto.nombre }

                    if (productoAModificar != null) {
                        val cantidad = productoAModificar.cantidadAComprar
                        val productoRef = query.child(productoAModificar.nombre)
                        productoRef.child("cantidadAComprar").setValue(cantidad + 1)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    callback.onCantidadAumentada()
                                } else {
                                    callback.onError(task.exception)
                                }
                            }
                    } else {
                        callback.onError(DatabaseError("No se encontró el producto en la lista de la compra."))
                    }
                } else {
                    callback.onError(DatabaseError("No se encontraron productos en la lista de la compra."))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onError(error.toException())
            }
        })
    }




    private fun DatabaseError(s: String): Exception? {
        return null
    }

}
