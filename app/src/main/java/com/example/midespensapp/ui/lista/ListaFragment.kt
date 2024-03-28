package com.example.midespensapp.ui.lista

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.midespensapp.DB.CasaManager
import com.example.midespensapp.DB.ObtenerCasaPorIdUsuarioCallBack
import com.example.midespensapp.DB.ObtenerProductosListaCompraCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.R
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoListaCompra
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseError
import com.google.firebase.ktx.Firebase


class ListaFragment : Fragment() {

    private lateinit var lvListaProductos: ListView
    private val realTimeManager = RealTimeManager()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño de tu fragmento
        val view = inflater.inflate(R.layout.fragment_lista, container, false)
        // Obtener la referencia de la lista de productos
        lvListaProductos = view.findViewById(R.id.lvListaCompra)
        //obtener los productos de la lista de la compra y mostrarlos en un log
        listarProductosListaCompra()
        //obtener la casa del usuario y mostrarla en un log


        return view
    }

    private fun listarProductosListaCompra() {
        // Obtener la casa del usuario
        obtenerCasaPorIdUsuario()
    }

    private fun obtenerCasaPorIdUsuario() {
        realTimeManager.obtenerCasaPorIdUsuario(
            obtenerUsuarioLogueado(),
            object : ObtenerCasaPorIdUsuarioCallBack {
                override fun onCasaObtenida(casa: Casa) {
                    Log.d("ListaFragment", "Casa obtenida: $casa")
                    // Una vez que se obtiene la casa del usuario, obtener los productos de la lista de compra
                    obtenerProductosListaCompra(casa)
                }

                override fun onError(error: DatabaseError) {
                    Log.e("ListaFragment", "Error al obtener la casa", error.toException())
                }
            })
    }

    private fun obtenerProductosListaCompra(casa: Casa) {
        realTimeManager.obtenerProductosListaCompraPorIdCasa(
            casa.id.toString(),
            object : ObtenerProductosListaCompraCallBack {
                override fun onProductosObtenidos(productos: MutableList<ProductoListaCompra>) {
                    Log.d("ListaFragment", "Productos obtenidos: $productos")
                    // Crear un adaptador para mostrar los productos en un ListView
                    val adapter = ProductosListaCompraAdapter(requireContext(), productos)
                    lvListaProductos.adapter = adapter
                }

                override fun onError(error: DatabaseError) {
                    Log.e("ListaFragment", "Error al obtener los productos", error.toException())
                }
            })
    }

    private fun obtenerUsuarioLogueado(): String {
        // Obtener el ID del usuario logueado
        val auth = Firebase.auth.currentUser?.uid
        val idUsuario = auth.toString()
        Log.d("ListaFragment", "ID del usuario logueado: $idUsuario")
        // Obtener la casa del usuario logueado
        return idUsuario
    }

    class ProductosListaCompraAdapter(
        context: Context,
        listaProductos: MutableList<ProductoListaCompra>
    ) :
        BaseAdapter() {
        private val mContext: Context = context
        private val listaDeProductos: MutableList<ProductoListaCompra> = listaProductos
        private val realTimeManager = RealTimeManager()

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

            tvNombreProducto.text = producto.nombre
            tvCantidadProducto.text = producto.cantidadAComprar.toString()

            val btnMas = view.findViewById<Button>(R.id.btnMas)
            btnMas.setOnClickListener {
                // Aumentar la cantidad del producto en la lista de la compra
                aumentarCantidadProducto(producto)
                tvCantidadProducto.text = producto.cantidadAComprar.toString()

            }

            val btnMenos = view.findViewById<Button>(R.id.btnMenos)
            btnMenos.setOnClickListener {
                // Disminuir la cantidad del producto en la lista de la compra
                disminuirCantidadProducto(producto)
                tvCantidadProducto.text = producto.cantidadAComprar.toString()

            }

            return view
        }

        //funcion para disminuir la cantidad de un producto en la lista de la compra
        fun disminuirCantidadProducto(producto: ProductoListaCompra) {

            val auth = Firebase.auth.currentUser?.uid
            val idUsuario = auth.toString()
            val casaManager = CasaManager()
            casaManager.obtenerCasaPorIdUsuario(idUsuario) { casa ->
                if (casa != null) {
                    realTimeManager.disminuirCantidadAComprarCompra(
                        casa.id.toString(),
                        producto.nombre.toString()
                    )
                    actualizarListaProductos()
                } else {
                    // Error al obtener la casa, manejarlo aquí
                    println("Error al obtener la casa.")

                }
            }
        }

        fun aumentarCantidadProducto(producto: ProductoListaCompra) {
            // Obtener la casa del usuario

            val auth = Firebase.auth.currentUser?.uid
            val idUsuario = auth.toString()
            val casaManager = CasaManager()
            casaManager.obtenerCasaPorIdUsuario(idUsuario) { casa ->
                if (casa != null) {
                    realTimeManager.aumentarCantidadAComprarCompra(
                        casa.id.toString(),
                        producto.nombre.toString()
                    )
                    actualizarListaProductos()
                } else {
                    // Error al obtener la casa, manejarlo aquí
                    println("Error al obtener la casa.")
                }
            }
        }

        private fun actualizarListaProductos() {
            // Volver a obtener los productos de la lista de compra y notificar cambios
            obtenerProductosListaCompra { nuevosProductos ->
                listaDeProductos.clear()
                listaDeProductos.addAll(nuevosProductos)
                notifyDataSetChanged()
            }
        }

        // Función para obtener los productos de la lista de compra
        private fun obtenerProductosListaCompra(callback: (MutableList<ProductoListaCompra>) -> Unit) {
            val auth = Firebase.auth.currentUser?.uid
            val idUsuario = auth.toString()
            val casaManager = CasaManager()
            casaManager.obtenerCasaPorIdUsuario(idUsuario) { casa ->
                if (casa != null) {
                    realTimeManager.obtenerProductosListaCompraPorIdCasa(
                        casa.id.toString(),
                        object : ObtenerProductosListaCompraCallBack {
                            override fun onProductosObtenidos(productos: MutableList<ProductoListaCompra>) {
                                callback(productos)
                            }

                            override fun onError(error: DatabaseError) {
                                // Manejar el error
                                println("Error al obtener los productos de la lista de compra.")
                            }
                        })
                } else {
                    // Error al obtener la casa, manejarlo aquí
                    println("Error al obtener la casa.")
                }
            }
        }
    }

}
