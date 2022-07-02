package com.example.appportfolio.other

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.appportfolio.other.Constants.ITEM


class CustomDecoration(
    private val mDivider:Drawable?,
    private val padding: Float,
    private val concat:Boolean
) : RecyclerView.ItemDecoration() {

    private val paint = Paint()

    init {
        //paint.color = color
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingStart+padding
        val right = parent.width - parent.paddingEnd-padding

        println("몇갠데 도대체 ${parent.childCount}")
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = (child.bottom + params.bottomMargin)
            val bottom = top + mDivider?.intrinsicHeight!!
            mDivider.setBounds(left.toInt(), top, right.toInt(), bottom)
            val adapterPosition = parent.getChildAdapterPosition(child)
            var viewType:Int?=null
            if(concat)
                viewType=parent.getChildViewHolder(child).bindingAdapter?.getItemViewType(adapterPosition)
            else
                mDivider.draw(c)

            viewType?.let{

                if (it == ITEM) {
                    mDivider.draw(c)
                }
            }
        }
    }
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (parent.adapter != null && position == parent.adapter!!.itemCount - 1) {
            outRect.set(0, 0, 0, mDivider?.intrinsicHeight!!)
        } else {
            super.getItemOffsets(outRect, view, parent, state)
        }
    }
}