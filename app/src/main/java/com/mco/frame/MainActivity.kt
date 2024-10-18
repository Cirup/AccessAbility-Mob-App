package com.mco.frame

import android.content.Intent
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import com.mco.frame.databinding.ActivityLayoutBinding
import com.mco.frame.databinding.LoginpageBinding
import com.mco.frame.databinding.RegisterpageBinding
import com.mco.frame.ui.theme.FrameTheme

class MainActivity : AppCompatActivity() {

    private lateinit var binding: LoginpageBinding

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginpageBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        replaceFragment(MapFragment(sharedViewModel))



        //login button on click listener
        login()
        //create account button on click listener
        createAccount()
    }

    private fun createAccount(){
        val textView = binding.registerHref

        //when clicked, it will lead to the register page
        textView.setOnClickListener{
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun login(){
        val button = binding.loginBtn

        button.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FrameTheme {
        Greeting("Android")
    }
}