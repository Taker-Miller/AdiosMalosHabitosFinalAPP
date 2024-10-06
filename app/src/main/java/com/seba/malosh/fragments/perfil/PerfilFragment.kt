package com.seba.malosh.fragments.perfil

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.seba.malosh.R

class PerfilFragment : Fragment() {

    private lateinit var nombreUsuarioTextView: TextView
    private lateinit var apellidoUsuarioTextView: TextView
    private lateinit var correoUsuarioTextView: TextView
    private lateinit var imagenPerfilImageView: ImageView
    private lateinit var btnCambiarImagenPerfil: Button
    private lateinit var btnEditarPerfil: Button

    private val imagenesPerfil = arrayOf(
        R.drawable.image_perfil_1,
        R.drawable.image_perfil_2,
        R.drawable.image_perfil_3,
        R.drawable.image_perfil_4
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        nombreUsuarioTextView = view.findViewById(R.id.nombre_usuario)
        apellidoUsuarioTextView = view.findViewById(R.id.apellido_usuario)
        correoUsuarioTextView = view.findViewById(R.id.correo_usuario)
        imagenPerfilImageView = view.findViewById(R.id.imagen_perfil)
        btnCambiarImagenPerfil = view.findViewById(R.id.btn_editar_imagen_perfil)
        btnEditarPerfil = view.findViewById(R.id.btn_editar_perfil)

        cargarDatosUsuario()

        // Escuchar los resultados de ModificarPerfilDialogFragment
        parentFragmentManager.setFragmentResultListener("modificarPerfilRequestKey", viewLifecycleOwner) { _, bundle ->
            val nombre = bundle.getString("nombre_modificado")
            val apellido = bundle.getString("apellido_modificado")
            val correo = bundle.getString("correo_modificado")

            // Actualizar la UI con los datos recibidos
            if (!nombre.isNullOrEmpty()) nombreUsuarioTextView.text = nombre
            if (!apellido.isNullOrEmpty()) apellidoUsuarioTextView.text = apellido
            if (!correo.isNullOrEmpty()) correoUsuarioTextView.text = correo

            // Guardar los cambios en SharedPreferences
            val sharedPreferences = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()

            editor?.putString("nombre_usuario", nombre)
            editor?.putString("apellido_usuario", apellido)
            editor?.putString("correo_usuario", correo)
            editor?.apply()
        }

        btnEditarPerfil.setOnClickListener {
            val dialog = ModificarPerfilDialogFragment()
            dialog.setDatosActuales(
                nombreUsuarioTextView.text.toString(),
                apellidoUsuarioTextView.text.toString(),
                correoUsuarioTextView.text.toString()
            )
            dialog.show(parentFragmentManager, "ModificarPerfilDialog")
        }

        btnCambiarImagenPerfil.setOnClickListener {
            mostrarOpcionesImagen()
        }

        return view
    }

    private fun cargarDatosUsuario() {
        val sharedPreferences = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val nombreGuardado = sharedPreferences?.getString("nombre_usuario", "Sebastián")
        val apellidoGuardado = sharedPreferences?.getString("apellido_usuario", "Villalobos")
        val correoGuardado = sharedPreferences?.getString("correo_usuario", "sebastian@example.com")
        val imagenGuardada = sharedPreferences?.getInt("imagen_perfil", R.drawable.image_perfil_1)

        nombreUsuarioTextView.text = nombreGuardado
        apellidoUsuarioTextView.text = apellidoGuardado
        correoUsuarioTextView.text = correoGuardado
        imagenPerfilImageView.setImageResource(imagenGuardada ?: R.drawable.image_perfil_1)
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf("Imagen 1", "Imagen 2", "Imagen 3", "Imagen 4")

        val dialog = AlertDialog.Builder(context)
            .setTitle("Selecciona una imagen de perfil")
            .setItems(opciones) { _, which ->
                imagenPerfilImageView.setImageResource(imagenesPerfil[which])

                val sharedPreferences = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences?.edit()
                editor?.putInt("imagen_perfil", imagenesPerfil[which])
                editor?.apply()
            }
            .create()

        dialog.show()
    }
}
