package com.example.demoproject.utils.bindingExtensions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

/**
 * Extension function for AppCompatActivity
 */
fun <T : ViewDataBinding> AppCompatActivity.setContentViewWithBinding(layoutId: Int): T {
    val binding: T = DataBindingUtil.setContentView(this, layoutId)
    binding.lifecycleOwner = this
    return binding
}

/**
 * Extension function for Fragment
 */
fun <T : ViewDataBinding> Fragment.inflateWithBinding(
    inflater: LayoutInflater, container: ViewGroup?, layoutId: Int, attachToParent: Boolean = false
): T {
    val binding: T = DataBindingUtil.inflate(inflater, layoutId, container, attachToParent)
    binding.lifecycleOwner = this.viewLifecycleOwner
    return binding
}