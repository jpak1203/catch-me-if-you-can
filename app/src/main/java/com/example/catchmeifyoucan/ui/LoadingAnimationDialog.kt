package com.example.catchmeifyoucan.ui

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.TypedArray
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.DialogFragment
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.DialogLoadingAnimationBinding

class LoadingAnimationDialog : DialogFragment(), DialogInterface.OnDismissListener {

    companion object {
        private val TAG = LoadingAnimationDialog::class.java.simpleName

        private const val DIM_AMOUNT_FLOAT = 0.2f
    }

    private lateinit var binding: DialogLoadingAnimationBinding

    fun isShowing() = true == dialog?.isShowing

    override fun getTheme(): Int {
        return R.style.TransparentLoadingDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val window = dialog.window

        //dim the background
        window?.setDimAmount(DIM_AMOUNT_FLOAT)
        window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  DialogLoadingAnimationBinding.inflate(inflater, container, false)

        val colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(getColorPrimary(), BlendModeCompat.SRC_ATOP)
        binding.circularProgressIndicator.indeterminateDrawable?.colorFilter = colorFilter

        binding.dialogConstraintLayout.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    private fun getColorPrimary(): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
            requireContext().obtainStyledAttributes(typedValue.data, intArrayOf(androidx.appcompat.R.attr.colorPrimary))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }

}