package com.example.midespensapp.clases

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Usuario(
    @SerializedName("email") val email: String = "",
    @SerializedName("idCasa") val idCasa: String = "",

)
{
    constructor() : this("", "")
}