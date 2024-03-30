package com.example.midespensapp.ui.despensa

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
import com.example.midespensapp.DB.ObtenerProductosDespensaCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.MainActivity2
import com.example.midespensapp.R
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.Exception

class DespensaFragment : Fragment() {

    private lateinit var lvListaProductos: ListView
    private val realTimeManager = RealTimeManager()
    private lateinit var botonAnadirProducto: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_despensa, container, false)
        lvListaProductos = view.findViewById(R.id.listViewDespensa)

        //obtener los productos de la lista de la compra y mostrarlos en un log

        botonAnadirProducto = view.findViewById(R.id.btn_add_producto)
        botonAnadirProducto.setOnClickListener {
            val intent = Intent(context, MainActivity2::class.java)
            startActivity(intent)
        }
        listarProductosDespensa()

        return view
    }

    private fun getCasaForCurrentUser(callback: ObtenerCasaPorIdUsuarioCallBack) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            realTimeManager.obtenerCasaPorIdUsuario(userId, callback)
            Log.d("DespensaFragment", "Id de usuario: $userId")
        } else {
            // Handle the case when the user is not logged in
            Log.e("DespensaFragment", "User not logged in")
        }
    }

    private fun listarProductosDespensa() {
        getCasaForCurrentUser(object : ObtenerCasaPorIdUsuarioCallBack {
            override fun onCasaObtenida(casa: Casa?) {
                if (casa != null) {
                    Log.d("DespensaFragment", "Casa obtenida: ${casa.id}")
                    realTimeManager.obtenerProductosDespensa(casa.id, object : ObtenerProductosDespensaCallBack {

                        override fun onProductosObtenidos(productos: MutableList<ProductoDespensa>) {
                            Log.d("DespensaFragment", "Productos obtenidos: ${productos.size}")
                            lvListaProductos.adapter = ProductosListaDespensaAdapter(requireContext(), productos)
                        }

                        override fun onError(error: Exception?) {
                            Log.e("DespensaFragment", "Error obteniendo productos: ${error?.message}")
                        }
                    })
                } else {
                    Log.e("DespensaFragment", "No se encontró la casa para el usuario actual")
                }
            }

            override fun onError(error: Exception?) {
                Log.e("DespensaFragment", "Error obteniendo casa: ${error?.message}")
            }
        })
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
                //aumentarCantidadProducto(producto)
                tvStockActual.text = producto.stockActual.toString()

            }

            val btnMenos = view.findViewById<Button>(R.id.btnMenos)
            btnMenos.setOnClickListener {
                // Disminuir la cantidad del producto en la lista de la compra
                //disminuirCantidadProducto(producto)
                tvStockActual.text = producto.stockActual.toString()

            }

            return view
        }
    }
}