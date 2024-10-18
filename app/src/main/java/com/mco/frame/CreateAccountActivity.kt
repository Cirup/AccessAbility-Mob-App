package com.mco.frame

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mco.frame.databinding.RegisterpageBinding

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var binding: RegisterpageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = RegisterpageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginHref.setOnClickListener{
            finish()
        }

        binding.registerBtn.setOnClickListener{
            finish()
        }

    }
}