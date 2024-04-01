package com.example.midespensapp.DB

import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra

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
    fun onError(error: Exception?)
}

interface ObtenerCasaPorIdUsuarioCallBack {
    fun onCasaObtenida(casa: Casa?)
    fun onError(error: Exception?)
}

interface ComprobarCasaExisteCallBack {
    fun onCasaExiste(existe: Boolean)
    fun onError(error: Exception?)
}

interface BorrarProductoDespensaCallBack {
    fun onProductoBorrado()
    fun onError(error: Exception?)
}

interface DisminuirCantidadAComprarCallBack {
    fun onCantidadDisminuida()
    fun onError(error: Exception?)
}

interface AumentarCantidadAComprarCallBack {
    fun onCantidadAumentada()
    fun onError(error: Exception?)
}

interface BorrarProductoListaCompraCallBack {
    fun onProductoBorrado()
    fun onError(error: Exception?)
}
