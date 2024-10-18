package com.mco.frame

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.mco.frame.databinding.ActivityLayoutBinding
import com.mco.frame.databinding.FragmentMapBinding

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLayoutBinding
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(MapFragment(sharedViewModel))

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.Maps -> replaceFragment(MapFragment(sharedViewModel))
                R.id.Profile -> replaceFragment(ProfileFragment())

                else ->{

                }
            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout,fragment)
        fragmentTransaction.commit()

        // Call log function to print marker data
        //sharedViewModel.logMarkerData()
    }
}