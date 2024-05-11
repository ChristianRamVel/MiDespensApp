package com.example.midespensapp.controlador

import com.example.midespensapp.modelo.Casa
import com.example.midespensapp.modelo.ProductoDespensa
import com.example.midespensapp.modelo.ProductoListaCompra

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
    fun onCasaExiste()

    fun onCasaNoExiste()
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

interface ObtenerUsuariosPorIdCasaCallBack {
    fun onUsuariosObtenidos(usuarios: List<String>)
    fun onError(error: Exception?)
}

interface BorrarCasaCallBack {
    fun onCasaBorrada()
    fun onError(error: Exception?)
}

interface BorrarUsuarioCallBack {
    fun onUsuarioBorrado()
    fun onError(error: Exception?)
}

interface DisminuirStockDespensaCallBack {
    fun onStockDisminuido()
    fun onError(error: Exception?)
}

interface AumentarStockDespensaCallBack {
    fun onStockAumentado()
    fun onError(error: Exception?)
}