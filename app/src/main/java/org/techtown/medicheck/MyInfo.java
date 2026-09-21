package org.techtown.medicheck;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

public class MyInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //설정바 검정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.BLACK); // 검정색 상태 표시줄
            window.setNavigationBarColor(Color.BLACK); // 검정색 네비게이션 바
        }

        //만성질환 추가-------------------------------------------------------------------------------
        Spinner spinner = findViewById(R.id.spinner);

        // 배열 준비
        String[] diseases = {"없음","고혈압", "당뇨병", "천식", "관절염", "심부전"};

        // ArrayAdapter 만들기
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, diseases) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.BLACK); // 선택된 글씨 색
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE); // 드롭다운 글씨 색
                return view;
            }
        };

    // 드롭다운 레이아웃 설정
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    // 스피너에 적용
        spinner.setAdapter(adapter);

        loadUserData();

    }
    //만성질환 끝-------------------------------------------------------------------------------------


    @Override
    protected void onPause() {
        super.onPause();

        saveUserData();
    }

    private void saveUserData() {
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 복용 금지 성분
        EditText editTextName = findViewById(R.id.editTextName);
        editor.putString("forbiddenIngredients", editTextName.getText().toString());

        // 만성질환 선택
        Spinner spinner = findViewById(R.id.spinner);
        editor.putInt("diseasePosition", spinner.getSelectedItemPosition());

        // 시간들
        TimePicker time1 = findViewById(R.id.timePicker);
        TimePicker time2 = findViewById(R.id.timePicker2);
        TimePicker time3 = findViewById(R.id.timePicker3);
        TimePicker time4 = findViewById(R.id.timePicker4);
        TimePicker time5 = findViewById(R.id.timePicker5);

        editor.putInt("time1_hour", time1.getHour());
        editor.putInt("time1_minute", time1.getMinute());

        editor.putInt("time2_hour", time2.getHour());
        editor.putInt("time2_minute", time2.getMinute());

        editor.putInt("time3_hour", time3.getHour());
        editor.putInt("time3_minute", time3.getMinute());

        editor.putInt("time4_hour", time4.getHour());
        editor.putInt("time4_minute", time4.getMinute());

        editor.putInt("time5_hour", time5.getHour());
        editor.putInt("time5_minute", time5.getMinute());

        editor.apply(); // 저장
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);

        // 복용 금지 성분
        EditText editTextName = findViewById(R.id.editTextName);
        editTextName.setText(prefs.getString("forbiddenIngredients", ""));

        // 만성질환 선택
        Spinner spinner = findViewById(R.id.spinner);
        int savedPosition = prefs.getInt("diseasePosition", 0);
        Log.d("MyInfo", "savedPosition = " + savedPosition);
        spinner.setSelection(savedPosition);

        // 시간 설정
        TimePicker time1 = findViewById(R.id.timePicker);
        TimePicker time2 = findViewById(R.id.timePicker2);
        TimePicker time3 = findViewById(R.id.timePicker3);
        TimePicker time4 = findViewById(R.id.timePicker4);
        TimePicker time5 = findViewById(R.id.timePicker5);

        time1.setHour(prefs.getInt("time1_hour", 7));
        time1.setMinute(prefs.getInt("time1_minute", 0));

        time2.setHour(prefs.getInt("time2_hour", 12));
        time2.setMinute(prefs.getInt("time2_minute", 0));

        time3.setHour(prefs.getInt("time3_hour", 18));
        time3.setMinute(prefs.getInt("time3_minute", 0));

        time4.setHour(prefs.getInt("time4_hour", 6));
        time4.setMinute(prefs.getInt("time4_minute", 30));

        time5.setHour(prefs.getInt("time5_hour", 23));
        time5.setMinute(prefs.getInt("time5_minute", 0));
    }
}