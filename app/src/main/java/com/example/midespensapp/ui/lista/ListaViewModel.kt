package com.example.midespensapp.ui.lista

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListaViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val _lista = MutableLiveData<String>().apply {
        value = "Lista de la compra"
    }
    val lista: LiveData<String> = _lista

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


}