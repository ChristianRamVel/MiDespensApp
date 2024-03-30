package com.example.midespensapp.clases

import com.google.gson.annotations.SerializedName
import java.lang.reflect.Constructor


class Casa(
    @SerializedName("id") val id: String = "",
    @SerializedName("productosDespensa") val productosDespensa: Map<String, ProductoDespensa> = mapOf(),
    @SerializedName("productosListaCompra") val productosListaCompra: Map<String, ProductoListaCompra> = mapOf()
) {
    // Constructor sin argumentos requerido por Firebase para deserialización
    constructor() : this("", mapOf(), mapOf())
}