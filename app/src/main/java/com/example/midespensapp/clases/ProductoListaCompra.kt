package com.example.midespensapp.clases


data class ProductoListaCompra(
    val nombre: String? = null,
    var cantidadAComprar: Int? = null,
    val comprado: Boolean? = null
) {

    // Función para convertir un objeto ProductoListaCompra a un mapa
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nombre" to nombre,
            "cantidadAComprar" to cantidadAComprar,
            "comprado" to comprado
        )
    }
}