package com.example.knu_mobapp1_proj_team3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.notihub.R
import com.example.notihub.activities.SelectPreferenceActivity

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [IntroFragment3.newInstance] factory method to
 * create an instance of this fragment.
 */
class IntroFragment3 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro3, container, false)
        view.findViewById<Button>(R.id.startButton).setOnClickListener {
            startActivity(Intent(activity, SelectPreferenceActivity::class.java))
            activity?.finish()
        }
        return view
    }
}
