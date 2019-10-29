package com.github.leadpogrommer.waker

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText


class WakerWidgetConfigureActivity : Activity() {
    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var mMachineName: EditText
    private lateinit var mMachineMAC: EditText
    private lateinit var mAddButton: Button


    private var mOnClickListener: View.OnClickListener = View.OnClickListener {
        val context = this@WakerWidgetConfigureActivity

        // When the button is clicked, store the string locally
        val wp = WidgetPrefs()
        wp.name = mMachineName.text.toString()
        wp.mac = mMachineMAC.text.toString()

        WidgetPrefs.saveWidgetPref(context, mAppWidgetId, wp)


        // It is the responsibility of the configuration activity to update the app widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        WakerWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId)

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)

        setContentView(R.layout.waker_widget_configure)

        mMachineMAC = findViewById(R.id.machine_mac)
        mMachineName = findViewById(R.id.machine_name)
        mAddButton = findViewById(R.id.add_button)

        mAddButton.isEnabled = false

        mMachineMAC.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                mAddButton.isEnabled = validateMAC(p0.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        WidgetPrefs.loadWidgetPref(this, mAppWidgetId)?.let {
            mMachineName.setText(it.name)
            mMachineMAC.setText(it.mac)
        }


        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        mAddButton.setOnClickListener(mOnClickListener)

    }

    companion object {
        private val macRegex: Regex = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})\$", setOf(RegexOption.MULTILINE))

        fun validateMAC(str: String): Boolean {
            return macRegex.matches(str)
        }
    }
}

