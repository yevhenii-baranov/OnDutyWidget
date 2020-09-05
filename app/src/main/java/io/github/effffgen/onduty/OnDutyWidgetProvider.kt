package io.github.effffgen.onduty

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import io.github.effffgen.onduty.service.OnDutyService
import java.util.*

/**
 * Manages widget and schedules updates.
 */
class OnDutyWidgetProvider : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_TIME_CHANGED, ACTION_SCHEDULED_UPDATE -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids =
                    manager.getAppWidgetIds(getComponentName(context))
                onUpdate(context, manager, ids)
            }
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val person = OnDutyService().whoIsOnDutyToday()
        val widgetText =
            context.resources.getIdentifier(person.name, "string", context.packageName)
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.on_duty_widget)
        views.setTextViewText(R.id.appwidget_text, context.resources.getString(widgetText))

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        scheduleNextUpdate(context)
    }

    companion object {
        private const val ACTION_SCHEDULED_UPDATE = "io.github.baranov.SCHEDULED_UPDATE"
        private fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, OnDutyWidgetProvider::class.java)
        }

        private fun scheduleNextUpdate(context: Context) {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, OnDutyWidgetProvider::class.java)
                .setAction(ACTION_SCHEDULED_UPDATE)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val midnight = getClosestMidnight()

            // Schedule to update when convenient for the system, will not wakeup device
            alarmManager[AlarmManager.RTC, midnight] = pendingIntent
        }

        private fun getClosestMidnight(): Long {
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0

            // One second later to be sure we are within the breakpoint
            calendar[Calendar.SECOND] = 1
            calendar[Calendar.MILLISECOND] = 0

            // Ensure date is in the future
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            return calendar.timeInMillis
        }
    }
}