package library.ussd.blocker.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.google.gson.Gson
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.crashes.ingestion.models.ErrorAttachmentLog
import org.apache.commons.io.FileUtils
import java.io.File
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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

        try {
            // val file = File(getExternalFilesDir(null), "${System.currentTimeMillis()}.txt")
            // FileUtils.writeStringToFile(file, Gson().toJson(parse(source)), "utf-8")

            Crashes.trackError(RuntimeException(),
                    mapOf(
                            "eventType" to eventType.toString(),
                            "eventClassName" to event?.className.toString(),
                            "isWindowStateChangedType" to (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED).toString(),
                            "isWindowContentChangedType" to (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED).toString()),

                    listOf(
                            ErrorAttachmentLog.attachmentWithText(Gson().toJson(parse(source)), "data.txt"),
                            ErrorAttachmentLog.attachmentWithText(event?.text.toString(), "eventText.txt"),
                            ErrorAttachmentLog.attachmentWithText(source?.text.toString(), "sourceText.txt")
                    ))
        } catch (error: Throwable) {
            Crashes.trackError(error,
                    mapOf(
                            "eventType" to eventType.toString(),
                            "eventClassName" to event?.className.toString(),
                            "isWindowStateChangedType" to (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED).toString(),
                            "isWindowContentChangedType" to (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED).toString()),

                    listOf())
        }

        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun parse(source: AccessibilityNodeInfo?): Map<String, Any> {
        val result = HashMap<String, Any>()

        val childCount = source?.childCount ?: 0
        for (i in 0 until childCount) {
            val child = source!!.getChild(i)
            val key = "$i - ${child.text}"
            val actionList = child.actionList

            actionList.forEach {
                val key2 = "$key - ${it.id}"
                result[key2] = it.label.toString()
            }

            result[key] = parse(child)
        }

        return result
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