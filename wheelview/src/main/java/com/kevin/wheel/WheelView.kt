/*
 * Copyright (c) 2019 Kevin zhou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kevin.wheel

import android.content.Context
import android.content.res.Resources
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.widget.OverScroller
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * WheelView
 *
 * @author zhouwenkai@baidu.com, Created on 2019-11-15 11:59:59
 *         Major Function：<b>WheelView</b>
 *         <p/>
 *         Note: If you modify this class please fill in the following content as a record.
 * @author mender，Modified Date Modify Content:
 */
open class WheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Runnable {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 字体大小
     */
    private var textSize: Float = 0.toFloat()

    /**
     * 是否自动调整字体大小以显示完全
     */
    private var isAutoFitTextSize: Boolean = false

    private var fontMetrics: Paint.FontMetrics? = null

    /**
     * 每个item的高度
     */
    private var itemHeight: Int = 0

    /**
     * 文字的最大宽度
     */
    private var maxTextWidth: Int = 0

    /**
     * 文字中心距离baseline的距离
     */
    private var centerToBaselineY: Int = 0

    /**
     * 可见的item条数
     */
    private var visibleItems: Int = 0

    /**
     * 每个item之间的空间，行间距
     */
    private var lineSpacing: Float = 0.toFloat()

    /**
     * 是否循环滚动
     */
    private var isCyclic: Boolean = false

    /**
     * 文字对齐方式
     */
    @TextAlign
    private var textAlign: Int = 0

    /**
     * 文字颜色
     */
    private var textColor: Int = 0

    /**
     * 选中item文字颜色
     */
    private var selectedItemTextColor: Int = 0

    /**
     * 是否显示分割线
     */
    private var isShowDivider: Boolean = false

    /**
     * 分割线的颜色
     */
    private var dividerColor: Int = 0

    /**
     * 分割线高度
     */
    private var dividerSize: Float = 0.toFloat()

    /**
     * 分割线填充类型
     */
    @DividerType
    private var dividerType: Int = 0

    /**
     * 分割线类型为DIVIDER_TYPE_WRAP时 分割线左右两端距离文字的间距
     */
    private var dividerPaddingForWrap: Float = 0.toFloat()

    /**
     * 分割线两端形状，默认圆头
     */
    private var dividerCap: Paint.Cap = Paint.Cap.ROUND

    /**
     * 是否绘制选中区域
     */
    private var isDrawSelectedRect: Boolean = false

    /**
     * 选中区域背景颜色
     */
    private var selectedRectColor: Int = 0

    /**
     * 选中区域背景左侧圆角
     */
    private var selectedRectLeftRadius: Float = 0F

    /**
     * 选中区域背景右侧圆角
     */
    private var selectedRectRightRadius: Float = 0F

    /**
     * 文字起始X
     */
    private var startX: Int = 0

    /**
     * X轴中心点
     */
    private var centerX: Int = 0

    /**
     * Y轴中心点
     */
    private var centerY: Int = 0

    /**
     * 选中边界的上下限制
     */
    private var selectedItemTopLimit: Int = 0

    private var selectedItemBottomLimit: Int = 0

    /**
     * 裁剪边界
     */
    private var clipLeft: Int = 0

    private var clipTop: Int = 0

    private var clipRight: Int = 0

    private var clipBottom: Int = 0

    /**
     * 绘制区域
     */
    private val drawRect = Rect()

    /**
     * 字体外边距，目的是留有边距
     */
    private var textBoundaryMargin: Float = 0.toFloat()

    /**
     * 数据为Integer类型时，是否需要格式转换
     */
    private var isIntegerNeedFormat: Boolean = false

    /**
     * 数据为Integer类型时，转换格式，默认转换为两位数
     */
    private var integerFormat: String = DEFAULT_INTEGER_FORMAT

    /**
     * 3D效果
     */
    private var camera = Camera()

    private var mMatrix = Matrix()

    /**
     * 是否是弯曲（3D）效果
     */
    private var isCurved: Boolean = false

    /**
     * 弯曲（3D）效果左右圆弧偏移效果方向 center 不偏移
     */
    @CurvedArcDirection
    private var curvedArcDirection: Int = 0

    /**
     * 弯曲（3D）效果左右圆弧偏移效果系数 0-1之间 越大越明显
     */
    private var curvedArcDirectionFactor: Float = 0.toFloat()

    /**
     * 弯曲（3D）效果选中后折射的偏移 与字体大小的比值，1为不偏移 越小偏移越明显
     */
    private var curvedRefractRatio: Float = 0.toFloat()

    /**
     * 数据列表
     */
    private var dataItems: MutableList<Any> = mutableListOf()

    /**
     * 数据变化时，是否重置选中下标到第一个位置
     */
    private var isResetSelectedPosition = false

    private var velocityTracker: VelocityTracker? = null

    private var maxFlingVelocity: Int = 0

    private var minFlingVelocity: Int = 0

    private var overScroller = OverScroller(context)

    /**
     * 最小滚动距离，上边界
     */
    private var minScrollY: Int = 0

    /**
     * 最大滚动距离，下边界
     */
    private var maxScrollY: Int = 0

    /**
     * Y轴滚动偏移
     */
    private var scrollOffsetY: Int = 0

    /**
     * Y轴已滚动偏移，控制重绘次数
     */
    private var scrolledY = 0

    /**
     * 手指最后触摸的位置
     */
    private var lastTouchY: Float = 0.toFloat()

    /**
     * 手指按下时间，根据按下抬起时间差处理点击滚动
     */
    private var downStartTime: Long = 0

    /**
     * 是否强制停止滚动
     */
    private var isForceFinishScroll = false

    /**
     * 是否是快速滚动，快速滚动结束后跳转位置
     */
    private var isFlingScroll: Boolean = false

    /**
     * 当前选中的下标
     */
    private var selectedItemPosition: Int = 0

    /**
     * 当前滚动经过的下标
     */
    private var currentScrollPosition: Int = 0

    /**
     * 选择监听器
     */
    private var onItemSelectedListener: OnItemSelectedListener? = null

    /**
     * 滚动变化监听
     */
    private var onWheelChangedListener: OnWheelChangedListener? = null

    /**
     * 音频
     */
    private var soundHelper = SoundHelper()

    /**
     * 是否开启音频效果
     */
    private var isSoundEffect = false

    private var selectItemPosition = 0

    private var isSmoothScroll = false

    init {
        initAttrsAndDefault(context, attrs)
        initValue(context)
    }

    /**
     * 初始化自定义属性及默认值
     *
     * @param context 上下文
     * @param attrs   attrs
     */
    private fun initAttrsAndDefault(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.WheelView)
        textSize = ta.getDimension(R.styleable.WheelView_wv_textSize, DEFAULT_TEXT_SIZE)
        isAutoFitTextSize = ta.getBoolean(R.styleable.WheelView_wv_autoFitTextSize, false)
        textAlign = ta.getInt(R.styleable.WheelView_wv_textAlign, TEXT_ALIGN_CENTER)
        textBoundaryMargin = ta.getDimension(R.styleable.WheelView_wv_textBoundaryMargin, DEFAULT_TEXT_BOUNDARY_MARGIN)
        textColor = ta.getColor(R.styleable.WheelView_wv_normalItemTextColor, DEFAULT_NORMAL_TEXT_COLOR)
        selectedItemTextColor = ta.getColor(R.styleable.WheelView_wv_selectedItemTextColor, DEFAULT_SELECTED_TEXT_COLOR)
        lineSpacing = ta.getDimension(R.styleable.WheelView_wv_lineSpacing, DEFAULT_LINE_SPACING)
        isIntegerNeedFormat = ta.getBoolean(R.styleable.WheelView_wv_integerNeedFormat, false)
        integerFormat = ta.getString(R.styleable.WheelView_wv_integerFormat) ?: DEFAULT_INTEGER_FORMAT
        visibleItems = ta.getInt(R.styleable.WheelView_wv_visibleItems, DEFAULT_VISIBLE_ITEM)
        // 跳转可见item为奇数
        visibleItems = adjustVisibleItems(visibleItems)
        selectedItemPosition = ta.getInt(R.styleable.WheelView_wv_selectedItemPosition, 0)
        // 初始化滚动下标
        currentScrollPosition = selectedItemPosition
        isCyclic = ta.getBoolean(R.styleable.WheelView_wv_cyclic, false)

        isShowDivider = ta.getBoolean(R.styleable.WheelView_wv_showDivider, false)
        dividerType = ta.getInt(R.styleable.WheelView_wv_dividerType, DIVIDER_TYPE_FILL)
        dividerSize = ta.getDimension(R.styleable.WheelView_wv_dividerHeight, DEFAULT_DIVIDER_HEIGHT)
        dividerColor = ta.getColor(R.styleable.WheelView_wv_dividerColor, DEFAULT_SELECTED_TEXT_COLOR)
        this.dividerPaddingForWrap = ta.getDimension(R.styleable.WheelView_wv_dividerPaddingForWrap, DEFAULT_TEXT_BOUNDARY_MARGIN)

