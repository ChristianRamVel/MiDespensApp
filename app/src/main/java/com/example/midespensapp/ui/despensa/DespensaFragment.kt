package com.example.midespensapp.ui.despensa

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
import com.example.midespensapp.DB.ObtenerProductosDespensaCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.R
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseError
import com.google.firebase.ktx.Firebase

class DespensaFragment : Fragment() {

    private lateinit var lvListaProductos: ListView
    private val realTimeManager = RealTimeManager()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_despensa, container, false)
        lvListaProductos = view.findViewById(R.id.listViewDespensa)

        //obtener los productos de la lista de la compra y mostrarlos en un log
        listarProductosDespensa()
        //obtener la casa del usuario y mostrarla en un log


        return view
    }

    private fun listarProductosDespensa() {
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
                    obtenerProductosDespensa(casa)
                }

                override fun onError(error: DatabaseError) {
                    Log.e("ListaFragment", "Error al obtener la casa", error.toException())
                }
            })
    }

    private fun obtenerProductosDespensa(casa: Casa) {
        realTimeManager.obtenerProductosDespensaPorIdCasa(
            casa.id.toString(),
            object : ObtenerProductosDespensaCallBack {
                override fun onProductosObtenidos(productos: MutableList<ProductoDespensa>) {
                    Log.d("ListaFragment", "Productos obtenidos: $productos")
                    // Crear un adaptador para mostrar los productos en un ListView
                    val adapter = ProductosListaDespensaAdapter(requireContext(), productos)
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

    class ProductosListaDespensaAdapter(
        context: Context,
        listaProductos: MutableList<ProductoDespensa>
    ) :
        BaseAdapter() {
        private val mContext: Context = context
        private val listaDeProductos: MutableList<ProductoDespensa> = listaProductos
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
            val view = layoutInflater.inflate(R.layout.item_despensa, parent, false)

            val producto = listaDeProductos[position]
            val tvNombreProducto = view.findViewById<TextView>(R.id.nombreProducto)
            val tvStockMinimo = view.findViewById<TextView>(R.id.textoStockMinimo)
            val tvStockActual = view.findViewById<TextView>(R.id.textoStockActual)

            tvNombreProducto.text = producto.nombre
            tvStockMinimo.text = producto.stockMinimo.toString()
            tvStockActual.text = producto.stockActual.toString()


            val btnMas = view.findViewById<Button>(R.id.btnMas)
            btnMas.setOnClickListener {
                // Aumentar la cantidad del producto en la lista de la compra
                aumentarCantidadProducto(producto)
                tvStockActual.text = producto.stockActual.toString()

            }

            val btnMenos = view.findViewById<Button>(R.id.btnMenos)
            btnMenos.setOnClickListener {
                // Disminuir la cantidad del producto en la lista de la compra
                disminuirCantidadProducto(producto)
                tvStockActual.text = producto.stockActual.toString()

            }

            return view
        }

        //funcion para disminuir la cantidad de un producto en la lista de la compra
        fun disminuirCantidadProducto(producto: ProductoDespensa) {

            val auth = Firebase.auth.currentUser?.uid
            val idUsuario = auth.toString()
            val casaManager = CasaManager()
            casaManager.obtenerCasaPorIdUsuario(idUsuario) { casa ->
                if (casa != null) {
                    realTimeManager.disminuirCantidadAComprarDespensa(
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

        fun aumentarCantidadProducto(producto: ProductoDespensa) {
            // Obtener la casa del usuario

            val auth = Firebase.auth.currentUser?.uid
            val idUsuario = auth.toString()
            val casaManager = CasaManager()
            casaManager.obtenerCasaPorIdUsuario(idUsuario) { casa ->
                if (casa != null) {
                    realTimeManager.aumentarCantidadAComprarDespensa(
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
            obtenerProductosDespensa { nuevosProductos ->
                listaDeProductos.clear()
                listaDeProductos.addAll(nuevosProductos)
                notifyDataSetChanged()
            }
        }

        // Función para obtener los productos de la lista de compra
        private fun obtenerProductosDespensa(callback: (MutableList<ProductoDespensa>) -> Unit) {
            val auth = Firebase.auth.currentUser?.uid
            val idUsuario = auth.toString()
            val casaManager = CasaManager()
            casaManager.obtenerCasaPorIdUsuario(idUsuario) { casa ->
                if (casa != null) {
                    realTimeManager.obtenerProductosDespensaPorIdCasa(
                        casa.id.toString(),
                        object : ObtenerProductosDespensaCallBack {
                            override fun onProductosObtenidos(productos: MutableList<ProductoDespensa>) {
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