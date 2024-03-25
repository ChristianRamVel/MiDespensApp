package com.example.midespensapp.ui.lista

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.examenrecuperacion_crv.DB.DbHelper
import com.example.midespensapp.MainActivity2
import com.example.midespensapp.R
import com.example.midespensapp.clases.Producto


class ListaFragment : Fragment() {

    private lateinit var dbHandler: DbHelper
    private lateinit var lvListaProductos: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño de tu fragmento
        val view = inflater.inflate(R.layout.fragment_lista, container, false)

        // Inicializar la base de datos y la lista de productos
        //dbHandler = DbHelper(requireContext())
        //listarProductos()

        lvListaProductos = view.findViewById(R.id.lvListaCompra)

        return view
    }

    private fun listarProductos() {
        lvListaProductos.adapter = ProductosAdapter(requireContext(), dbHandler.getAllProductos())
    }

    class ProductosAdapter(context: Context, tareas: List<Producto>) : BaseAdapter() {
        private val mContext: Context = context
        private val listaDeProductos: List<Producto> = tareas

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

            val dbHandler = DbHelper(mContext)
            val producto = getItem(position) as Producto
            var view = convertView
            if (view == null) {
                val inflater = LayoutInflater.from(mContext)
                view = inflater.inflate(R.layout.item_lista, parent, false)
            }

            val textViewNombreProducto = view?.findViewById<TextView>(R.id.nombreProducto)
            textViewNombreProducto?.text = producto.nombre

            val textViewCantidadProducto = view?.findViewById<TextView>(R.id.textoCantidadItem)
            textViewCantidadProducto?.text = producto.cantidadAComprar.toString()

            val buttonSumarProducto = view?.findViewById<TextView>(R.id.btnMas)
            buttonSumarProducto?.setOnClickListener {
                textViewCantidadProducto?.text =
                    (textViewCantidadProducto?.text.toString().toInt() + 1).toString()
                dbHandler.actualizarCantidadAComprar(
                    producto,
                    textViewCantidadProducto?.text.toString().toInt()
                )
            }

            val buttonRestarProducto = view?.findViewById<TextView>(R.id.btnMenos)
            buttonRestarProducto?.setOnClickListener {
                textViewCantidadProducto?.text =
                    (textViewCantidadProducto?.text.toString().toInt() - 1).toString()
                dbHandler.actualizarCantidadAComprar(
                    producto,
                    textViewCantidadProducto?.text.toString().toInt()
                )
            }
            return view!!
        }
    }
}