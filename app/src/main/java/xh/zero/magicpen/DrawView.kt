package xh.zero.magicpen

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class DrawView : View {

    enum class Shape {
        TRIANGLE, RECT, CIRCLE, STAR, ARROW, PATH
    }

    enum class ArrowDirection {
        TOP, BOTTOM, LEFT, RIGHT
    }

    companion object {
        private const val TAG = "DrawView"

        private const val WRAP_WIDTH = 200
        private const val WRAP_HEIGHT = 200
        private const val STROKE_WIDTH = 10f

        private const val ARROW_WIDTH = 30f
        private const val ARROW_LENGTH = 200f
    }

    private var listener: OnDrawListener? = null
    private lateinit var paint: Paint
    private lateinit var manualPaint: Paint
    lateinit var path: Path
        private set
    private lateinit var arrowPath: Path

    private var isTouchDraw = true
    private var isDrawCircle = false
    private var autoDrawShape: Shape? = null
    private var arrowDirection: ArrowDirection? = null

    private var startPoint: PointF = PointF()
    private var endPoint: PointF = PointF()

    private val points = ArrayList<PointF>()

    private var isClear = false

    constructor(context: Context?) : super(context) {
        initial()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initial()
    }

    private fun initial() {
        path = Path()
        arrowPath = Path()

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
                startPoint.x = touchX
                startPoint.y = touchY
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(touchX, touchY)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                points.clear()
                endPoint.x = touchX
                endPoint.y = touchY
                Log.d(TAG, "draw direction = $arrowDirection, ${startPoint}, ${endPoint}")

                listener?.onCompleted(path)
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (isClear) {
            canvas.drawColor(Color.WHITE)
            isClear = false
            return
        }
        if (isTouchDraw) {
            canvas.drawPath(path, paint)
        } else {
            when (autoDrawShape) {
                Shape.CIRCLE -> drawCircle(canvas)
                Shape.ARROW -> drawArrow(canvas)
                Shape.PATH -> canvas.drawPath(path, manualPaint)
            }
        }

    }

    fun setOnDrawListener(listener: OnDrawListener) {
        this.listener = listener
    }

    fun setPath(path: Path) {
        this.path = path
        isTouchDraw = false
        autoDrawShape = Shape.PATH
        invalidate()
    }

    fun setCircle() {
        isTouchDraw = false
        autoDrawShape = Shape.CIRCLE
        invalidate()
    }

    fun drawArrow(direction: ArrowDirection?) {
        isTouchDraw = false
        arrowDirection = direction
        autoDrawShape = Shape.ARROW
        invalidate()
    }

    fun clear() {
        isClear = true
        invalidate()
    }

    fun getTouchDirection() : ArrowDirection? {
        if (startPoint.x < endPoint.x && startPoint.y > endPoint.y) {
            // 第一象限
            val tan = Math.atan2((startPoint.y - endPoint.y).toDouble(), (endPoint.x - startPoint.x).toDouble())
            val angle = tan * 180 / Math.PI
            if (angle > 45) {
                return ArrowDirection.TOP
            } else {
                return ArrowDirection.RIGHT
            }
        } else if (startPoint.x < endPoint.x && startPoint.y < endPoint.y) {
            // 第二象限
            val tan = Math.atan2((endPoint.y - startPoint.y).toDouble(), (endPoint.x - startPoint.x).toDouble())
            val angle = tan * 180 / Math.PI
            if (angle > 45) {
                return ArrowDirection.BOTTOM
            } else {
                return ArrowDirection.RIGHT
            }
        } else if (startPoint.x > endPoint.x && startPoint.y < endPoint.y) {
            // 第三象限
            val tan = Math.atan2((endPoint.y - startPoint.y).toDouble(), (startPoint.x - endPoint.x).toDouble())
            val angle = tan * 180 / Math.PI
            if (angle > 45) {
                return ArrowDirection.BOTTOM
            } else {
                return ArrowDirection.LEFT
            }
        } else if (startPoint.x > endPoint.x && startPoint.y > endPoint.y) {
            // 第四象限
            val tan = Math.atan2((startPoint.y - endPoint.y).toDouble(), (startPoint.x - endPoint.x).toDouble())
            val angle = tan * 180 / Math.PI
            if (angle > 45) {
                return ArrowDirection.TOP
            } else {
                return ArrowDirection.LEFT
            }
        }

        // 落在轴上
        if (startPoint.x < endPoint.x && startPoint.y == endPoint.y) {
            return ArrowDirection.RIGHT
        } else if (startPoint.x == endPoint.x && startPoint.y < endPoint.y) {
            return ArrowDirection.BOTTOM
        } else if(startPoint.x > endPoint.x && startPoint.y == endPoint.y) {
            return ArrowDirection.LEFT
        } else if (startPoint.x == endPoint.x && startPoint.y > endPoint.y) {
            return ArrowDirection.TOP
        }

        return null
    }

    private fun drawCircle(canvas: Canvas) {
        val width = (width - paddingLeft - paddingRight).toFloat()
        val height = (height - paddingTop - paddingBottom).toFloat()
        val radius = min(width, height) / 4
        canvas.drawCircle(width/2, height/2, radius, manualPaint)
    }

    private fun drawArrow(canvas: Canvas) {
        val width = (width - paddingLeft - paddingRight).toFloat()
        val height = (height - paddingTop - paddingBottom).toFloat()
        val center = PointF(width/2, height/2)
        arrowPath.reset()
        Log.d(TAG, "arrow direction = $arrowDirection")
        when (arrowDirection) {
            ArrowDirection.TOP -> {
                arrowPath.moveTo(center.x - ARROW_WIDTH / 2, center.y + ARROW_LENGTH / 2)
                arrowPath.rLineTo(ARROW_WIDTH, 0f)
                arrowPath.rLineTo(0f, -ARROW_LENGTH)
                arrowPath.rLineTo(ARROW_WIDTH, 0f)
                arrowPath.rLineTo(-ARROW_WIDTH * 1.5f, -ARROW_WIDTH * 2)
                arrowPath.rLineTo(-ARROW_WIDTH * 1.5f, ARROW_WIDTH * 2)
                arrowPath.rLineTo(ARROW_WIDTH, 0f)
                arrowPath.close()
            }
            ArrowDirection.BOTTOM -> {
                arrowPath.moveTo(center.x - ARROW_WIDTH / 2, center.y - ARROW_LENGTH / 2)
                arrowPath.rLineTo(ARROW_WIDTH, 0f)
                arrowPath.rLineTo(0f, ARROW_LENGTH)
                arrowPath.rLineTo(ARROW_WIDTH, 0f)
                arrowPath.rLineTo(-ARROW_WIDTH * 1.5f, ARROW_WIDTH * 2)
                arrowPath.rLineTo(-ARROW_WIDTH * 1.5f, -ARROW_WIDTH * 2)
                arrowPath.rLineTo(ARROW_WIDTH, 0f)
                arrowPath.close()
            }
            ArrowDirection.LEFT -> {
                arrowPath.moveTo(center.x + ARROW_LENGTH / 2, center.y - ARROW_WIDTH / 2)
                arrowPath.rLineTo(0f, ARROW_WIDTH)
                arrowPath.rLineTo(-ARROW_LENGTH, 0f)
                arrowPath.rLineTo(0f, ARROW_WIDTH)
                arrowPath.rLineTo(-ARROW_WIDTH * 2, -ARROW_WIDTH * 1.5f)
                arrowPath.rLineTo(ARROW_WIDTH * 2, -ARROW_WIDTH * 1.5f)
                arrowPath.rLineTo(0f, ARROW_WIDTH)
                arrowPath.close()
            }
            ArrowDirection.RIGHT -> {
                arrowPath.moveTo(center.x - ARROW_LENGTH / 2, center.y - ARROW_WIDTH / 2)
                arrowPath.rLineTo(0f, ARROW_WIDTH)
                arrowPath.rLineTo(ARROW_LENGTH, 0f)
                arrowPath.rLineTo(0f, ARROW_WIDTH)
                arrowPath.rLineTo(ARROW_WIDTH * 2, -ARROW_WIDTH * 1.5f)
                arrowPath.rLineTo(-ARROW_WIDTH * 2, -ARROW_WIDTH * 1.5f)
                arrowPath.rLineTo(0f, ARROW_WIDTH)
                arrowPath.close()
            }
        }
        canvas.drawPath(arrowPath, manualPaint)
    }

    interface OnDrawListener {
        fun onCompleted(path: Path)
    }
}