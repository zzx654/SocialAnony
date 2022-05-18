package com.example.appportfolio.other

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Switch
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import com.example.appportfolio.R

class SwitchPreferenceExtend: SwitchPreference {
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    var listener: Listener? = null
    private var switchCompat: Switch? = null

    interface Listener {
        fun onChecked(checked: Boolean)
    }

    init {
        widgetLayoutResource = R.layout.custom_switch
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val initval=this.getPersistedBoolean(false)
        switchCompat = holder?.findViewById(R.id.custom_switch_item) as Switch
        switchCompat?.apply {
            isChecked = initval
            this.setOnCheckedChangeListener { p0, checked -> listener?.onChecked(checked) }
        }
    }
}