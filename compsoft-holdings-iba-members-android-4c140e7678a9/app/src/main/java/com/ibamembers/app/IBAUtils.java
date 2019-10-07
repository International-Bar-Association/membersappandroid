package com.ibamembers.app;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.ibamembers.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class IBAUtils {

    public static String getFormattedElapsedTimeFromDate(Context context, Date date) {
        SimpleDateFormat daysDateFormat = new SimpleDateFormat(context.getString(R.string.content_date_seen), Locale.getDefault());

        DateTime old = new DateTime(date);
        DateTime now = new DateTime();
        Period period = new Period(old, now);

        Resources resources = context.getResources();
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays().appendSuffix(resources.getString(R.string.date_manager_day), resources.getString(R.string.date_manager_days))
                .appendHours().appendSuffix(resources.getString(R.string.date_manager_hour), resources.getString(R.string.date_manager_hours))
                .appendMinutes().appendSuffix(resources.getString(R.string.date_manager_minute), resources.getString(R.string.date_manager_minutes))
                .printZeroNever()
                .toFormatter();

        if (period.getDays() > 0) {
            return "| " + daysDateFormat.format(date);
        } else if (period.getHours() > 0) {
            return resources.getString(R.string.date_manager_hours_ago, period.getHours());
        }

        String elapsed = formatter.print(period);
        return !TextUtils.isEmpty(elapsed) ? resources.getString(R.string.date_manager_ago, elapsed) : resources.getString(R.string.date_manager_just_now);
    }

    public static String formatMessageTime(Context context, Date date, boolean usesTodayPrefix) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.messages_date_format), Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat(context.getString(R.string.messages_time_format), Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        timeFormat.setTimeZone(TimeZone.getDefault());
        String finalDateString = null;

        DateTime todayDate = new DateTime().withTimeAtStartOfDay();
        DateTime yesterdayDate = new DateTime().withTimeAtStartOfDay().minusDays(1);
        DateTime messageDateTime = new DateTime(date).withTimeAtStartOfDay();

        if (yesterdayDate.isEqual(messageDateTime)) {
            finalDateString = context.getString(R.string.messages_date_format_yesterday);
        } else if (messageDateTime.isBefore(yesterdayDate)) {
            finalDateString = dateFormat.format(date);
        } else if (messageDateTime.isEqual(todayDate)) {
            if (usesTodayPrefix) {
                finalDateString = context.getString(R.string.messages_date_format_today_at, timeFormat.format(date));
            } else {
                finalDateString = timeFormat.format(date);
            }
        }
        return finalDateString;
    }

    public static DateTime getDateTimeFromString(Context context, String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(context.getString(R.string.default_api_date_format), Locale.getDefault());
            return getDateTimeForTimeZone(new DateTime(sdf.parse(date)).toDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    //This is the timezone that is set by the phone
    public static DateTime getDateTimeForTimeZone(Date date) {
        DateTime dateTime = new DateTime();
        if (date != null) dateTime = new DateTime(date);
        return dateTime.withZone(DateTimeZone.forTimeZone(Calendar.getInstance().getTimeZone()));
    }

    //This is the timezone that is set by the phone
    public static DateTime getDateTimeOffSetTimeZone(Date date) {
        TimeZone tz = Calendar.getInstance().getTimeZone();
        long offsetFromCurrentTimezone = (long) tz.getOffset(System.currentTimeMillis());
        long hourDifference = (offsetFromCurrentTimezone * -1) / (1000 * 60 * 60);

        return getDateTimeForTimeZone(date).plusHours((int)hourDifference);
    }
}
