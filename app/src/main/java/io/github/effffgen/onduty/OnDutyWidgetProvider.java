package io.github.effffgen.onduty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.Calendar;

import io.github.effffgen.onduty.service.OnDutyService;

/**
 * Manages widget and schedules updates.
 */
public class OnDutyWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_SCHEDULED_UPDATE =
            "io.github.baranov.SCHEDULED_UPDATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Intent.ACTION_TIMEZONE_CHANGED:
            case Intent.ACTION_TIME_CHANGED:
            case ACTION_SCHEDULED_UPDATE:
                AppWidgetManager manager = AppWidgetManager.getInstance(context);
                int[] ids = manager.getAppWidgetIds(_getComponentName(context));
                onUpdate(context, manager, ids);
                break;
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Entity person = new OnDutyService().whoIsOnDutyToday();
        int widgetText = context.getResources().getIdentifier(person.name(), "string", context.getPackageName());
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.on_duty_widget);
        views.setTextViewText(R.id.appwidget_text, context.getResources().getString(widgetText));

        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        _scheduleNextUpdate(context);
    }

    private static ComponentName _getComponentName(Context context) {
        return new ComponentName(context, OnDutyWidgetProvider.class);
    }

    private static void _scheduleNextUpdate(Context context) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, OnDutyWidgetProvider.class)
                .setAction(ACTION_SCHEDULED_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        long midnight = _getClosestMidnight();

        // Schedule to update when convenient for the system, will not wakeup device
        alarmManager.set(AlarmManager.RTC, midnight, pendingIntent);
    }

    private static long _getClosestMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        // One second later to be sure we are within the breakpoint
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 0);

        // Ensure date is in the future
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        return calendar.getTimeInMillis();
    }

}