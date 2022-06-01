package tradly.social.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class CircleView:AppCompatTextView{
    private var strokeWidth: Float = 0.toFloat()
     var strokeColor: Int? = 0
     var solidColor:Int ?= 0
     var defaultStrokeColor = "#000"

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun draw(canvas: Canvas) {

        val circlePaint = Paint()
        circlePaint.color = solidColor?:0
        circlePaint.flags = Paint.ANTI_ALIAS_FLAG

        val strokePaint = Paint()
        strokePaint.color = strokeColor?:Color.BLACK
        strokePaint.flags = Paint.ANTI_ALIAS_FLAG

        val h = this.getHeight()
        val w = this.getWidth()

        val diameter = if (h > w) h else w
        val radius = diameter / 2

        this.setHeight(diameter)
        this.setWidth(diameter)

        canvas.drawCircle((diameter / 2).toFloat(), (diameter / 2).toFloat(), radius.toFloat(), strokePaint)

        canvas.drawCircle((diameter / 2).toFloat(), (diameter / 2).toFloat(), radius - strokeWidth, circlePaint)

        super.draw(canvas)
    }

    fun setStrokeWidth(dp: Int) {
        val scale = getContext().getResources().getDisplayMetrics().density
        strokeWidth = dp * scale

    }

    fun setStrokeColor(color: Int) {
        strokeColor = color
    }

    fun setSolidColor(color: String) {
        solidColor = if (TextUtils.isEmpty(color)) Color.WHITE else Color.parseColor(color)
    }
}