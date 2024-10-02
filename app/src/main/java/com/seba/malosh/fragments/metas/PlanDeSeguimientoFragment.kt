package com.seba.malosh.fragments.metas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
    private var selectedHabits: ArrayList<String> = arrayListOf()
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


        selectedHabits = arguments?.getStringArrayList(SELECTED_HABITS_KEY) ?: arrayListOf()

        fechaInicioButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerInicio = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                fechaInicio = selectedCalendar.timeInMillis
                fechaInicioTextView.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            }, year, month, day)

            datePickerInicio.datePicker.minDate = calendar.timeInMillis

            val endOfMonth = Calendar.getInstance()
            endOfMonth.set(year, month, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
            datePickerInicio.datePicker.maxDate = endOfMonth.timeInMillis

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
                fechaFinTextView.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
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
                val resumenFragment = ResumenFragment.newInstance(
                    fechaInicioTextView.text.toString(),
                    fechaFinTextView.text.toString(),
                    selectedHabits
                )

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, resumenFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        volverButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}
