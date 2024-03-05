package com.example.midespensapp.ui.despensa

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.midespensapp.MainActivity2
import com.example.midespensapp.databinding.FragmentDespensaBinding

class DespensaFragment : Fragment() {

    private var _binding: FragmentDespensaBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val despensaViewModel = ViewModelProvider(this).get(DespensaViewModel::class.java)
        //
        _binding = FragmentDespensaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val botonMas = binding.buttonAddProduct

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


}