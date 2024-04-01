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
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.midespensapp.DB.AumentarCantidadAComprarCallBack
import com.example.midespensapp.DB.BorrarProductoListaCompraCallBack
import com.example.midespensapp.DB.DisminuirCantidadAComprarCallBack
import com.example.midespensapp.DB.ObtenerCasaPorIdUsuarioCallBack
import com.example.midespensapp.DB.ObtenerProductosDespensaCallBack
import com.example.midespensapp.DB.ObtenerProductosListaCompraCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.MainActivity3
import com.example.midespensapp.R
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.clases.ProductoDespensa
import com.example.midespensapp.clases.ProductoListaCompra
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class ListaFragment : Fragment() {

    private lateinit var lvListaProductos: ListView
    private val realTimeManager = RealTimeManager()
    private lateinit var botonAnadirProducto: Button
    lateinit var botonAnadirDespensa: Button
    lateinit var botonBorrarProductosSeleccionados: Button
    private var productosDespensa = mutableListOf<ProductoDespensa>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño de tu fragmento
        val view = inflater.inflate(R.layout.fragment_lista, container, false)
        // Obtener la referencia de la lista de productos
        lvListaProductos = view.findViewById(R.id.lvListaCompra)
        botonAnadirProducto = view.findViewById(R.id.btn_add_producto)
        botonAnadirDespensa = view.findViewById(R.id.btn_añadir_seleccionados_despensa)
        botonBorrarProductosSeleccionados = view.findViewById(R.id.btn_borrar_seleccionados)
        rellenarListaDespensa()

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


    private fun configurarBotones() {
        botonAnadirProducto.setOnClickListener {
            val intent = Intent(context, MainActivity3::class.java)
            startActivity(intent)
        }

        botonAnadirDespensa.setOnClickListener {
            botonAnadirDespensaOnClick()

        }

        botonBorrarProductosSeleccionados.setOnClickListener {
            borrarProductosSeleccionados()
            listarProductosListaCompra()
        }
    }

    private fun getCasaForCurrentUser(callback: ObtenerCasaPorIdUsuarioCallBack) {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            realTimeManager.obtenerCasaPorIdUsuario(userId, callback)
            Log.d("ListaFragment", "Id de usuario: $userId")
        } else {
            Log.e("ListaFragment", "Usuario no autenticado")
        }
    }

    private fun procesarProductosSeleccionados(accion: (ProductoListaCompra) -> Unit) {
        for (i in 0 until lvListaProductos.count) {
            val producto = lvListaProductos.adapter.getItem(i) as ProductoListaCompra
            if (producto.comprado) {
                accion(producto)
            }
        }
    }

    private fun borrarProductosSeleccionados() {
        procesarProductosSeleccionados { producto ->
            borrarProductoEnListaCompra(producto)
        }
        listarProductosListaCompra()
    }

    private fun botonAnadirDespensaOnClick() {
        procesarProductosSeleccionados { producto ->
            guardarProductoEnDespensaDesdeLista(producto.nombre, producto.cantidadAComprar)
            borrarProductoEnListaCompra(producto)
        }
        listarProductosListaCompra()
    }

    private fun borrarProductoEnListaCompra(producto: ProductoListaCompra) {
        getCasaForCurrentUser(object : ObtenerCasaPorIdUsuarioCallBack {
            override fun onCasaObtenida(casa: Casa?) {
                if (casa != null) {
                    Log.d("CompraFragment", "Casa obtenida: ${casa.id}")
                    realTimeManager.borrarProductoListaCompra(casa.id, producto, object :
                        BorrarProductoListaCompraCallBack {

                        override fun onProductoBorrado() {
                            Toast.makeText(
                                context,
                                "Producto ${producto.nombre} borrado de la lista de la compra",
                                Toast.LENGTH_SHORT
                            ).show()
                            listarProductosListaCompra()
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


    private fun listarProductosListaCompra() {
        getCasaForCurrentUser(object : ObtenerCasaPorIdUsuarioCallBack {
            override fun onCasaObtenida(casa: Casa?) {
                if (casa != null) {
                    Log.d("CompraFragment", "Casa obtenida: ${casa.id}")
                    realTimeManager.obtenerProductosListaCompra(casa.id, object :
                        ObtenerProductosListaCompraCallBack {

                        override fun onProductosObtenidos(productos: MutableList<ProductoListaCompra>) {
                            if (isAdded) { // Verifica si el fragmento está adjunto a la actividad
                                Log.d("CompraFragment", "Productos obtenidos: ${productos.size}")
                                lvListaProductos.adapter =
                                    ProductosListaCompraAdapter(requireContext(), productos)
                            } else {

                                Log.e("CompraFragment", "Fragmento no adjunto a una actividad")
                            }
                        }

                        override fun onError(error: Exception?) {
                            Log.e("CompraFragment", "Error obteniendo productos: ${error?.message}")
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

    private fun guardarProductoEnDespensaDesdeLista(nombreProducto: String, cantidadComprada: Int) {
        // 1. Obtener referencia a la base de datos Firebase
        val databaseReference = FirebaseDatabase.getInstance().reference

        // 2. Obtener el ID de la casa del usuario actual (asumiendo que ya tienes implementada la obtención del ID del usuario)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                ObtenerCasaPorIdUsuarioCallBack {
                override fun onCasaObtenida(casa: Casa?) {
                    if (casa != null) {
                        //buscar el nombre del producto en la listaDespensa
                        var productoDespensa =
                            productosDespensa.find { it.nombre == nombreProducto }
                        if (productoDespensa != null) {
                            // El producto ya existe en la despensa
                            productoDespensa.stockActual += cantidadComprada

                            // Actualizar el stock actual del producto en la despensa en la base de datos
                            databaseReference.child("casas").child(casa.id)
                                .child("productosDespensa")
                                .child(nombreProducto).setValue(productoDespensa)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Producto añadido a la despensa",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Log.e(
                                        "ListaFragment",
                                        "Error actualizando producto en la despensa: ${it.message}"
                                    )
                                    Toast.makeText(
                                        requireContext(),
                                        "Error añadiendo producto a la despensa",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                        } else {
                            // El producto no existe en la despensa
                            productoDespensa = ProductoDespensa(nombreProducto, cantidadComprada)
                            productosDespensa.add(productoDespensa)
                            guardarProductoEnDespensa(nombreProducto,cantidadComprada,1 )
                        }
                    }
                }

                internal fun guardarProductoEnDespensa(
                    nombreProducto: String,
                    cantidadActualProducto: Int,
                    cantidadMinimaProducto: Int

                ) {
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
                                    val productoDespensa =
                                        ProductoDespensa(
                                            nombreProducto,
                                            cantidadActualProducto,
                                            cantidadMinimaProducto,
                                        )
                                    // 4. Utilizar el nombre del producto como clave para almacenar el objeto ProductoDespensa en la lista productosDespensa de la casa
                                    databaseReference.child("casas").child(casa.id)
                                        .child("productosDespensa").child(nombreProducto)
                                        .setValue(productoDespensa)
                                        .addOnSuccessListener {
                                            // Éxito al guardar el producto en la despensa
                                            Toast.makeText(
                                                requireContext(),
                                                "Producto guardado en la despensa correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Error al guardar el producto en la despensa",
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

                        Toast.makeText(requireContext(), "El usuario no está autenticado", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onError(error: Exception?) {
                    Log.e("ListaFragment", "Error obteniendo casa: ${error?.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error obteniendo casa: ${error?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Log.e("ListaFragment", "Usuario no autenticado")
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }


    class ProductosListaCompraAdapter(
        context: Context,
        listaProductos: MutableList<ProductoListaCompra>
    ) :
        BaseAdapter() {
        private val mContext: Context = context
        private val listaDeProductos: MutableList<ProductoListaCompra> = listaProductos
        val realTimeManager = RealTimeManager()

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
            val checkBox = view.findViewById<CheckBox>(R.id.checkBox)

            checkBox.setOnClickListener {
                if (checkBox.isChecked) {
                    producto.comprado = true
                } else {
                    producto.comprado = false
                }
            }

            tvNombreProducto.text = producto.nombre
            tvCantidadProducto.text = producto.cantidadAComprar.toString()

            val btnMas = view.findViewById<Button>(R.id.btnMas)
            btnMas.setOnClickListener {
                val userId = Firebase.auth.currentUser?.uid
                if (userId != null) {
                    realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                        ObtenerCasaPorIdUsuarioCallBack {
                        override fun onCasaObtenida(casa: Casa?) {
                            if (casa != null) {
                                realTimeManager.aumentarCantidadAComprar(casa.id, producto, object :
                                    AumentarCantidadAComprarCallBack {
                                    override fun onCantidadAumentada() {
                                        Toast.makeText(
                                            mContext,
                                            "Producto añadido a la lista de la compra",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onError(error: Exception?) {
                                        Toast.makeText(
                                            mContext,
                                            "Error añadiendo producto a la lista de la compra",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            }
                        }

                        override fun onError(error: Exception?) {
                            Toast.makeText(
                                mContext,
                                "Error obteniendo casa",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    Toast.makeText(mContext, "El usuario no está autenticado", Toast.LENGTH_SHORT)
                        .show()
                }


                tvCantidadProducto.text = producto.cantidadAComprar.toString()

            }

            val btnMenos = view.findViewById<Button>(R.id.btnMenos)
            btnMenos.setOnClickListener {
                // Disminuir la cantidad del producto en la lista de la compra
                val userId = Firebase.auth.currentUser?.uid
                if (userId != null) {
                    realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                        ObtenerCasaPorIdUsuarioCallBack {
                        override fun onCasaObtenida(casa: Casa?) {
                            if (casa != null) {
                                realTimeManager.disminuirCantidadAComprar(
                                    casa.id,
                                    producto,
                                    object :
                                        DisminuirCantidadAComprarCallBack {
                                        override fun onCantidadDisminuida() {

                                        }

                                        override fun onError(error: Exception?) {

                                        }
                                    })
                            }
                        }

                        override fun onError(error: Exception?) {
                            Toast.makeText(
                                mContext,
                                "Error obteniendo casa",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    Toast.makeText(mContext, "El usuario no está autenticado", Toast.LENGTH_SHORT)
                        .show()
                }
                tvCantidadProducto.text = producto.cantidadAComprar.toString()

            }

            return view
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarBotones()
        listarProductosListaCompra()
    }

    override fun onResume() {
        super.onResume()
        listarProductosListaCompra()
    }

}
