package com.example.midespensapp.ui.lista

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.midespensapp.DB.BorrarProductoDespensaCallBack
import com.example.midespensapp.DB.ObtenerCasaPorIdUsuarioCallBack
import com.example.midespensapp.DB.ObtenerProductosListaCompraCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.MainActivity3
import com.example.midespensapp.R
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class ListaFragment : Fragment() {

    private lateinit var lvListaProductos: ListView
    private val realTimeManager = RealTimeManager()
    private lateinit var botonAnadirProducto: Button
    lateinit var botonAnadirDespensa: Button
    lateinit var botonBorrarProductosSeleccionados: Button
    var alMenosUnProductoSeleccionado = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño de tu fragmento
        val view = inflater.inflate(R.layout.fragment_lista, container, false)
        // Obtener la referencia de la lista de productos
        lvListaProductos = view.findViewById(R.id.lvListaCompra)
        botonAnadirProducto = view.findViewById(R.id.btn_add_producto)
        botonAnadirDespensa = view.findViewById(R.id.btn_añadir_seleccionados_despensa)
        botonBorrarProductosSeleccionados = view.findViewById(R.id.btn_borrar_seleccionados)


        return view
    }



    private fun configurarBotones() {
        botonAnadirProducto.setOnClickListener {
            val intent = Intent(context, MainActivity3::class.java)
            startActivity(intent)
        }

        botonAnadirDespensa.setOnClickListener {
            botonAnadirDespensaOnClick()
            listarProductosListaCompra()
        }

        botonBorrarProductosSeleccionados.setOnClickListener {
            borrarProductosSeleccionados()
            listarProductosListaCompra()
        }
    }
    private fun getCasaForCurrentUser(callback: ObtenerCasaPorIdUsuarioCallBack) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            realTimeManager.obtenerCasaPorIdUsuario(userId, callback)
            Log.d("ListaFragment", "Id de usuario: $userId")
        } else {
            Log.e("ListaFragment", "Usuario no autenticado")
        }
    }

    private fun procesarProductosSeleccionados(accion: (ProductoListaCompra) -> Unit) {
        for (i in 0 until lvListaProductos.count) {
            val producto = lvListaProductos.adapter.getItem(i) as ProductoListaCompra
            if (producto.comprado) {
                accion(producto)
            }
        }
    }

    private fun borrarProductosSeleccionados() {
        procesarProductosSeleccionados { producto ->
            borrarProductoEnListaCompra(producto)
        }
        listarProductosListaCompra()
    }

    private fun botonAnadirDespensaOnClick() {
        procesarProductosSeleccionados { producto ->
            guardarProductoEnDespensaDesdeLista(producto.nombre, producto.cantidadAComprar)
        }
    }

    private fun borrarProductoEnListaCompra(producto: ProductoListaCompra) {
        getCasaForCurrentUser(object : ObtenerCasaPorIdUsuarioCallBack {
            override fun onCasaObtenida(casa: Casa?) {
                if (casa != null) {
                    Log.d("CompraFragment", "Casa obtenida: ${casa.id}")
                    realTimeManager.borrarProductoListaCompra(casa.id, producto, object :
                        BorrarProductoDespensaCallBack {

                        override fun onProductoBorrado() {
                            Log.d("CompraFragment", "Producto borrado de la lista de la compra correctamente")
                        }

                        override fun onError(error: Exception?) {
                            Log.e("CompraFragment", "Error borrando producto: ${error?.message}")
                        }
                    })
                } else {
                    Log.e("CompraFragment", "No se encontró la casa para el usuario actual")
                }
            }

            override fun onError(error: Exception?) {
                Log.e("CompraFragment", "Error obteniendo casa: ${error?.message}")
            }
        })
    }


    private fun listarProductosListaCompra() {
        getCasaForCurrentUser(object : ObtenerCasaPorIdUsuarioCallBack {
            override fun onCasaObtenida(casa: Casa?) {
                if (casa != null) {
                    Log.d("CompraFragment", "Casa obtenida: ${casa.id}")
                    realTimeManager.obtenerProductosListaCompra(casa.id, object :
                        ObtenerProductosListaCompraCallBack {

                        override fun onProductosObtenidos(productos: MutableList<ProductoListaCompra>) {
                            if (isAdded) { // Verifica si el fragmento está adjunto a la actividad
                                Log.d("CompraFragment", "Productos obtenidos: ${productos.size}")
                                lvListaProductos.adapter = ProductosListaCompraAdapter(requireContext(), productos)
                            } else {

                                Log.e("CompraFragment", "Fragmento no adjunto a una actividad")
                            }
                        }

                        override fun onError(error: Exception?) {
                            Log.e("CompraFragment", "Error obteniendo productos: ${error?.message}")
                        }
                    })
                } else {
                    Log.e("CompraFragment", "No se encontró la casa para el usuario actual")
                }
            }

            override fun onError(error: Exception?) {
                Log.e("CompraFragment", "Error obteniendo casa: ${error?.message}")
            }
        })
    }

    private fun guardarProductoEnDespensaDesdeLista(nombreProducto: String, cantidadComprada:Int) {
        // 1. Obtener referencia a la base de datos Firebase
        val databaseReference = FirebaseDatabase.getInstance().reference

        // 2. Obtener el ID de la casa del usuario actual (asumiendo que ya tienes implementada la obtención del ID del usuario)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                ObtenerCasaPorIdUsuarioCallBack {
                override fun onCasaObtenida(casa: Casa?) {
                    if (casa != null) {

                        //comprobamos si el producto ya existe en la despensa
                        if(casa.productosDespensa.containsKey(nombreProducto)){
                        //actualizar el producto, sumando la cantidad comprada a la cantidadActual del producto
                            val productoDespensa = casa.productosDespensa[nombreProducto]
                            val cantidadActual = productoDespensa?.stockActual?.plus(cantidadComprada)
                            databaseReference.child("casas").child(casa.id).child("productosDespensa").child(nombreProducto).child("stockActual").setValue(cantidadActual)
                                .addOnSuccessListener {
                                    Log.d("ListaFragment", "Producto $nombreProducto actualizado en la despensa correctamente")
                                    // Éxito al guardar el producto en la despensa
                                    Toast.makeText(requireContext(), "Producto actualizado en la despensa correctamente", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    // Error al guardar el producto en la despensa
                                    Log.e("ListaFragment", "Error al guardar el producto en la despensa", e)
                                    Toast.makeText(requireContext(), "Error al guardar el producto en la despensa", Toast.LENGTH_SHORT).show()
                                }
                        }else{
                            // 3. Crear un objeto ProductoDespensa con los datos proporcionados
                            val productoDespensa = ProductoDespensa(nombreProducto, cantidadComprada, 1)

                            // 4. Utilizar el nombre del producto como clave para almacenar el objeto ProductoDespensa en la lista productosDespensa de la casa
                            databaseReference.child("casas").child(casa.id).child("productosDespensa").child(nombreProducto).setValue(productoDespensa)
                                .addOnSuccessListener {
                                    Log.d("ListaFragment", "Producto $nombreProducto guardado en la despensa correctamente")
                                    // Éxito al guardar el producto en la despensa
                                    Toast.makeText(requireContext(), "Producto guardado en la despensa correctamente", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    // Error al guardar el producto en la despensa
                                    Log.e("ListaFragment", "Error al guardar el producto en la despensa", e)
                                    Toast.makeText(requireContext(), "Error al guardar el producto en la despensa", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Log.e("ListaFragment", "No se encontró la casa para el usuario actual")
                        Toast.makeText(requireContext(), "No se encontró la casa para el usuario actual", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(error: Exception?) {
                    Log.e("ListaFragment", "Error obteniendo casa: ${error?.message}")
                    Toast.makeText(requireContext(), "Error obteniendo casa: ${error?.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // El usuario no está autenticado
            Log.e("ListaFragment", "El usuario no está autenticado")
            Toast.makeText(requireContext(), "El usuario no está autenticado", Toast.LENGTH_SHORT).show()
        }
    }


    class ProductosListaCompraAdapter(
        context: Context,
        listaProductos: MutableList<ProductoListaCompra>
    ) :
        BaseAdapter() {
        private val mContext: Context = context
        private val listaDeProductos: MutableList<ProductoListaCompra> = listaProductos

        override fun getCount(): Int {
            return listaDeProductos.size
        }

        override fun getItem(position: Int): Any {
            return listaDeProductos[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val view = layoutInflater.inflate(R.layout.item_lista, parent, false)

            val producto = listaDeProductos[position]
            val tvNombreProducto = view.findViewById<TextView>(R.id.nombreProducto)
            val tvCantidadProducto = view.findViewById<TextView>(R.id.textoCantidadItem)
            val checkBox = view.findViewById<CheckBox>(R.id.checkBox)

            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    producto.comprado = true
                } else {
                    producto.comprado = false
                }
            }

            tvNombreProducto.text = producto.nombre
            tvCantidadProducto.text = producto.cantidadAComprar.toString()

            val btnMas = view.findViewById<Button>(R.id.btnMas)
            btnMas.setOnClickListener {
                // Aumentar la cantidad del producto en la lista de la compra
                //aumentarCantidadProducto(producto)
                tvCantidadProducto.text = producto.cantidadAComprar.toString()

            }

            val btnMenos = view.findViewById<Button>(R.id.btnMenos)
            btnMenos.setOnClickListener {
                // Disminuir la cantidad del producto en la lista de la compra
                //disminuirCantidadProducto(producto)
                tvCantidadProducto.text = producto.cantidadAComprar.toString()

            }

            return view
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Configurar los botones de la interfaz de usuario
        configurarBotones()
        listarProductosListaCompra()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar la lista de productos cuando el fragmento se vuelva a mostrar
        listarProductosListaCompra()
    }

}
