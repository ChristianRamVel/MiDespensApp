package com.example.midespensapp.ui.lista

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.examenrecuperacion_crv.DB.DbHelper
import com.example.midespensapp.MainActivity2
import com.example.midespensapp.R
import com.example.midespensapp.clases.Producto
import com.example.midespensapp.databinding.FragmentListaBinding

class ListaFragment : Fragment() {

    private var _binding: FragmentListaBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val listaViewModel = ViewModelProvider(this).get(ListaViewModel::class.java)

        _binding = FragmentListaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val botonMas = binding.buttonAddProduct
        mostrarProductos()
        botonMas.setOnClickListener {
            //abrir activity para añadir producto

            val intent = Intent(requireContext(), MainActivity2::class.java)
            startActivity(intent)

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //funcion para mostrar los productos de la base de datos en la lista
    fun mostrarProductos() {
        val dbHelper = DbHelper(requireContext())
        val productos = dbHelper.getAllProductos()
        val adapter = ProductosAdapter(requireContext(), productos)

        val listaProductos = binding.listView
        listaProductos.adapter = adapter

    }

    //clase para el adaptador de la lista
    class ProductosAdapter(context: Context, productos: List<Producto>) : BaseAdapter() {
        private val mContext: Context = context
        private val listaDeProductos: List<Producto> = productos

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
            val producto = getItem(position) as Producto
            var view = convertView
            if (view == null) {
                val inflater = LayoutInflater.from(mContext)
                view = inflater.inflate(R.layout.item_despensa, parent, false)
            }

            val nombreProducto = view!!.findViewById<EditText>(R.id.nombreProducto)

            nombreProducto.setText(producto.nombre)

            return view
        }
    }
}