package com.seba.malosh.fragments.progresos.metas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import java.text.SimpleDateFormat
import java.util.*

class ResumenMesFragment : Fragment() {

    private lateinit var estadoMesTextView: TextView
    private var mesSeleccionado: Int = 0
    private var añoSeleccionado: Int = 0
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    companion object {
        private const val MES_SELECCIONADO_KEY = "mes_seleccionado"
        private const val AÑO_SELECCIONADO_KEY = "año_seleccionado"


        fun newInstance(mes: Int, año: Int): ResumenMesFragment {
            val fragment = ResumenMesFragment()
            val args = Bundle()
            args.putInt(MES_SELECCIONADO_KEY, mes)
            args.putInt(AÑO_SELECCIONADO_KEY, año)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resumen_mes, container, false)

        estadoMesTextView = view.findViewById(R.id.estadoMesTextView)


        mesSeleccionado = arguments?.getInt(MES_SELECCIONADO_KEY) ?: 0
        añoSeleccionado = arguments?.getInt(AÑO_SELECCIONADO_KEY) ?: 0


        mostrarEstadoDelMes()

        return view
    }

    private fun mostrarEstadoDelMes() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, añoSeleccionado)
        calendar.set(Calendar.MONTH, mesSeleccionado)


        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val estados = StringBuilder()


        estadoMesTextView.text = ""


        while (calendar.get(Calendar.MONTH) == mesSeleccionado) {
            val fechaFormateada = dateFormat.format(calendar.time)
            val estado = obtenerEstadoDia(fechaFormateada)


            if (estado != null) {
                estados.append("Día: $fechaFormateada - Estado: $estado\n")
            } else {
                estados.append("Día: $fechaFormateada - Estado: Sin Datos\n") // Indicar que no hay datos para el día
            }


            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }


        estadoMesTextView.text = estados.toString()
    }


    private fun obtenerEstadoDia(fecha: String): String? {
        val sharedPreferences = requireContext().getSharedPreferences("ProgresoMetaPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("dia_completado-$fecha", null)
    }
}
