package com.seba.malosh.fragments.registromalosh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.seba.malosh.R

class TusMalosHabitosFragment : Fragment() {

    private lateinit var malosHabitosTextView: TextView

    companion object {
        private const val HABITOS_KEY = "habitos"

        fun newInstance(habitos: ArrayList<String>): TusMalosHabitosFragment {
            val fragment = TusMalosHabitosFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(HABITOS_KEY, habitos)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tus_malos_habitos, container, false)

        malosHabitosTextView = view.findViewById(R.id.malosHabitosTextView)

        val habitos = arguments?.getStringArrayList(HABITOS_KEY)

        malosHabitosTextView.text = habitos?.joinToString(separator = "\n") ?: "No has registrado hábitos aún."

        return view
    }
}
