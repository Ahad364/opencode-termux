package com.dualmode.keyboard.utils

import android.content.Context
import android.util.TypedValue

/** dp → px */
fun Float.dp(context: Context): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics
)

/** sp → px */
fun Float.sp(context: Context): Float = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_SP, this, context.resources.displayMetrics
)
