package openfoodfacts.github.scrachx.openfood.camera

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import com.google.android.gms.common.images.Size
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.utils.CameraUtils.isPortraitMode
import java.io.IOException

/**
 * Preview the camera image in the screen.
 */
class CameraSourcePreview(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val surfaceView: SurfaceView = SurfaceView(context).apply {
        holder.addCallback(SurfaceCallback())
        addView(this)
    }
    private var graphicOverlay: GraphicOverlay? = null
    private var startRequested = false
    private var surfaceAvailable = false
    private var cameraSource: CameraSource? = null
    private var cameraPreviewSize: Size? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        graphicOverlay = findViewById(R.id.camera_preview_graphic_overlay)
    }

    @Throws(IOException::class)
    fun start(cameraSource: CameraSource) {
        this.cameraSource = cameraSource
        startRequested = true
        startIfReady()
    }

    fun stop() {
        cameraSource?.let {
            it.stop()
            cameraSource = null
            startRequested = false
        }
    }

    @Throws(IOException::class)
    private fun startIfReady() {
        if (startRequested && surfaceAvailable) {
            cameraSource?.start(surfaceView.holder)
            requestLayout()
            graphicOverlay?.let { overlay ->
                cameraSource?.let {
                    overlay.setCameraInfo(it)
                }
                overlay.clear()
            }
            startRequested = false
        }
    }

    /**
     * Called from layout when this view should
     * assign a size and position to each of its children.
     *
     * Derived classes with children should override
     * this method and call layout on each of
     * their children.
     *
     * @param changed This is a new size or position for this view
     * @param left Left position, relative to parent
     * @param top Top position, relative to parent
     * @param right Right position, relative to parent
     * @param bottom Bottom position, relative to parent
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val layoutWidth = right - left
        val layoutHeight = bottom - top

        var previewWidth: Int = layoutWidth
        var previewHeight: Int = layoutHeight

        cameraPreviewSize?.let { size ->
            if (isPortraitMode(context)) {
                // Camera's natural orientation is landscape, so need to swap width and height.
                previewWidth = size.height
                previewHeight = size.width
            } else {
                previewWidth = size.width
                previewHeight = size.height
            }
        }

        // Match the width of the child view to its parent.
        if (layoutWidth * previewHeight <= layoutHeight * previewWidth) {
            val scaledChildWidth = previewWidth * layoutHeight / previewHeight

            for (i in 0 until childCount) {
                getChildAt(i).layout((layoutWidth - scaledChildWidth) / 2, 0,
                        (layoutWidth + scaledChildWidth) / 2, height)
            }
        } else {
            val scaledChildHeight = previewHeight * layoutWidth / previewWidth

            for (i in 0 until childCount) {
                getChildAt(i).layout(0, (layoutHeight - scaledChildHeight) / 2,
                        width, (layoutHeight + scaledChildHeight) / 2)
            }
        }

        try {
            startIfReady()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Could not start camera source.", e)
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            surfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Could not start camera source.", e)
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            surfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit
    }

    companion object {
        private const val LOG_TAG = "CameraSourcePreview"
    }
}
