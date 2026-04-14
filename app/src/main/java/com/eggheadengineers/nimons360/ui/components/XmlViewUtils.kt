package com.eggheadengineers.nimons360.ui.components

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding
import com.eggheadengineers.nimons360.R
import coil.load

private data class ViewPadding(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

private fun View.capturePadding() = ViewPadding(
    left = paddingLeft,
    top = paddingTop,
    right = paddingRight,
    bottom = paddingBottom,
)

private fun View.initialPadding(): ViewPadding {
    val existing = getTag(R.id.tag_xml_initial_padding) as? ViewPadding
    if (existing != null) return existing
    return capturePadding().also { setTag(R.id.tag_xml_initial_padding, it) }
}

private fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        doOnAttach { requestApplyInsets() }
    }
}

fun View.applyStatusBarInset() {
    if (getTag(R.id.tag_xml_status_inset_listener) == true) {
        requestApplyInsetsWhenAttached()
        return
    }
    val initial = initialPadding()
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.updatePadding(
            left = initial.left,
            top = initial.top + top,
            right = initial.right,
            bottom = initial.bottom,
        )
        insets
    }
    setTag(R.id.tag_xml_status_inset_listener, true)
    requestApplyInsetsWhenAttached()
}

fun View.applyNavigationBarInset() {
    if (getTag(R.id.tag_xml_nav_inset_listener) == true) {
        requestApplyInsetsWhenAttached()
        return
    }
    val initial = initialPadding()
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        view.updatePadding(
            left = initial.left,
            top = initial.top,
            right = initial.right,
            bottom = initial.bottom + bottom,
        )
        insets
    }
    setTag(R.id.tag_xml_nav_inset_listener, true)
    requestApplyInsetsWhenAttached()
}

fun View.refreshWindowInsets() {
    requestApplyInsetsWhenAttached()
}

fun View.setTopInsetPadding(topInset: Int) {
    val initial = initialPadding()
    updatePadding(
        left = initial.left,
        top = initial.top + topInset,
        right = initial.right,
        bottom = initial.bottom,
    )
}

fun View.setBottomInsetPadding(bottomInset: Int) {
    val initial = initialPadding()
    updatePadding(
        left = initial.left,
        top = initial.top,
        right = initial.right,
        bottom = initial.bottom + bottomInset,
    )
}

fun ImageView.loadUrl(
    url: String?,
    placeholderResId: Int = R.drawable.ic_xml_image,
) {
    load(url) {
        crossfade(true)
        placeholder(placeholderResId)
        error(placeholderResId)
        fallback(placeholderResId)
    }
}

fun EditText.updateTextIfNeeded(value: String) {
    if (text?.toString() != value) {
        setText(value)
        setSelection(value.length)
    }
}

fun View.applyElasticPress() {
    setOnTouchListener { view, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                view.animate()
                    .scaleX(0.98f)
                    .scaleY(0.98f)
                    .translationY(1f)
                    .setDuration(180L)
                    .start()
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0f)
                    .setDuration(180L)
                    .start()
            }
        }
        false
    }
}

private fun View.dpToPx(value: Float): Float = value * resources.displayMetrics.density

fun View.applyBoundedRipple(
    cornerRadiusDp: Float = 14f,
    darkSurface: Boolean = false,
) {
    val rippleColor = if (darkSurface) {
        Color.argb(32, 255, 255, 255)
    } else {
        Color.argb(18, 17, 17, 17)
    }
    val mask = GradientDrawable().apply {
        cornerRadius = dpToPx(cornerRadiusDp)
        setColor(Color.WHITE)
    }
    foreground = RippleDrawable(ColorStateList.valueOf(rippleColor), null, mask)
}

fun View.applyBorderlessRipple(darkSurface: Boolean = false) {
    val rippleColor = if (darkSurface) {
        Color.argb(32, 255, 255, 255)
    } else {
        Color.argb(18, 17, 17, 17)
    }
    foreground = RippleDrawable(ColorStateList.valueOf(rippleColor), null, null)
}
