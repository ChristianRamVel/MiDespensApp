package com.example.midespensapp.DB

import android.util.Log
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra
import com.example.midespensapp.clases.Usuario
import com.google.firebase.database.*


class RealTimeManager {
    private val databaseReference = FirebaseDatabase.getInstance("https://midespensaapp-ddc2e-default-rtdb.europe-west1.firebasedatabase.app/").reference
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
    fun obtenerCasas(callback: ObtenerDatosCallBack) {
        val casasReference = databaseReference.child("casas")
        casasReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val casas = dataSnapshot.children.mapNotNull { it.getValue(Casa::class.java) }
                callback.onDatosObtenidos(casas)
                Log.d("RealTimeManager", "Casas obtenidas: $casas")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onError(databaseError)
            }
        })
    }

    fun obtenerProductosDespensaPorIdCasa(idCasa: String, callback: ObtenerProductosDespensaCallBack) {
        val productosDespensaReference = databaseReference.child("casas").child(idCasa).child("productosDespensa")
        productosDespensaReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productosDespensa = dataSnapshot.children.mapNotNull { it.getValue(ProductoDespensa::class.java) }
                callback.onProductosObtenidos(productosDespensa)
                Log.d("RealTimeManager", "Productos de la despensa obtenidos: $productosDespensa")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onError(databaseError)
            }
        })
    }

    fun obtenerProductosListaCompraPorIdCasa(idCasa: String, callback: ObtenerProductosListaCompraCallBack) {
        val productosListaCompraReference = databaseReference.child("casas").child(idCasa).child("productosListaCompra")
        productosListaCompraReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productosListaCompra = dataSnapshot.children.mapNotNull { it.getValue(ProductoListaCompra::class.java) }
                callback.onProductosObtenidos(productosListaCompra)
                Log.d("RealTimeManager", "Productos de la lista de la compra obtenidos: $productosListaCompra")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback.onError(databaseError)
            }
        })
    }

}
