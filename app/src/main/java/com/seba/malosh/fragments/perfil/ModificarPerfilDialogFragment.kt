package com.seba.malosh.fragments.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.seba.malosh.R

class ModificarPerfilDialogFragment : DialogFragment() {

    private lateinit var editNombre: EditText
    private lateinit var editApellido: EditText
    private lateinit var editCorreo: EditText

    private var nombreActual: String = ""
    private var apellidoActual: String = ""
    private var correoActual: String = ""

    fun setDatosActuales(nombre: String, apellido: String, correo: String) {
        nombreActual = nombre
        apellidoActual = apellido
        correoActual = correo
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_modificar_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editNombre = view.findViewById(R.id.edit_nombre)
        editApellido = view.findViewById(R.id.edit_apellido)
        editCorreo = view.findViewById(R.id.edit_correo)

        // Establecer los valores actuales
        editNombre.setText(nombreActual)
        editApellido.setText(apellidoActual)
        editCorreo.setText(correoActual)

        val btnGuardar: Button = view.findViewById(R.id.btn_guardar)

        btnGuardar.setOnClickListener {
            val nuevoNombre = editNombre.text.toString().ifBlank { nombreActual }
            val nuevoApellido = editApellido.text.toString().ifBlank { apellidoActual }
            val nuevoCorreo = editCorreo.text.toString().ifBlank { correoActual }

            val result = Bundle().apply {
                putString("nombre_modificado", nuevoNombre)
                putString("apellido_modificado", nuevoApellido)
                putString("correo_modificado", nuevoCorreo)
            }

            parentFragmentManager.setFragmentResult("modificarPerfilRequestKey", result)

            dismiss()
        }
    }
}
