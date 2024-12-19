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
