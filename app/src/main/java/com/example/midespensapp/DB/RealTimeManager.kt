package com.example.midespensapp.DB

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra
import com.example.midespensapp.clases.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class RealTimeManager {
    private val databaseReference =
        FirebaseDatabase.getInstance("https://midespensaapp-ddc2e-default-rtdb.europe-west1.firebasedatabase.app").reference
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
                callback.onProductosObtenidos(productosDespensa.toMutableList())
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
                callback.onProductosObtenidos(productosListaCompra.toMutableList())
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

    fun crearCasaNueva(): Casa {
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

    fun comprobarSiCasaExistePorIDCasa(idCasa: String, callback: ComprobarSiCasaExisteCallBack) {
        val casaReference = databaseReference.child("casas").child(idCasa)
        casaReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    callback.onCasaExiste(true)
                    Log.d("Firebase", "La casa existe")
                } else {
                    callback.onCasaNoExiste(false)
                    Log.d("Firebase", "La casa no existe")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onError(databaseError)
            }
        })
    }

    //funcion para aumentar en 1 la cantidad de un producto en la lista de la compra, este producto ya existe, a is que es un update

    fun aumentarCantidadAComprarCompra(casaId: String, productoNombre: String) {
        Log.d(
            "Firebase",
            "Comenzando a aumentar la cantidad a comprar del producto $productoNombre en la casa $casaId"
        )

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("casas").child(casaId).child("productosListaCompra")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productosListaCompra = dataSnapshot.getValue(object :
                    GenericTypeIndicator<List<ProductoListaCompra>>() {})
                Log.d("Firebase", "Datos obtenidos de Firebase: $productosListaCompra")

                productosListaCompra?.forEach { producto ->
                    if (producto.nombre == productoNombre) {
                        producto.cantidadAComprar = producto.cantidadAComprar!! + 1
                        Log.d(
                            "Firebase",
                            "Nueva cantidad a comprar para $productoNombre: ${producto.cantidadAComprar}"
                        )

                        ref.child(productosListaCompra.indexOf(producto).toString())
                            .child("cantidadAComprar").setValue(producto.cantidadAComprar)
                        Log.d(
                            "Firebase",
                            "Cantidad a comprar actualizada correctamente en Firebase"
                        )

                        return@forEach
                    }
                }
                Log.d("Firebase", "Producto $productoNombre no encontrado en la lista de compra")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error al acceder a Firebase: ${databaseError.message}")
                // Manejar error
            }
        })
    }

    fun disminuirCantidadAComprarCompra(casaId: String, productoNombre: String) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("casas").child(casaId).child("productosListaCompra")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productosListaCompra = dataSnapshot.getValue(object :
                    GenericTypeIndicator<List<ProductoListaCompra>>() {})

                productosListaCompra?.forEach { producto ->
                    if (producto.nombre == productoNombre) {
                        if (producto.cantidadAComprar!! > 0) {
                            producto.cantidadAComprar = producto.cantidadAComprar!! - 1
                            ref.child(productosListaCompra.indexOf(producto).toString())
                                .child("cantidadAComprar").setValue(producto.cantidadAComprar)
                        }
                        return@forEach
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar error
            }
        })
    }


    fun aumentarCantidadAComprarDespensa(casaId: String, productoNombre: String) {
        Log.d(
            "Firebase",
            "Comenzando a aumentar la cantidad en stock $productoNombre en la casa $casaId"
        )

        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("casas").child(casaId).child("productosDespensa")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productosDespensa = dataSnapshot.getValue(object :
                    GenericTypeIndicator<List<ProductoDespensa>>() {})
                Log.d("Firebase", "Datos obtenidos de Firebase: $productosDespensa")

                productosDespensa?.forEach { producto ->
                    if (producto.nombre == productoNombre) {
                        producto.stockActual = producto.stockActual!! + 1
                        Log.d(
                            "Firebase",
                            "Nueva cantidad de stock $productoNombre: ${producto.stockActual}"
                        )

                        ref.child(productosDespensa.indexOf(producto).toString())
                            .child("stockActual").setValue(producto.stockActual)
                        Log.d(
                            "Firebase",
                            "Cantidad a comprar actualizada correctamente en Firebase"
                        )

                        return@forEach
                    }
                }
                Log.d("Firebase", "Producto $productoNombre no encontrado en la lista de compra")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error al acceder a Firebase: ${databaseError.message}")
                // Manejar error
            }
        })
    }

    fun disminuirCantidadAComprarDespensa(casaId: String, productoNombre: String) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("casas").child(casaId).child("productosDespensa")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productosDespensa = dataSnapshot.getValue(object :
                    GenericTypeIndicator<List<ProductoDespensa>>() {})

                productosDespensa?.forEach { producto ->
                    if (producto.nombre == productoNombre) {
                        if (producto.stockActual!! > 0) {
                            producto.stockActual = producto.stockActual!! - 1
                            ref.child(productosDespensa.indexOf(producto).toString())
                                .child("stockActual").setValue(producto.stockActual)
                        }
                        return@forEach
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar error
            }
        })
    }


}
