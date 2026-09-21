package org.techtown.medicheck;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoadExcelActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private android.app.AlertDialog progressDialog;

    private final List<Set<String>> prohibitedPairs = new ArrayList<>();
    private List<MedicineItem> filteredList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_excel_dummy);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.BLACK); // 검정색 상태 표시줄
            window.setNavigationBarColor(Color.BLACK); // 검정색 네비게이션 바
        }

        recyclerView = findViewById(R.id.recyclerView);

        prohibitedPairs.add(new HashSet<>(Arrays.asList("tiropramide hydrochloride", "bacillus subtilis")));
        prohibitedPairs.add(new HashSet<>(Arrays.asList("C", "D")));

        Intent intent = getIntent();
        String fileUriString = intent.getStringExtra("fileUri");
        if (fileUriString != null) {
            Uri fileUri = Uri.parse(fileUriString);
            readExcelFile(fileUri);
        } else {
            Toast.makeText(this, "파일이 전달되지 않았습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        findViewById(R.id.btnLoadSelected).setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    Intent intentforper = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intentforper);
                    return;
                }
            }

            // ✅ 현재 recyclerView의 adapter 가져오기
            MedicineAdapter adapter = (MedicineAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.saveAllToSharedPreferences(this);  // ✅ MedicineAdapter 안에 있는 저장 메서드 호출
            }

            // ✅ 기존처럼 MainActivity로 이동
            Intent mainIntent = new Intent(LoadExcelActivity.this, AlarmList.class);
            startActivity(mainIntent);
            finish();  // LoadExcelActivity 종료
        });

    }

    private void readExcelFile(Uri uri) {
        try {
            String fileName = getFileName(uri);
            File copiedFile = copyUriToFile(uri, fileName);
            InputStream inputStream = new FileInputStream(copiedFile);

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            Set<String> dateSet = new LinkedHashSet<>();
            List<Row> allRows = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Cell cellB = row.getCell(1);
                Cell cellC = row.getCell(2);

                if (cellB == null || cellC == null) continue;

                String date = cellB.toString().trim();
                String hospital = cellC.toString().trim();

                if (!date.isEmpty() && !hospital.isEmpty()) {
                    dateSet.add(date + " - " + hospital);
                    allRows.add(row);
                }
            }

            workbook.close();
            inputStream.close();

            showDateSelectionDialog(new ArrayList<>(dateSet), allRows);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "엑셀 파일 읽기 오류: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDateSelectionDialog(List<String> dateSet, List<Row> allRows) {
        String[] items = dateSet.toArray(new String[0]);
        boolean[] checkedItems = new boolean[items.length];
        List<String> selectedItems = new ArrayList<>();
        List<String> selectedItemsCopy = new ArrayList<>(selectedItems);

        new AlertDialog.Builder(this)
                .setTitle("조제일자 및 처방기관을 선택하세요")
                .setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedItems.add(items[which]);
                    } else {
                        selectedItems.remove(items[which]);
                    }
                })
                .setPositiveButton("확인", (dialog, which) -> {
                    List<MedicineItem> filteredList = new ArrayList<>();
                    List<String> conflictMessages = new ArrayList<>();
                    List<Set<String>> conflictPairs = new ArrayList<>();


                    for (Row row : allRows) {
                        String date = row.getCell(1).toString().trim();
                        String hospital = row.getCell(2).toString().trim();
                        String combo = date + " - " + hospital;

                        if (!selectedItems.contains(combo)) continue;

                        Cell cellE = row.getCell(4);  // 약 이름
                        Cell cellG = row.getCell(6);  // 성분
                        Cell cellK = row.getCell(10); // 권장 시간

                        if (cellE == null || cellK == null || cellG == null) continue;

                        String name = cellE.toString().trim();
                        String ingredient = cellG.toString().trim();
                        String recommendCode = cellK.toString().trim();

                        boolean morning = false, lunch = false, dinner = false,wakeup=false, beforebed=false;
                        String recommendText = "";

                        if (recommendCode.equals("2")) {
                            morning = true; dinner = true;
                            recommendText = "아침, 저녁";
                        } else if (recommendCode.equals("3")) {
                            morning = true; lunch = true; dinner = true;
                            recommendText = "아침, 점심, 저녁";
                        } else if (recommendCode.equals("1")) {
                            morning = true;
                            recommendText = "아침";
                        } else if (recommendCode.equals("5")) {
                            lunch = true;
                            recommendText = "점심";
                        } else if (recommendCode.equals("6")) {
                            dinner = true;
                            recommendText = "저녁";
                        }

                        filteredList.add(new MedicineItem(name, morning, lunch, dinner, false,false,recommendText, ingredient));
                    }


                    if (filteredList.isEmpty()) {
                        Toast.makeText(this, "선택한 항목에 해당하는 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    recyclerView.setAdapter(new MedicineAdapter(filteredList, prohibitedPairs));
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                })
                .show();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private File copyUriToFile(Uri uri, String fileName) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File file = new File(getCacheDir(), fileName);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}