package com.seba.malosh.fragments.progresos.metas

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.seba.malosh.R
import java.text.SimpleDateFormat
import java.util.*

class ProgresoMetaFragment : Fragment() {

    private lateinit var calendarioMeta: CalendarView
    private lateinit var estadoDiaTextView: TextView
    private lateinit var habitos: ArrayList<String>
    private var fechaInicio: Long = 0
    private var fechaFin: Long = 0
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val viewModel: ProgresoMetaViewModel by activityViewModels()

    companion object {
        private const val FECHA_INICIO_KEY = "fecha_inicio"
        private const val FECHA_FIN_KEY = "fecha_fin"
        private const val HABITOS_KEY = "habitos"

        fun newInstance(fechaInicio: Long, fechaFin: Long, habitos: ArrayList<String>): ProgresoMetaFragment {
            val fragment = ProgresoMetaFragment()
            val args = Bundle()
            args.putLong(FECHA_INICIO_KEY, fechaInicio)
            args.putLong(FECHA_FIN_KEY, fechaFin)
            args.putStringArrayList(HABITOS_KEY, habitos)
            fragment.arguments = args
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progreso_meta, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        calendarioMeta = view.findViewById(R.id.calendarioMeta)
        estadoDiaTextView = view.findViewById(R.id.estadoDiaTextView)

        fechaInicio = arguments?.getLong(FECHA_INICIO_KEY) ?: 0L
        fechaFin = arguments?.getLong(FECHA_FIN_KEY) ?: 0L
        habitos = arguments?.getStringArrayList(HABITOS_KEY) ?: arrayListOf()

        configurarCalendario()

        return view
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun configurarCalendario() {

        if (fechaInicio > 0 && fechaFin > fechaInicio) {
            calendarioMeta.minDate = fechaInicio
            calendarioMeta.maxDate = fechaFin
        } else {
            Toast.makeText(context, "Error al cargar las fechas de la meta. Por favor, reinicia la meta.", Toast.LENGTH_SHORT).show()

            calendarioMeta.minDate = System.currentTimeMillis() // Fecha actual como mínimo
            calendarioMeta.maxDate = System.currentTimeMillis() + 31536000000L // 1 año como máximo
        }

        calendarioMeta.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            val today = Calendar.getInstance()

            if (esDiaActual(selectedDate, today)) {
                val fechaSeleccionada = dateFormat.format(selectedDate.time)
                mostrarDialogoEstadoDia(fechaSeleccionada)
            } else {
                Toast.makeText(context, "Solo puedes marcar el día actual.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun esDiaActual(selectedDate: Calendar, today: Calendar): Boolean {
        return selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                selectedDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                selectedDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun mostrarDialogoEstadoDia(fecha: String) {
        val opciones = arrayOf("Completado", "Fallido")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("¿Cómo te fue el $fecha?")
        builder.setItems(opciones) { _, which ->
            val estado = if (which == 0) "Completado" else "Fallido"

            viewModel.estadoDias[fecha] = estado
            actualizarVisualizacionCalendario(fecha, estado)

            Toast.makeText(context, "Día $fecha marcado como $estado", Toast.LENGTH_SHORT).show()
            estadoDiaTextView.text = "Día $fecha marcado como $estado"
        }
        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun actualizarVisualizacionCalendario(fecha: String, estado: String) {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(fecha)
        actualizarColorFecha(calendar, estado)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun actualizarColorFecha(calendar: Calendar, estado: String) {
        val fechaEnMillis = calendar.timeInMillis
        calendarioMeta.setDate(fechaEnMillis, true, true)

        val verde = resources.getColor(R.color.verde, null)
        val rojo = resources.getColor(R.color.rojo, null)

        if (estado == "Completado") {
            calendarioMeta.setBackgroundColor(verde)
        } else if (estado == "Fallido") {
            calendarioMeta.setBackgroundColor(rojo)
        }
    }
}
