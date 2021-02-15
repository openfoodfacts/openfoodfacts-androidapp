/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/** Preview the camera image in the screen.  */
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

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val layoutWidth = right - left
        val layoutHeight = bottom - top

        cameraSource?.previewSize?.let { cameraPreviewSize = it }

        val previewWidth = cameraPreviewSize?.let { size ->
            if (isPortraitMode(context)) {
                // Camera's natural orientation is landscape, so need to swap width and height.
                size.height
            } else {
                size.width
            }
        } ?: layoutWidth

        val previewHeight = cameraPreviewSize?.let { size ->
            if (isPortraitMode(context)) {
                // Camera's natural orientation is landscape, so need to swap width and height.
                size.width
            } else {
                size.height
            }
        } ?: layoutHeight

        // Match the width of the child view to its parent.
        if (layoutWidth*previewHeight <= layoutHeight*previewWidth) {
            val scaledChildWidth = previewWidth * layoutHeight / previewHeight;

            for (i in 0 until childCount) {
                getChildAt(i).layout((layoutWidth - scaledChildWidth) / 2, 0,
                        (layoutWidth + scaledChildWidth) / 2, height)
            }
        } else {
            // When the child view is too tall to be fitted in its parent: If the child view is
            // static overlay view container (contains views such as bottom prompt chip), we apply
            // the size of the parent view to it. Otherwise, we offset the top/bottom position
            // equally to position it in the center of the parent.
            val scaledChildHeight = previewHeight * layoutWidth / previewWidth;

            for (i in 0 until childCount) {
                getChildAt(i).layout(0, (layoutHeight - scaledChildHeight) / 2,
                        width, (layoutHeight + scaledChildHeight) / 2)
            }
        }

        try {
            startIfReady()
        } catch (e: IOException) {
            Log.e(TAG, "Could not start camera source.", e)
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            surfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            surfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        }
    }

    companion object {
        private const val TAG = "CameraSourcePreview"
    }
}
