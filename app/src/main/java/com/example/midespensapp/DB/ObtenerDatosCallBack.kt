package com.example.midespensapp.DB

import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import kotlin.Exception

interface CrearCasaCallBack {
    fun onCasaCreada(casa: Casa)
    fun onError(error: Exception?)
}
interface ObtenerProductosDespensaCallBack {
    fun onProductosObtenidos(productos: MutableList<ProductoDespensa>,)
    fun onError(error: Exception?)
}

interface ObtenerProductosListaCompraCallBack {
    fun onProductosObtenidos(productos: MutableList<ProductoListaCompra>,)
    fun onError(error: DatabaseError)
}

interface ObtenerCasaPorIdUsuarioCallBack {
    fun onCasaObtenida(casa: Casa?)
    fun onError(error: Exception?)
}

interface ComprobarCasaExisteCallBack {
    fun onCasaExiste(existe: Boolean)
    fun onError(error: Exception?)
}