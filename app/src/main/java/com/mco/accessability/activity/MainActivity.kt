package com.mco.accessability.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mco.accessability.SharedViewModel
import com.mco.accessability.databinding.LoginpageBinding
import com.mco.accessability.ui.theme.FrameTheme

class MainActivity : AppCompatActivity() {

    private lateinit var binding: LoginpageBinding

    private val sharedViewModel: SharedViewModel by viewModels()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

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
