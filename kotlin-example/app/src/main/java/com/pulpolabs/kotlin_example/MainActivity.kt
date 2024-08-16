package com.pulpolabs.kotlin_example

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager


class MainActivity : AppCompatActivity() {
    private lateinit var pulpoARFragment: PulpoARFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Get the FragmentManager
        val fragmentManager: FragmentManager = supportFragmentManager

        if (savedInstanceState == null) {
            // Create a new instance of PulpoARFragment
            pulpoARFragment = PulpoARFragment()
            // Add the fragment to the 'fragment_container' FrameLayout
            fragmentManager.beginTransaction().setReorderingAllowed(true)
                .add(R.id.fragment_web_view, pulpoARFragment, null).commit()

        } else {
            // Restore the fragment from the FragmentManager
            pulpoARFragment =
                fragmentManager.findFragmentById(R.id.fragment_web_view) as PulpoARFragment
        }

        // Set up the button to interact with the WebViewFragment
        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            // Call a method in WebViewFragment to set the path
            pulpoARFragment.setPath("choose-model")
        }
    }

}
