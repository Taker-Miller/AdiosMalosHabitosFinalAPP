package com.seba.malosh.fragments.desafios

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.seba.malosh.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DesafiosDiariosFragment : Fragment() {

    private lateinit var contenedorDesafios: LinearLayout
    private lateinit var aceptarDesafioButton: Button
    private lateinit var cancelarDesafioButton: Button
    private lateinit var desafioDescripcion: TextView

    private lateinit var inicioCheckBox: CheckBox
    private lateinit var enProgresoCheckBox: CheckBox
    private lateinit var casiPorTerminarCheckBox: CheckBox
    private lateinit var completadoCheckBox: CheckBox
    private val desafiosList = mutableListOf<String>()
    private var currentDesafio: String? = null
    private var desafioEnProgreso = false
    private val handler = Handler()
    private lateinit var registeredHabits: ArrayList<String>

    companion object {
        private const val HABITOS_KEY = "habitos_registrados"
        private const val TEMPORIZADOR_INICIO_KEY = "temporizador_inicio"
        private const val TEMPORIZADOR_DURACION = 60000L // 60 segundos
        private const val TEMPORIZADOR_ESPERA = 20000L // 20 segundos

        fun newInstance(habits: ArrayList<String>): DesafiosDiariosFragment {
            val fragment = DesafiosDiariosFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(HABITOS_KEY, habits)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_desafios_diarios, container, false)

        contenedorDesafios = view.findViewById(R.id.contenedorDesafios)
        aceptarDesafioButton = view.findViewById(R.id.aceptarDesafioButton)
        cancelarDesafioButton = view.findViewById(R.id.cancelarDesafioButton)
        desafioDescripcion = view.findViewById(R.id.desafioDescripcion)

        inicioCheckBox = view.findViewById(R.id.inicioCheckBox)
        enProgresoCheckBox = view.findViewById(R.id.enProgresoCheckBox)
        casiPorTerminarCheckBox = view.findViewById(R.id.casiPorTerminarCheckBox)
        completadoCheckBox = view.findViewById(R.id.completadoCheckBox)

        registeredHabits = arguments?.getStringArrayList(HABITOS_KEY) ?: arrayListOf()

        actualizarCheckBoxesRestaurados()

        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        inicioCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("inicio_check", isChecked).apply()
        }

        enProgresoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("en_progreso_check", isChecked).apply()
        }

        casiPorTerminarCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("casi_terminado_check", isChecked).apply()
        }

        completadoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("completado_check", isChecked).apply()
        }

        val inicioTemporizador = sharedPreferences.getLong(TEMPORIZADOR_INICIO_KEY, 0L)
        val temporizadorActivo = sharedPreferences.getBoolean("temporizador_activo", false)
        var tiempoRestante = sharedPreferences.getLong("tiempo_restante", TEMPORIZADOR_ESPERA)

        currentDesafio = obtenerDesafioEnProgreso(requireContext())

        if (temporizadorActivo && tiempoRestante > 0L) {
            iniciarTemporizadorRestaurado(tiempoRestante)
        } else if (inicioTemporizador > 0L && currentDesafio != null) {
            reanudarTemporizador(inicioTemporizador)
            mostrarDesafioEnProgreso()
        } else {
            generarDesafiosSiEsNecesario()
        }

        aceptarDesafioButton.setOnClickListener { aceptarDesafio() }
        cancelarDesafioButton.setOnClickListener { cancelarDesafio() }

        return view
    }

    private fun iniciarTemporizadorRestaurado(tiempoRestante: Long) {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        desafioDescripcion.text =
            "Próximo desafío disponible en ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)} segundos."
        aceptarDesafioButton.isEnabled = false

        val runnable = object : Runnable {
            var tiempoActualRestante = tiempoRestante
            override fun run() {
                if (tiempoActualRestante > 0) {
                    tiempoActualRestante -= 1000
                    desafioDescripcion.text =
                        "Próximo desafío disponible en ${TimeUnit.MILLISECONDS.toSeconds(tiempoActualRestante)} segundos."
                    handler.postDelayed(this, 1000)

                    editor.putLong("tiempo_restante", tiempoActualRestante)
                    editor.apply()
                } else {
                    desafioDescripcion.text = "¡Nuevo desafío disponible!"
                    editor.putBoolean("temporizador_activo", false)
                    editor.remove("tiempo_restante")
                    editor.apply()

                    limpiarDesafioAnterior()
                    generarDesafios(registeredHabits)
                    mostrarDesafio()

                    aceptarDesafioButton.visibility = View.VISIBLE
                    cancelarDesafioButton.visibility = View.GONE
                    aceptarDesafioButton.isEnabled = true

                    sharedPreferences.edit().remove(TEMPORIZADOR_INICIO_KEY).apply()
                }
            }
        }
        handler.post(runnable)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actualizarCheckBoxesRestaurados()
    }

    private fun generarDesafiosSiEsNecesario() {
        val desafioGuardado = obtenerDesafioEnProgreso(requireContext())
        if (desafioGuardado != null) {
            currentDesafio = desafioGuardado
            mostrarDesafioEnProgreso()
        } else {
            generarDesafios(registeredHabits)
            mostrarDesafio()
        }
    }

    private fun iniciarTemporizador1Minuto() {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val tiempoInicio = System.currentTimeMillis()
        editor.putLong(TEMPORIZADOR_INICIO_KEY, tiempoInicio)
        editor.apply()

        setCheckBoxesVisibility(View.VISIBLE)
        resetCheckBoxes()
        reanudarTemporizador(tiempoInicio)
    }

    private fun reanudarTemporizador(tiempoInicio: Long) {
        val tiempoActual = System.currentTimeMillis()
        val tiempoRestante = TEMPORIZADOR_DURACION - (tiempoActual - tiempoInicio)

        if (tiempoRestante > 0) {
            aceptarDesafioButton.visibility = View.GONE
            cancelarDesafioButton.visibility = View.VISIBLE
            desafioDescripcion.text =
                "Desafío en progreso. Tiempo restante: ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)} segundos."
            setCheckBoxesVisibility(View.VISIBLE)

            handler.postDelayed(object : Runnable {
                var tiempoRestanteActualizado = tiempoRestante

                override fun run() {
                    if (tiempoRestanteActualizado > 0) {
                        tiempoRestanteActualizado -= 1000
                        desafioDescripcion.text =
                            "Desafío en progreso. Tiempo restante: ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestanteActualizado)} segundos."
                        actualizarCheckBoxes(tiempoRestanteActualizado)
                        handler.postDelayed(this, 1000)
                    } else {
                        validarDesafioCompletado()
                    }
                }
            }, 1000)
        } else {
            validarDesafioCompletado()
        }
    }

    private fun actualizarCheckBoxes(tiempoRestante: Long) {
        val porcentajeRestante = 100 - ((tiempoRestante.toDouble() / TEMPORIZADOR_DURACION) * 100).toInt()
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (porcentajeRestante >= 25) {
            inicioCheckBox.isEnabled = true
            editor.putBoolean("inicio_check", inicioCheckBox.isChecked)
        }

        if (porcentajeRestante >= 50) {
            enProgresoCheckBox.isEnabled = true
            editor.putBoolean("en_progreso_check", enProgresoCheckBox.isChecked)
        }

        if (porcentajeRestante >= 75) {
            casiPorTerminarCheckBox.isEnabled = true
            editor.putBoolean("casi_terminado_check", casiPorTerminarCheckBox.isChecked)
        }

        if (porcentajeRestante >= 90) {
            completadoCheckBox.isEnabled = true
            editor.putBoolean("completado_check", completadoCheckBox.isChecked)
        }
        editor.apply()
    }

    private fun resetCheckBoxes() {
        inicioCheckBox.isChecked = false
        enProgresoCheckBox.isChecked = false
        casiPorTerminarCheckBox.isChecked = false
        completadoCheckBox.isChecked = false
        inicioCheckBox.isEnabled = false
        enProgresoCheckBox.isEnabled = false
        casiPorTerminarCheckBox.isEnabled = false
        completadoCheckBox.isEnabled = false
    }

    private fun limpiarEstadoCheckBoxes() {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    private fun validarDesafioCompletado() {
        val sharedPreferences = requireContext().getSharedPreferences("DesafiosCompletados", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        if (inicioCheckBox.isChecked && enProgresoCheckBox.isChecked && casiPorTerminarCheckBox.isChecked && completadoCheckBox.isChecked) {
            Toast.makeText(context, "¡Desafío completado exitosamente!", Toast.LENGTH_SHORT).show()
            val contador = sharedPreferences.getInt("contador_desafios", 0)
            val formatter = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("es", "ES"))
            val fechaActual = formatter.format(Date())
            editor.putString("fecha_$contador", fechaActual)
            editor.putString("desafio_$contador", currentDesafio)
            editor.putInt("contador_desafios", contador + 1)
            editor.apply()
        } else {
            Toast.makeText(context, "Desafío fallido. No completaste todas las etapas.", Toast.LENGTH_SHORT).show()
        }

        setCheckBoxesVisibility(View.GONE)
        limpiarEstadoCheckBoxes()
        iniciarTemporizador20Segundos()
    }

    private fun iniciarTemporizador20Segundos() {
        var tiempoRestante = TEMPORIZADOR_ESPERA

        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val tiempoInicioGuardado = sharedPreferences.getLong("tiempo_restante", -1L)
        if (tiempoInicioGuardado > 0) {
            tiempoRestante = tiempoInicioGuardado
        }

        editor.putBoolean("temporizador_activo", true)
        editor.apply()

        aceptarDesafioButton.isEnabled = false
        desafioDescripcion.text =
            "Próximo desafío disponible en ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)} segundos."

        val runnable = object : Runnable {
            override fun run() {
                if (tiempoRestante > 0) {
                    tiempoRestante -= 1000
                    desafioDescripcion.text =
                        "Próximo desafío disponible en ${TimeUnit.MILLISECONDS.toSeconds(tiempoRestante)} segundos."
                    handler.postDelayed(this, 1000)

                    // Guardar el tiempo restante en SharedPreferences
                    editor.putLong("tiempo_restante", tiempoRestante)
                    editor.apply()
                } else {
                    desafioDescripcion.text = "¡Nuevo desafío disponible!"
                    editor.putBoolean("temporizador_activo", false)
                    editor.remove("tiempo_restante")
                    editor.apply()

                    limpiarDesafioAnterior()
                    generarDesafios(registeredHabits)
                    mostrarDesafio()

                    aceptarDesafioButton.visibility = View.VISIBLE
                    cancelarDesafioButton.visibility = View.GONE
                    aceptarDesafioButton.isEnabled = true

                    sharedPreferences.edit().remove(TEMPORIZADOR_INICIO_KEY).apply()
                }
            }
        }

        handler.post(runnable)
    }

    private fun limpiarDesafioAnterior() {
        desafioEnProgreso = false
        currentDesafio = null
        guardarDesafioEnProgreso(requireContext(), null, false)
    }

    private fun mostrarDesafioEnProgreso() {
        aceptarDesafioButton.isEnabled = false
        cancelarDesafioButton.visibility = View.VISIBLE
        contenedorDesafios.removeAllViews()
        val textView = TextView(context).apply {
            text = "Desafío en progreso: $currentDesafio"
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.white))
        }
        contenedorDesafios.addView(textView)
        setCheckBoxesVisibility(View.VISIBLE)
        actualizarCheckBoxesRestaurados()
    }

    private fun cancelarDesafio() {
        handler.removeCallbacksAndMessages(null)

        guardarDesafioEnProgreso(requireContext(), null, false)

        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("temporizador_activo", true)  // Guardar que el temporizador de espera está activo
        editor.apply()

        desafioEnProgreso = false
        currentDesafio = null
        setCheckBoxesVisibility(View.GONE)
        desafioDescripcion.text = "Próximo desafío disponible en 20 segundos."

        limpiarEstadoCheckBoxes()

        iniciarTemporizador20Segundos()
    }

    private fun aceptarDesafio() {
        if (desafioEnProgreso) {
            Toast.makeText(
                context,
                "Ya tienes un desafío en progreso. Finaliza o cancela el desafío actual primero.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            desafioEnProgreso = true
            guardarDesafioEnProgreso(requireContext(), currentDesafio, true)
            Toast.makeText(context, "¡Desafío aceptado!", Toast.LENGTH_SHORT).show()
            setCheckBoxesVisibility(View.VISIBLE)
            resetCheckBoxes()
            iniciarTemporizador1Minuto()
        }
    }

    private fun generarDesafios(habitos: List<String>) {
        desafiosList.clear()
        for (habito in habitos) {
            when (habito.lowercase().trim()) {
                "cafeína", "consumo de cafeína" -> desafiosList.addAll(
                    listOf(
                        "No tomes café en las próximas 3 horas.",
                        "Reemplaza el café de la tarde con agua.",
                        "No consumas cafeína después de las 3 p.m."
                    )
                )
                "dormir mal", "dormir a deshoras" -> desafiosList.addAll(
                    listOf(
                        "No tomes siestas durante el día.",
                        "Duerme al menos 7 horas esta noche.",
                        "Apaga tus dispositivos electrónicos 30 minutos antes de dormir.",
                        "Evita tomar bebidas con cafeína después de las 5 p.m.",
                        "Realiza una rutina de relajación antes de dormir.",
                        "Intenta acostarte antes de las 11 p.m. esta noche."
                    )
                )
                "interrumpir a otros" -> desafiosList.addAll(
                    listOf(
                        "No interrumpas a nadie durante las próximas 3 horas.",
                        "Escucha activamente en una conversación sin interrumpir.",
                        "Deja que los demás terminen de hablar antes de dar tu opinión en la próxima conversación."
                    )
                )
                "mala alimentación" -> desafiosList.addAll(
                    listOf(
                        "Evita la comida rápida durante todo el día.",
                        "Come tres comidas balanceadas hoy.",
                        "Reemplaza los snacks poco saludables por frutas o verduras.",
                        "Reduce el consumo de azúcares en tu próxima comida."
                    )
                )
                "fumar" -> desafiosList.addAll(
                    listOf(
                        "No fumes durante las próximas 4 horas.",
                        "Evita fumar un cigarrillo después de cada comida hoy.",
                        "Intenta reducir tu consumo de cigarrillos a la mitad durante el día.",
                        "Fuma solo la mitad de tu cigarrillo en tu próximo descanso."
                    )
                )
                "alcohol" -> desafiosList.addAll(
                    listOf(
                        "No consumas alcohol durante las próximas 4 horas.",
                        "No consumas bebidas alcohólicas durante todo el día.",
                        "Evita tomar más de una copa de alcohol durante las próximas 5 horas.",
                        "Reemplaza el alcohol con agua o una bebida sin alcohol en tu próxima comida."
                    )
                )
                "poco ejercicio" -> desafiosList.addAll(
                    listOf(
                        "Realiza una caminata de al menos 30 minutos hoy.",
                        "Haz 15 minutos de estiramientos esta mañana.",
                        "Realiza 10 flexiones durante tu próximo descanso.",
                        "Sube las escaleras en lugar de usar el ascensor durante el día."
                    )
                )
                "comer a deshoras" -> desafiosList.addAll(
                    listOf(
                        "No comas nada después de las 9 p.m.",
                        "Establece horarios regulares para tus comidas y cúmplelos hoy.",
                        "No comas nada entre comidas durante las próximas 3 horas.",
                        "Desayuna dentro de la primera hora después de despertar."
                    )
                )
                "mala higiene" -> desafiosList.addAll(
                    listOf(
                        "Cepilla tus dientes después de cada comida hoy.",
                        "Lávate las manos antes y después de cada comida.",
                        "Dedica 10 minutos a limpiar tu espacio personal hoy.",
                        "Toma una ducha antes de acostarte esta noche."
                    )
                )
                else -> {
                    Toast.makeText(context, "No se encontraron desafíos para el hábito: $habito", Toast.LENGTH_SHORT).show()
                }
            }
        }
        desafiosList.shuffle()
        mostrarDesafio()
    }

    private fun mostrarDesafio() {
        if (desafiosList.isNotEmpty()) {
            currentDesafio = desafiosList.first()
            contenedorDesafios.removeAllViews()
            val textView = TextView(context).apply {
                text = currentDesafio
                textSize = 18f
                setTextColor(resources.getColor(android.R.color.white))
            }
            contenedorDesafios.addView(textView)
            aceptarDesafioButton.visibility = View.VISIBLE
            aceptarDesafioButton.isEnabled = true
        } else {
            Toast.makeText(context, "No hay desafíos disponibles.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setCheckBoxesVisibility(visibility: Int) {
        val progresoChecklist = view?.findViewById<LinearLayout>(R.id.progresoChecklist)
        progresoChecklist?.visibility = visibility
    }

    private fun actualizarCheckBoxesRestaurados() {
        val sharedPreferences = requireContext().getSharedPreferences("temporizador_prefs", Context.MODE_PRIVATE)
        inicioCheckBox.isChecked = sharedPreferences.getBoolean("inicio_check", false)
        enProgresoCheckBox.isChecked = sharedPreferences.getBoolean("en_progreso_check", false)
        casiPorTerminarCheckBox.isChecked = sharedPreferences.getBoolean("casi_terminado_check", false)
        completadoCheckBox.isChecked = sharedPreferences.getBoolean("completado_check", false)
        setCheckBoxesVisibility(View.VISIBLE)
    }

    private fun guardarDesafioEnProgreso(context: Context, desafio: String?, enProgreso: Boolean) {
        val sharedPreferences = context.getSharedPreferences("desafio_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        if (enProgreso) {
            editor.putString("desafio_actual", desafio)
            editor.putBoolean("en_progreso", true)
        } else {
            editor.remove("desafio_actual")
            editor.putBoolean("en_progreso", false)
        }
        editor.apply()
    }

    private fun obtenerDesafioEnProgreso(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("desafio_prefs", Context.MODE_PRIVATE)
        return if (sharedPreferences.getBoolean("en_progreso", false)) {
            sharedPreferences.getString("desafio_actual", null)
        } else {
            null
        }
    }
}
