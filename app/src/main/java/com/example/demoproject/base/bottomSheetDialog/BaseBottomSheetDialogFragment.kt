package com.example.demoproject.base.bottomSheetDialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.example.demoproject.utils.bindingExtensions.inflateWithBinding
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment<T : ViewDataBinding>(@LayoutRes private val layoutResId: Int) :
    BottomSheetDialogFragment() {

    protected var binding: T? = null
        private set

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = inflateWithBinding(inflater, container, layoutResId)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        setUpClickEvents()
        setUpObservers()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    abstract fun initViews()
    open fun setUpClickEvents() {}
    open fun setUpObservers() {}

    fun makeBottomSheetExpanded() {
        val view: FrameLayout? = dialog?.findViewById(R.id.design_bottom_sheet)
        //Set the view height
        view?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        //Get behavior
        val behavior = view?.let { BottomSheetBehavior.from(it) }
        behavior?.peekHeight = resources.displayMetrics.heightPixels
        //Set the expanded state
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding == null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}