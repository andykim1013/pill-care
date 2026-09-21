package org.techtown.medicheck;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> filePickerLauncher;
    //private TextView resultTextView;
    CheckBox chbox1, chbox2;
    ProgressBar progressBar2;
    CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.BLACK); // 검정색 상태 표시줄
            window.setNavigationBarColor(Color.BLACK); // 검정색 네비게이션 바
        }

        // 권한 요청 (Android 13 이상에서 POST_NOTIFICATIONS 권한 요청)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        //체크박스 테스트부분-------------------------------------------------------------------------
        // CheckBox 및 ProgressBar 참조 연결
        chbox1 = findViewById(R.id.chbox1);
        chbox2 = findViewById(R.id.chbox2);
        calendarView = findViewById(R.id.calendarView);

        progressBar2 = findViewById(R.id.progressBar2);

        // ProgressBar 최대값 설정
        progressBar2.setMax(100);

        // 체크박스 상태 변화 리스너 등록
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> updateProgress();

        chbox1.setOnCheckedChangeListener(listener);
        chbox2.setOnCheckedChangeListener(listener);


        // 초기값 세팅
        updateProgress();
        //------------------------------------------------------------------------------------------


        // 버튼 참조
        ImageButton button5 = findViewById(R.id.button5);
        ImageButton button_addAlarm = findViewById(R.id.button7);
        ImageButton button_Myinfo = findViewById(R.id.button2);
        ImageButton button_AlarmList = findViewById(R.id.button8);

        //resultTextView = findViewById(R.id.textView8);  // 레이아웃에 이 TextView 있어야 함

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "alarm_channel",
                    "Alarm Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // 알림 꺼짐 수신 처리
        IntentFilter filter = new IntentFilter("STOP_ALARM");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AlarmReceiver.stopAlarm();
            }
        }, filter, Context.RECEIVER_NOT_EXPORTED);

        button_AlarmList.setOnClickListener(v-> {
            Intent intent = new Intent(MainActivity.this, AlarmList.class);
            startActivity(intent);
        });

        button_addAlarm.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AlarmSetting.class);
            startActivity(intent);
        });

        // 기타 버튼들 (엑셀 파일 로드)
        button5.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoadExcel.class);
            startActivity(intent);
        });

        button_Myinfo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyInfo.class);
            startActivity(intent);
        });

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            loadAlarmsForDate(selected.getTime());
        });

        Calendar selected = Calendar.getInstance();
        calendarView.post(()-> loadAlarmsForDate(selected.getTime()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 오늘 날짜로 설정
        Calendar todayforset = Calendar.getInstance();
        calendarView.setDate(todayforset.getTimeInMillis(), false, true);

        SharedPreferences prefs = getSharedPreferences("CheckStates", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        SharedPreferences sharedPreferences = getSharedPreferences("alarms", MODE_PRIVATE);
        Map<String, ?> allAlarms = sharedPreferences.getAll();

        TextView textView2 = findViewById(R.id.textView2);
        LinearLayout alarmContainer = findViewById(R.id.alarm_today_layout);
        alarmContainer.removeAllViews(); // 기존에 있던 뷰들 초기화

        String[] weekKorean = {"", "일", "월", "화", "수", "목", "금", "토"}; // Calendar.DAY_OF_WEEK는 1~7
        int todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        String today = weekKorean[todayIndex];
        List<CheckBox> checkBoxList = new ArrayList<>();

        int checkedCount = 0;
        int alarmCount = 0;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());  // 알람 시간 포맷
        Calendar todayCalendar = Calendar.getInstance();


        // 체크할 때 저장
        for (CheckBox cb : checkBoxList) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // 시간 비교를 위해 알람 시간을 가져옴
                String timeText = ((TextView)((View) cb.getParent()).findViewById(R.id.alarm_time)).getText().toString();
                SimpleDateFormat timeFormat1 = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Calendar now = Calendar.getInstance();

                String key = cb.getText().toString() + "-" + ((TextView)((View) cb.getParent()).findViewById(R.id.alarm_time)).getText().toString();
                editor.putBoolean(key, isChecked);
                editor.apply();

                int newCheckedCount = 0;
                for (CheckBox c : checkBoxList) {
                    if (c.isChecked()) newCheckedCount++;
                }
                progressBar2.setProgress(newCheckedCount);
            });
        }

        // 오늘 날짜 기준으로 알람 로드
        loadAlarmsForDate(new Date());

    }

    // 권한 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용됨
                Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 권한 거부됨
                Toast.makeText(this, "알림 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProgress() {
        int total = 3; // 총 체크박스 개수
        int checkedCount = 0;

        if (chbox1.isChecked()) checkedCount++;
        if (chbox2.isChecked()) checkedCount++;

        // 비율 계산
        int progress = (int) ((checkedCount / (float) total) * 100);
        progressBar2.setProgress(progress);
    }

    // 알람 객체
    private static class Alarm {
        private String name;
        private List<String> timeList;

        public Alarm(String name, List<String> timeList) {
            this.name = name;
            this.timeList = timeList;
        }

        public String getName() {
            return name;
        }

        public List<String> getTimeList() {
            return timeList;
        }
    }


    private void loadAlarmsForDate(Date selectedDate) {
        SharedPreferences prefs = getSharedPreferences("CheckStates", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        SharedPreferences sharedPreferences = getSharedPreferences("alarms", MODE_PRIVATE);
        Map<String, ?> allAlarms = sharedPreferences.getAll();

        TextView textView2 = findViewById(R.id.textView2);
        LinearLayout alarmContainer = findViewById(R.id.alarm_today_layout);
        alarmContainer.removeAllViews();

        String[] weekKorean = {"", "일", "월", "화", "수", "목", "금", "토"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String selectedDay = weekKorean[dayOfWeek];

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        List<Alarm> alarmList = new ArrayList<>();
        int checkedCount = 0;
        int alarmCount = 0;
        List<CheckBox> checkBoxList = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allAlarms.entrySet()) {
            String alarmInfo = entry.getValue().toString();
            String[] lines = alarmInfo.split("\n");

            String alarmName = "", alarmTime = "", startDateStr = "", repeatLine = "";

            for (String line : lines) {
                if (line.startsWith("이름:")) alarmName = line.replace("이름:", "").trim();
                else if (line.startsWith("시간:")) alarmTime = line.replace("시간:", "").trim();
                else if (line.startsWith("반복:")) repeatLine = line.replace("반복:", "").trim();
                else if (line.startsWith("시작일:")) startDateStr = line.replace("시작일:", "").trim();
            }

            boolean shouldAdd = false;

            try {
                if (!startDateStr.isEmpty()) {
                    Date startDate = dateFormat.parse(startDateStr);
                    long diffInMillis = selectedDate.getTime() - startDate.getTime();
                    int daysBetween = (int) (diffInMillis / (1000 * 60 * 60 * 24));

                    if (repeatLine.contains("일마다")) {
                        int interval = Integer.parseInt(repeatLine.replace("일마다", "").trim());
                        if (daysBetween % interval == 0 && daysBetween >= 0) {
                            shouldAdd = true;
                        }
                    }
                } else {
                    if (repeatLine.contains(selectedDay)) {
                        shouldAdd = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (shouldAdd) {
                List<String> timeList = new ArrayList<>();
                for (String time : alarmTime.split(",")) timeList.add(time.trim());
                alarmList.add(new Alarm(alarmName, timeList));
            }
        }

        // 정렬 및 출력
        List<Pair<String, String>> flatAlarmList = new ArrayList<>();
        for (Alarm alarm : alarmList) {
            for (String time : alarm.getTimeList()) {
                flatAlarmList.add(new Pair<>(alarm.getName(), time));
            }
        }

        Collections.sort(flatAlarmList, Comparator.comparing(pair -> pair.second));

        int index = 0;
        for (Pair<String, String> pair : flatAlarmList) {
            String name = pair.first;
            String time = pair.second;

            LinearLayout newAlarmView = (LinearLayout) getLayoutInflater().inflate(R.layout.single_alarm_layout, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 16, 0, 16);
            newAlarmView.setLayoutParams(params);

            if (index == 0) {
                newAlarmView.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_background));
            }

            CheckBox checkBox = newAlarmView.findViewById(R.id.chbox1);
            checkBox.setText(name);
            checkBox.setChecked(prefs.getBoolean(name + "-" + time, false));
            checkBoxList.add(checkBox);

            TextView timeText = newAlarmView.findViewById(R.id.alarm_time);
            timeText.setText(time);

            Switch alarmSwitch = newAlarmView.findViewById(R.id.alarm_switch);
            alarmSwitch.setChecked(prefs.getBoolean("switch_" + name + "_" + time, true));
            alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("switch_" + name + "_" + time, isChecked).apply();
            });

            if (checkBox.isChecked()) checkedCount++;
            alarmContainer.addView(newAlarmView);
            alarmCount++;
            index++;
        }

        textView2.setText(dateFormat.format(selectedDate) + " 복용 알람: " + alarmCount + "개");
        progressBar2.setMax(alarmCount);
        progressBar2.setProgress(checkedCount);

        for (CheckBox cb : checkBoxList) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String key = cb.getText().toString() + "-" +
                        ((TextView) ((View) cb.getParent()).findViewById(R.id.alarm_time)).getText().toString();
                editor.putBoolean(key, isChecked).apply();

                int newCheckedCount = 0;
                for (CheckBox c : checkBoxList) {
                    if (c.isChecked()) newCheckedCount++;
                }
                progressBar2.setProgress(newCheckedCount);
            });
        }
    }
}