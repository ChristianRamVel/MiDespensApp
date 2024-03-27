package com.example.midespensapp.clases

data class Casa(
    var id: String? = null, // Cambiado de "key" a "id" para que coincida con el campo en tu base de datos
    val productosDespensa: MutableList<ProductoDespensa> = mutableListOf(),
    val productosListaCompra: MutableList<ProductoListaCompra> = mutableListOf()
) {
    // Esta función te será útil si necesitas convertir un objeto Casa a un mapa para enviarlo a Firebase
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "productosDespensa" to productosDespensa.map { it.toMap() },
            "productosListaCompra" to productosListaCompra.map { it.toMap() }
        )
    }
}