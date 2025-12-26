package com.example.demoproject.main

import androidx.activity.viewModels
import com.example.demoproject.R
import com.example.demoproject.base.activitiy.BaseActivity
import com.example.demoproject.databinding.ActivityMainBinding
import com.example.demoproject.main.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val mainViewModel : MainViewModel by viewModels()

    override fun initViews() {

        mainViewModel.getProfileData()
    }
}