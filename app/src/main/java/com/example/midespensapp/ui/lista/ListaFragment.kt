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
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.midespensapp.DB.ObtenerCasaPorIdUsuarioCallBack
import com.example.midespensapp.DB.ObtenerProductosListaCompraCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.MainActivity2
import com.example.midespensapp.R
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoListaCompra
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseError
import com.google.firebase.ktx.Firebase


class ListaFragment : Fragment() {

    private lateinit var lvListaProductos: ListView
    private val realTimeManager = RealTimeManager()
    private lateinit var botonAnadirProducto: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño de tu fragmento
        val view = inflater.inflate(R.layout.fragment_lista, container, false)
        // Obtener la referencia de la lista de productos
        lvListaProductos = view.findViewById(R.id.lvListaCompra)
        botonAnadirProducto = view.findViewById(R.id.btn_add_producto)
        botonAnadirProducto.setOnClickListener {
            val intent = Intent(context, MainActivity2::class.java)
            startActivity(intent)
        }
        //obtener los productos de la lista de la compra y mostrarlos en un log
        //listarProductosListaCompra()


        return view
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

}
