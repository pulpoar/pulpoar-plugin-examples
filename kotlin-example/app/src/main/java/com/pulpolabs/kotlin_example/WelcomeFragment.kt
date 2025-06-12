package com.pulpolabs.kotlin_example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class WelcomeFragment : Fragment() {

    interface WelcomeListener {
        fun onTryOnClicked()
    }

    private var listener: WelcomeListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)
        
        val welcomeText: TextView = view.findViewById(R.id.welcome_text)
        val tryOnButton: Button = view.findViewById(R.id.try_on_button)
        
        welcomeText.text = "Welcome"
        
        tryOnButton.setOnClickListener {
            listener?.onTryOnClicked()
        }
        
        return view
    }

    fun setWelcomeListener(listener: WelcomeListener) {
        this.listener = listener
    }

    companion object {
        @JvmStatic
        fun newInstance() = WelcomeFragment()
    }
} 