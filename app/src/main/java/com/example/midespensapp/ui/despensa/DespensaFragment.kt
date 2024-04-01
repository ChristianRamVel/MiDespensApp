package com.example.midespensapp.ui.despensa

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.midespensapp.DB.BorrarProductoDespensaCallBack
import com.example.midespensapp.DB.ObtenerCasaPorIdUsuarioCallBack
import com.example.midespensapp.DB.ObtenerProductosDespensaCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.MainActivity2
import com.example.midespensapp.R
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class DespensaFragment : Fragment() {

    private lateinit var lvListaProductos: ListView
    private val realTimeManager = RealTimeManager()
    private lateinit var botonAnadirProducto: Button
    lateinit var botonBorrarProductosSeleccionados: Button
    lateinit var botonEditarProducto: Button
    private var productosDespensa = mutableListOf<ProductoDespensa>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_despensa, container, false)
        lvListaProductos = view.findViewById(R.id.listViewDespensa)
        registerForContextMenu(lvListaProductos)
        rellenarListaDespensa()

        botonAnadirProducto = view.findViewById(R.id.btn_add_producto)
        botonBorrarProductosSeleccionados = view.findViewById(R.id.btn_borrar_seleccionados)
        botonEditarProducto = view.findViewById(R.id.btn_editarSeleccionado)

        return view
    }

    private fun rellenarListaDespensa() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                ObtenerCasaPorIdUsuarioCallBack {
                override fun onCasaObtenida(casa: Casa?) {
                    if (casa != null) {
                        Log.d("CompraFragment", "Casa obtenida: ${casa.id}")
                        realTimeManager.obtenerProductosDespensa(casa.id, object :
                            ObtenerProductosDespensaCallBack {
                            override fun onProductosObtenidos(productos: MutableList<ProductoDespensa>) {
                                if (isAdded) { // Verifica si el fragmento está adjunto a la actividad
                                    Log.d(
                                        "CompraFragment",
                                        "Productos obtenidos: ${productos.size}"
                                    )
                                    productosDespensa = productos
                                } else {
                                    Log.e("CompraFragment", "Fragmento no adjunto a una actividad")
                                }
                            }

                            override fun onError(error: Exception?) {
                                Log.e(
                                    "CompraFragment",
                                    "Error obteniendo productos: ${error?.message}"
                                )
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
        } else {
            Log.e("CompraFragment", "Usuario no autenticado")
        }
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
                    realTimeManager.obtenerProductosDespensa(
                        casa.id,
                        object : ObtenerProductosDespensaCallBack {

                            override fun onProductosObtenidos(productos: MutableList<ProductoDespensa>) {
                                Log.d("DespensaFragment", "Productos obtenidos: ${productos.size}")
                                lvListaProductos.adapter =
                                    ProductosListaDespensaAdapter(requireContext(), productos)
                            }

                            override fun onError(error: Exception?) {
                                Log.e(
                                    "DespensaFragment",
                                    "Error obteniendo productos: ${error?.message}"
                                )
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
        internal val listaDeProductos: MutableList<ProductoDespensa> = listaProductos

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
            //val tvStockMinimo = view.findViewById<TextView>(R.id.textoStockMinimo)
            val tvStockActual = view.findViewById<TextView>(R.id.textoStockActual)
            val checkBox = view.findViewById<android.widget.CheckBox>(R.id.checkBox)

            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    producto.seleccionado = true
                } else {
                    producto.seleccionado = false
                }
            }


            tvNombreProducto.text = producto.nombre + " (${producto.stockMinimo}) "
            //tvStockMinimo.text = producto.stockMinimo.toString()
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

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                realTimeManager.obtenerCasaPorIdUsuario(
                    Firebase.auth.currentUser?.uid!!,
                    object : ObtenerCasaPorIdUsuarioCallBack {
                        override fun onCasaObtenida(casa: Casa?) {
                            if (casa != null) {
                                val producto = lvListaProductos.selectedItem as ProductoDespensa
                                realTimeManager.borrarProductoDespensa(casa.id, producto, object :
                                    BorrarProductoDespensaCallBack {
                                    override fun onProductoBorrado() {
                                        Log.d("DespensaFragment", "Producto borrado de la despensa")
                                        listarProductosDespensa()
                                    }

                                    override fun onError(error: Exception?) {
                                        Log.e(
                                            "DespensaFragment",
                                            "Error borrando producto: ${error?.message}"
                                        )
                                    }
                                })
                                listarProductosDespensa()
                            }
                        }

                        override fun onError(error: Exception?) {
                            Log.e("DespensaFragment", "Error obteniendo casa: ${error?.message}")
                        }
                    })
                true
            }

            R.id.action_edit -> {
                // Acción para la opción del menú 2
                true
            }

            R.id.añadirAListaCompra -> {

                //coger el producto seleccionado
                val producto = lvListaProductos.selectedItem as ProductoDespensa
                //añadir el producto a la lista de la compra
                AlertDialog.Builder(requireContext())
                    .setTitle("Añadir a la lista de la compra")
                    .setMessage("¿Cuántas unidades de ${producto.nombre} quieres añadir a la lista de la compra?")
                    .setView(R.layout.dialog_cantidad_producto)
                    .setPositiveButton("Añadir") { dialog, _ ->
                        val dialogView = dialog as AlertDialog
                        val cantidadAComprar =
                            dialogView.findViewById<EditText>(R.id.etCantidadProducto).text.toString()
                                .toInt()
                        guardarProductoEnListaCompra(producto.nombre, cantidadAComprar)
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()


                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    private fun guardarProductoEnListaCompra(nombreProducto: String, cantidadAComprar: Int) {
        // 1. Obtener referencia a la base de datos Firebase
        val databaseReference = FirebaseDatabase.getInstance().reference

        // 2. Obtener el ID de la casa del usuario actual (asumiendo que ya tienes implementada la obtención del ID del usuario)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                ObtenerCasaPorIdUsuarioCallBack {
                override fun onCasaObtenida(casa: Casa?) {
                    if (casa != null) {
                        // 3. Crear un objeto ProductoDespensa con los datos proporcionados
                        val productoCompra =
                            ProductoListaCompra(nombreProducto, cantidadAComprar, false)
                        // 4. Utilizar el nombre del producto como clave para almacenar el objeto ProductoDespensa en la lista productosDespensa de la casa
                        databaseReference.child("casas").child(casa.id)
                            .child("productosListaCompra").child(nombreProducto)
                            .setValue(productoCompra)
                            .addOnSuccessListener {
                                // Éxito al guardar el producto en la despensa
                                Toast.makeText(
                                    requireContext(),
                                    "Producto guardado en la lista de la compra correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Error al guardar el producto en la lista de la compra",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No se encontró la casa para el usuario actual",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onError(error: Exception?) {
                    Toast.makeText(
                        requireContext(),
                        "Error obteniendo casa: ${error?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            // El usuario no está autenticado
            Toast.makeText(requireContext(), "El usuario no está autenticado", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun procesarProductosSeleccionados(accion: (ProductoDespensa) -> Unit) {
        for (i in 0 until lvListaProductos.count) {
            val producto = lvListaProductos.adapter.getItem(i) as ProductoDespensa
            if (producto.seleccionado) {
                accion(producto)
            }
        }
    }

    private fun borrarProductosSeleccionados() {
        procesarProductosSeleccionados { producto ->
            borrarProductoEnDespensa(producto)
        }
        listarProductosDespensa()
    }

    private fun borrarProductoEnDespensa(producto: ProductoDespensa) {
        getCasaForCurrentUser(object : ObtenerCasaPorIdUsuarioCallBack {
            override fun onCasaObtenida(casa: Casa?) {
                if (casa != null) {
                    Log.d("CompraFragment", "Casa obtenida: ${casa.id}")
                    realTimeManager.borrarProductoDespensa(casa.id, producto, object :
                        BorrarProductoDespensaCallBack {

                        override fun onProductoBorrado() {
                            Toast.makeText(
                                requireContext(),
                                "Producto borrado de la despensa",
                                Toast.LENGTH_SHORT
                            ).show()
                            listarProductosDespensa()
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

    private fun configurarBotones() {

        botonAnadirProducto.setOnClickListener {
            val intent = Intent(context, MainActivity2::class.java)
            startActivity(intent)
        }
        botonBorrarProductosSeleccionados.setOnClickListener {
            borrarProductosSeleccionados()

        }

        botonEditarProducto.setOnClickListener {

            //buscar en la listaDespensa cuantos productos estan seleccionados
            var contador = 0
            var productoParaEditar = ProductoDespensa()
            for (i in 0 until lvListaProductos.count) {
                val producto = lvListaProductos.adapter.getItem(i) as ProductoDespensa
                if (producto.seleccionado) {
                    productoParaEditar.nombre = producto.nombre
                    productoParaEditar.stockMinimo = producto.stockMinimo
                    productoParaEditar.stockActual = producto.stockActual
                    contador++
                    if (contador > 1) {
                        Toast.makeText(
                            requireContext(),
                            "Solo puedes editar un producto a la vez",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }
                }
            }
            if (contador == 1){
                val intent = Intent(context, MainActivity2::class.java)
                    .putExtra("nombreProducto", productoParaEditar.nombre)
                    .putExtra("stockMinimo", productoParaEditar.stockMinimo)
                    .putExtra("stockActual", productoParaEditar.stockActual)
                startActivity(intent)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Actualizar la lista de productos cuando el fragmento se haya creado
        listarProductosDespensa()

        configurarBotones()
        registerForContextMenu(lvListaProductos)

    }

    override fun onResume() {
        super.onResume()
        // Actualizar la lista de productos cuando el fragmento vuelva a estar en primer plano
        listarProductosDespensa()
        registerForContextMenu(lvListaProductos)

    }
}