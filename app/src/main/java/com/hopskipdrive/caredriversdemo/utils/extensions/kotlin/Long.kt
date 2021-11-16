package com.hopskipdrive.caredriversdemo.utils.extensions.kotlin

fun Long.formatCentsAsUsd(): String {
    val dollars = this / 100L
    val cents = this % 100L
    return "$${dollars}.${if (cents < 10L) "0" else ""}${cents}"
}
