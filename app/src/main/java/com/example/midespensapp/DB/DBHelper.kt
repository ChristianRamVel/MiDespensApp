package com.example.examenrecuperacion_crv.DB


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.midespensapp.clases.Producto


class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Bloque companion object para definir constantes que serán usadas en toda la clase.
    // Son como los valores estáticos en Java
    companion object {
        // Nombre de la base de datos.
        private const val DATABASE_NAME = "ProductosDatabase"
        // Versión de la base de datos, útil para manejar actualizaciones esquemáticas.
        private const val DATABASE_VERSION = 1
        // Nombre de la tabla donde se almacenarán los discos.
        private const val TABLE_PRODUCTOS = "productos"
        // Nombres de las columnas de la tabla.
        private const val KEY_ID = "id"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_CANTIDAD_MINIMA = "cantidadMinima"
        private const val KEY_CANTIDAD_ACTUAL = "cantidadActual"
        private const val KEY_CANTIDAD_COMPRAR = "cantidadAComprar"
        private const val KEY_COMPRADO = "comprado"
    }

    // Método llamado cuando la base de datos se crea por primera vez.
    override fun onCreate(db: SQLiteDatabase) {
        // Define la sentencia SQL para crear la tabla de productos.
        val CREATE_PRODUCTOS_TABLE = ("CREATE TABLE $TABLE_PRODUCTOS($KEY_ID INTEGER PRIMARY KEY, " +
                "$KEY_NOMBRE TEXT, " +
                "$KEY_CANTIDAD_MINIMA INTEGER," +
                "$KEY_CANTIDAD_ACTUAL INTEGER, " +
                "$KEY_CANTIDAD_COMPRAR INTEGER," +
                "$KEY_COMPRADO INTEGER DEFAULT 0)")
        // Ejecuta la sentencia SQL.
        db.execSQL(CREATE_PRODUCTOS_TABLE)
    }

    // Método llamado cuando se necesita actualizar la base de datos, por ejemplo, cuando se incrementa DATABASE_VERSION.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Elimina la tabla existente y crea una nueva.
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTOS")
        onCreate(db)
    }

    // Método para obtener todos los productos de la base de datos.
    fun getAllProductos(): ArrayList<Producto> {
        val productosList = ArrayList<Producto>()
        val selectQuery = "SELECT * FROM $TABLE_PRODUCTOS"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var id: Int
        var nombre: String
        var cantidadMinima: Int
        var cantidadActual: Int
        var cantidadAComprar: Int
        var comprado: Int
        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(KEY_ID)
                val nombreIndex = cursor.getColumnIndex(KEY_NOMBRE)
                val cantidadMinimaIndex = cursor.getColumnIndex(KEY_CANTIDAD_MINIMA)
                val cantidadActualIndex = cursor.getColumnIndex(KEY_CANTIDAD_ACTUAL)
                val cantidadAComprarIndex = cursor.getColumnIndex(KEY_CANTIDAD_COMPRAR)
                val compradoIndex = cursor.getColumnIndex(KEY_COMPRADO)
                if (idIndex != -1 && nombreIndex != -1 && cantidadMinimaIndex != -1 && cantidadActualIndex != -1 && cantidadAComprarIndex != -1 && compradoIndex != -1) {
                    id = cursor.getInt(idIndex)
                    nombre = cursor.getString(nombreIndex)
                    cantidadMinima = cursor.getInt(cantidadMinimaIndex)
                    cantidadActual = cursor.getInt(cantidadActualIndex)
                    cantidadAComprar = cursor.getInt(cantidadAComprarIndex)
                    comprado = cursor.getInt(compradoIndex)
                    val producto = Producto(id = id, nombre = nombre, cantidadMinima = cantidadMinima, cantidadActual = cantidadActual, cantidadAComprar = cantidadAComprar, comprado = comprado)
                    productosList.add(producto)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return productosList
    }

    // Método para actualizar un producto en la base de datos.
    fun updateProducto(producto: Producto): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        // Prepara los valores a actualizar.
        contentValues.put(KEY_NOMBRE, producto.nombre)
        contentValues.put(KEY_CANTIDAD_MINIMA, producto.cantidadMinima)
        contentValues.put(KEY_CANTIDAD_ACTUAL, producto.cantidadActual)
        contentValues.put(KEY_CANTIDAD_COMPRAR, producto.cantidadAComprar)
        // Actualiza el producto y retorna el número de filas afectadas.
        val success = db.update(TABLE_PRODUCTOS, contentValues, "$KEY_ID = ?", arrayOf(producto.id.toString()))
        return success
    }

    // Método para eliminar un producto de la base de datos.
    fun deleteProducto(producto: Producto): Int {
        val db = this.writableDatabase
        // Elimina la fila correspondiente y retorna el número de filas afectadas.
        val success = db.delete(TABLE_PRODUCTOS, "$KEY_ID = ?", arrayOf(producto.id.toString()))
        db.close()
        return success
    }

    // Método para añadir un nuevo producto a la base de datos.
    fun addProducto(producto: Producto): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        // Prepara los valores a insertar.
        contentValues.put(KEY_NOMBRE, producto.nombre)
        contentValues.put(KEY_CANTIDAD_MINIMA, producto.cantidadMinima)
        contentValues.put(KEY_CANTIDAD_ACTUAL, producto.cantidadActual)
        contentValues.put(KEY_CANTIDAD_COMPRAR, producto.cantidadAComprar)
        contentValues.put(KEY_COMPRADO, producto.comprado)
        // Inserta el producto y retorna el ID de la fila insertada.
        val success = db.insert(TABLE_PRODUCTOS, null, contentValues)
        db.close()
        return success
    }

    fun buscarProductoPorId(productoId: Int): Producto? {
        val db = this.readableDatabase
        var producto: Producto? = null

        val selectQuery = "SELECT * FROM $TABLE_PRODUCTOS WHERE $KEY_ID = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(productoId.toString()))
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOMBRE))
            val cantidadMinima = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CANTIDAD_MINIMA))
            val cantidadActual = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CANTIDAD_ACTUAL))
            val cantidadAComprar = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CANTIDAD_COMPRAR))
            val comprado = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_COMPRADO))
            producto = Producto(id = id, nombre = nombre, cantidadMinima = cantidadMinima, cantidadActual = cantidadActual, cantidadAComprar = cantidadAComprar, comprado = comprado)
        }
        cursor.close()
        return producto
    }

    fun ponerProuctoComoComprado(producto: Producto): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_COMPRADO, 1)
        return db.update(TABLE_PRODUCTOS, contentValues, "$KEY_ID = ?", arrayOf(producto.id.toString()))
    }

    fun getProductosComprados(): ArrayList<Producto>{
        val productosList = ArrayList<Producto>()
        val selectQuery = "SELECT * FROM $TABLE_PRODUCTOS WHERE KEY_$KEY_COMPRADO = 1"
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        var id: Int
        var nombre: String
        var cantidadMinima: Int
        var cantidadActual: Int
        var cantidadAComprar: Int
        var comprado: Int
        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(KEY_ID)
                val nombreIndex = cursor.getColumnIndex(KEY_NOMBRE)
                val cantidadMinimaIndex = cursor.getColumnIndex(KEY_CANTIDAD_MINIMA)
                val cantidadActualIndex = cursor.getColumnIndex(KEY_CANTIDAD_ACTUAL)
                val cantidadAComprarIndex = cursor.getColumnIndex(KEY_CANTIDAD_COMPRAR)
                val compradoIndex = cursor.getColumnIndex(KEY_COMPRADO)
                if (idIndex != -1 && nombreIndex != -1 && cantidadMinimaIndex != -1 && cantidadActualIndex != -1 && cantidadAComprarIndex != -1 && compradoIndex != -1) {
                    id = cursor.getInt(idIndex)
                    nombre = cursor.getString(nombreIndex)
                    cantidadMinima = cursor.getInt(cantidadMinimaIndex)
                    cantidadActual = cursor.getInt(cantidadActualIndex)
                    cantidadAComprar = cursor.getInt(cantidadAComprarIndex)
                    comprado = cursor.getInt(compradoIndex)
                    val producto = Producto(id = id, nombre = nombre, cantidadMinima = cantidadMinima, cantidadActual = cantidadActual, cantidadAComprar = cantidadAComprar, comprado = comprado)
                    productosList.add(producto)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return productosList
    }

    //funcion para actualizar la cantidad actual de un producto
    fun actualizarCantidadActual(producto: Producto, cantidad: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_CANTIDAD_ACTUAL, cantidad)
        return db.update(TABLE_PRODUCTOS, contentValues, "$KEY_ID = ?", arrayOf(producto.id.toString()))
    }

    //funcion para actualizar la cantidad a comprar de un producto
    fun actualizarCantidadAComprar(producto: Producto, cantidad: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_CANTIDAD_COMPRAR, cantidad)
        return db.update(TABLE_PRODUCTOS, contentValues, "$KEY_ID = ?", arrayOf(producto.id.toString()))
    }

    //funcion para obtener la cantidad actual de un producto
    @SuppressLint("Range")
    fun getCantidadActual(producto: Producto): Int {
        val db = this.readableDatabase
        val selectQuery = "SELECT $KEY_CANTIDAD_ACTUAL FROM $TABLE_PRODUCTOS WHERE $KEY_ID = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(producto.id.toString()))
        var cantidad = 0
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(cursor.getColumnIndex(KEY_CANTIDAD_ACTUAL))
        }
        cursor.close()
        return cantidad
    }

    //funcion para obtener la cantidad a comprar de un producto
    @SuppressLint("Range")
    fun getCantidadAComprar(producto: Producto): Int {
        val db = this.readableDatabase
        val selectQuery = "SELECT $KEY_CANTIDAD_COMPRAR FROM $TABLE_PRODUCTOS WHERE $KEY_ID = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(producto.id.toString()))
        var cantidad = 0
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(cursor.getColumnIndex(KEY_CANTIDAD_COMPRAR))
        }
        cursor.close()
        return cantidad
    }

    //funcion para obtener la cantidad minima de un producto
    @SuppressLint("Range")
    fun getCantidadMinima(producto: Producto): Int {
        val db = this.readableDatabase
        val selectQuery = "SELECT $KEY_CANTIDAD_MINIMA FROM $TABLE_PRODUCTOS WHERE $KEY_ID = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(producto.id.toString()))
        var cantidad = 0
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(cursor.getColumnIndex(KEY_CANTIDAD_MINIMA))
        }
        cursor.close()
        return cantidad
    }
}