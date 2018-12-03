package allstars.com.mediaviewer.ui.custom

import allstars.com.mediaviewer.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import java.util.*


class ClockView : View {

    private var mHeight: Int = 0
    private var mWidth = 0
    private var padding = 0
    private var fontSize = 0
    private val numeralSpacing = 0
    private var handTruncation: Int = 0
    private var hourHandTruncation = 0
    private var radius = 0
    private var paint: Paint? = null
    private var isInit: Boolean = false
    private val numbers = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
    private val rect = Rect()

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    private fun initClock() {
        mHeight = height
        mWidth = width
        padding = numeralSpacing + 50
        fontSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 13f,
            resources.displayMetrics
        ).toInt()
        val min = Math.min(mHeight, mWidth)
        radius = min / 2 - padding
        handTruncation = min / 20
        hourHandTruncation = min / 7
        paint = Paint()
        isInit = true
    }

    override fun onDraw(canvas: Canvas) {
        if (!isInit) {
            initClock()
        }
        drawCircle(canvas)
        drawCenter(canvas)
        drawNumeral(canvas)
        drawHands(canvas)
        postInvalidateDelayed(500)
        invalidate()
    }

    private fun drawHand(canvas: Canvas, loc: Int, isHour: Boolean) {
        val angle = Math.PI * loc / 30 - Math.PI / 2
        val handRadius = if (isHour) radius - handTruncation - hourHandTruncation else radius - handTruncation
        paint?.let {
            canvas.drawLine(
                mWidth / 2F, mHeight / 2F,
                (mWidth / 2 + Math.cos(angle) * handRadius).toFloat(),
                (mHeight / 2 + Math.sin(angle) * handRadius).toFloat(),
                it
            )
        }
    }

    private fun drawHands(canvas: Canvas) {
        val c = Calendar.getInstance()
        var hour = c.get(Calendar.HOUR_OF_DAY)
        hour = if (hour > 12) hour - 12 else hour
        drawHand(canvas, ((hour + c.get(Calendar.MINUTE) / 60f) * 5).toInt(), true)
        drawHand(canvas, c.get(Calendar.MINUTE), false)
        drawHand(canvas, c.get(Calendar.SECOND), false)
    }

    private fun drawNumeral(canvas: Canvas) {
        paint?.let {
            it.textSize = fontSize.toFloat()
            for (number in numbers) {
                val tmp = number.toString()
                it.getTextBounds(tmp, 0, tmp.length, rect)
                val angle = Math.PI / 6 * (number - 3)
                val x = (mWidth / 2 + Math.cos(angle) * radius - rect.width() / 2).toFloat()
                val y = (mHeight / 2 + Math.sin(angle) * radius + rect.height() / 2).toFloat()
                canvas.drawText(tmp, x, y, it)
            }
        }
    }

    private fun drawCenter(canvas: Canvas) {
        paint?.let {
            it.style = Paint.Style.FILL
            canvas.drawCircle(mWidth / 2.toFloat(), mHeight / 2.toFloat(), 12F, it)
        }
    }

    private fun drawCircle(canvas: Canvas) {
        paint?.let {
            it.reset()
            it.color = ContextCompat.getColor(
                context, R.color.analog_clock_face_color
            )
            it.strokeWidth = 5F
            it.style = Paint.Style.STROKE
            it.isAntiAlias = true
            canvas.drawCircle(mWidth / 2F, mHeight / 2F, radius + padding - 10F, it)
        }
    }
}
