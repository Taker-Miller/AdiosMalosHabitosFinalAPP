package com.seba.malosh.fragments.progresos.metas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import com.seba.malosh.fragments.desafios.DesafiosCompletadosFragment
import com.seba.malosh.fragments.progresos.logros.LogrosFragment
import java.util.*

class ProgresoFragment : Fragment() {

    private lateinit var progresoSpinner: Spinner
    private lateinit var seleccionarButton: Button
    private lateinit var tituloProgresos: TextView
    private lateinit var descripcionProgresos: TextView
    private var opcionSeleccionada: String? = null
    private var metaEnProgreso = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progresos, container, false)

        progresoSpinner = view.findViewById(R.id.progresoSpinner)
        seleccionarButton = view.findViewById(R.id.seleccionarButton)
        tituloProgresos = view.findViewById(R.id.tituloProgresos)
        descripcionProgresos = view.findViewById(R.id.descripcionProgresos)

        // No se resetearán las preferencias para no perder el progreso del plan actual.
        // Si necesitas hacerlo para debug, puedes descomentar la línea siguiente.
        // resetearPreferenciasMeta()

        metaEnProgreso = verificarMetaEnProgreso()

        val opcionesProgreso = arrayOf("Progreso Meta", "Logros", "Metas Cumplidas", "Desafíos Completados")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opcionesProgreso)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        progresoSpinner.adapter = adapter

        progresoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                opcionSeleccionada = opcionesProgreso[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        fun obtenerHabitosRegistrados(): ArrayList<String> {
            val sharedPreferences = requireContext().getSharedPreferences("HabitosPrefs", Context.MODE_PRIVATE)
            val habitosSet = sharedPreferences.getStringSet("habitos_registrados", setOf()) // Recupera los hábitos como un Set
            return ArrayList(habitosSet ?: listOf()) // Convertir Set en ArrayList
        }


        seleccionarButton.setOnClickListener {
            when (opcionSeleccionada) {
                "Progreso Meta" -> {

                    if (metaEnProgreso) {
                        val fechaInicio = obtenerFechaInicioMeta()
                        val fechaFin = obtenerFechaFinMeta()


                        val habitos = obtenerHabitosRegistrados()

                        val fragment = ProgresoMetaFragment.newInstance(fechaInicio, fechaFin, habitos)
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit()
                    } else {

                        Toast.makeText(requireContext(), "No tienes ninguna meta en progreso.", Toast.LENGTH_LONG).show()
                    }
                }


                "Logros" -> {
                    val fragment = LogrosFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                "Metas Cumplidas" -> {
                    val fragment = MetasCumplidasFragment()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                "Desafíos Completados" -> {
                    val fragment = DesafiosCompletadosFragment.newInstance()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        return view
    }

    // Método para verificar si hay una meta en progreso.
    private fun verificarMetaEnProgreso(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("meta_en_progreso", false)
    }

    // Obtener la fecha de inicio de la meta desde SharedPreferences.
    private fun obtenerFechaInicioMeta(): Long {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        val fechaInicio = sharedPreferences.getLong("fecha_inicio_meta", 0L)

        // Verificación opcional para mostrar la fecha de inicio.
        if (fechaInicio > 0) {
            Toast.makeText(requireContext(), "Fecha inicio meta: ${Date(fechaInicio)}", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "No se ha registrado una fecha de inicio.", Toast.LENGTH_SHORT).show()
        }

        return fechaInicio
    }

    // Obtener la fecha de fin de la meta desde SharedPreferences.
    private fun obtenerFechaFinMeta(): Long {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        val fechaFin = sharedPreferences.getLong("fecha_fin_meta", 0L)


        if (fechaFin > 0) {
            Toast.makeText(requireContext(), "Fecha fin meta: ${Date(fechaFin)}", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "No se ha registrado una fecha de fin.", Toast.LENGTH_SHORT).show()
        }

        return fechaFin
    }


    private fun resetearPreferenciasMeta() {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
