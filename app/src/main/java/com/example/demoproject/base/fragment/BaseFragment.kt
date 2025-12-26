package com.example.demoproject.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.example.demoproject.utils.bindingExtensions.inflateWithBinding

abstract class BaseFragment<T : ViewDataBinding>(@LayoutRes private val layoutResId: Int) :
    Fragment() {

    val TAG: String = javaClass.simpleName
    protected var binding: T? = null
        private set

    protected val backPressCallback: OnBackPressedCallback by lazy {
        // Add OnBackPressedCallback to handle back press
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Custom back press logic
                // For example, show a dialog or navigate back
                onBackPressed(this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        if (binding == null) {
            binding = inflateWithBinding(inflater, container, layoutResId)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.root?.post {
            if (isAdded && !isDetached) {
                initViews()
                setUpClickEvents()
                setUpObservers()
                initBackPress()
                setUpToolbar()
            }
        }
    }

    abstract fun initViews()
    open fun setUpClickEvents() {}
    open fun setUpObservers() {}
    open fun setUpToolbar() {}
    open fun onBackPressed(callback: OnBackPressedCallback) {
        callback.isEnabled = false
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun initBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, backPressCallback
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


}