package allstars.com.mediaviewer.ui.settings

import allstars.com.mediaviewer.R
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.view.WindowManager
import android.widget.EditText

class SettingDialog : DialogFragment() {

    companion object {
        private const val TAG = "EditTextDialog"

        private const val EXTRA_TITLE = "title"
        private const val EXTRA_HINT = "hint"
        private const val EXTRA_MULTILINE = "multiline"
        private const val EXTRA_TEXT = "text"

        fun newInstance(
            title: String? = null,
            hint: String? = null,
            text: String? = null,
            isMultiline: Boolean = false
        ): SettingDialog {
            val dialog = SettingDialog()
            val args = Bundle().apply {
                putString(EXTRA_TITLE, title)
                putString(EXTRA_HINT, hint)
                putString(EXTRA_TEXT, text)
                putBoolean(EXTRA_MULTILINE, isMultiline)
            }
            dialog.arguments = args
            return dialog
        }
    }

    lateinit var editText: EditText
    var onOk: (() -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(EXTRA_TITLE)
        val hint = arguments?.getString(EXTRA_HINT)
        val text: String? = arguments?.getString(EXTRA_TEXT)
        val isMultiline = arguments?.getBoolean(EXTRA_MULTILINE) ?: false

        val view = activity!!.layoutInflater.inflate(R.layout.dialog_edit_text, null)

        editText = view.findViewById(R.id.editText)
        editText.hint = hint

        if (isMultiline) {
            editText.minLines = 3
            editText.inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
        if (text != null) {
            editText.append(text)
        }

        val builder = AlertDialog.Builder(context!!)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onOk?.invoke()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                onCancel?.invoke()
            }
        val dialog = builder.create()

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        return dialog
    }
}