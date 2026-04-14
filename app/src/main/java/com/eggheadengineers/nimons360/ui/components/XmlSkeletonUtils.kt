package com.eggheadengineers.nimons360.ui.components

import android.animation.ValueAnimator
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Shader
import android.graphics.drawable.PaintDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.eggheadengineers.nimons360.R

/**
 * Applies a shimmer animation to a [View] by painting a translating gradient over a rounded rect.
 * Call [stopShimmer] to clean up when no longer needed.
 */
fun View.startShimmer(cornerRadiusDp: Float = 10f) {
    val density = resources.displayMetrics.density
    val cornerPx = cornerRadiusDp * density

    val baseColor = ContextCompat.getColor(context, R.color.xml_skeleton_base)
    val highlightColor = ContextCompat.getColor(context, R.color.xml_skeleton_highlight)

    val radii = FloatArray(8) { cornerPx }
    val shape = RoundRectShape(radii, null, null)
    val drawable = PaintDrawable().apply {
        this.shape = shape
        shaderFactory = object : ShapeDrawable.ShaderFactory() {
            override fun resize(width: Int, height: Int): Shader {
                return LinearGradient(
                    0f, 0f, width.toFloat(), 0f,
                    intArrayOf(baseColor, highlightColor, baseColor),
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP,
                )
            }
        }
    }

    background = drawable

    val animator = ValueAnimator.ofFloat(-1f, 2f).apply {
        duration = 1200L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { anim ->
            val fraction = anim.animatedValue as Float
            val shaderLocal = drawable.paint.shader ?: return@addUpdateListener
            val matrix = Matrix()
            matrix.setTranslate(fraction * width, 0f)
            shaderLocal.setLocalMatrix(matrix)
            invalidate()
        }
    }
    animator.start()
    setTag(R.id.shimmer_animator_tag, animator)
}

fun View.stopShimmer() {
    (getTag(R.id.shimmer_animator_tag) as? ValueAnimator)?.cancel()
    setTag(R.id.shimmer_animator_tag, null)
}

/**
 * Toggles visibility of a skeleton container and starts/stops shimmer on every placeholder view
 * whose id starts with "skeleton".
 */
fun View.setSkeletonVisible(visible: Boolean) {
    if (this !is android.view.ViewGroup) return
    this.visibility = if (visible) View.VISIBLE else View.GONE
    if (visible) {
        applyShimmerRecursive(this)
    } else {
        stopShimmerRecursive(this)
    }
}

private fun applyShimmerRecursive(view: View) {
    if (view is android.view.ViewGroup) {
        for (i in 0 until view.childCount) {
            applyShimmerRecursive(view.getChildAt(i))
        }
    } else {
        val name = try { view.resources.getResourceEntryName(view.id) } catch (_: Exception) { null }
        if (name != null && name.startsWith("skeleton")) {
            view.startShimmer()
        }
    }
}

private fun stopShimmerRecursive(view: View) {
    if (view is android.view.ViewGroup) {
        for (i in 0 until view.childCount) {
            stopShimmerRecursive(view.getChildAt(i))
        }
    } else {
        view.stopShimmer()
    }
}
