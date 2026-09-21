package org.techtown.medicheck;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

public class SnoozeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        AlarmReceiver.stopAlarm(); // 알람 멈추기
        // 알림 지우기
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(1);

        // 5분 뒤로 알람 설정
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class); // 원래 알람을 울리던 리시버
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + 5 * 60 * 1000; // 현재 시간 + 5분


        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    // 정확한 알람 설정 가능
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                } else {
                    // 권한이 없는 경우: 사용자에게 설정으로 유도
                    Intent intentSettings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intentSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentSettings);
                }
            } else {
                // Android 12 이하에서는 바로 설정
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }

            // 🔔 알람 설정되었음을 사용자에게 알림
            Toast.makeText(context, "5분 후에 다시 알람이 울립니다", Toast.LENGTH_SHORT).show();
        }
    }
}
