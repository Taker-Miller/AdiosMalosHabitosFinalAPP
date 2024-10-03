package com.seba.malosh.fragments.perfil

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

class PerfilFragment : Fragment(), ModificarPerfilDialogFragment.ModificarPerfilListener {

    private lateinit var nombreUsuarioTextView: TextView
    private lateinit var correoUsuarioTextView: TextView
    private lateinit var imagenPerfilImageView: ImageView
    private lateinit var btnCambiarImagenPerfil: Button
    private lateinit var btnEditarPerfil: Button

    // Arreglo con los recursos de las imágenes de perfil
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

        // Inicializamos las vistas
        nombreUsuarioTextView = view.findViewById(R.id.nombre_usuario)
        correoUsuarioTextView = view.findViewById(R.id.correo_usuario)
        imagenPerfilImageView = view.findViewById(R.id.imagen_perfil)
        btnCambiarImagenPerfil = view.findViewById(R.id.btn_editar_imagen_perfil)
        btnEditarPerfil = view.findViewById(R.id.btn_editar_perfil)

        // Acción para editar el nombre y correo
        btnEditarPerfil.setOnClickListener {
            val dialog = ModificarPerfilDialogFragment()
            dialog.show(childFragmentManager, "ModificarPerfilDialog")
        }

        // Acción para cambiar la imagen de perfil
        btnCambiarImagenPerfil.setOnClickListener {
            mostrarOpcionesImagen()
        }

        // Cargar datos del usuario
        cargarDatosUsuario()

        return view
    }

    private fun cargarDatosUsuario() {
        // Obtener los datos del usuario de SharedPreferences o usar valores predeterminados
        val sharedPreferences = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val imagenGuardada = sharedPreferences?.getInt("imagen_perfil", R.drawable.image_perfil_1)
        val nombreGuardado = sharedPreferences?.getString("nombre_usuario", "Sebastián Villalobos")
        val correoGuardado = sharedPreferences?.getString("correo_usuario", "sebastian@example.com")

        // Cargar la imagen guardada o predeterminada
        imagenPerfilImageView.setImageResource(imagenGuardada ?: R.drawable.image_perfil_1)

        // Cargar el nombre y el correo
        nombreUsuarioTextView.text = nombreGuardado
        correoUsuarioTextView.text = correoGuardado
    }

    private fun mostrarOpcionesImagen() {
        // Mostrar un diálogo con las opciones de imagen de perfil
        val opciones = arrayOf("Imagen 1", "Imagen 2", "Imagen 3", "Imagen 4")

        val dialog = android.app.AlertDialog.Builder(context)
            .setTitle("Selecciona una imagen de perfil")
            .setItems(opciones) { _, which ->
                // Cambiar la imagen de perfil según la opción seleccionada
                imagenPerfilImageView.setImageResource(imagenesPerfil[which])

                // Guardar la selección en SharedPreferences
                val sharedPreferences = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences?.edit()
                editor?.putInt("imagen_perfil", imagenesPerfil[which])
                editor?.apply()
            }
            .create()

        dialog.show()
    }

    // Implementación de la interfaz para recibir los nuevos datos del diálogo
    override fun onPerfilModificado(nombre: String, correo: String) {
        // Actualizamos las vistas con los nuevos datos
        nombreUsuarioTextView.text = nombre
        correoUsuarioTextView.text = correo

        // Guardar los nuevos datos en SharedPreferences
        val sharedPreferences = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putString("nombre_usuario", nombre)
        editor?.putString("correo_usuario", correo)
        editor?.apply()
    }
}
