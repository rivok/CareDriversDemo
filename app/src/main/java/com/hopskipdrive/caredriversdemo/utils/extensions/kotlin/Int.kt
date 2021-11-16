package com.hopskipdrive.caredriversdemo.utils.extensions.kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun @receiver:LayoutRes Int.inflateWithRoot(rootView: ViewGroup): View =
    LayoutInflater.from(rootView.context).inflate(this, rootView, false)
