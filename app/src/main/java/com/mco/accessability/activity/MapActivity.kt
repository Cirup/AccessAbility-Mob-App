package com.mco.accessability.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mco.accessability.R
import com.mco.accessability.SharedViewModel
import com.mco.accessability.databinding.ActivityLayoutBinding
import com.mco.accessability.fragment.MapFragment
import com.mco.accessability.fragment.ProfileFragment

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(MapFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.Maps -> replaceFragment(MapFragment())
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