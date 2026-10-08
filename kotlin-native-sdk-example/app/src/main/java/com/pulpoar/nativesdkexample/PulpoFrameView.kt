package com.pulpoar.nativesdkexample

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

// Shows the engine's rendered frames. Each frame is drawn straight onto a SurfaceView
// from the engine thread, so Compose and the main thread never redraw a full-size image
// 30 times a second.

/** Shows what `engine` renders (camera or photo, with makeup). */
@Composable
fun PulpoFrameView(engine: PulpoEngine, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context -> SurfaceView(context).also { engine.frameSurface.attach(it) } },
        modifier = modifier,
    )
}

/** Draws the latest frame it was given, fitted to the surface (black bars around it). */
class FrameSurface : SurfaceHolder.Callback {
    private val lock = Any()
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

    // Guarded by lock.
    private var holder: SurfaceHolder? = null
    private var latest: Bitmap? = null

    fun attach(view: SurfaceView) {
        view.holder.addCallback(this)
    }

    /** Call from any thread, usually the engine thread. */
    fun display(frame: Bitmap) = synchronized(lock) {
        latest = frame
        draw()
    }

    override fun surfaceCreated(holder: SurfaceHolder) = synchronized(lock) {
        this.holder = holder
        draw()
    }

    // Redraw the last frame at the new size (matters for a still photo).
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = synchronized(lock) {
        draw()
    }

    // Waits for any draw in progress, so the surface is never used after it is gone.
    override fun surfaceDestroyed(holder: SurfaceHolder) = synchronized(lock) {
        this.holder = null
    }

    /** Must hold lock. */
    private fun draw() {
        val holder = holder ?: return
        val frame = latest ?: return
        val canvas: Canvas = (if (Build.VERSION.SDK_INT >= 26) holder.lockHardwareCanvas() else holder.lockCanvas()) ?: return
        try {
            canvas.drawColor(Color.BLACK)
            // Fit the frame inside the surface, keeping its shape.
            val scale = minOf(canvas.width.toFloat() / frame.width, canvas.height.toFloat() / frame.height)
            val width = frame.width * scale
            val height = frame.height * scale
            val left = (canvas.width - width) / 2
            val top = (canvas.height - height) / 2
            canvas.drawBitmap(frame, null, RectF(left, top, left + width, top + height), paint)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }
}
