package com.seba.malosh.fragments.metas

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import com.seba.malosh.activities.BienvenidaActivity
import com.seba.malosh.fragments.progresos.logros.listaLogros
import com.seba.malosh.fragments.progresos.logros.Logro
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Locale

class ResumenFragment : Fragment() {

    private lateinit var volverButton: Button
    private lateinit var comenzarPlanButton: Button
    private lateinit var periodoSeleccionadoTextView: TextView
    private lateinit var habitoSeleccionadoTextView: TextView
    private var fechaInicio: String? = null
    private var fechaFin: String? = null
    private var habitos: ArrayList<String>? = null

    companion object {
        private const val FECHA_INICIO_KEY = "fecha_inicio"
        private const val FECHA_FIN_KEY = "fecha_fin"
        private const val HABITOS_KEY = "habitos"
        private const val CHANNEL_ID = "logros_channel"

        fun newInstance(fechaInicio: String, fechaFin: String, habitos: ArrayList<String>): ResumenFragment {
            val fragment = ResumenFragment()
            val bundle = Bundle()
            bundle.putString(FECHA_INICIO_KEY, fechaInicio)
            bundle.putString(FECHA_FIN_KEY, fechaFin)
            bundle.putStringArrayList(HABITOS_KEY, habitos)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resumen, container, false)

        volverButton = view.findViewById(R.id.volverButton)
        comenzarPlanButton = view.findViewById(R.id.comenzarPlanButton)
        periodoSeleccionadoTextView = view.findViewById(R.id.periodoSeleccionado)
        habitoSeleccionadoTextView = view.findViewById(R.id.habitoSeleccionado)

        fechaInicio = arguments?.getString(FECHA_INICIO_KEY)
        fechaFin = arguments?.getString(FECHA_FIN_KEY)
        habitos = arguments?.getStringArrayList(HABITOS_KEY)

        periodoSeleccionadoTextView.text = "Periodo: $fechaInicio - $fechaFin"
        habitoSeleccionadoTextView.text = "Hábitos seleccionados: ${habitos?.joinToString(", ")}"

        volverButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        comenzarPlanButton.setOnClickListener {
            guardarMetaEnProgreso()
            verificarDesbloqueoLogros()

            (activity as? BienvenidaActivity)?.comenzarPlan()
            Toast.makeText(context, "¡El plan ha comenzado!", Toast.LENGTH_SHORT).show()

            requireActivity().supportFragmentManager.popBackStack(null, 1)
        }

        return view
    }

    private fun guardarMetaEnProgreso() {
        val sharedPreferences = requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val fechaInicioLong = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaInicio)?.time ?: 0L
        val fechaFinLong = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaFin)?.time ?: 0L

        if (fechaInicioLong > 0 && fechaFinLong > fechaInicioLong) {
            editor.putBoolean("plan_iniciado", true)
            editor.putBoolean("meta_en_progreso", true)
            editor.putLong("fecha_inicio_meta", fechaInicioLong)
            editor.putLong("fecha_fin_meta", fechaFinLong)
            editor.apply()
        } else {
            Toast.makeText(context, "Error al guardar las fechas del plan. Por favor, revisa las fechas seleccionadas.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verificarDesbloqueoLogros() {
        val sharedPreferences = requireContext().getSharedPreferences("LogrosPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val logroPrimerMeta = listaLogros.firstOrNull { it.id == 1 }
        if (logroPrimerMeta != null && !logroPrimerMeta.desbloqueado) {
            logroPrimerMeta.desbloqueado = true
            editor.putBoolean("logro_${logroPrimerMeta.id}", true)
            mostrarNotificacionLogro(logroPrimerMeta)
        }

        editor.apply()
    }

    private fun mostrarNotificacionLogro(logro: Logro) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Logros Desbloqueados",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificación de logros desbloqueados"
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_desbloqueado)
            .setContentTitle("¡Logro Desbloqueado!")
            .setContentText(logro.titulo)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(requireContext()).notify(logro.id, builder.build())
    }
}
