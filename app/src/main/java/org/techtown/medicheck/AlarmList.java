package org.techtown.medicheck;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.view.GestureDetector;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.util.Log;
import androidx.annotation.NonNull;

public class AlarmList extends AppCompatActivity {

    private AlertDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseApp.initializeApp(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alarm_list);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.BLACK);
            window.setNavigationBarColor(Color.BLACK);
        }

        LinearLayout alarmListContainer = findViewById(R.id.alarm_list_container);
        alarmListContainer.setOrientation(LinearLayout.VERTICAL);

        SharedPreferences sharedPreferences = getSharedPreferences("alarms", MODE_PRIVATE);
        Map<String, ?> allAlarms = sharedPreferences.getAll();

        SharedPreferences pref = getSharedPreferences("UserInfo", MODE_PRIVATE);  // 개인정보 화면에서 사용한 이름
        int diseasePosition = pref.getInt("diseasePosition", -1);  // 고협압이면 예: 1번

        Map<Integer, String[]> diseaseDangerMap = new HashMap<>();
        diseaseDangerMap.put(1, new String[]{"스테로이드"});
        diseaseDangerMap.put(2, new String[]{"A"});
        diseaseDangerMap.put(3, new String[]{"B"});
        diseaseDangerMap.put(4, new String[]{"C"});
        diseaseDangerMap.put(5, new String[]{"D"});

        String forbiddenIngredients = pref.getString("forbiddenIngredients","");
        String[] ingredientsArray = forbiddenIngredients.split(",");

        Log.d("AlarmList", "Loaded diseasePosition = " + diseasePosition);
        Log.d("AlarmList", "Loaded forbiddenIngredients = " + forbiddenIngredients);

        // 1. 알람 데이터를 먼저 리스트에 모으기
        List<String[]> medInList = new ArrayList<>();
        List<LinearLayout> layoutList = new ArrayList<>();

        for (Map.Entry<String, ?> entry : allAlarms.entrySet()) {
            String alarmData = (String) entry.getValue();
            String MedIn = extractValue(alarmData, "성분:");
            String[] MedInArray = MedIn.split(",");

            LinearLayout alarmItemLayout = new LinearLayout(this);
            alarmItemLayout.setOrientation(LinearLayout.HORIZONTAL);
            alarmItemLayout.setGravity(Gravity.CENTER_VERTICAL);

            TextView alarmText = new TextView(this);
            alarmText.setText(alarmData);
            alarmText.setTextSize(18);
            alarmText.setTextColor(Color.WHITE);

            // 복용금지 성분 포함 여부 판단
            boolean isDangerous_forbiden = false;
            boolean isDangerous_dease = false;
            for (String forbidden : ingredientsArray) {
                for (String med : MedInArray) {
                    if (forbidden.trim().equals(med.trim())) {
                        isDangerous_forbiden = true;
                        break;
                    }
                }
                if (isDangerous_forbiden) break;
            }

            // 2. 질환별 위험 성분 비교
            if (!isDangerous_forbiden && diseaseDangerMap.containsKey(diseasePosition)) {
                String[] diseaseForbidden = diseaseDangerMap.get(diseasePosition);
                for (String danger : diseaseForbidden) {
                    for (String med : MedInArray) {
                        if (danger.trim().equalsIgnoreCase(med.trim())) {
                            isDangerous_dease = true;
                            break;
                        }
                    }
                    if (isDangerous_dease) break;
                }
            }

            // 위험 약물이면 배경 색상 변경
            if (isDangerous_forbiden || isDangerous_dease) {
                alarmItemLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_background3));
                alarmText.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.dangerous));
                if(isDangerous_dease) showNotification_dease(diseasePosition, MedIn);
                else if(isDangerous_forbiden)  showNotification_forbiden(MedIn);
            }
            else {
                alarmItemLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_background));
                alarmText.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.teal_700));
            }

            alarmItemLayout.setPadding(20, 20, 20, 20);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(70, 40, 70, 20);

            alarmItemLayout.setLayoutParams(params);
            alarmText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));

            Switch alarmSwitch = new Switch(this);
            alarmSwitch.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            alarmItemLayout.addView(alarmText);
            alarmItemLayout.addView(alarmSwitch);
            alarmListContainer.addView(alarmItemLayout);

            // 💡 제스처 감지기 제거, 더블탭만 처리
            alarmItemLayout.setOnClickListener(v -> {
                // 더블클릭을 위한 딜레이 설정
                alarmItemLayout.postDelayed(() -> {
                    if (alarmItemLayout.isPressed()) {
                        showDeleteDialog(alarmItemLayout); // 더블탭 → 삭제 다이얼로그
                    }
                }, 200);  // 더블클릭 간격 설정
            });



            if (MedIn != null && !MedIn.trim().isEmpty()) {
                medInList.add(MedInArray);
                layoutList.add(alarmItemLayout);
            } else {
                Log.d("AlarmCheck", "빈 성분 알람 제외: " + alarmData);
            }
        }

        // 1. ProgressDialog 생성 (AlertDialog + ProgressBar 커스텀)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(30, 30, 30, 30);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        TextView message = new TextView(this);
        message.setText("약물상호작용 확인 중...");
        message.setTextSize(18);
        message.setPadding(30, 0, 0, 0);

        layout.addView(progressBar);
        layout.addView(message);

        builder.setView(layout);

        progressDialog = builder.create();

        // 2. Firebase 호출 전에 다이얼로그 보여주기
        progressDialog.show();

        FirebaseDatabase database = FirebaseDatabase.getInstance("YOUR_FIREBASE_DATABASE_URL");
        DatabaseReference ref = database.getReference("drugInteractions");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Set<String>> dangerMap = new HashMap<>();

                for (DataSnapshot drug : snapshot.getChildren()) {
                    String drugName = drug.getKey().toLowerCase().trim();
                    Set<String> comboSet = new HashSet<>();
                    for (DataSnapshot combo : drug.getChildren()) {
                        String dangerCombo = combo.getValue(String.class).toLowerCase().trim();
                        comboSet.add(dangerCombo);
                    }
                    dangerMap.put(drugName, comboSet);
                    Log.d("DangerMap", "drugName: " + drugName + ", combos: " + comboSet.toString());
                }

                // 3. 모든 알람들 간 비교
                for (int i = 0; i < medInList.size(); i++) {
                    String[] ingA = medInList.get(i);

                    for (int j = i + 1; j < medInList.size(); j++) {
                        String[] ingB = medInList.get(j);

                        boolean found = false;
                        String foundA = "", foundB = "";

                        for (String a : ingA) {
                            Set<String> dangerSet = dangerMap.get(a.trim());
                            if (dangerSet == null)
                            {Log.d("CompareCheck", "No dangerSet found for: " + a.trim());
                                continue;}

                            for (String b : ingB) {
                                Log.d("CompareCheck", "Comparing: dangerSet contains? '" + b.trim() + "' in " + dangerSet);
                                for (String dangerMed : dangerSet) {
                                    if (dangerMed.trim().equalsIgnoreCase(b.trim())) {
                                        found = true;
                                        foundA = a.trim();  // 위험 조합 성분 저장
                                        foundB = b.trim();
                                        Log.d("CompareCheck", "FOUND interaction: " + a.trim() + " <-> " + b.trim());
                                        break;
                                    }
                                }
                                if (found) break;
                            }
                            if (found) break;
                        }

                        if (found) {
                            Log.d("CompareCheck", "Marking alarms " + i + " and " + j + " as dangerous");
                            layoutList.get(i).setBackground(ContextCompat.getDrawable(AlarmList.this, R.drawable.rounded_background3));
                            layoutList.get(j).setBackground(ContextCompat.getDrawable(AlarmList.this, R.drawable.rounded_background3));

                            // 다이얼로그가 뜨기 전에 MediaPlayer 준비
                            MediaPlayer mediaPlayer = MediaPlayer.create(AlarmList.this, R.raw.warningsound);
                            mediaPlayer.setLooping(true); // 반복 재생 설정
                            mediaPlayer.start();

                            // 다이얼로그 표시
                            new AlertDialog.Builder(AlarmList.this)
                                    .setTitle("⚠️ 약물 상호작용 감지")
                                    .setMessage("다음 성분 조합이 위험합니다:\n\n" +
                                            "• " + foundA + "\n" +
                                            "• " + foundB + "\n\n" +
                                            "복용 전 반드시 전문가와 상담하세요.")
                                    .setPositiveButton("확인", (dialog, which) -> {
                                        // 다이얼로그 닫힐 때 반복 재생 중지 및 해제
                                        if (mediaPlayer != null) {
                                            mediaPlayer.stop();
                                            mediaPlayer.release();
                                        }
                                    })
                                    .setOnCancelListener(dialog -> {
                                        // 다이얼로그 바깥 터치로 닫을 경우도 처리
                                        if (mediaPlayer != null) {
                                            mediaPlayer.stop();
                                            mediaPlayer.release();
                                        }
                                    })
                                    .show();
                        }
                    }
                }
                // ✅ 다이얼로그 닫기
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.e("Firebase", "Error: ", error.toException());
            }
        });

        /*
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot drug : snapshot.getChildren()) {
                    String drugName = drug.getKey(); // ex: "warfarin"
                    for (DataSnapshot combo : drug.getChildren()) {
                        String dangerCombo = combo.getValue(String.class); // ex: "aspirin"
                        Log.d("DangerCombo", drugName + " + " + dangerCombo);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: ", error.toException());
            }
        });
         */
    }

    private void showDeleteDialog(View v) {
        new AlertDialog.Builder(this)
                .setTitle("알람 삭제")
                .setMessage("정말로 이 알람을 삭제하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> deleteAlarm(v))
                .setNegativeButton("아니오", null)
                .show();
    }

    // 알림과 경고음 처리 메서드
    private void showNotification_dease(int dease, String In) {
        String deaseName="";
        if (dease==1) {deaseName = "고혈압";}
        else if (dease==2) {deaseName = "당뇨병";}
        else if (dease==3) {deaseName = "천식";}
        else if (dease==4) {deaseName = "관절염";}
        else if (dease==5) {deaseName = "심부전";}

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setNegativeButton("닫기", null);

        if (dease == 0) {
            builder.setTitle("복용 금지성분 감지");
            builder.setMessage(In + " 복용금지");
        }
        else {
            builder.setTitle("❗ 질환 관련 주의\n\n");
            builder.setMessage(
                    "• 관련 질환: *" + deaseName + "*\n" +
                            "• 위험 성분: *" + In + "*\n\n" +
                            "이 조합은 해당 질환에 **악영향**을 줄 수 있으므로\n" +
                            "**복용을 중단하고 전문가와 상담**하세요."
            );
        }

        builder.show();

    }

    // 알림과 경고음 처리 메서드
    private void showNotification_forbiden(String In) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setNegativeButton("닫기",null);

        builder.setTitle("🚫 복용 금지 성분 감지!");
        builder.setMessage(
                        "• 감지된 성분: *" + In + "*\n\n" +
                        "이 성분은 환자에게 복용이 **금지된 성분**입니다.\n" +
                        "복용 전 반드시 전문가의 상담이 필요합니다."
        );

        builder.show();
    }


    private void deleteAlarm(View v) {
        // v는 alarmItemLayout 자체여야 함 (즉, showDeleteDialog에서 넘긴 view 그대로)
        if (!(v instanceof LinearLayout)) return;

        LinearLayout alarmItemLayout = (LinearLayout) v;
        ViewGroup parentLayout = (ViewGroup) alarmItemLayout.getParent();
        if (parentLayout != null) {
            parentLayout.removeView(alarmItemLayout);
        }

        // alarmItemLayout의 첫 번째 자식이 TextView
        TextView alarmTextView = (TextView) alarmItemLayout.getChildAt(0);
        if (alarmTextView != null) {
            String alarmInfo = alarmTextView.getText().toString();
            String alarmName = extractValue(alarmInfo, "이름:");
            String timeString = extractValue(alarmInfo, "시간:");
            String repeatDays = extractValue(alarmInfo, "반복:");

            cancelAllAlarms(alarmName, timeString, repeatDays);

            SharedPreferences sharedPreferences = getSharedPreferences("alarms", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(alarmName);
            editor.apply();
        }
    }

    private String extractValue(String source, String key) {
        int start = source.indexOf(key);
        if (start == -1) return "";
        start += key.length();
        int end = source.indexOf("\n", start);
        if (end == -1) end = source.length();
        return source.substring(start, end).trim();
    }

    private void cancelAllAlarms(String alarmName, String timeString, String repeatDays) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager == null) return;

        String[] timeArray = timeString.split(",");
        int timeCount = timeArray.length;

        // 요일 반복 알람 취소
        if (repeatDays != null && !repeatDays.trim().isEmpty()) {
            String[] days = repeatDays.trim().split("\\s+");
            for (String day : days) {
                int dayOfWeek = getDayOfWeekFromText(day); // "목" -> 5
                if (dayOfWeek == -1) continue;
                for (int i = 0; i < timeCount; i++) {
                    int requestCode = (alarmName.toString() + dayOfWeek + i).hashCode();

                    Intent intent = new Intent(this, AlarmReceiver.class);

                    // 알람을 설정할 때 사용한 Intent와 동일한 Intent를 사용
                    //intent.putExtra("dayOfWeek", dayOfWeek);
                    //.putExtra("alarmName", alarmName);

                    // 알람 시간 정보 추가
                    //String[] timeArrayForCode = timeString.split(",");
                    //String timeStringForCode = timeArrayForCode[i];
                    //String[] timeParts = timeStringForCode.split(":");
                    //int pickerHour = Integer.parseInt(timeParts[0]);
                    //int pickerMinute = Integer.parseInt(timeParts[1]);

                    //intent.putExtra("hour", pickerHour);
                    //intent.putExtra("minute", pickerMinute);
                    intent.putExtra("requestCode", requestCode);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    alarmManager.cancel(pendingIntent);
                    Log.d("AlarmSetting", "요일 알람 취소됨: "+ (alarmName.toString() + dayOfWeek + i) + "요청코드:" + requestCode);
                }
            }
        }

        // 주기 반복 알람 취소
        for (int i = 0; i < timeCount; i++) {
            int requestCode = (alarmName.toString() + i).hashCode();

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("requestCode", requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);  // 정확히 매칭된 PendingIntent 취소
            Log.d("AlarmSetting", "주기 알람 취소됨: " + (alarmName.toString() + i) + "요청코드:"+ requestCode);  // 로그 추가
        }
    }

    private int getDayOfWeekFromText(String day) {
        switch (day) {
            case "일": return Calendar.SUNDAY;
            case "월": return Calendar.MONDAY;
            case "화": return Calendar.TUESDAY;
            case "수": return Calendar.WEDNESDAY;
            case "목": return Calendar.THURSDAY;
            case "금": return Calendar.FRIDAY;
            case "토": return Calendar.SATURDAY;
            default: return -1;
        }
    }
}
