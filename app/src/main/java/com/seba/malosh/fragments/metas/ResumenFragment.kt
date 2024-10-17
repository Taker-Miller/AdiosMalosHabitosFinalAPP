package com.seba.malosh.fragments.metas

import android.Manifest
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
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.Locale

class ResumenFragment : Fragment() {

    private lateinit var volverButton: Button
    private lateinit var comenzarPlanButton: Button
    private lateinit var periodoSeleccionadoTextView: TextView
    private lateinit var habitoSeleccionadoTextView: TextView
    private var fechasHabitos: ArrayList<Pair<String, Pair<String, String>>>? = null

    companion object {
        private const val FECHAS_HABITOS_KEY = "fechas_habitos"
        private const val CHANNEL_ID = "logros_channel"


        fun newInstance(
            fechasHabitos: ArrayList<Pair<String, Pair<String, String>>>
        ): ResumenFragment {
            val fragment = ResumenFragment()
            val bundle = Bundle()
            bundle.putSerializable(FECHAS_HABITOS_KEY, fechasHabitos)
            fragment.arguments = bundle
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resumen, container, false)

        volverButton = view.findViewById(R.id.volverButton)
        comenzarPlanButton = view.findViewById(R.id.comenzarPlanButton)
        periodoSeleccionadoTextView = view.findViewById(R.id.periodoSeleccionado)
        habitoSeleccionadoTextView = view.findViewById(R.id.habitoSeleccionado)

        fechasHabitos = arguments?.getSerializable(FECHAS_HABITOS_KEY) as ArrayList<Pair<String, Pair<String, String>>>

        mostrarResumen()

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

    private fun mostrarResumen() {
        val resumenBuilder = StringBuilder()
        fechasHabitos?.forEach { (habito, fechas) ->
            val (fechaInicio, fechaFin) = fechas
            resumenBuilder.append("Hábito: $habito\n")
            resumenBuilder.append("Fecha de inicio: $fechaInicio\n")
            resumenBuilder.append("Fecha de fin: $fechaFin\n\n")
        }
        periodoSeleccionadoTextView.text = resumenBuilder.toString()
        habitoSeleccionadoTextView.text = getString(R.string.habitos_seleccionados, fechasHabitos?.joinToString(", ") { it.first })
    }

    private fun guardarMetaEnProgreso() {
        val sharedPreferences =
            requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        fechasHabitos?.forEach { (habito, fechas) ->
            val (fechaInicioStr, fechaFinStr) = fechas
            val fechaInicioLong = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(
                fechaInicioStr
            )?.time ?: 0L
            val fechaFinLong = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(
                fechaFinStr
            )?.time ?: 0L

            if (fechaInicioLong < fechaFinLong) {
                editor.putBoolean("plan_iniciado_$habito", true)
                editor.putBoolean("meta_en_progreso_$habito", true)
                editor.putLong("fecha_inicio_meta_$habito", fechaInicioLong)
                editor.putLong("fecha_fin_meta_$habito", fechaFinLong)
            } else {
                Toast.makeText(
                    context,
                    "Error al guardar las fechas del plan para $habito.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun verificarDesbloqueoLogros() {
        val sharedPreferences =
            requireContext().getSharedPreferences("LogrosPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val logroPrimerMeta = listaLogros.firstOrNull { it.id == 1 }
        if (logroPrimerMeta != null && !logroPrimerMeta.desbloqueado) {
            logroPrimerMeta.desbloqueado = true
            editor.putBoolean("logro_${logroPrimerMeta.id}", true)
            mostrarNotificacionLogro(logroPrimerMeta)
        }

        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            val requestCodeNotification = 0
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestCodeNotification
            )

            return
        }

        NotificationManagerCompat.from(requireContext()).notify(logro.id, builder.build())
    }
}
