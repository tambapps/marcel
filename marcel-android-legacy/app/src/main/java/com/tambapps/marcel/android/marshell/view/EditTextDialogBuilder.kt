package com.tambapps.marcel.android.marshell.view

import android.content.Context
import android.content.DialogInterface
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import com.tambapps.marcel.android.marshell.util.toDp

typealias OnButtonClick = (dialogInterface: DialogInterface, which: Int, editText: EditText) -> Boolean
class EditTextDialogBuilder(context: Context): AlertDialog.Builder(context) {

  private val buttonClickMap = mutableMapOf<Int, OnButtonClick>()
  private val buttonColorMap = mutableMapOf<Int, Int>()
  private val editText = EditText(context)

  init {
    val layout = FrameLayout(context)
    layout.addView(editText)
    val params: FrameLayout.LayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    val dpPixelsX = 10.toDp(context)
    val dpPixelsY = 12.toDp(context)
    params.setMargins(dpPixelsX, dpPixelsY, dpPixelsX, dpPixelsY)
    editText.layoutParams = params
    super.setView(layout)
  }

  fun setHint(resId: Int): EditTextDialogBuilder {
    return setHint(context.getString(resId))
  }

  fun setHint(hint: String): EditTextDialogBuilder {
    editText.hint = hint
    return this
  }

  fun setSingleLine(singleLine: Boolean): EditTextDialogBuilder {
    editText.setSingleLine(singleLine)
    return this
  }


  fun setSingleLine(): EditTextDialogBuilder {
    editText.setSingleLine()
    return this
  }

  fun setMaxLength(n: Int): EditTextDialogBuilder {
    val filter = InputFilter.LengthFilter(n)
    editText.filters = if (editText.filters.isEmpty()) arrayOf(filter) else editText.filters + filter
    return this
  }

  fun setText(string: String): EditTextDialogBuilder {
    editText.setText(string)
    return this
  }

  fun setPositiveButton(text: CharSequence?, listener: OnButtonClick): EditTextDialogBuilder {
    super.setPositiveButton(text, null)
    buttonClickMap[AlertDialog.BUTTON_POSITIVE] = listener
    return this
  }

  fun setPositiveButton(textId: Int, listener: OnButtonClick): EditTextDialogBuilder {
    super.setPositiveButton(textId, null)
    buttonClickMap[AlertDialog.BUTTON_POSITIVE] = listener
    return this
  }

  fun setPositiveButton(textId: Int): EditTextDialogBuilder {
    super.setPositiveButton(textId, null)
    buttonClickMap.remove(AlertDialog.BUTTON_POSITIVE)
    return this
  }

  fun setPositiveButton(text: CharSequence?): EditTextDialogBuilder {
    super.setPositiveButton(text, null)
    buttonClickMap.remove(AlertDialog.BUTTON_POSITIVE)
    return this
  }

  override fun setPositiveButton(text: CharSequence?, listener: DialogInterface.OnClickListener?): EditTextDialogBuilder {
    throw IllegalAccessException("You shouldn't use this method")
  }

  override fun setPositiveButton(textId: Int, listener: DialogInterface.OnClickListener?): EditTextDialogBuilder {
    throw IllegalAccessException("You shouldn't use this method")
  }


  fun setNeutralButton(text: CharSequence?, listener: OnButtonClick): EditTextDialogBuilder {
    super.setNeutralButton(text, null)
    buttonClickMap[AlertDialog.BUTTON_NEUTRAL] = listener
    return this
  }

  fun setNeutralButton(textId: Int, listener: OnButtonClick): EditTextDialogBuilder {
    super.setNeutralButton(textId, null)
    buttonClickMap[AlertDialog.BUTTON_NEUTRAL] = listener
    return this
  }

  fun setNeutralButton(text: CharSequence?): EditTextDialogBuilder {
    super.setNeutralButton(text, null)
    buttonClickMap.remove(AlertDialog.BUTTON_NEUTRAL)
    return this
  }

  fun setNeutralButton(textId: Int): EditTextDialogBuilder {
    super.setNeutralButton(textId, null)
    buttonClickMap.remove(AlertDialog.BUTTON_NEUTRAL)
    return this
  }

  override fun setNeutralButton(text: CharSequence?, listener: DialogInterface.OnClickListener?): EditTextDialogBuilder {
    throw IllegalAccessException("You shouldn't use this method")
  }

  override fun setNeutralButton(textId: Int, listener: DialogInterface.OnClickListener?): EditTextDialogBuilder {
    throw IllegalAccessException("You shouldn't use this method")
  }

  fun setNegativeButton(text: CharSequence?, listener: OnButtonClick): EditTextDialogBuilder {
    super.setNegativeButton(text, null)
    buttonClickMap[AlertDialog.BUTTON_NEGATIVE] = listener
    return this
  }

  fun setNegativeButton(text: CharSequence?): EditTextDialogBuilder {
    super.setNegativeButton(text, null)
    buttonClickMap.remove(AlertDialog.BUTTON_NEGATIVE)
    return this
  }

  fun setNegativeButton(textId: Int, listener: OnButtonClick): EditTextDialogBuilder {
    super.setNegativeButton(textId, null)
    buttonClickMap[AlertDialog.BUTTON_NEGATIVE] = listener
    return this 
  }

  fun setNegativeButton(textId: Int): EditTextDialogBuilder {
    super.setNegativeButton(textId, null)
    buttonClickMap.remove(AlertDialog.BUTTON_NEGATIVE)
    return this
  }

  fun setButtonTextColor(button: Int, color: Int): EditTextDialogBuilder {
    buttonColorMap[button] = color
    return this
  }

  override fun setNegativeButton(text: CharSequence?, listener: DialogInterface.OnClickListener?): EditTextDialogBuilder {
    throw IllegalAccessException("You shouldn't use this method")
  }

  override fun setNegativeButton(textId: Int, listener: DialogInterface.OnClickListener?): EditTextDialogBuilder {
    throw IllegalAccessException("You shouldn't use this method")
  }

  override fun setView(view: View?): EditTextDialogBuilder {
    throw IllegalAccessException("You shouldn't use this method")
  }

  override fun setView(layoutResId: Int): EditTextDialogBuilder {
    throw IllegalAccessException("You shouldn't use this method")
  }

  override fun setTitle(titleId: Int): EditTextDialogBuilder {
    super.setTitle(titleId)
    return this
  }

  override fun setTitle(title: CharSequence?): EditTextDialogBuilder {
    super.setTitle(title)
    return this
  }

  override fun setMessage(messageId: Int): EditTextDialogBuilder {
    super.setMessage(messageId)
    return this
  }

  override fun setMessage(message: CharSequence?): EditTextDialogBuilder {
    super.setMessage(message)
    return this
  }

  override fun create(): AlertDialog {
    return super.create().apply {
      setOnShowListener {
        editText.requestFocus()
        for ((button, onClick) in buttonClickMap) {
          getButton(button).setOnClickListener {
            if (onClick.invoke(this, button, editText)) {
              dismiss()
            }
          }
        }
        for ((button, color) in buttonColorMap) {
          getButton(button).setTextColor(color)
        }
      }
    }
  }
  
}