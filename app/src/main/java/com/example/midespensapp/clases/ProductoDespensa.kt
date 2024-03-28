package com.example.midespensapp.clases


data class ProductoDespensa(
    val nombre: String? = null,
    val stockMinimo: Int? = null,
    var stockActual: Int? = null
) {
    // Función para convertir un objeto ProductoDespensa a un mapa
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nombre" to nombre,
            "stockMinimo" to stockMinimo,
            "stockActual" to stockActual
        )
    }
}