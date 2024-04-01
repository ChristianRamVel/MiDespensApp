package com.example.midespensapp.clases

import com.google.gson.annotations.SerializedName

class ProductoDespensa(
    @SerializedName("nombre") var nombre: String = "",
    @SerializedName("stockActual") var stockActual: Int = 0,
    @SerializedName("stockMinimo") var stockMinimo: Int = 0,
    var seleccionado : Boolean = false
){
    // Constructor sin argumentos requerido por Firebase para deserialización
    constructor() : this("", 0, 0)
    //constuctor sin argumentos
}
