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

    interface ModificarPerfilListener {
        fun onPerfilModificado(nombre: String, apellido: String, correo: String)
    }

    private lateinit var listener: ModificarPerfilListener

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

        listener = try {
            targetFragment as ModificarPerfilListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$targetFragment debe implementar ModificarPerfilListener")
        }

        editNombre = view.findViewById(R.id.edit_nombre)
        editApellido = view.findViewById(R.id.edit_apellido)
        editCorreo = view.findViewById(R.id.edit_correo)

        editNombre.setText(nombreActual)
        editApellido.setText(apellidoActual)
        editCorreo.setText(correoActual)

        val btnGuardar: Button = view.findViewById(R.id.btn_guardar)

        btnGuardar.setOnClickListener {
            val nuevoNombre = if (editNombre.text.toString().isNotBlank()) editNombre.text.toString() else nombreActual
            val nuevoApellido = if (editApellido.text.toString().isNotBlank()) editApellido.text.toString() else apellidoActual
            val nuevoCorreo = if (editCorreo.text.toString().isNotBlank()) editCorreo.text.toString() else correoActual

            listener.onPerfilModificado(nuevoNombre, nuevoApellido, nuevoCorreo)

            dismiss()
        }
    }
}
