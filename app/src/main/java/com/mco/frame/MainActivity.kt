package com.mco.frame

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mco.frame.databinding.ActivityLayoutBinding
import com.mco.frame.ui.theme.FrameTheme

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLayoutBinding

    private val sharedViewModel: SharedViewModel by viewModels()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

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
