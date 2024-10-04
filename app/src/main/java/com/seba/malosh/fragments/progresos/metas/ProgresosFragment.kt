package com.seba.malosh.fragments.progresos.metas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import com.seba.malosh.fragments.desafios.DesafiosCompletadosFragment
import com.seba.malosh.fragments.progresos.logros.LogrosFragment
import java.util.*

class ProgresoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progresos, container, false)

        val cardMeta: CardView = view.findViewById(R.id.card_meta)
        val cardLogros: CardView = view.findViewById(R.id.card_logros)

        cardMeta.setOnClickListener {

            val fechaInicio = obtenerFechaInicioMeta()
            val fechaFin = obtenerFechaFinMeta()
            val habitos = obtenerHabitosRegistrados()


            val fragment = ProgresoMetaFragment.newInstance(fechaInicio, fechaFin, habitos)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        // Acción para Logros
        cardLogros.setOnClickListener {
            // Navegar al fragmento de Logros
            val fragment = LogrosFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }




        return view
    }


    private fun verificarMetaEnProgreso(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("meta_en_progreso", false)
    }


    private fun obtenerFechaInicioMeta(): Long {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("fecha_inicio_meta", 0L)
    }


    private fun obtenerFechaFinMeta(): Long {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("fecha_fin_meta", 0L)
    }


    private fun obtenerHabitosRegistrados(): ArrayList<String> {
        val sharedPreferences = requireContext().getSharedPreferences("HabitosPrefs", Context.MODE_PRIVATE)
        val habitosSet = sharedPreferences.getStringSet("habitos_registrados", setOf())
        return ArrayList(habitosSet ?: listOf())
    }
}
