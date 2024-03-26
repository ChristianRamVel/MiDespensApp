package com.example.midespensapp.clases

data class Usuario(
    var idUsuario: String? = null,
    var email: String? = null,
    var idCasa: String? = null
) {
    // Función para convertir un objeto Usuario a un mapa
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "idUsuario" to idUsuario,
            "email" to email,
            "idCasa" to idCasa
        )
    }
}