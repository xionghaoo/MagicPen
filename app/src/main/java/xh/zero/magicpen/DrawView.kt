package xh.zero.magicpen

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawView : View {

    companion object {
        private const val WRAP_WIDTH = 200
        private const val WRAP_HEIGHT = 200
        private const val STROKE_WIDTH = 10f
    }

    private var listener: OnDrawListener? = null
    private lateinit var paint: Paint
    private lateinit var manualPaint: Paint
    lateinit var path: Path
        private set

    private var isTouchDraw = false

    private val points = ArrayList<PointF>()

    constructor(context: Context?) : super(context) {
        initial()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initial()
    }

    private fun initial() {
        path = Path()

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.isDither = true
        paint.strokeWidth = STROKE_WIDTH
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.pathEffect = CornerPathEffect(10f)
        paint.color = resources.getColor(R.color.color_line)
        paint.style = Paint.Style.STROKE

        manualPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        manualPaint.isDither = true
        manualPaint.strokeWidth = STROKE_WIDTH
        manualPaint.strokeJoin = Paint.Join.ROUND
        manualPaint.strokeCap = Paint.Cap.ROUND
        manualPaint.pathEffect = CornerPathEffect(10f)
        manualPaint.color = resources.getColor(R.color.color_line)
        manualPaint.style = Paint.Style.FILL_AND_STROKE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(WRAP_WIDTH, WRAP_HEIGHT)
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(WRAP_WIDTH, heightSize)
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, WRAP_HEIGHT)
        } else {
            setMeasuredDimension(widthSize, heightSize)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var touchX = event.x
        var touchY = event.y

        val width = (width - paddingLeft - paddingRight).toFloat()
        val height = (height - paddingTop - paddingBottom).toFloat()

        if (touchX <= 0) {
            touchX = 0f + STROKE_WIDTH / 2
        }
        else if (touchX >= width) {
            touchX = width - STROKE_WIDTH / 2
        }

        if (touchY <= 0) {
            touchY = 0f + STROKE_WIDTH / 2
        } else if (touchY >= height) {
            touchY = height - STROKE_WIDTH / 2
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouchDraw = true
                path.reset()
                path.moveTo(touchX, touchY)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(touchX, touchY)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                listener?.onCompleted(path)
                points.clear()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (isTouchDraw) {
            canvas.drawPath(path, paint)
        } else {
            canvas.drawPath(path, manualPaint)
        }
    }

    fun setOnDrawListener(listener: OnDrawListener) {
        this.listener = listener
    }

    fun setPath(path: Path) {
        this.path = path
        isTouchDraw = false
        invalidate()
    }

    interface OnDrawListener {
        fun onCompleted(path: Path)
    }
}