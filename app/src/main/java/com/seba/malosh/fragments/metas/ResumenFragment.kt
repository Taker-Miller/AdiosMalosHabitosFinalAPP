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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ResumenFragment : Fragment() {

    private lateinit var volverButton: Button
    private lateinit var comenzarPlanButton: Button
    private lateinit var detallesTextView: TextView // Usaremos este TextView para mostrar todos los hábitos y fechas

    companion object {
        private const val HABITOS_CON_FECHAS_KEY = "habitos_con_fechas"
        private const val CHANNEL_ID = "logros_channel"

        fun newInstance(habitosConFechas: ArrayList<Pair<String, Pair<String, String>>>): ResumenFragment {
            val fragment = ResumenFragment()
            val bundle = Bundle()
            bundle.putSerializable(HABITOS_CON_FECHAS_KEY, habitosConFechas)
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
        detallesTextView = view.findViewById(R.id.detallesTextView) // TextView para mostrar hábitos y fechas

        // Obtener los hábitos con sus respectivas fechas
        val habitosConFechas = arguments?.getSerializable(HABITOS_CON_FECHAS_KEY) as? ArrayList<Pair<String, Pair<String, String>>>

        // Mostrar cada hábito con su respectiva fecha de inicio y fin en un solo TextView
        val detalles = habitosConFechas?.joinToString(separator = "\n") { habitoConFechas ->
            val habito = habitoConFechas.first
            val fechaInicio = habitoConFechas.second.first
            val fechaFin = habitoConFechas.second.second
            "$habito: $fechaInicio - $fechaFin"
        }

        detallesTextView.text = detalles

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
        val sharedPreferences =
            requireContext().getSharedPreferences("MetaPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putBoolean("plan_iniciado", true)
        editor.putBoolean("meta_en_progreso", true)
        editor.apply()
    }

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

    private fun mostrarNotificacionLogro(logro: Logro) {
        // Solo solicitar permisos en Android 13 y versiones superiores
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

        // Solicitar permiso solo si la versión de Android lo requiere (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
        }

        NotificationManagerCompat.from(requireContext()).notify(logro.id, builder.build())
    }
}
