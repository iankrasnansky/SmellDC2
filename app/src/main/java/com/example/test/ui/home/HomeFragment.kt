package com.example.test.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.test.MainActivity
import com.example.test.R
import com.example.test.Report

//THIS IS THE REPORT FRAGMENT

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val RG = root.findViewById<RadioGroup>(R.id.radioG)
        val desc = root.findViewById<EditText>(R.id.answer2)
        val symp = root.findViewById<EditText>(R.id.answer3)
        val note = root.findViewById<EditText>(R.id.editText3)

        val b = root.findViewById<Button>(R.id.button)
        val mainAct = activity as MainActivity
        b.setOnClickListener {
            val selRad = root.findViewById<RadioButton>(RG.checkedRadioButtonId)
            if(selRad==null){
                val t = Toast.makeText(mainAct.baseContext,"Please select a severity level.", Toast.LENGTH_LONG)
                t.show()

            }else {
                val radTxt = selRad.text.toString()
                mainAct.toSend = Report(
                    0.0,
                    0.0,
                    radTxt,
                    desc.text.toString(),
                    symp.text.toString(),
                    note.text.toString()
                )
                mainAct.getGeoData()
            }
        }
        return root
    }
}