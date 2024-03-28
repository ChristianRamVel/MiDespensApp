package com.example.midespensapp.DB

import com.example.midespensapp.clases.Casa
import com.google.firebase.database.DatabaseError

class CasaManager {

    private val realTimeManager = RealTimeManager()

    fun obtenerCasaPorIdUsuario(idUsuario: String, callback: (Casa?) -> Unit) {
        realTimeManager.obtenerCasaPorIdUsuario(idUsuario, object : ObtenerCasaPorIdUsuarioCallBack {
            override fun onCasaObtenida(casa: Casa) {
                callback(casa)
            }

            override fun onError(error: DatabaseError) {
                // Manejo de errores, puedes implementar una lógica para manejar el error aquí
                callback(null)
            }
        })
    }
}