package com.example.midespensapp.ui.despensa

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.midespensapp.R

class DespensaFragment : Fragment() {

    private lateinit var lvListaProductos: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_despensa, container, false)
        lvListaProductos = view.findViewById(R.id.listViewDespensa)

        return view
    }









/*
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
    */
}