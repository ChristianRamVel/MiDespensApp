package com.example.midespensapp.ui.lista

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.midespensapp.DB.ObtenerDatosCallBack
import com.example.midespensapp.DB.ObtenerProductosListaCompraCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.R
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoListaCompra
import com.example.midespensapp.clases.Usuario
import com.google.firebase.database.DatabaseError


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
        realTimeManager.obtenerProductosListaCompraPorIdCasa("0", object : ObtenerProductosListaCompraCallBack {
            override fun onProductosObtenidos(productos: List<ProductoListaCompra>) {
                Log.d("ListaFragment", "Productos obtenidos: $productos")
                lvListaProductos.adapter = ProductosListaCompraAdapter(requireContext(), productos)
            }

            override fun onError(error: DatabaseError) {
                Log.e("ListaFragment", "Error al obtener los productos de la lista de la compra", error.toException())
            }
        })
        return view
    }
    class ProductosListaCompraAdapter(context: Context, listaProductos: List<ProductoListaCompra>) :
        BaseAdapter() {
        private val mContext: Context = context
        private val listaDeProductos: List<ProductoListaCompra> = listaProductos

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

            return view
        }


    }


}

