        isDrawSelectedRect = ta.getBoolean(R.styleable.WheelView_wv_drawSelectedRect, false)
        selectedRectColor = ta.getColor(R.styleable.WheelView_wv_selectedRectColor, Color.TRANSPARENT)
        val selectedRectRadius = ta.getDimension(R.styleable.WheelView_wv_selectedRectRadius, 0F)
        selectedRectLeftRadius = ta.getDimension(R.styleable.WheelView_wv_selectedRectLeftRadius, selectedRectRadius)
        selectedRectRightRadius = ta.getDimension(R.styleable.WheelView_wv_selectedRectRightRadius, selectedRectRadius)

        isCurved = ta.getBoolean(R.styleable.WheelView_wv_curved, true)
        curvedArcDirection = ta.getInt(R.styleable.WheelView_wv_curvedArcDirection, CURVED_ARC_DIRECTION_CENTER)
        curvedArcDirectionFactor = ta.getFloat(R.styleable.WheelView_wv_curvedArcDirectionFactor, DEFAULT_CURVED_FACTOR)
        // 折射偏移默认值
        curvedRefractRatio = ta.getFloat(R.styleable.WheelView_wv_curvedRefractRatio, DEFAULT_REFRACT_RATIO)
        if (curvedRefractRatio > 1f) {
            curvedRefractRatio = 1.0f
        } else if (curvedRefractRatio < 0f) {
            curvedRefractRatio = DEFAULT_REFRACT_RATIO
        }
        ta.recycle()
    }

    /**
     * 初始化并设置默认值
     *
     * @param context 上下文
     */
    private fun initValue(context: Context) {
        val viewConfiguration = ViewConfiguration.get(context)
        maxFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity
        minFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
        initDefaultVolume(context)
        calculateTextSize()
        updateTextAlign()
    }

    /**
     * 获取文字对齐方式
     *
     * @return 文字对齐 [TEXT_ALIGN_LEFT] [TEXT_ALIGN_CENTER] [TEXT_ALIGN_RIGHT]
     */
    fun getTextAlign(): Int {
        return textAlign
    }

    /**
     * 设置文字对齐方式
     *
     * @param textAlign 文字对齐方式 [TEXT_ALIGN_LEFT] [TEXT_ALIGN_CENTER] [TEXT_ALIGN_RIGHT]
     */
    fun setTextAlign(@TextAlign textAlign: Int) {
        if (this.textAlign == textAlign) {
            return
        }
        this.textAlign = textAlign
        updateTextAlign()
        calculateDrawStart()
        invalidate()
    }

    /**
     * 获取未选中条目颜色
     *
     * @return 未选中条目颜色 ColorInt
     */
    fun getNormalItemTextColor(): Int {
        return textColor
    }

    /**
     * 设置未选中条目颜色
     *
     * @param textColor 未选中条目颜色 [ColorInt]
     */
    fun setNormalItemTextColor(@ColorInt textColor: Int) {
        if (this.textColor == textColor) {
            return
        }
        this.textColor = textColor
        invalidate()
    }

    /**
     * 设置未选中条目颜色
     *
     * @param textColorRes 未选中条目颜色 [ColorRes]
     */
    fun setNormalItemTextColorRes(@ColorRes textColorRes: Int) {
        setNormalItemTextColor(ContextCompat.getColor(context, textColorRes))
    }

    /**
     * 获取选中条目颜色
     *
     * @return 选中条目颜色 ColorInt
     */
    fun getSelectedItemTextColor(): Int {
        return selectedItemTextColor
    }

    /**
     * 设置选中条目颜色
     *
     * @param selectedItemTextColor 选中条目颜色 [ColorInt]
     */
    fun setSelectedItemTextColor(@ColorInt selectedItemTextColor: Int) {
        if (this.selectedItemTextColor == selectedItemTextColor) {
            return
        }
        this.selectedItemTextColor = selectedItemTextColor
        invalidate()
    }

    /**
     * 设置选中条目颜色
     *
     * @param selectedItemColorRes 选中条目颜色 [ColorRes]
     */
    fun setSelectedItemTextColorRes(@ColorRes selectedItemColorRes: Int) {
        setSelectedItemTextColor(ContextCompat.getColor(context, selectedItemColorRes))
    }

    /**
     * 获取文字距离边界的外边距
     *
     * @return 外边距值
     */
    fun getTextBoundaryMargin(): Float {
        return textBoundaryMargin
    }

    /**
     * 设置文字距离边界的外边距
     *
     * @param textBoundaryMargin 外边距值
     */
    fun setTextBoundaryMargin(textBoundaryMargin: Float) {
        setTextBoundaryMargin(textBoundaryMargin, false)
    }

    /**
     * 设置文字距离边界的外边距
     *
     * @param textBoundaryMargin 外边距值
     * @param isDp               单位是否为 dp
     */
    fun setTextBoundaryMargin(textBoundaryMargin: Float, isDp: Boolean) {
        val tempTextBoundaryMargin = this.textBoundaryMargin
        this.textBoundaryMargin = if (isDp) dp2px(textBoundaryMargin) else textBoundaryMargin
        if (tempTextBoundaryMargin == this.textBoundaryMargin) {
            return
        }
        requestLayout()
        invalidate()
    }

    /**
     * 获取Integer类型转换格式
     *
     * @return integerFormat
     */
    fun getIntegerFormat(): String {
        return integerFormat
    }

    /**
     * 设置Integer类型转换格式
     *
     * @param integerFormat
     */
    fun setIntegerFormat(integerFormat: String) {
        if (integerFormat.isEmpty() || integerFormat == this.integerFormat) {
            return
        }
        this.integerFormat = integerFormat
        calculateTextSize()
        requestLayout()
        invalidate()
    }

    /**
     * 获取可见条目数
     *
     * @return 可见条目数
     */
    fun getVisibleItems(): Int {
        return visibleItems
    }

    /**
     * 设置可见的条目数
     *
     * @param visibleItems 可见条目数
     */
    fun setVisibleItems(visibleItems: Int) {
        if (this.visibleItems == visibleItems) {
            return
        }
        this.visibleItems = adjustVisibleItems(visibleItems)
        scrollOffsetY = 0
        requestLayout()
        invalidate()
    }

    /**
     * 获取当前选中下标
     *
     * @return 当前选中的下标
     */
    fun getSelectedItemPosition(): Int {
        return selectedItemPosition
    }

    /**
     * 获取分割线颜色
     *
     * @return 分割线颜色 ColorInt
     */
    fun getDividerColor(): Int {
        return dividerColor
    }

    /**
     * 设置分割线颜色
     *
     * @param dividerColor 分割线颜色 [ColorInt]
     */
    fun setDividerColor(@ColorInt dividerColor: Int) {
        if (this.dividerColor == dividerColor) {
            return
        }
        this.dividerColor = dividerColor
        invalidate()
    }

    /**
     * 设置分割线颜色
     *
     * @param dividerColorRes 分割线颜色 [ColorRes]
     */
    fun setDividerColorRes(@ColorRes dividerColorRes: Int) {
        setDividerColor(ContextCompat.getColor(context, dividerColorRes))
    }

    /**
     * 获取分割线高度
     *
     * @return 分割线高度
     */
    fun getDividerHeight(): Float {
        return dividerSize
    }

    /**
     * 设置分割线高度
     *
     * @param dividerHeight 分割线高度
     */
    fun setDividerHeight(dividerHeight: Float) {
        setDividerHeight(dividerHeight, false)
    }

    /**
     * 设置分割线高度
     *
     * @param dividerHeight 分割线高度
     * @param isDp          单位是否是 dp
     */
    fun setDividerHeight(dividerHeight: Float, isDp: Boolean) {
        val tempDividerHeight = dividerSize
        dividerSize = if (isDp) dp2px(dividerHeight) else dividerHeight
        if (tempDividerHeight == dividerSize) {
            return
        }
        invalidate()
    }

    /**
     * 获取分割线填充类型
     *
     * @return 分割线填充类型 [DIVIDER_TYPE_FILL] [DIVIDER_TYPE_WRAP]
     */
    fun getDividerType(): Int {
        return dividerType
    }

    /**
     * 设置分割线填充类型
     *
     * @param dividerType 分割线填充类型 [DIVIDER_TYPE_FILL] [DIVIDER_TYPE_WRAP]
     */
    fun setDividerType(@DividerType dividerType: Int) {
        if (this.dividerType == dividerType) {
            return
        }
        this.dividerType = dividerType
        invalidate()
    }

    /**
     * 获取自适应分割线类型时的分割线内边距
     *
     * @return 分割线内边距
     */
    fun getDividerPaddingForWrap(): Float {
        return dividerPaddingForWrap
    }

    /**
     * 设置自适应分割线类型时的分割线内边距
     *
     * @param dividerPaddingForWrap 分割线内边距
     */
    fun setDividerPaddingForWrap(dividerPaddingForWrap: Float) {
        setDividerPaddingForWrap(dividerPaddingForWrap, false)
    }

    /**
     * 设置自适应分割线类型时的分割线内边距
     *
     * @param dividerPaddingForWrap 分割线内边距
     * @param isDp                  单位是否是 dp
     */
    fun setDividerPaddingForWrap(dividerPaddingForWrap: Float, isDp: Boolean) {
        val tempDividerPadding = this.dividerPaddingForWrap
        this.dividerPaddingForWrap =
            if (isDp) dp2px(dividerPaddingForWrap) else dividerPaddingForWrap
        if (tempDividerPadding == this.dividerPaddingForWrap) {
            return
        }
        invalidate()
    }

    /**
     * 获取分割线两端形状
     *
     * @return 分割线两端形状 [Paint.Cap.BUTT] [Paint.Cap.ROUND] [Paint.Cap.SQUARE]
     */
    fun getDividerCap(): Paint.Cap {
        return dividerCap
    }

    /**
     * 设置分割线两端形状
     *
     * @param dividerCap 分割线两端形状 [Paint.Cap.BUTT] [Paint.Cap.ROUND] [Paint.Cap.SQUARE]
     */
    fun setDividerCap(dividerCap: Paint.Cap) {
        if (this.dividerCap == dividerCap) {
            return
        }
        this.dividerCap = dividerCap
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        soundHelper.release()
    }

    /**
     * 初始化默认音量
     *
     * @param context 上下文
     */
    private fun initDefaultVolume(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE)
        if (audioManager != null) {
            audioManager as AudioManager
            // 获取系统媒体当前音量
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            // 获取系统媒体最大音量
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            // 设置播放音量
            soundHelper.playVolume = currentVolume * 1.0f / maxVolume
        } else {
            soundHelper.playVolume = 0.3f
        }
    }

    /**
     * 测量文字最大所占空间
     */
    private fun calculateTextSize() {
        paint.textSize = textSize
        for (i in dataItems.indices) {
            val textWidth = paint.measureText(getDataText(dataItems[i])).toInt()
            maxTextWidth = textWidth.coerceAtLeast(maxTextWidth)
        }

        this.fontMetrics = paint.fontMetrics
        // itemHeight实际等于字体高度+一个行间距
        this.itemHeight = (fontMetrics!!.bottom - fontMetrics!!.top + lineSpacing).toInt()
    }

    /**
     * 更新textAlign
     */
    private fun updateTextAlign() {
        when (textAlign) {
            TEXT_ALIGN_LEFT -> paint.textAlign = Paint.Align.LEFT
            TEXT_ALIGN_RIGHT -> paint.textAlign = Paint.Align.RIGHT
            else -> paint.textAlign = Paint.Align.CENTER
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Line Space算在了itemHeight中
        val height: Int = if (isCurved) {
            (this.itemHeight * visibleItems * 2 / Math.PI + paddingTop.toDouble() + paddingBottom.toDouble()).toInt()
        } else {
            this.itemHeight * visibleItems + paddingTop + paddingBottom
        }
        var width =
            (maxTextWidth.toFloat() + paddingLeft.toFloat() + paddingRight.toFloat() + textBoundaryMargin * 2).toInt()
        if (isCurved) {
            val towardRange = (sin(Math.PI / 48) * height).toInt()
            width += towardRange
        }
        setMeasuredDimension(
            resolveSizeAndState(width, widthMeasureSpec, 0),
            resolveSizeAndState(height, heightMeasureSpec, 0)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 设置内容可绘制区域
        drawRect.set(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
        centerX = drawRect.centerX()
        this.centerY = drawRect.centerY()
        selectedItemTopLimit = centerY - itemHeight / 2
        selectedItemBottomLimit = centerY + itemHeight / 2
        clipLeft = paddingLeft
        clipTop = paddingTop
        clipRight = width - paddingRight
        clipBottom = height - paddingBottom

        calculateDrawStart()
        // 计算滚动限制
        calculateLimitY()
    }

    /**
     * 起算起始位置
     */
    private fun calculateDrawStart() {
        startX = when (textAlign) {
            TEXT_ALIGN_LEFT -> (paddingLeft + textBoundaryMargin).toInt()
            TEXT_ALIGN_RIGHT -> (width - paddingRight - textBoundaryMargin).toInt()
            else -> width / 2
        }

        // 文字中心距离baseline的距离
        centerToBaselineY =
            (fontMetrics!!.ascent + (fontMetrics!!.descent - fontMetrics!!.ascent) / 2).toInt()
    }

    /**
     * 计算滚动限制
     */
    private fun calculateLimitY() {
        minScrollY = if (isCyclic) Integer.MIN_VALUE else 0
        // 下边界 (dataItemsSize - 1 - mInitPosition) * itemHeight
        maxScrollY = if (isCyclic) Integer.MAX_VALUE else (dataItems.size - 1) * this.itemHeight
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制选中区域
        drawSelectedRect(canvas)
        // 绘制分割线
        drawDivider(canvas)

        // 滚动了多少个item，滚动的Y值高度除去每行Item的高度
        val scrolledItem = scrollOffsetY / itemHeight
        // 没有滚动完一个item时的偏移值，平滑滑动
        val scrolledOffset = scrollOffsetY % itemHeight
        // 向上取整
        val halfItem = (visibleItems + 1) / 2
        // 计算的最小index
        val minIndex: Int
        // 计算的最大index
        val maxIndex: Int
        when {
            scrolledOffset < 0 -> {
                // 小于0
                minIndex = scrolledItem - halfItem - 1
                maxIndex = scrolledItem + halfItem
            }
            scrolledOffset > 0 -> {
                minIndex = scrolledItem - halfItem
                maxIndex = scrolledItem + halfItem + 1
            }
            else -> {
                minIndex = scrolledItem - halfItem
                maxIndex = scrolledItem + halfItem
            }
        }

        // 绘制item
        for (i in minIndex until maxIndex) {
            if (isCurved) {
                draw3DItem(canvas, i, scrolledOffset)
            } else {
                drawItem(canvas, i, scrolledOffset)
            }
        }

    }

    /**
     * 绘制选中区域
     *
     * @param canvas 画布
     */
    private fun drawSelectedRect(canvas: Canvas) {
        if (!isDrawSelectedRect) {
            return
        }

        paint.color = selectedRectColor

        val path = Path()
        path.moveTo(clipLeft + selectedRectLeftRadius, selectedItemTopLimit.toFloat())
        // 上边
        path.lineTo(clipRight - selectedRectLeftRadius, selectedItemTopLimit.toFloat())
        // 右上角
        val rightTopCircleRect = RectF(
            clipRight - selectedRectRightRadius * 2,
            selectedItemTopLimit.toFloat(),
            clipRight.toFloat(),
            selectedItemTopLimit + selectedRectRightRadius * 2
        )
        path.arcTo(rightTopCircleRect, 270f, 90f)
        // 右边
        path.lineTo(clipRight.toFloat(), selectedItemBottomLimit - selectedRectRightRadius)
        // 右下角
        val rightBottomCircleRect = RectF(
            clipRight - selectedRectRightRadius * 2,
            selectedItemBottomLimit - selectedRectRightRadius * 2,
            clipRight.toFloat(),
            selectedItemBottomLimit.toFloat()
        )
        path.arcTo(rightBottomCircleRect, 0f, 90f)
        // 下边
        path.lineTo(clipLeft + selectedRectLeftRadius, selectedItemBottomLimit.toFloat())
        // 左下角
        val leftBottomCircleRect = RectF(
            clipLeft.toFloat(),
            selectedItemBottomLimit - selectedRectLeftRadius * 2,
            clipLeft + selectedRectLeftRadius * 2,
            selectedItemBottomLimit.toFloat()
        )
        path.arcTo(leftBottomCircleRect, 90f, 90f)
        // 左边
        path.lineTo(clipLeft.toFloat(), selectedItemTopLimit + selectedRectLeftRadius)
        // 左上角
        val leftTopCircleRect = RectF(
            clipLeft.toFloat(),
            selectedItemTopLimit.toFloat(),
            clipLeft + selectedRectLeftRadius * 2,
            selectedItemTopLimit + selectedRectLeftRadius * 2
        )
        path.arcTo(leftTopCircleRect, 180f, 90f)

        path.close()
        canvas.drawPath(path, paint)
    }

    /**
     * 绘制分割线
     *
     * @param canvas 画布
     */
    private fun drawDivider(canvas: Canvas) {
        if (isShowDivider) {
            paint.color = dividerColor
            val originStrokeWidth = paint.strokeWidth
            paint.strokeJoin = Paint.Join.ROUND
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = dividerSize
            if (dividerType == DIVIDER_TYPE_FILL) {
                canvas.drawLine(
                    clipLeft.toFloat(),
                    selectedItemTopLimit.toFloat(),
                    clipRight.toFloat(),
                    selectedItemTopLimit.toFloat(),
                    paint
                )
                canvas.drawLine(
                    clipLeft.toFloat(),
                    selectedItemBottomLimit.toFloat(),
                    clipRight.toFloat(),
                    selectedItemBottomLimit.toFloat(),
                    paint
                )
            } else {
                // 边界处理 超过边界直接按照DIVIDER_TYPE_FILL类型处理
                val startX =
                    (centerX.toFloat() - (maxTextWidth / 2).toFloat() - dividerPaddingForWrap).toInt()
                val stopX =
                    (this.centerX.toFloat() + (maxTextWidth / 2).toFloat() + dividerPaddingForWrap).toInt()

                val wrapStartX = if (startX < clipLeft) clipLeft else startX
                val wrapStopX = if (stopX > clipRight) clipRight else stopX
                canvas.drawLine(
                    wrapStartX.toFloat(),
                    selectedItemTopLimit.toFloat(),
                    wrapStopX.toFloat(),
                    selectedItemTopLimit.toFloat(),
                    paint
                )
                canvas.drawLine(
                    wrapStartX.toFloat(),
                    selectedItemBottomLimit.toFloat(),
                    wrapStopX.toFloat(),
                    selectedItemBottomLimit.toFloat(),
                    paint
                )
            }
            paint.strokeWidth = originStrokeWidth
        }
    }

    /**
     * 绘制2D效果
     *
     * @param canvas         画布
     * @param index          下标
     * @param scrolledOffset 滚动偏移
     */
    private fun drawItem(canvas: Canvas, index: Int, scrolledOffset: Int) {
        val text = getDataByIndex(index) ?: return

        // index 的 item 距离中间项的偏移
        val item2CenterOffsetY =
            (index - scrollOffsetY / this.itemHeight) * this.itemHeight - scrolledOffset
        // 记录初始测量的字体起始X
        val startX = startX
        // 重新测量字体宽度和基线偏移
        val centerToBaselineY =
            if (isAutoFitTextSize) remeasureTextSize(text) else centerToBaselineY

        if (abs(item2CenterOffsetY) <= 0) {
            // 绘制选中的条目
            paint.color = selectedItemTextColor
            clipAndDraw2DText(
                canvas,
                text,
                selectedItemTopLimit,
                selectedItemBottomLimit,
                item2CenterOffsetY,
                centerToBaselineY
            )
        } else if (item2CenterOffsetY > 0 && item2CenterOffsetY < this.itemHeight) {
            // 绘制与下边界交汇的条目
            paint.color = selectedItemTextColor
            clipAndDraw2DText(
                canvas,
                text,
                selectedItemTopLimit,
                selectedItemBottomLimit,
                item2CenterOffsetY,
                centerToBaselineY
            )

            paint.color = textColor
            clipAndDraw2DText(
                canvas,
                text,
                selectedItemBottomLimit,
                clipBottom,
                item2CenterOffsetY,
                centerToBaselineY
            )

        } else if (item2CenterOffsetY < 0 && item2CenterOffsetY > -this.itemHeight) {
            // 绘制与上边界交汇的条目
            paint.color = selectedItemTextColor
            clipAndDraw2DText(
                canvas,
                text,
                selectedItemTopLimit,
                selectedItemBottomLimit,
                item2CenterOffsetY,
                centerToBaselineY
            )

            paint.color = textColor
            clipAndDraw2DText(
                canvas,
                text,
                clipTop,
                selectedItemTopLimit,
                item2CenterOffsetY,
                centerToBaselineY
            )

        } else {
            // 绘制其他条目
            paint.color = textColor
            clipAndDraw2DText(
                canvas,
                text,
                clipTop,
                clipBottom,
                item2CenterOffsetY,
                centerToBaselineY
            )
        }

        if (isAutoFitTextSize) {
            // 恢复重新测量之前的样式
            paint.textSize = textSize
            this.startX = startX
        }
    }

    /**
     * 裁剪并绘制2d text
     *
     * @param canvas             画布
     * @param text               绘制的文字
     * @param clipTop            裁剪的上边界
     * @param clipBottom         裁剪的下边界
     * @param item2CenterOffsetY 距离中间项的偏移
     * @param centerToBaselineY  文字中心距离baseline的距离
     */
    private fun clipAndDraw2DText(
        canvas: Canvas,
        text: String,
        clipTop: Int,
        clipBottom: Int,
        item2CenterOffsetY: Int,
        centerToBaselineY: Int
    ) {
        canvas.save()
        canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom)
        canvas.drawText(
            text,
            0,
            text.length,
            startX.toFloat(),
            (this.centerY + item2CenterOffsetY - centerToBaselineY).toFloat(),
            paint
        )
        canvas.restore()
    }

    /**
     * 重新测量字体大小
     *
     * @param contentText 被测量文字内容
     * @return 文字中心距离baseline的距离
     */
    private fun remeasureTextSize(contentText: String): Int {
        var textWidth = paint.measureText(contentText)
        var drawWidth = width.toFloat()
        var textMargin = textBoundaryMargin * 2
        // 稍微增加了一点文字边距 最大为宽度的1/10
        if (textMargin > drawWidth / 10f) {
            drawWidth = drawWidth * 9f / 10f
            textMargin = drawWidth / 10f
        } else {
            drawWidth -= textMargin
        }
        if (drawWidth <= 0) {
            return centerToBaselineY
        }
        var textSize = this.textSize
        while (textWidth > drawWidth) {
            textSize--
            if (textSize <= 0) {
                break
            }
            paint.textSize = textSize
            textWidth = paint.measureText(contentText)
        }
        // 重新计算文字起始X
        recalculateStartX(textMargin / 2.0f)
        // 高度起点也变了
        return recalculateCenterToBaselineY()
    }

    /**
     * 重新计算字体起始X
     *
     * @param textMargin 文字外边距
     */
    private fun recalculateStartX(textMargin: Float) {
        startX = when (textAlign) {
            TEXT_ALIGN_LEFT -> textMargin.toInt()
            TEXT_ALIGN_RIGHT -> (width - textMargin).toInt()
            else -> width / 2
        }
    }

    /**
     * 字体大小变化后重新计算距离基线的距离
     *
     * @return 文字中心距离baseline的距离
     */
    private fun recalculateCenterToBaselineY(): Int {
        val fontMetrics = paint.fontMetrics
        // 高度起点也变了
        return (fontMetrics.ascent + (fontMetrics.descent - fontMetrics.ascent) / 2).toInt()
    }

    /**
     * 绘制弯曲（3D）效果的item
     *
     * @param canvas         画布
     * @param index          下标
     * @param scrolledOffset 滚动偏移
     */
    private fun draw3DItem(canvas: Canvas, index: Int, scrolledOffset: Int) {
        val text = getDataByIndex(index) ?: return
        // 滚轮的半径
        val radius = (height - paddingTop - paddingBottom) / 2
        // index 的 item 距离中间项的偏移
        val item2CenterOffsetY = (index - scrollOffsetY / itemHeight) * itemHeight - scrolledOffset

        // 当滑动的角度和y轴垂直时（此时文字已经显示为一条线），不绘制文字
        if (abs(item2CenterOffsetY) > radius * Math.PI / 2) {
            return
        }

        val angle = item2CenterOffsetY.toDouble() / radius
        // 绕x轴滚动的角度
        val rotateX = Math.toDegrees(-angle).toFloat()
        // 滚动的距离映射到y轴的长度
        val translateY = (sin(angle) * radius).toFloat()
        // 滚动的距离映射到z轴的长度
        val translateZ = ((1 - cos(angle)) * radius).toFloat()
        // 透明度
        val alpha = (cos(angle) * 255).toInt()

        // 记录初始测量的字体起始X
        val startX = this.startX
        // 重新测量字体宽度和基线偏移
        val centerToBaselineY =
            if (isAutoFitTextSize) remeasureTextSize(text) else centerToBaselineY
        if (abs(item2CenterOffsetY) <= 0) {
            // 绘制选中的条目
            paint.color = selectedItemTextColor
            paint.alpha = 255
            clipAndDraw3DText(
                canvas,
                text,
                selectedItemTopLimit,
                selectedItemBottomLimit,
                rotateX,
                translateY,
                translateZ,
                centerToBaselineY
            )
        } else if (item2CenterOffsetY in 1 until itemHeight) {
            // 绘制与下边界交汇的条目
            paint.color = selectedItemTextColor
            paint.alpha = 255
            clipAndDraw3DText(
                canvas,
                text,
                selectedItemTopLimit,
                selectedItemBottomLimit,
                rotateX,
                translateY,
                translateZ,
                centerToBaselineY
            )

            paint.color = textColor
            paint.alpha = alpha
            // 缩小字体，实现折射效果
            val textSize = paint.textSize
            paint.textSize = textSize * curvedRefractRatio
            // 字体变化，重新计算距离基线偏移
            val reCenterToBaselineY = recalculateCenterToBaselineY()
            clipAndDraw3DText(
                canvas,
                text,
                selectedItemBottomLimit,
                clipBottom,
                rotateX,
                translateY,
                translateZ,
                reCenterToBaselineY
            )
            paint.textSize = textSize
        } else if (item2CenterOffsetY < 0 && item2CenterOffsetY > -itemHeight) {
            // 绘制与上边界交汇的条目
            paint.color = selectedItemTextColor
            paint.alpha = 255
            clipAndDraw3DText(
                canvas,
                text,
                selectedItemTopLimit,
                selectedItemBottomLimit,
                rotateX,
                translateY,
                translateZ,
                centerToBaselineY
            )

            paint.color = textColor
            paint.alpha = alpha

            // 缩小字体，实现折射效果
            val textSize = paint.textSize
            paint.textSize = textSize * curvedRefractRatio
            // 字体变化，重新计算距离基线偏移
            val reCenterToBaselineY = recalculateCenterToBaselineY()
            clipAndDraw3DText(
                canvas,
                text,
                clipTop,
                selectedItemTopLimit,
                rotateX,
                translateY,
                translateZ,
                reCenterToBaselineY
            )
            paint.textSize = textSize
        } else {
            // 绘制其他条目
            paint.color = textColor
            paint.alpha = alpha

            // 缩小字体，实现折射效果
            val textSize = paint.textSize
            paint.textSize = textSize * curvedRefractRatio
            // 字体变化，重新计算距离基线偏移
            val reCenterToBaselineY = recalculateCenterToBaselineY()
            clipAndDraw3DText(
                canvas,
                text,
                clipTop,
                clipBottom,
                rotateX,
                translateY,
                translateZ,
                reCenterToBaselineY
            )
            paint.textSize = textSize
        }

        if (isAutoFitTextSize) {
            // 恢复重新测量之前的样式
            paint.textSize = textSize
            this.startX = startX
        }
    }

    /**
     * 裁剪并绘制弯曲（3D）效果
     *
     * @param canvas            画布
     * @param text              绘制的文字
     * @param clipTop           裁剪的上边界
     * @param clipBottom        裁剪的下边界
     * @param rotateX           绕X轴旋转角度
     * @param offsetY           Y轴偏移
     * @param offsetZ           Z轴偏移
     * @param centerToBaselineY 文字中心距离baseline的距离
     */
    private fun clipAndDraw3DText(
        canvas: Canvas,
        text: String,
        clipTop: Int,
        clipBottom: Int,
        rotateX: Float,
        offsetY: Float,
        offsetZ: Float,
        centerToBaselineY: Int
    ) {
        canvas.save()
        canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom)
        draw3DText(canvas, text, rotateX, offsetY, offsetZ, centerToBaselineY)
        canvas.restore()
    }

    /**
     * 绘制弯曲（3D）的文字
     *
     * @param canvas            画布
     * @param text              绘制的文字
     * @param rotateX           绕X轴旋转角度
     * @param offsetY           Y轴偏移
     * @param offsetZ           Z轴偏移
     * @param centerToBaselineY 文字中心距离baseline的距离
     */
    private fun draw3DText(
        canvas: Canvas,
        text: String,
        rotateX: Float,
        offsetY: Float,
        offsetZ: Float,
        centerToBaselineY: Int
    ) {
        camera.save()
        camera.translate(0f, 0f, offsetZ)
        camera.rotateX(rotateX)
        camera.getMatrix(mMatrix)
        camera.restore()

        // 调节中心点
        var centerX = this.centerX.toFloat()
        // 根据弯曲（3d）对齐方式设置系数
        if (curvedArcDirection == CURVED_ARC_DIRECTION_LEFT) {
            centerX = this.centerX * (1 + curvedArcDirectionFactor)
        } else if (curvedArcDirection == CURVED_ARC_DIRECTION_RIGHT) {
            centerX = this.centerX * (1 - curvedArcDirectionFactor)
        }

        val centerY = this.centerY + offsetY
        mMatrix.preTranslate(-centerX, -centerY)
        mMatrix.postTranslate(centerX, centerY)

        canvas.concat(mMatrix)
        canvas.drawText(
            text,
            0,
            text.length,
            startX.toFloat(),
            centerY - centerToBaselineY,
            paint
        )

    }

    /**
     * 根据下标获取到内容
     *
     * @param index 下标
     * @return 绘制的文字内容
     */
    private fun getDataByIndex(index: Int): String? {
        val dataSize = dataItems.size
        if (dataSize == 0) {
            return null
        }

        var itemText: String? = null
        if (isCyclic) {
            var i = index % dataSize
            if (i < 0) {
                i += dataSize
            }
            itemText = getDataText(dataItems[i])
        } else {
            if (index in 0 until dataSize) {
                itemText = getDataText(dataItems[index])
            }
        }
        return itemText
    }

    /**
     * 获取item text
     *
     * @param item item数据
     * @return 文本内容
     */
    private fun getDataText(item: Any?): String {
        return when (item) {
            null -> ""
            is Int -> // 如果为整形则最少保留两位数.
                if (isIntegerNeedFormat) String.format(
                    Locale.getDefault(),
                    integerFormat,
                    item
                ) else item.toString()
            is String -> item
            else -> item.toString()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        initVelocityTracker()
        velocityTracker!!.addMovement(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 手指按下
                // 处理滑动事件嵌套 拦截事件序列
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                // 如果未滚动完成，强制滚动完成
                if (!overScroller.isFinished) {
                    // 强制滚动完成
                    overScroller.forceFinished(true)
                    isForceFinishScroll = true
                }
                lastTouchY = event.y
                // 按下时间
                downStartTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_MOVE -> {
                // 手指移动
                val moveY = event.y
                val deltaY = moveY - lastTouchY

                if (onWheelChangedListener != null) {
                    onWheelChangedListener!!.onWheelScrollStateChanged(SCROLL_STATE_DRAGGING)
                }
                if (abs(deltaY) >= 1) {
                    // deltaY 上滑为正，下滑为负
                    doScroll((-deltaY).toInt())
                    lastTouchY = moveY
                    invalidateIfYChanged()
                }
            }
            MotionEvent.ACTION_UP -> {
                // 手指抬起
                isForceFinishScroll = false
                velocityTracker!!.computeCurrentVelocity(1000, maxFlingVelocity.toFloat())
                val velocityY = velocityTracker!!.yVelocity
                if (abs(velocityY) > minFlingVelocity) {
                    // 快速滑动
                    overScroller.forceFinished(true)
                    isFlingScroll = true
                    overScroller.fling(
                        0,
                        scrollOffsetY,
                        0,
                        (-velocityY).toInt(),
                        0,
                        0,
                        minScrollY,
                        maxScrollY
                    )
                } else {
                    var clickToCenterDistance = 0
                    if (System.currentTimeMillis() - downStartTime <= DEFAULT_CLICK_CONFIRM) {
                        // 处理点击滚动
                        // 手指抬起的位置到中心的距离为滚动差值
                        clickToCenterDistance = (event.y - centerY).toInt()
                    }
                    val scrollRange =
                        clickToCenterDistance + calculateDistanceToEndPoint((scrollOffsetY + clickToCenterDistance) % itemHeight)
                    // 平稳滑动
                    overScroller.startScroll(0, scrollOffsetY, 0, scrollRange)
                }

                invalidateIfYChanged()
                ViewCompat.postOnAnimation(this, this)

                // 回收 VelocityTracker
                recycleVelocityTracker()
            }
            MotionEvent.ACTION_CANCEL ->
                // 事件被终止, 回收
                recycleVelocityTracker()
            else -> {
            }
        }
        return true
    }

    /**
     * 初始化 VelocityTracker
     */
    private fun initVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
    }

    /**
     * 回收 VelocityTracker
     */
    private fun recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker!!.recycle()
            velocityTracker = null
        }
    }

    /**
     * 计算滚动偏移
     *
     * @param distance 滚动距离
     */
    private fun doScroll(distance: Int) {
        scrollOffsetY += distance
        if (!isCyclic) {
            // 修正边界
            if (scrollOffsetY < minScrollY) {
                scrollOffsetY = minScrollY
            } else if (scrollOffsetY > maxScrollY) {
                scrollOffsetY = maxScrollY
            }
        }
    }

    /**
     * 当Y轴的偏移值改变时再重绘，减少重回次数
     */
    private fun invalidateIfYChanged() {
        if (scrollOffsetY != scrolledY) {
            scrolledY = scrollOffsetY
            // 滚动偏移发生变化
            if (onWheelChangedListener != null) {
                onWheelChangedListener!!.onWheelScroll(scrollOffsetY)
            }
            // 观察item变化
            observeItemChanged()
            invalidate()
        }
        selectedItemPosition = currentScrollPosition
        if (onItemSelectedListener != null) {
            onItemSelectedListener!!.onItemSelected(
                this,
                dataItems[currentScrollPosition],
                currentScrollPosition
            )
        }
    }

    /**
     * 观察item改变
     */
    private fun observeItemChanged() {
        // item改变回调
        val oldPosition = currentScrollPosition
        val newPosition = currentPosition
        if (oldPosition != newPosition) {
            // 改变了
            if (onWheelChangedListener != null) {
                onWheelChangedListener!!.onWheelItemChanged(oldPosition, newPosition)
            }
            // 播放音频
            playSoundEffect()
            // 更新下标
            currentScrollPosition = newPosition
        }
    }

    /**
     * 播放滚动音效
     */
    fun playSoundEffect() {
        if (isSoundEffect) {
            soundHelper.playSoundEffect()
        }
    }

    /**
     * 强制滚动完成，直接停止
     */
    fun forceFinishScroll() {
        if (!overScroller.isFinished) {
            overScroller.forceFinished(true)
        }
    }

    /**
     * 强制滚动完成，并且直接滚动到最终位置
     */
    fun abortFinishScroll() {
        if (!overScroller.isFinished) {
            overScroller.abortAnimation()
        }
    }

    /**
     * 计算距离终点的偏移，修正选中条目
     *
     * @param remainder 余数
     * @return 偏移量
     */
    private fun calculateDistanceToEndPoint(remainder: Int): Int {
        return if (abs(remainder) > this.itemHeight / 2) {
            if (scrollOffsetY < 0) {
                -this.itemHeight - remainder
            } else {
                this.itemHeight - remainder
            }
        } else {
            -remainder
        }
    }

    /**
     * 使用run方法而不是computeScroll是因为，invalidate也会执行computeScroll导致回调执行不准确
     */
    override fun run() {
        // 停止滚动更新当前下标
        if (overScroller.isFinished && !isForceFinishScroll && !isFlingScroll) {
            if (this.itemHeight == 0) {
                return
            }
            // 滚动状态停止
            if (onWheelChangedListener != null) {
                onWheelChangedListener!!.onWheelScrollStateChanged(SCROLL_STATE_IDLE)
            }
            val currentItemPosition = currentPosition
            // 当前选中的Position没变时不回调 onItemSelected()
            if (currentItemPosition == selectedItemPosition) {
                return
            }
            selectedItemPosition = currentItemPosition
            // 停止后重新赋值
            currentScrollPosition = selectedItemPosition

            // 停止滚动，选中条目回调
            if (onItemSelectedListener != null) {
                onItemSelectedListener!!.onItemSelected(
                    this,
                    dataItems[selectedItemPosition],
                    selectedItemPosition
                )
            }
            // 滚动状态回调
            if (onWheelChangedListener != null) {
                onWheelChangedListener!!.onWheelSelected(selectedItemPosition)
            }
        }

        if (overScroller.computeScrollOffset()) {
            val oldY = scrollOffsetY
            scrollOffsetY = overScroller.currY

            if (oldY != scrollOffsetY) {
                if (onWheelChangedListener != null) {
                    onWheelChangedListener!!.onWheelScrollStateChanged(SCROLL_STATE_SCROLLING)
                }
            }
            invalidateIfYChanged()
            ViewCompat.postOnAnimation(this, this)
        } else if (isFlingScroll) {
            // 滚动完成后，根据是否为快速滚动处理是否需要调整最终位置
            isFlingScroll = false
            // 快速滚动后需要调整滚动完成后的最终位置，重新启动scroll滑动到中心位置
            overScroller.startScroll(
                0,
                scrollOffsetY,
                0,
                calculateDistanceToEndPoint(scrollOffsetY % this.itemHeight)
            )
            invalidateIfYChanged()
            ViewCompat.postOnAnimation(this, this)
        }
    }


    /**
     * 根据偏移计算当前位置下标
     *
     * @return 偏移量对应的当前下标
     */
    private val currentPosition: Int
        get() {
            val itemPosition = if (scrollOffsetY < 0) {
                (scrollOffsetY - this.itemHeight / 2) / this.itemHeight
            } else {
                (scrollOffsetY + this.itemHeight / 2) / this.itemHeight
            }
            var currentPosition = itemPosition % dataItems.size
            if (currentPosition < 0) {
                currentPosition += dataItems.size
            }

            return currentPosition
        }

    /**
     * 获取音效开关状态
     *
     * @return 是否开启滚动音效
     */
    fun isSoundEffect(): Boolean {
        return isSoundEffect
    }

    /**
     * 设置音效开关
     *
     * @param isSoundEffect 是否开启滚动音效
     */
    fun setSoundEffect(isSoundEffect: Boolean) {
        this.isSoundEffect = isSoundEffect
    }

    /**
     * 设置声音效果资源
     *
     * @param rawResId 声音效果资源 越小效果越好 [RawRes]
     */
    fun setSoundEffectResource(@RawRes rawResId: Int) {
        soundHelper.load(context, rawResId)
    }

    /**
     * 获取播放音量
     *
     * @return 播放音量 range 0.0-1.0
     */
    fun getPlayVolume(): Float {
        return soundHelper.playVolume
    }

    /**
     * 设置播放音量
     *
     * @param playVolume 播放音量 range 0.0-1.0
     */
    fun setPlayVolume(@FloatRange(from = 0.0, to = 1.0) playVolume: Float) {
        soundHelper.playVolume = playVolume
    }

    /**
     * 获取指定 position 的数据
     *
     * @param position 下标
     * @return position 对应的数据
     */
    fun getItemData(position: Int): Any? {
        if (isPositionInRange(position)) {
            return dataItems[position]
        } else if (dataItems.size in 1..position) {
            return dataItems[dataItems.size - 1]
        } else if (dataItems.size > 0 && position < 0) {
            return dataItems[0]
        }
        return null
    }

    /**
     * 获取当前选中的item数据
     *
     * @return 当前选中的item数据
     */
    fun getSelectedItemData(): Any {
        return getItemData(selectedItemPosition) ?: ""
    }

    /**
     * 获取数据列表
     *
     * @return 数据列表
     */
    fun getDataItems(): MutableList<Any> {
        return dataItems
    }

    /**
     * 设置数据
     *
     * @param dataItems 数据列表
     */
    fun setDataItems(dataItems: MutableList<*>?) {
        if (dataItems == null) {
            return
        }
        this.dataItems.clear()
        this.dataItems.addAll(dataItems.filterNotNull())
        if (!isResetSelectedPosition && this.dataItems.size > 0) {
            // 不重置选中下标
            if (selectedItemPosition >= this.dataItems.size) {
                selectedItemPosition = this.dataItems.size - 1
                // 重置滚动下标
                currentScrollPosition = selectedItemPosition
            }
        } else {
            // 重置选中下标和滚动下标
            selectedItemPosition = 0
            currentScrollPosition = selectedItemPosition
        }
        // 强制滚动完成
        forceFinishScroll()
        calculateTextSize()
        calculateLimitY()
        // 重置滚动偏移
        scrollOffsetY = selectedItemPosition * this.itemHeight
        requestLayout()
        invalidate()
    }

    /**
     * 当数据变化时，是否重置选中下标到第一个
     *
     * @return 是否重置选中下标到第一个
     */
    fun isResetSelectedPosition(): Boolean {
        return isResetSelectedPosition
    }

    /**
     * 设置当数据变化时，是否重置选中下标到第一个
     *
     * @param isResetSelectedPosition 当数据变化时,是否重置选中下标到第一个
     */
    fun setResetSelectedPosition(isResetSelectedPosition: Boolean) {
        this.isResetSelectedPosition = isResetSelectedPosition
    }

    /**
     * 获取字体大小
     *
     * @return 字体大小
     */
    fun getTextSize(): Float {
        return textSize
    }

    /**
     * 设置字体大小
     *
     * @param textSize 字体大小
     * @param isSp     单位是否是 sp
     */
    fun setTextSize(textSize: Float, isSp: Boolean = false) {
        val tempTextSize = textSize
        this.textSize = if (isSp) sp2px(textSize) else textSize
        if (tempTextSize == this.textSize) {
            return
        }
        // 强制滚动完成
        forceFinishScroll()
        calculateTextSize()
        calculateDrawStart()
        calculateLimitY()
        // 字体大小变化，偏移距离也变化了
        scrollOffsetY = selectedItemPosition * this.itemHeight
        requestLayout()
        invalidate()
    }

    /**
     * 获取是否自动调整字体大小，以显示完全
     *
     * @return 是否自动调整字体大小
     */
    fun isAutoFitTextSize(): Boolean {
        return isAutoFitTextSize
    }

    /**
     * 设置是否自动调整字体大小，以显示完全
     *
     * @param isAutoFitTextSize 是否自动调整字体大小
     */
    fun setAutoFitTextSize(isAutoFitTextSize: Boolean) {
        this.isAutoFitTextSize = isAutoFitTextSize
        invalidate()
    }

    /**
     * 获取当前字体
     *
     * @return 字体
     */
    fun getTypeface(): Typeface {
        return paint.typeface
    }

    /**
     * 设置当前字体
     *
     * @param typeface 字体
     */
    fun setTypeface(typeface: Typeface) {
        if (paint.typeface === typeface) {
            return
        }
        // 强制滚动完成
        forceFinishScroll()
        paint.typeface = typeface
        calculateTextSize()
        calculateDrawStart()
        // 字体大小变化，偏移距离也变化了
        scrollOffsetY = selectedItemPosition * itemHeight
        calculateLimitY()
        requestLayout()
        invalidate()
    }

    /**
     * 获取item间距
     *
     * @return 行间距值
     */
    fun getLineSpacing(): Float {
        return lineSpacing
    }

    /**
     * 设置item间距
     *
     * @param lineSpacing 行间距值
     * @param isDp        lineSpacing 单位是否为 dp
     */
    fun setLineSpacing(lineSpacing: Float, isDp: Boolean = false) {
        val tempLineSpace = lineSpacing
        this.lineSpacing = if (isDp) dp2px(lineSpacing) else lineSpacing
        if (tempLineSpace == lineSpacing) {
            return
        }
        scrollOffsetY = 0
        calculateTextSize()
        requestLayout()
        invalidate()
    }

    /**
     * 获取数据为Integer类型时是否需要转换
     *
     * @return isIntegerNeedFormat
     */
    fun isIntegerNeedFormat(): Boolean {
        return isIntegerNeedFormat
    }

    /**
     * 设置数据为Integer类型时是否需要转换
     *
     * @param isIntegerNeedFormat 数据为Integer类型时是否需要转换
     */
    fun setIntegerNeedFormat(isIntegerNeedFormat: Boolean) {
        if (this.isIntegerNeedFormat == isIntegerNeedFormat) {
            return
        }
        this.isIntegerNeedFormat = isIntegerNeedFormat
        calculateTextSize()
        requestLayout()
        invalidate()
    }

    /**
     * 同时设置 isIntegerNeedFormat=true 和 mIntegerFormat=integerFormat
     *
     * @param integerFormat
     */
    fun setIntegerNeedFormat(integerFormat: String) {
        isIntegerNeedFormat = true
        this.integerFormat = integerFormat
        calculateTextSize()
        requestLayout()
        invalidate()
    }

    /**
     * 跳转可见条目数为奇数
     *
     * @param visibleItems 可见条目数
     * @return 调整后的可见条目数
     */
    private fun adjustVisibleItems(visibleItems: Int): Int {
        return abs(visibleItems / 2 * 2 + 1) // 当传入的值为偶数时,换算成奇数;
    }

    /**
     * 是否是循环滚动
     *
     * @return 是否是循环滚动
     */
    fun isCyclic(): Boolean {
        return isCyclic
    }

    /**
     * 设置是否循环滚动
     *
     * @param isCyclic 是否是循环滚动
     */
    fun setCyclic(isCyclic: Boolean) {
        if (this.isCyclic == isCyclic) {
            return
        }
        this.isCyclic = isCyclic

        forceFinishScroll()
        calculateLimitY()
        // 设置当前选中的偏移值
        scrollOffsetY = selectedItemPosition * itemHeight
        invalidate()
    }

    /**
     * 设置当前选中下标
     *
     * @param position       下标
     * @param isSmoothScroll 是否平滑滚动
     */
    fun setSelectedItemPosition(position: Int, isSmoothScroll: Boolean = false) {
        this.selectItemPosition = position
        this.isSmoothScroll = isSmoothScroll
        setSelectedItemPosition(position, isSmoothScroll, 0)
    }

    /**
     * 设置当前选中下标
     *
     * @param position       下标
     * @param isSmoothScroll 是否平滑滚动
     * @param smoothDuration 平滑滚动时间
     */
    fun setSelectedItemPosition(position: Int, isSmoothScroll: Boolean, smoothDuration: Int) {
        if (!isPositionInRange(position)) {
            return
        }

        // item之间差值
        val itemDistance = position * itemHeight - scrollOffsetY
        // 如果Scroller滑动未停止，强制结束动画
        abortFinishScroll()

        if (isSmoothScroll) {
            // 如果是平滑滚动并且之前的Scroll滚动完成
            overScroller.startScroll(
                0,
                scrollOffsetY,
                0,
                itemDistance,
                if (smoothDuration > 0) smoothDuration else DEFAULT_SCROLL_DURATION
            )
            invalidateIfYChanged()
            ViewCompat.postOnAnimation(this, this)

        } else {
            doScroll(itemDistance)
            selectedItemPosition = position
            // 选中条目回调
            if (onItemSelectedListener != null) {
                onItemSelectedListener!!.onItemSelected(
                    this,
                    this.dataItems[selectedItemPosition],
                    selectedItemPosition
                )
            }

            if (onWheelChangedListener != null) {
                onWheelChangedListener!!.onWheelSelected(selectedItemPosition)
            }
            invalidateIfYChanged()
        }

    }

    /**
     * 判断下标是否在数据列表范围内
     *
     * @param position 下标
     * @return 是否在数据列表范围内
     */
    fun isPositionInRange(position: Int): Boolean {
        return position >= 0 && position < this.dataItems.size
    }

    /**
     * 获取是否显示分割线
     *
     * @return 是否显示分割线
     */
    fun isShowDivider(): Boolean {
        return isShowDivider
    }

    /**
     * 设置是否显示分割线
     *
     * @param isShowDivider 是否显示分割线
     */
    fun setShowDivider(isShowDivider: Boolean) {
        if (this.isShowDivider == isShowDivider) {
            return
        }
        this.isShowDivider = isShowDivider
        invalidate()
    }

    /**
     * 获取是否绘制选中区域
     *
     * @return 是否绘制选中区域
     */
    fun isDrawSelectedRect(): Boolean {
        return isDrawSelectedRect
    }

    /**
     * 设置是否绘制选中区域
     *
     * @param isDrawSelectedRect 是否绘制选中区域
     */
    fun setDrawSelectedRect(isDrawSelectedRect: Boolean) {
        this.isDrawSelectedRect = isDrawSelectedRect
        invalidate()
    }

    /**
     * 获取选中区域颜色
     *
     * @return 选中区域颜色 ColorInt
     */
    fun getSelectedRectColor(): Int {
        return selectedRectColor
    }

    /**
     * 设置选中区域颜色
     *
     * @param selectedRectColor 选中区域颜色 [ColorInt]
     */
    fun setSelectedRectColor(@ColorInt selectedRectColor: Int) {
        this.selectedRectColor = selectedRectColor
        invalidate()
    }

    /**
     * 设置选中区域颜色
     *
     * @param selectedRectColorRes 选中区域颜色 [ColorRes]
     */
    fun setSelectedRectColorRes(@ColorRes selectedRectColorRes: Int) {
        setSelectedRectColor(ContextCompat.getColor(context, selectedRectColorRes))
    }

    /**
     * 设置选中区域圆角
     *
     * @param selectedRectRadius 选中区域圆角
     */
    fun setSelectedRectRadius(selectedRectRadius: Float) {
        this.selectedRectLeftRadius = selectedRectRadius
        this.selectedRectRightRadius = selectedRectRadius
        invalidate()
    }

    /**
     * 获取选中区域左侧圆角
     */
    fun getSelectedRectLeftRadius(): Float {
        return selectedRectLeftRadius
    }

    /**
     * 设置选中区域左侧圆角
     *
     * @param selectedLeftRectRadius 选中区域左侧圆角
     */
    fun setSelectedRectLeftRadius(selectedLeftRectRadius: Float) {
        this.selectedRectLeftRadius = selectedLeftRectRadius
        invalidate()
    }

    /**
     * 获取选中区域右侧圆角
     */
    fun getSelectedRectRightRadius(): Float {
        return selectedRectRightRadius
    }

    /**
     * 设置选中区域右侧圆角
     *
     * @param selectedRightRectRadius 选中区域右侧圆角
     */
    fun setSelectedRectRightRadius(selectedRightRectRadius: Float) {
        this.selectedRectRightRadius = selectedRightRectRadius
        invalidate()
    }

    /**
     * 获取是否是弯曲（3D）效果
     *
     * @return 是否是弯曲（3D）效果
     */
    fun isCurved(): Boolean {
        return isCurved
    }

    /**
     * 设置是否是弯曲（3D）效果
     *
     * @param isCurved 是否是弯曲（3D）效果
     */
    fun setCurved(isCurved: Boolean) {
        if (this.isCurved == isCurved) {
            return
        }
        this.isCurved = isCurved
        calculateTextSize()
        requestLayout()
        invalidate()
    }

    /**
     * 获取弯曲（3D）效果左右圆弧效果方向
     *
     * @return 左右圆弧效果方向 [.CURVED_ARC_DIRECTION_LEFT]
     * [.CURVED_ARC_DIRECTION_CENTER]
     * [.CURVED_ARC_DIRECTION_RIGHT]
     */
    fun getCurvedArcDirection(): Int {
        return curvedArcDirection
    }

    /**
     * 设置弯曲（3D）效果左右圆弧效果方向
     *
     * @param curvedArcDirection 左右圆弧效果方向 [.CURVED_ARC_DIRECTION_LEFT]
     * [.CURVED_ARC_DIRECTION_CENTER]
     * [.CURVED_ARC_DIRECTION_RIGHT]
     */
    fun setCurvedArcDirection(@CurvedArcDirection curvedArcDirection: Int) {
        if (this.curvedArcDirection == curvedArcDirection) {
            return
        }
        this.curvedArcDirection = curvedArcDirection
        invalidate()
    }

    /**
     * 获取弯曲（3D）效果左右圆弧偏移效果方向系数
     *
     * @return 左右圆弧偏移效果方向系数
     */
    fun getCurvedArcDirectionFactor(): Float {
        return curvedArcDirectionFactor
    }

    /**
     * 设置弯曲（3D）效果左右圆弧偏移效果方向系数
     *
     * @param curvedArcDirectionFactor 左右圆弧偏移效果方向系数 range 0.0-1.0 越大越明显
     */
    fun setCurvedArcDirectionFactor(
        @FloatRange(
            from = 0.0,
            to = 1.0
        ) curvedArcDirectionFactor: Float
    ) {
        var tempcurvedArcDirectionFactor = curvedArcDirectionFactor
        if (this.curvedArcDirectionFactor == tempcurvedArcDirectionFactor) {
            return
        }
        if (curvedArcDirectionFactor < 0) {
            tempcurvedArcDirectionFactor = 0f
        } else if (curvedArcDirectionFactor > 1) {
            tempcurvedArcDirectionFactor = 1f
        }
        this.curvedArcDirectionFactor = tempcurvedArcDirectionFactor
        invalidate()
    }

    /**
     * 获取折射偏移比例
     *
     * @return 折射偏移比例
     */
    fun getCurvedRefractRatio(): Float {
        return curvedRefractRatio
    }

    /**
     * 设置选中条目折射偏移比例
     *
     * @param curvedRefractRatio 折射偏移比例 range 0.0-1.0
     */
    fun setCurvedRefractRatio(@FloatRange(from = 0.0, to = 1.0) curvedRefractRatio: Float) {
        val tempRefractRatio = this.curvedRefractRatio
        this.curvedRefractRatio = curvedRefractRatio
        if (this.curvedRefractRatio > 1f) {
            this.curvedRefractRatio = 1.0f
        } else if (this.curvedRefractRatio < 0f) {
            this.curvedRefractRatio =
                DEFAULT_REFRACT_RATIO
        }
        if (tempRefractRatio == this.curvedRefractRatio) {
            return
        }
        invalidate()
    }

    /**
     * 获取选中监听
     *
     * @return 选中监听器
     */
    fun getOnItemSelectedListener(): OnItemSelectedListener? {
        return onItemSelectedListener
    }

    /**
     * 设置选中监听
     *
     * @param onItemSelectedListener 选中监听器
     */
    fun setOnItemSelectedListener(onItemSelectedListener: OnItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener
        setSelectedItemPosition(selectedItemPosition, isSmoothScroll, 0)
    }

    /**
     * 获取滚动变化监听
     *
     * @return 滚动变化监听器
     */
    fun getOnWheelChangedListener(): OnWheelChangedListener? {
        return onWheelChangedListener
    }

    /**
     * 设置滚动变化监听
     *
     * @param onWheelChangedListener 滚动变化监听器
     */
    fun setOnWheelChangedListener(onWheelChangedListener: OnWheelChangedListener) {
        this.onWheelChangedListener = onWheelChangedListener
    }

    /**
     * 条目选中监听器
     */
    interface OnItemSelectedListener {

        /**
         * 条目选中回调
         *
         * @param wheelView wheelView
         * @param data      选中的数据
         * @param position  选中的下标
         */
        fun onItemSelected(wheelView: WheelView, data: Any, position: Int)
    }

    /**
     * WheelView滚动状态改变监听器
     */
    interface OnWheelChangedListener {

        /**
         * WheelView 滚动
         *
         * @param scrollOffsetY 滚动偏移
         */
        fun onWheelScroll(scrollOffsetY: Int)

        /**
         * WheelView 条目变化
         *
         * @param oldPosition 旧的下标
         * @param newPosition 新下标
         */
        fun onWheelItemChanged(oldPosition: Int, newPosition: Int)

        /**
         * WheelView 选中
         *
         * @param position 选中的下标
         */
        fun onWheelSelected(position: Int)

        /**
         * WheelView 滚动状态
         *
         * @param state 滚动状态 [WheelView.SCROLL_STATE_IDLE]
         * [WheelView.SCROLL_STATE_DRAGGING]
         * [WheelView.SCROLL_STATE_SCROLLING]
         */
        fun onWheelScrollStateChanged(state: Int)
    }

    open class SimpleOnWheelChangedListener : OnWheelChangedListener {
        override fun onWheelScroll(scrollOffsetY: Int) {}

        override fun onWheelItemChanged(oldPosition: Int, newPosition: Int) {}

        override fun onWheelSelected(position: Int) {}

        override fun onWheelScrollStateChanged(state: Int) {}
    }

    /**
     * SoundPool 辅助类
     */
    private class SoundHelper {

        private val soundPool: SoundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder().build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(1, AudioManager.STREAM_SYSTEM, 1)
        }

        private var soundId: Int = 0

        /**
         * 音频播放音量 range 0.0-1.0
         */
        var playVolume: Float = 0.toFloat()

        /**
         * 加载音频资源
         *
         * @param context 上下文
         * @param resId   音频资源 [RawRes]
         */
        fun load(context: Context, @RawRes resId: Int) {
            soundId = soundPool.load(context, resId, 1)
        }

        /**
         * 播放声音效果
         */
        fun playSoundEffect() {
            if (soundId != 0) {
                soundPool.play(soundId, playVolume, playVolume, 1, 0, 1f)
            }
        }

        /**
         * 释放SoundPool
         */
        fun release() {
            soundPool.release()
        }
    }

    companion object {

        private val DEFAULT_LINE_SPACING =
            dp2px(2f)

        private val DEFAULT_TEXT_SIZE =
            sp2px(15f)

        private val DEFAULT_TEXT_BOUNDARY_MARGIN =
            dp2px(2f)

        private val DEFAULT_DIVIDER_HEIGHT =
            dp2px(1f)

        private const val DEFAULT_NORMAL_TEXT_COLOR = Color.DKGRAY

        private const val DEFAULT_SELECTED_TEXT_COLOR = Color.BLACK

        private const val DEFAULT_VISIBLE_ITEM = 5

        private const val DEFAULT_SCROLL_DURATION = 250

        private const val DEFAULT_CLICK_CONFIRM: Long = 120

        private const val DEFAULT_INTEGER_FORMAT = "%02d"

        /**
         * 默认折射比值，通过字体大小来实现折射视觉差
         */
        private const val DEFAULT_REFRACT_RATIO = 0.9f

        /**
         * 文字对齐方式
         */
        const val TEXT_ALIGN_LEFT = 0

        const val TEXT_ALIGN_CENTER = 1

        const val TEXT_ALIGN_RIGHT = 2

        /**
         * 滚动状态
         */
        const val SCROLL_STATE_IDLE = 0

        const val SCROLL_STATE_DRAGGING = 1

        const val SCROLL_STATE_SCROLLING = 2

        /**
         * 弯曲效果对齐方式
         */
        const val CURVED_ARC_DIRECTION_LEFT = 0

        const val CURVED_ARC_DIRECTION_CENTER = 1

        const val CURVED_ARC_DIRECTION_RIGHT = 2

        const val DEFAULT_CURVED_FACTOR = 0.75f

        /**
         * 分割线填充类型
         */
        const val DIVIDER_TYPE_FILL = 0

        const val DIVIDER_TYPE_WRAP = 1

        /**
         * dp转换px
         *
         * @param dp dp值
         * @return 转换后的px值
         */
        private fun dp2px(dp: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                Resources.getSystem().displayMetrics
            )
        }

        /**
         * sp转换px
         *
         * @param sp sp值
         * @return 转换后的px值
         */
        private fun sp2px(sp: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                Resources.getSystem().displayMetrics
            )
        }
    }
}

/**
 * 文字对齐方式注解
 */
@IntDef(WheelView.TEXT_ALIGN_LEFT, WheelView.TEXT_ALIGN_CENTER, WheelView.TEXT_ALIGN_RIGHT)
@Retention(AnnotationRetention.SOURCE)
annotation class TextAlign

/**
 * 左右圆弧效果方向注解
 */
@IntDef(WheelView.CURVED_ARC_DIRECTION_LEFT, WheelView.CURVED_ARC_DIRECTION_CENTER, WheelView.CURVED_ARC_DIRECTION_RIGHT)
@Retention(AnnotationRetention.SOURCE)
annotation class CurvedArcDirection

/**
 * 分割线类型注解
 */
@IntDef(WheelView.DIVIDER_TYPE_FILL, WheelView.DIVIDER_TYPE_WRAP)
@Retention(AnnotationRetention.SOURCE)
annotation class DividerType