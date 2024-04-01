package com.example.midespensapp.clases

import com.google.gson.annotations.SerializedName

class ProductoDespensa(
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("stockActual") var stockActual: Int = 0,
    @SerializedName("stockMinimo") val stockMinimo: Int = 0
){
    // Constructor sin argumentos requerido por Firebase para deserialización
    constructor() : this("", 0, 0)
}
