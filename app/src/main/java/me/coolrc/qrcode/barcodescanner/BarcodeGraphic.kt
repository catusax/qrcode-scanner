package me.coolrc.qrcode.barcodescanner

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.mlkit.vision.barcode.Barcode
import me.coolrc.qrcode.barcodescanner.GraphicOverlay.Graphic

/**
 * Graphic instance for rendering Barcode position and content information in an overlay view.
 */
class BarcodeGraphic internal constructor(
    overlay: GraphicOverlay, //private final Paint barcodePaint;
    private val barcode: Barcode?
) :
    Graphic(overlay) {
    private val rectPaint: Paint = Paint()

    /**
     * Draws the barcode block annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        checkNotNull(barcode) { "Attempting to draw a null barcode." }

        // Draws the bounding box around the BarcodeBlock.
        val rect = RectF(barcode.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        canvas.drawRect(rect, rectPaint)

        // Draws other object info.
        /*float lineHeight = TEXT_SIZE + (2 * STROKE_WIDTH);
        float textWidth = barcodePaint.measureText(barcode.getRawValue());
        float left = isImageFlipped() ? rect.right : rect.left;
        canvas.drawRect(
                left - STROKE_WIDTH,
                rect.top - lineHeight,
                left + textWidth + (2 * STROKE_WIDTH),
                rect.top,
                labelPaint);
        //Renders the barcode at the bottom of the box.
        canvas.drawText(barcode.getRawValue(), left, rect.top - STROKE_WIDTH, barcodePaint);*/
    }

    companion object {
        //private static final int TEXT_COLOR = Color.BLACK;
        //private static final int MARKER_COLOR = Color.WHITE;
        //private static final float TEXT_SIZE = 54.0f;
        private const val STROKE_WIDTH = 4.0f
        private const val BOX_COLOR = Color.GREEN
    }

    //private final Paint labelPaint;
    init {
        rectPaint.color = BOX_COLOR
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = STROKE_WIDTH

        /*barcodePaint = new Paint();
        barcodePaint.setColor(TEXT_COLOR);
        barcodePaint.setTextSize(TEXT_SIZE);

        labelPaint = new Paint();
        labelPaint.setColor(MARKER_COLOR);
        labelPaint.setStyle(Paint.Style.FILL);*/
    }
}