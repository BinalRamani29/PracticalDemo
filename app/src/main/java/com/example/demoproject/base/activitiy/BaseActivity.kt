package com.example.demoproject.base.activitiy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import com.example.demoproject.utils.bindingExtensions.setContentViewWithBinding

abstract class BaseActivity<T : ViewDataBinding>(
    @LayoutRes private val contentLayoutId: Int,
) : AppCompatActivity() {


    val mTAG = javaClass.simpleName.toString()


    var binding: T? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentManager = supportFragmentManager
        if (savedInstanceState != null) {
            val transaction = fragmentManager.beginTransaction()
            for (fragment in fragmentManager.fragments) {
                transaction.remove(fragment)
            }
            transaction.commitNow()
        }/* ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
             val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
             v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
             insets
         }*/
        binding = setContentViewWithBinding(contentLayoutId)
        binding?.root?.post {
            initViews()
            setUpClickEvents()
            setUpObservers()
        }
    }

    abstract fun initViews()
    open fun setUpClickEvents() {}
    open fun setUpObservers() {}

    private var onActivityResult: ((ActivityResult) -> Unit)? = null
    private val startIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onActivityResult?.invoke(it)
            onActivityResult = null
        }

    fun startActivityForResult(intent: Intent, callback: (ActivityResult) -> Unit = {}) {
        onActivityResult = callback
        startIntent.launch(intent)
    }

    private var onPermissionResult: ((Map<String, Boolean>) -> Unit)? = null
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            onPermissionResult?.invoke(it)
            onPermissionResult = null
        }

    fun requestPermissionLauncher(
        permissions: Array<String>,
        callback: (Map<String, Boolean>) -> Unit = {},
    ) {
        onPermissionResult = callback
        requestPermissionLauncher.launch(permissions)
    }


    override fun onDestroy() {
        super.onDestroy()
        binding == null
    }
}