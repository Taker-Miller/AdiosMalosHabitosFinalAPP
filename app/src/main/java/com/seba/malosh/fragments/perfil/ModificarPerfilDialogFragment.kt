package com.seba.malosh.fragments.perfil

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.seba.malosh.R

class ModificarPerfilDialogFragment : DialogFragment() {

    // Interfaz para comunicar los cambios al perfil
    interface ModificarPerfilListener {
        fun onPerfilModificado(nombre: String, correo: String)
    }

    private lateinit var listener: ModificarPerfilListener

    // Enlazamos la actividad con el listener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as ModificarPerfilListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context debe implementar ModificarPerfilListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_modificar_perfil, container, false)

        // Referenciar los elementos del layout
        val editNombre: EditText = view.findViewById(R.id.edit_nombre)
        val editCorreo: EditText = view.findViewById(R.id.edit_correo)
        val btnGuardar: Button = view.findViewById(R.id.btn_guardar)

        // Configuramos la acción del botón guardar
        btnGuardar.setOnClickListener {
            val nuevoNombre = editNombre.text.toString()
            val nuevoCorreo = editCorreo.text.toString()
            listener.onPerfilModificado(nuevoNombre, nuevoCorreo)
            dismiss() // Cierra el diálogo
        }

        return view
    }
}
