package com.madinaappstudio.knowme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.madinaappstudio.knowme.adapters.ProfileViewPagerAdapter
import com.madinaappstudio.knowme.databinding.TempActivityBinding

class TempActivity : AppCompatActivity() {

    private lateinit var binding: TempActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TempActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}