package com.seba.malosh.fragments.metas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import java.util.Calendar

class PlanDeSeguimientoFragment : Fragment() {

    private lateinit var volverButton: Button
    private lateinit var definirButton: Button
    private lateinit var fechaInicioButton: Button
    private lateinit var fechaFinButton: Button
    private lateinit var fechaInicioTextView: TextView
    private lateinit var fechaFinTextView: TextView
    private lateinit var habitoActualTextView: TextView

    private var selectedHabits: ArrayList<String> = arrayListOf()
    private var habitIndex = 0
    private var fechasHabitos: MutableMap<String, Pair<Long, Long>> = mutableMapOf()
    private var fechaInicio: Long = 0
    private var fechaFin: Long = 0

    companion object {
        private const val SELECTED_HABITS_KEY = "selected_habits"

        fun newInstance(selectedHabits: ArrayList<String>): PlanDeSeguimientoFragment {
            val fragment = PlanDeSeguimientoFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(SELECTED_HABITS_KEY, selectedHabits)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan_de_seguimiento, container, false)

        volverButton = view.findViewById(R.id.volverButton)
        definirButton = view.findViewById(R.id.definirButton)
        fechaInicioButton = view.findViewById(R.id.fechaInicioButton)
        fechaFinButton = view.findViewById(R.id.fechaFinButton)
        fechaInicioTextView = view.findViewById(R.id.fechaInicioTextView)
        fechaFinTextView = view.findViewById(R.id.fechaFinTextView)
        habitoActualTextView = view.findViewById(R.id.habitoActualTextView)

        selectedHabits = arguments?.getStringArrayList(SELECTED_HABITS_KEY) ?: arrayListOf()


        actualizarVistaHabitoActual()

        fechaInicioButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerInicio = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                fechaInicio = selectedCalendar.timeInMillis
                fechaInicioTextView.text = getString(
                    R.string.fecha_seleccionada,
                    selectedDay, selectedMonth + 1, selectedYear
                )

                fechaFin = 0
                fechaFinTextView.text = ""
            }, year, month, day)

            datePickerInicio.datePicker.minDate = calendar.timeInMillis
            datePickerInicio.show()
        }

        fechaFinButton.setOnClickListener {
            if (fechaInicio == 0L) {
                Toast.makeText(context, "Por favor selecciona primero la fecha de inicio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = fechaInicio
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerFin = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                fechaFin = selectedCalendar.timeInMillis

                val selectedYearDifference = selectedYear - calendar.get(Calendar.YEAR)
                if (selectedYearDifference < 1) {
                    Toast.makeText(context, "La fecha de fin debe ser al menos un año después de la fecha de inicio.", Toast.LENGTH_SHORT).show()
                    fechaFin = 0
                } else if (selectedYearDifference > 3) {
                    Toast.makeText(context, "La fecha de fin no puede ser más de 3 años después de la fecha de inicio.", Toast.LENGTH_SHORT).show()
                    fechaFin = 0
                } else {
                    fechaFinTextView.text = getString(
                        R.string.fecha_seleccionada,
                        selectedDay, selectedMonth + 1, selectedYear
                    )
                }
            }, year, month, day)


            val minFechaFin = Calendar.getInstance()
            minFechaFin.timeInMillis = fechaInicio
            minFechaFin.add(Calendar.YEAR, 1)

            val maxFechaFin = Calendar.getInstance()
            maxFechaFin.timeInMillis = fechaInicio
            maxFechaFin.add(Calendar.YEAR, 3)

            datePickerFin.datePicker.minDate = minFechaFin.timeInMillis
            datePickerFin.datePicker.maxDate = maxFechaFin.timeInMillis
            datePickerFin.show()
        }

        definirButton.setOnClickListener {
            if (fechaInicio == 0L || fechaFin == 0L) {
                Toast.makeText(context, "Por favor, selecciona fechas de inicio y fin.", Toast.LENGTH_SHORT).show()
            } else if (fechaInicio >= fechaFin) {
                Toast.makeText(context, "La fecha de fin debe ser posterior a la fecha de inicio.", Toast.LENGTH_SHORT).show()
            } else {
                guardarFechasHabitoActual()
                avanzarOEnviarAlResumen()
            }
        }

        volverButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().supportFragmentManager.popBackStack()
            }
        })

        return view
    }

    private fun actualizarVistaHabitoActual() {
        if (habitIndex < selectedHabits.size) {
            val habitoActual = selectedHabits[habitIndex]
            habitoActualTextView.text = "Configurando: $habitoActual"
            fechaInicioTextView.text = ""
            fechaFinTextView.text = ""
            fechaInicio = 0L
            fechaFin = 0L
        }
    }

    private fun guardarFechasHabitoActual() {
        val habitoActual = selectedHabits[habitIndex]
        fechasHabitos[habitoActual] = Pair(fechaInicio, fechaFin)
        Toast.makeText(context, "Fechas guardadas para $habitoActual", Toast.LENGTH_SHORT).show()
    }

    private fun avanzarOEnviarAlResumen() {
        if (habitIndex < selectedHabits.size - 1) {
            habitIndex++
            actualizarVistaHabitoActual()
        } else {
            enviarAlResumenFragment()
        }
    }

    private fun enviarAlResumenFragment() {
        val resumenFragment = ResumenFragment.newInstance(
            fechasHabitos.map { it.key to Pair(
                formatFecha(it.value.first),
                formatFecha(it.value.second)
            ) } as ArrayList<Pair<String, Pair<String, String>>>
        )

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, resumenFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun formatFecha(millis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return getString(R.string.fecha_seleccionada, day, month, year)
    }
}
