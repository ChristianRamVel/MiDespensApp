package com.example.midespensapp.DB

import android.util.Log
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra
import com.example.midespensapp.clases.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*


class RealTimeManager {
    private val databaseReference =
        FirebaseDatabase.getInstance("https://midespensaapp-ddc2e-default-rtdb.europe-west1.firebasedatabase.app/").reference
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
//segun este Json, crear los metodos para obtener los datos de la base de datos
    /*
{
    "casas": [
    {
        "id": "casa1",
        "productosDespensa": [
        {
            "nombre": "Leche",
            "stockActual": 8,
            "stockMinimo": 5
        },
        {
            "nombre": "Pan",
            "stockActual": 3,
            "stockMinimo": 2
        }
        ],
        "productosListaCompra": [
        {
            "cantidadAComprar": 1,
            "comprado": false,
            "nombre": "Huevos"
        },
        {
            "cantidadAComprar": 2,
            "comprado": true,
            "nombre": "Frutas"
        }
        ]
    },
    {
        "id": "casa2",
        "productosDespensa": [
        {
            "nombre": "Leche",
            "stockActual": 8,
            "stockMinimo": 5
        }
        ],
        "productosListaCompra": [
        {
            "cantidadAComprar": 1,
            "comprado": false,
            "nombre": "Huevos"
        }
        ]
    }
    ],
    "usuarios": [
    {
        "email": "usuario1@example.com",
        "idCasa": "casa1",
        "idUsuario": "usuario1"
    },
    {
        "email": "usuario2@example.com",
        "idCasa": "casa2",
        "idUsuario": "usuario2"
    }
    ]
}*/

    fun findUser(callback: (Usuario?) -> Unit) {
        val currentUser: FirebaseUser? = mAuth.currentUser
        currentUser?.uid?.let { userId ->
            databaseReference.child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val usuario: Usuario? = dataSnapshot.getValue(Usuario::class.java)
                        callback(usuario)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejar el error
                        callback(null)
                    }
                })
        } ?: run {
            // No hay usuario autenticado
            callback(null)
        }
    }

    fun obtenerProductosDespensaPorIdCasa(
        idCasa: String,
        callback: ObtenerProductosDespensaCallBack
    ) {
        val productosDespensaReference =
            databaseReference.child("casas").child(idCasa).child("productosDespensa")
        productosDespensaReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productosDespensa =
                    dataSnapshot.children.mapNotNull { it.getValue(ProductoDespensa::class.java) }
                callback.onProductosObtenidos(productosDespensa)
                Log.d("RealTimeManager", "Productos de la despensa obtenidos: $productosDespensa")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onError(databaseError)
            }
        })
    }

    fun obtenerProductosListaCompraPorIdCasa(
        idCasa: String,
        callback: ObtenerProductosListaCompraCallBack
    ) {
        val productosListaCompraReference =
            databaseReference.child("casas").child(idCasa).child("productosListaCompra")
        productosListaCompraReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productosListaCompra =
                    dataSnapshot.children.mapNotNull { it.getValue(ProductoListaCompra::class.java) }
                callback.onProductosObtenidos(productosListaCompra)
                Log.d(
                    "RealTimeManager",
                    "Productos de la lista de la compra obtenidos: $productosListaCompra"
                )
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onError(databaseError)
            }
        })
    }

    fun obtenerCasaPorIdUsuario(idUsuario: String, callback: ObtenerCasaPorIdUsuarioCallBack) {
        val casaReference =
            databaseReference.child("usuarios").orderByChild("idUsuario").equalTo(idUsuario)
        casaReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val casa = dataSnapshot.children.mapNotNull { it.getValue(Usuario::class.java) }
                    .firstOrNull()?.idCasa
                if (casa != null) {
                    databaseReference.child("casas").child(casa)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val casaObtenida = dataSnapshot.getValue(Casa::class.java)
                                callback.onCasaObtenida(casaObtenida!!)
                                Log.d("RealTimeManager", "Casa obtenida: $casaObtenida")
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                callback.onError(databaseError)
                            }
                        })
                } else {
                    callback.onError(DatabaseError.fromException(Exception("No se ha encontrado la casa")))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onError(databaseError)
            }
        })
    }

    fun guardarUsuario(email: String, idCasa: String) {
        val currentUser = mAuth.currentUser
        val userId = currentUser?.uid

        userId?.let {
            val usuario = Usuario(idUsuario = it, email = email, idCasa = idCasa)
            databaseReference.child("usuarios").child(it).setValue(usuario)
                .addOnSuccessListener {
                    // La información del usuario se guardó correctamente
                }
                .addOnFailureListener { exception ->
                    // Ocurrió un error al guardar la información del usuario
                    // Manejar el error
                }
        }


    }

    fun crearCasaNueva() : Casa{
        val casa = Casa()
        val key = databaseReference.child("casas").push().key
        key?.let {
            casa.id = it
            databaseReference.child("casas").child(it).setValue(casa)
                .addOnSuccessListener {
                    crearProductosdespensaParaCasaNueva(casa.id!!)
                    crearProductosListaCompraParaCasaNueva(casa.id!!)
                }
                .addOnFailureListener { exception ->
                    // Ocurrió un error al guardar la información de la casa
                    // Manejar el error
                }
        }
        return casa
    }

    fun crearProductosdespensaParaCasaNueva(idCasa: String) {
        val productosDespensa = listOf(
            ProductoDespensa("Leche", 8, 5),
            ProductoDespensa("Pan", 3, 2)
        )
        databaseReference.child("casas").child(idCasa).child("productosDespensa")
            .setValue(productosDespensa)
            .addOnSuccessListener {
                // Los productos de la despensa se guardaron correctamente
            }
            .addOnFailureListener { exception ->
                // Ocurrió un error al guardar los productos de la despensa
                // Manejar el error
            }
    }

    fun crearProductosListaCompraParaCasaNueva(idCasa: String) {
        val productosListaCompra = listOf(
            ProductoListaCompra("leche", 1, false),
            ProductoListaCompra("pan", 2, false)
        )
        databaseReference.child("casas").child(idCasa).child("productosListaCompra")
            .setValue(productosListaCompra)
            .addOnSuccessListener {
                // Los productos de la lista de la compra se guardaron correctamente
            }
            .addOnFailureListener { exception ->
                // Ocurrió un error al guardar los productos de la lista de la compra
                // Manejar el error
            }
    }

    fun guardarUsuarioEnCasaExistente(email: String, idCasa: String) {
    //primero comprobar si el id de la casa existe
        databaseReference.child("casas").child(idCasa).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // La casa existe, guardar el usuario
                    val currentUser = mAuth.currentUser
                    val userId = currentUser?.uid

                    userId?.let {
                        val usuario = Usuario(idUsuario = it, email = email, idCasa = idCasa)
                        databaseReference.child("usuarios").child(it).setValue(usuario)
                            .addOnSuccessListener {
                                // La información del usuario se guardó correctamente
                            }
                            .addOnFailureListener { exception ->
                                // Ocurrió un error al guardar la información del usuario
                                // Manejar el error
                            }
                    }
                } else {
                    // La casa no existe
                    // Manejar el error
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar el error
            }
        })
    }
}
