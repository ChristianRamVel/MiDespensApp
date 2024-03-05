package com.example.midespensapp.ui.despensa

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.examenrecuperacion_crv.DB.DbHelper
import com.example.midespensapp.R
import com.example.midespensapp.clases.Producto

class DespensaViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text

    private val _despensa = MutableLiveData<String>().apply {
        value = "Stock de la despensa"
    }
    val despensa: LiveData<String> = _despensa

    private val _productos = MutableLiveData<String>().apply {
        value = "Productos"
    }

    val productos: LiveData<String> = _productos

    private val _cantidadAComprar = MutableLiveData<String>().apply {
        value = "Cantidad a comprar"
    }

    val cantidadAComprar: LiveData<String> = _cantidadAComprar

    private val _comprado = MutableLiveData<String>().apply {
        value = "Comprado"
    }

    val comprado: LiveData<String> = _comprado

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
            val tarea = getItem(position) as Producto
            var view = convertView
            if (view == null) {
                val inflater = LayoutInflater.from(mContext)
                view = inflater.inflate(R.layout.item_despensa, parent, false)
            }

            val textViewNombreTarea = view?.findViewById<TextView>(R.id.textoTareaItem)
            textViewNombreTarea?.text = tarea.nombre

            return view!!
        }
    }
}