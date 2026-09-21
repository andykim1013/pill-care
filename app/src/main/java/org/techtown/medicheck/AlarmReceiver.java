package org.techtown.medicheck;

import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.content.ContextCompat.startActivity;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.app.AlarmManager;
import java.util.Calendar;
import java.util.Locale;

import android.speech.tts.TextToSpeech;
public class AlarmReceiver extends BroadcastReceiver {
    public static MediaPlayer mediaPlayer;
    private static TextToSpeech tts;
    private static Handler ttsHandler = new Handler(Looper.getMainLooper());
    private static Runnable ttsRunnable;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "알람이 도착했습니다!");

        // 알람 상태 (스위치 상태) 확인--------------------------------------------------------------
        /*
        SharedPreferences prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE);
        String alarmName1 = intent.getStringExtra("alarmName");
        int hour1 = intent.getIntExtra("hour", -1);  // hour 값 가져오기
        int minute1 = intent.getIntExtra("minute", -1);  // minute 값 가져오기
        String time = String.format("%02d:%02d", hour1, minute1);  // "HH:mm" 형식으로 결합
        String alarmKey = "switch_" + alarmName1 + "_" + time;
        boolean isAlarmActive = prefs.getBoolean(alarmKey, true);

        if (!isAlarmActive) {
            Log.d("AlarmReceiver", "알람이 비활성화 되어 있습니다.");
            return;  // 스위치가 꺼져 있으면 알람 처리하지 않음
        }
        */
        //----------------------------------------------------------------------------------------

        // 알림 채널 설정 (Android 8.0 이상에서만)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alarm_channel",
                    "Alarm Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // AlarmManager 인스턴스를 가져옵니다.
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            // 정확한 알람을 예약할 수 있는지 확인합니다.
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // 권한이 없으면 권한 요청
                Intent intent1 = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent1);
                return; // 권한 요청 후 알람 설정을 진행하지 않습니다.
            }
        }

        // 알람 소리 울리기 (예외 처리 추가)
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.aquaa);
            if (mediaPlayer != null) {
                mediaPlayer.start();

                // TTS 초기화
                tts = new TextToSpeech(context, status -> {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(Locale.KOREAN);
                        String message = "약 복용 시간입니다! 약을 드신 후 체크박스를 눌러주세요";

                        // TTS 반복 Runnable 정의
                        ttsRunnable = new Runnable() {
                            @Override
                            public void run() {
                                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "AlarmTTS");
                                // 3초마다 반복 (말 길이에 따라 조절해도 돼)
                                ttsHandler.postDelayed(this, 7000);
                            }
                        };

                        // 반복 시작
                        ttsRunnable.run();
                    }
                });


            } else {
                Log.e("AlarmReceiver", "MediaPlayer가 null입니다. 알람 사운드를 재생할 수 없음.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 알람 끄기 위한 Intent
        Intent stopIntent = new Intent(context, StopAlarmReceiver.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                stopIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE // 기존 알람 취소 후 새로 설정
        );

        // 5분 후 알람을 위한 Intent
        Intent snoozeIntent = new Intent(context, SnoozeReceiver.class);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                snoozeIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Notification 만들기
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // <-- 이 줄 추가
                .setContentTitle("약 먹을 시간입니다")
                .setContentText("시간내에 약을 복용해주세요")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "끄기", stopPendingIntent)
                .addAction(android.R.drawable.ic_popup_reminder, "5분 후 알람", snoozePendingIntent);

        //코테 준비
        //토익
        //이즈케어텍 C# ,관련 프로젝트
        //대학원 진학 후 지원 시 우대사항


        // NotificationManagerCompat 인스턴스 생성
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // 권한이 있는지 확인 후 알림 보내기
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // 권한이 있을 경우 알림 보내기
            notificationManager.notify(1, builder.build());
        } else {
            // 권한이 없으면 알림을 보내지 않음
            Log.e("AlarmReceiver", "알림 권한이 없습니다.");
        }

        // 반복 알람 재등록======================================================================
        int dayOfWeek = intent.getIntExtra("dayOfWeek", -1);
        int hour = intent.getIntExtra("hour", -1);
        int minute = intent.getIntExtra("minute", -1);
        int requestCode = intent.getIntExtra("requestCode", -1);
        String alarmName = intent.getStringExtra("alarmName");
        int intervalDays = intent.getIntExtra("intervalDays", -1); // 주기 반복일 경우


        //요일반복
        if (dayOfWeek != -1 && hour != -1 && minute != -1 && requestCode != -1) {
            Calendar nextAlarmTime = Calendar.getInstance();
            nextAlarmTime.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            nextAlarmTime.set(Calendar.HOUR_OF_DAY, hour);
            nextAlarmTime.set(Calendar.MINUTE, minute);
            nextAlarmTime.set(Calendar.SECOND, 0);
            nextAlarmTime.add(Calendar.WEEK_OF_YEAR, 1);  // 다음 주로

            Intent newIntent = new Intent(context, AlarmReceiver.class);

            Bundle extras = intent.getExtras();
            if (extras != null) {
                newIntent.putExtras(extras);  // 기존 extras를 새로운 intent에 넣음
            }

            //newIntent.putExtra("dayOfWeek", dayOfWeek);
            //newIntent.putExtra("hour", hour);
            //newIntent.putExtra("minute", minute);
            //newIntent.putExtra("requestCode", requestCode);
            //.putExtra("alarmName", alarmName);

            PendingIntent newPendingIntent = PendingIntent.getBroadcast(context, requestCode, newIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), newPendingIntent);

            Log.d("AlarmReceiver", "[요일 반복] 재등록 시작 - requestCode=" + requestCode + ", dayOfWeek=" + dayOfWeek +
                    ", 시간=" + hour + ":" + minute + ", 다음 알람=" + nextAlarmTime.getTime());
        }

        // 주기 반복 처리 (intervalDays가 설정된 경우)
        if (intervalDays != -1) {
            // 주기 반복 알람 설정
            Calendar nextAlarmTime = Calendar.getInstance();
            nextAlarmTime.add(Calendar.DAY_OF_YEAR, intervalDays);  // 주기만큼 더하기

            // 알람 시간 설정
            nextAlarmTime.set(Calendar.HOUR_OF_DAY, hour);
            nextAlarmTime.set(Calendar.MINUTE, minute);
            nextAlarmTime.set(Calendar.SECOND, 0);

            // 알람 시간이 이미 지나면 다음 주로 설정
            if (nextAlarmTime.before(Calendar.getInstance())) {
                nextAlarmTime.add(Calendar.DAY_OF_YEAR, intervalDays);
            }

            // 알람 재설정
            Intent newIntent = new Intent(context, AlarmReceiver.class);
            Bundle extras = intent.getExtras();
            if (extras != null) {
                newIntent.putExtras(extras);  // 기존 extras를 새로운 intent에 넣음
            }
            //newIntent.putExtra("requestCode", requestCode);
            //newIntent.putExtra("alarmName", alarmName);
            //newIntent.putExtra("hour", hour);
            //newIntent.putExtra("minute", minute);
            //newIntent.putExtra("intervalDays", intervalDays);

            PendingIntent newPendingIntent = PendingIntent.getBroadcast(context, requestCode, newIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextAlarmTime.getTimeInMillis(), newPendingIntent);

            Log.d("AlarmReceiver", "주기 반복 알람 재설정: " + nextAlarmTime.getTime() + "  requestCode" + requestCode  );
        }
    }

    // 알람을 멈추는 메서드
    public static void stopAlarm() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // TTS 중지 및 해제
        if (tts != null) {
            ttsHandler.removeCallbacks(ttsRunnable); // 반복 중단
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}