package com.example.examenrecuperacion_crv.DB

import android.provider.BaseColumns

class DBTareas {
    object tareas : BaseColumns {
        const val TABLE_NAME = "tareas"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_DIA = "dia"
        const val COLUMN_HORA = "hora"
        const val COLUMN_COMPLETADO = "completado"
    }
}