package com.chronokeep.registration.layouts

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.chronokeep.registration.R

class ClearEditText(context: Context?, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    private var inflater: LayoutInflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var editText: EditText
    var clearButton: ImageButton

    init {
        inflater.inflate(R.layout.clearable_edit_text, this, true)
        editText = findViewById(R.id.clearable_edit)
        clearButton = findViewById(R.id.clearable_button_clear)
        clearButton.visibility = View.INVISIBLE
        clearButton.setOnClickListener {
            editText.setText("")
        }
        editText.addTextChangedListener (object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, end: Int, count: Int) {
                if (s !== null && s.isNotEmpty()) {
                    clearButton.visibility = View.VISIBLE
                } else {
                    clearButton.visibility = View.INVISIBLE
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    fun addTextChangedListener(tw: TextWatcher) {
        editText.addTextChangedListener(tw)
    }
}