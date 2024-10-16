package com.seba.malosh.fragments.desafios

import DesafiosCompletadosViewModel
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.seba.malosh.R

class DesafiosCompletadosFragment : Fragment() {

    private lateinit var listaDesafios: ListView
    private val viewModel: DesafiosCompletadosViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_desafios_completados, container, false)
        listaDesafios = view.findViewById(R.id.listaDesafiosCompletados)

        // Observar cambios en los desafíos completados
        viewModel.desafiosCompletados.observe(viewLifecycleOwner, Observer { desafios ->
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, desafios)
            listaDesafios.adapter = adapter
        })

        // Cargar los datos en el ViewModel
        viewModel.cargarDesafiosCompletados(requireContext())

        // Gestionar el comportamiento del botón "Volver"
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Aquí defines qué hacer cuando el usuario presiona el botón "Volver"
                if (parentFragmentManager.backStackEntryCount > 0) {
                    // Si hay fragmentos en la pila de retroceso, volvemos al fragmento anterior
                    parentFragmentManager.popBackStack()
                } else {
                    // Si no hay más fragmentos en la pila, salir de la actividad o comportamiento predeterminado
                    requireActivity().finish()
                }
            }
        })

        return view
    }

    companion object {
        fun newInstance(): DesafiosCompletadosFragment {
            return DesafiosCompletadosFragment()
        }
    }
}
