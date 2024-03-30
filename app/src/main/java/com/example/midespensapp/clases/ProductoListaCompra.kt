package com.example.midespensapp.clases

import com.google.gson.annotations.SerializedName


class ProductoListaCompra(
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("cantidadAComprar") val cantidadAComprar: Int = 0,
    @SerializedName("comprado") val comprado: Boolean = false
) {
    // Constructor sin argumentos requerido por Firebase para deserialización
    constructor() : this("", 0, false)
}