package com.example.midespensapp.DB

import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra
import com.example.midespensapp.clases.Usuario
import com.google.firebase.database.DatabaseError

interface ObtenerDatosCallBack {
    fun onDatosObtenidos(datos: List<Casa>)
    fun onError(error: DatabaseError)
}

interface ObtenerProductosDespensaCallBack {
    fun onProductosObtenidos(productos: MutableList<ProductoDespensa>,)
    fun onError(error: DatabaseError)
}

interface ObtenerProductosListaCompraCallBack {
    fun onProductosObtenidos(productos: MutableList<ProductoListaCompra>,)
    fun onError(error: DatabaseError)
}

interface ObtenerCasaPorIdUsuarioCallBack {
    fun onCasaObtenida(casa: Casa)
    fun onError(error: DatabaseError)
}
