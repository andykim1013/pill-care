package org.techtown.medicheck;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.Manifest;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import android.content.BroadcastReceiver;  // 알람 수신을 위한 BroadcastReceiver
import android.content.IntentFilter;        // 알람 수신을 위한 IntentFilter
import android.app.Notification;           // 알람 Notification을 위한 클래스
import android.app.PendingIntent;          // 알람 Notification에서 사용할 PendingIntent

public class AlarmSetting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setting);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.BLACK);
            window.setNavigationBarColor(Color.BLACK);
        }

        // 뷰 참조
        EditText alarmNameEditText = findViewById(R.id.editTextAlarmName);
        EditText MedInEditText = findViewById(R.id.editTextMedIn);
        RadioGroup repeatGroup = findViewById(R.id.radioGroupRepeatType);
        RadioButton radioWeekly = findViewById(R.id.radioWeekly);
        RadioButton radioInterval = findViewById(R.id.radioInterval);

        LinearLayout weeklyLayout = findViewById(R.id.layoutWeekly);
        LinearLayout intervalLayout = findViewById(R.id.layoutInterval);
        EditText intervalEditText = findViewById(R.id.editTextIntervalDays);
        Button saveButton = findViewById(R.id.buttonSave);

        MaterialButtonToggleGroup toggleGroupDays = findViewById(R.id.toggleGroupDays);
        TimePicker timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(Boolean.FALSE);

        weeklyLayout.setVisibility(View.VISIBLE);
        intervalLayout.setVisibility(View.GONE);

        LinearLayout timePickerContainer = findViewById(R.id.timePickerContainer);
        Button buttonAddTime = findViewById(R.id.buttonAddTime);

        TimePicker timePicker2 = findViewById(R.id.timePicker2);
        TimePicker timePicker3 = findViewById(R.id.timePicker3);

        buttonAddTime.setOnClickListener(v -> {
            if (timePicker2.getVisibility() == View.GONE) {
                timePicker2.setVisibility(View.VISIBLE);
            } else if (timePicker3.getVisibility() == View.GONE) {
                timePicker3.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "더 이상 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        repeatGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioWeekly) {
                weeklyLayout.setVisibility(View.VISIBLE);
                intervalLayout.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioInterval) {
                weeklyLayout.setVisibility(View.GONE);
                intervalLayout.setVisibility(View.VISIBLE);
            }
        });



        // 알람 설정 버튼 클릭 시
        saveButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                    return;
                }
            }

            // 시간 가져오기
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            // 현재 시간이 이미 지나간 경우, 다음날로 설정
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1);
            }

            // 알람 설정을 위한 PendingIntent
            Intent intent = new Intent(AlarmSetting.this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(AlarmSetting.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // 요일 선택된 경우 반복 알람 설정-----------------------------------------------------------
            if (radioWeekly.isChecked()) {
                StringBuilder selectedDays = new StringBuilder();
                List<Integer> checkedIds = toggleGroupDays.getCheckedButtonIds();
                List<TimePicker> allPickers = new ArrayList<>();
                allPickers.add(timePicker);
                if (timePicker2.getVisibility() == View.VISIBLE) allPickers.add(timePicker2);
                if (timePicker3.getVisibility() == View.VISIBLE) allPickers.add(timePicker3);


                for (int i = 0; i < toggleGroupDays.getChildCount(); i++) {
                    View child = toggleGroupDays.getChildAt(i);
                    if (child instanceof MaterialButton) {
                        MaterialButton button = (MaterialButton) child;
                        if (checkedIds.contains(button.getId())) {
                            selectedDays.append(button.getText().toString()).append(" ");
                            int dayOfWeek = getDayOfWeekFromButton(button);

                            for (int iPicker = 0; iPicker < allPickers.size(); iPicker++) {
                                TimePicker picker = allPickers.get(iPicker);
                                int pickerHour = picker.getHour();
                                int pickerMinute = picker.getMinute();

                                Calendar day_calendar = Calendar.getInstance();
                                day_calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                                day_calendar.set(Calendar.HOUR_OF_DAY, pickerHour);
                                day_calendar.set(Calendar.MINUTE, pickerMinute);
                                day_calendar.set(Calendar.SECOND, 0);

                                // 현재 시간보다 이전이면 다음 주로 설정
                                if (day_calendar.before(Calendar.getInstance())) {
                                    day_calendar.add(Calendar.WEEK_OF_YEAR, 1);
                                }

                                int requestCode = (alarmNameEditText.getText().toString() + dayOfWeek + iPicker).hashCode();
                                Intent day_intent = new Intent(AlarmSetting.this, AlarmReceiver.class);

                                StringBuilder timeBuilder1 = new StringBuilder();
                                for (TimePicker picker1 : allPickers) {
                                    int h = picker1.getHour();
                                    int m = picker1.getMinute();
                                    timeBuilder1.append(String.format("%02d:%02d", h, m)).append(", ");
                                }
                                String times1 = timeBuilder1.toString().replaceAll(", $", ""); // 마지막 쉼표 제거

                                day_intent.putExtra("dayOfWeek", dayOfWeek);
                                day_intent.putExtra("hour", pickerHour);
                                day_intent.putExtra("minute", pickerMinute);
                                day_intent.putExtra("requestCode", requestCode);
                                day_intent.putExtra("alarmName", alarmNameEditText.getText().toString());
                                day_intent.putExtra("MedIn", MedInEditText.getText().toString());
                                //day_intent.putExtra("alarmTime", times1);

                                PendingIntent day_pendingIntent = PendingIntent.getBroadcast(AlarmSetting.this, requestCode, day_intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, day_calendar.getTimeInMillis(), day_pendingIntent);
                                Log.d("AlarmSetting", "알람 설정됨: requestCode=" + requestCode);
                            }
                        }
                    }
                }

                if (selectedDays.length() == 0) {
                    Toast.makeText(this, "요일을 선택하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String alarmName = alarmNameEditText.getText().toString();
                String MedIn = MedInEditText.getText().toString();

                StringBuilder timeBuilder = new StringBuilder();
                for (TimePicker picker : allPickers) {
                    int h = picker.getHour();
                    int m = picker.getMinute();
                    timeBuilder.append(String.format("%02d:%02d", h, m)).append(", ");
                }
                String times = timeBuilder.toString().replaceAll(", $", ""); // 마지막 쉼표 제거

                String repeatSetting = selectedDays.toString().trim();
                if (repeatSetting.isEmpty()) {
                    repeatSetting = "선택 없음";
                }

                String fullAlarmInfo = "이름: " + alarmName + "\n"
                        + "성분: " + MedIn + "\n"
                        + "시간: " + times + "\n"
                        + "반복: " + repeatSetting;


                SharedPreferences sharedPreferences = getSharedPreferences("alarms", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(alarmName, fullAlarmInfo);
                editor.apply();

                Toast.makeText(AlarmSetting.this, fullAlarmInfo, Toast.LENGTH_LONG).show();

                Log.d("AlarmSetting", "저장된 알람 정보\n" + fullAlarmInfo);

                Intent intent90 = new Intent(AlarmSetting.this, AlarmList.class);
                startActivity(intent90);
                finish();
            }

            // 주기 반복 설정---------------------------------------------------------------------
            else {

                String intervalStr = intervalEditText.getText().toString();
                int intervalDays;

                try {
                    intervalDays = Integer.parseInt(intervalStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(AlarmSetting.this, "올바른 숫자를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                long repeatIntervalMillis = AlarmManager.INTERVAL_DAY * intervalDays;

                // TimePicker들 리스트에 추가
                List<TimePicker> allPickers = new ArrayList<>();
                allPickers.add(timePicker);
                if (timePicker2.getVisibility() == View.VISIBLE) allPickers.add(timePicker2);
                if (timePicker3.getVisibility() == View.VISIBLE) allPickers.add(timePicker3);

                // 각 TimePicker에 대해 알람 설정
                for (int i = 0; i < allPickers.size(); i++) {
                    TimePicker picker = allPickers.get(i);
                    int pickerHour = picker.getHour();
                    int pickerMinute = picker.getMinute();

                    // 알람 시간 설정
                    Calendar week_calendar = Calendar.getInstance();
                    week_calendar.set(Calendar.HOUR_OF_DAY, pickerHour);
                    week_calendar.set(Calendar.MINUTE, pickerMinute);
                    week_calendar.set(Calendar.SECOND, 0);

                    // 현재 시간이 지나면 내일로 설정
                    if (week_calendar.before(Calendar.getInstance())) {
                        week_calendar.add(Calendar.DATE, 1);
                    }

                    String alarmName1 = alarmNameEditText.getText().toString();

                    // 알람 ID (다수일 경우 고유하게 설정)
                    int requestCode = (alarmNameEditText.getText().toString() + i).hashCode();
                    Intent intervalIntent = new Intent(AlarmSetting.this, AlarmReceiver.class);
                    intervalIntent.putExtra("requestCode", requestCode); // 알람 이름 추가
                    intervalIntent.putExtra("alarmName", alarmName1); // 알람 이름 추가
                    intervalIntent.putExtra("hour", pickerHour);
                    intervalIntent.putExtra("minute", pickerMinute);
                    intervalIntent.putExtra("intervalDays", intervalDays);


                    PendingIntent week_pendingIntent = PendingIntent.getBroadcast(
                            AlarmSetting.this,
                            requestCode,
                            intervalIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    // 알람 설정 (setExactAndAllowWhileIdle 사용)
                    try {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, week_calendar.getTimeInMillis(), week_pendingIntent);
                        Log.d("AlarmSetting", "알람 설정됨: requestCode=" + requestCode);
                    } catch (SecurityException e) {
                        Toast.makeText(AlarmSetting.this, "알람 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                // 알람 설정 내용 요약
                String alarmName = alarmNameEditText.getText().toString();
                String MedIn = MedInEditText.getText().toString();
                LocalDate today = LocalDate.now();
                String startDate = today.toString();  // "2025-05-10" 같은 형식

                StringBuilder timeBuilder = new StringBuilder();
                for (TimePicker picker : allPickers) {
                    int h = picker.getHour();
                    int m = picker.getMinute();
                    timeBuilder.append(String.format("%02d:%02d", h, m)).append(", ");
                }
                String times = timeBuilder.toString().replaceAll(", $", "");

                String repeatSetting = intervalDays + "일마다";

                String fullAlarmInfo = "이름: " + alarmName + "\n"
                        + "성분: " + MedIn + "\n"
                        + "시간: " + times + "\n"
                        + "반복: " + repeatSetting + "\n"
                        + "시작일: " + startDate;

                SharedPreferences sharedPreferences = getSharedPreferences("alarms", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(alarmName, fullAlarmInfo);
                editor.apply();

                Toast.makeText(AlarmSetting.this, fullAlarmInfo, Toast.LENGTH_LONG).show();

                Log.d("AlarmSetting", "저장된 알람 정보\n" + fullAlarmInfo);

                Intent intent90 = new Intent(AlarmSetting.this, AlarmList.class);
                startActivity(intent90);
                finish();
            }
        });
    }

    // 요일을 얻는 메서드
    private int getDayOfWeekFromButton(MaterialButton button) {
        switch (button.getText().toString()) {
            case "월":
                return Calendar.MONDAY;
            case "화":
                return Calendar.TUESDAY;
            case "수":
                return Calendar.WEDNESDAY;
            case "목":
                return Calendar.THURSDAY;
            case "금":
                return Calendar.FRIDAY;
            case "토":
                return Calendar.SATURDAY;
            case "일":
                return Calendar.SUNDAY;
            default:
                return Calendar.MONDAY;
        }
    }
}
