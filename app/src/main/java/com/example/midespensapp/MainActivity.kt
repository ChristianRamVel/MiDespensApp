package com.example.midespensapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.midespensapp.DB.BorrarProductoDespensaCallBack
import com.example.midespensapp.DB.ObtenerCasaPorIdUsuarioCallBack
import com.example.midespensapp.DB.RealTimeManager
import com.example.midespensapp.clases.Casa
import com.example.midespensapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val realTimeManager = RealTimeManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    //inflar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    //configurar el menu

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sing_out -> {
                if (auth.currentUser != null) {
                    auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("logout", true)
                    startActivity(intent)
                    finish() // Asegúrate de finalizar esta actividad para que no se pueda volver atrás
                }
                true
            }
            R.id.enviar_casa -> {

                // 2. Obtener el ID de la casa del usuario actual (asumiendo que ya tienes implementada la obtención del ID del usuario)
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                        ObtenerCasaPorIdUsuarioCallBack {
                        override fun onCasaObtenida(casa: Casa?) {
                            if (casa != null) {
                               //abrir chooser para enviar la casa
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, casa.id)
                                    type = "text/plain"
                                }

                                val shareIntent = Intent.createChooser(sendIntent, null)
                                startActivity(shareIntent)
                            }
                        }

                        override fun onError(error: Exception?) {
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.error_obteniendo_casa, error?.message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    Toast.makeText(this@MainActivity,  getString(R.string.el_usuario_no_est_autenticado), Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }
            R.id.borrar_despensa -> {

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                        ObtenerCasaPorIdUsuarioCallBack {
                        override fun onCasaObtenida(casa: Casa?) {
                            if (casa != null) {
                                realTimeManager.borrarDespensa(casa.id, object : BorrarProductoDespensaCallBack {
                                    override fun onProductoBorrado() {

                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.despensa_borrada),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onError(error: Exception?) {

                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(
                                                R.string.error_borrando_despensa,
                                                error?.message
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            }
                        }

                        override fun onError(error: Exception?) {
                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.error_obteniendo_casa),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {

                    Toast.makeText(this@MainActivity, getString(R.string.el_usuario_no_est_autenticado), Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }

            R.id.borrar_compra -> {

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    realTimeManager.obtenerCasaPorIdUsuario(userId, object :
                        ObtenerCasaPorIdUsuarioCallBack {
                        override fun onCasaObtenida(casa: Casa?) {
                            if (casa != null) {
                                realTimeManager.borrarListaCompra(casa.id, object : BorrarProductoDespensaCallBack {
                                    override fun onProductoBorrado() {
                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(R.string.lista_de_compra_borrada),
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    }

                                    override fun onError(error: Exception?) {

                                        Toast.makeText(
                                            this@MainActivity,
                                            getString(
                                                R.string.error_borrando_lista_de_compra,
                                                error?.message
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                })
                            }
                        }

                        override fun onError(error: Exception?) {

                            Toast.makeText(
                                this@MainActivity,
                                getString(R.string.error_obteniendo_casa, error?.message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.el_usuario_no_est_autenticado), Toast.LENGTH_SHORT)
                        .show()
                }
                true
            }

            R.id.informacion -> {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.informacion))
                    .setMessage(getString(R.string.informacion_texto))
                    .setPositiveButton(getString(R.string.aceptar)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


}