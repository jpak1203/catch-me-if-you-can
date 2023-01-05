package com.example.catchmeifyoucan

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

object Bindings {

    private val glideOptions = RequestOptions.placeholderOf(R.drawable.placeholder)

    @JvmStatic
    @BindingAdapter(value = ["load", "placeholder"], requireAll = false)
    fun ImageView.load(imagePath: String?, placeholder: Drawable?) {
        val options = if (null == placeholder) glideOptions else
            RequestOptions.placeholderOf(placeholder)
        Glide.with(context).load(imagePath).apply(options).into(this@load)
    }
}