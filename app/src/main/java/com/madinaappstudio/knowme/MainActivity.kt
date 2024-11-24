package com.madinaappstudio.knowme

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.madinaappstudio.knowme.databinding.ActivityMainBinding
import com.madinaappstudio.knowme.utils.USER_NODE
import com.madinaappstudio.knowme.utils.getUserUid
import com.madinaappstudio.knowme.utils.showContent
import com.madinaappstudio.knowme.utils.showLoading
import com.madinaappstudio.knowme.utils.startLog


class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.frameMainContent) as NavHostFragment
        navHostFragment.navController

        showLoading(
            binding.progBarMain,
            binding.frameMainContent
        )

        firebaseAuth = FirebaseAuth.getInstance()
        val uid = getUserUid()

        if (uid != null) {
            Firebase.firestore.collection(USER_NODE).document(uid).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val username = task.result.getString("username")
                        if (!username.isNullOrEmpty()) {
                            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                            finishAffinity()
                        } else {
                            showContent(
                                binding.progBarMain,
                                binding.frameMainContent
                            )
                            navHostFragment.navController.navigate(R.id.graphSetUsernameFragment)
                        }
                    } else {
                        startLog(task.exception!!)
                        Toast.makeText(
                            this@MainActivity, task.exception?.localizedMessage, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            showContent(
                binding.progBarMain,
                binding.frameMainContent
            )
            navHostFragment.navController.navigate(R.id.graphSignInFragment)
        }
    }

}
