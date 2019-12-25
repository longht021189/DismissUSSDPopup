package library.ussd.blocker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.util.*

class USSDService : AccessibilityService() {

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()

        val info = AccessibilityServiceInfo()
        info.flags = AccessibilityServiceInfo.DEFAULT
        info.packageNames = arrayOf("com.android.phone")
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC

        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventType = event?.eventType
        val source = event?.source

        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (!event.className.contains(CLASS_NAME_DIALOG)) {
                    return
                }
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                if (source == null || source.className != CLASS_NAME_TEXT) {
                    return
                }
                if (TextUtils.isEmpty(source.text)) {
                    return
                }
            }
        }

        val eventText =
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) { event.text }
            else { Collections.singletonList(source?.text ?: "") }

        val text = processUSSDText(eventText)

        if (TextUtils.isEmpty(text)) return

        Log.e("Long", "[USSDService][onAccessibilityEvent] ${eventText.joinToString()}")

        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun processUSSDText(eventText: List<CharSequence>): String? {
        /*
        for (s in eventText) {
            val text = s.toString()
            // Return text if text is the expected ussd response
            if (true) {
                return text
            }
        }
        return null
        */

        return TAG
    }

    companion object {
        private val TAG = USSDService::class.java.simpleName
        private const val CLASS_NAME_DIALOG = "AlertDialog"
        private const val CLASS_NAME_TEXT = "android.widget.TextView"
    }
}