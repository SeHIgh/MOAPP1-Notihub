package com.example.notihub.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.knu_mobapp1_proj_team3.IntroFragment2
import com.example.notihub.R


class IntroFragment1 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro1, container, false)
        view.findViewById<Button>(R.id.nextButton).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, IntroFragment2())
                .commit()
        }
        return view
    }
}
