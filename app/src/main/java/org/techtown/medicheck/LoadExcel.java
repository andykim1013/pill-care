package org.techtown.medicheck;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.InputStream;
import java.util.Calendar;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class LoadExcel extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_excel); // 연결된 레이아웃

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.BLACK); // 검정색 상태 표시줄
            window.setNavigationBarColor(Color.BLACK); // 검정색 네비게이션 바
        }

        imageView = findViewById(R.id.imageView);
        Button btnNext = findViewById(R.id.btn_next);
        Button btnWeb = findViewById(R.id.btn_web);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(LoadExcel.this, "엑셀 파일을 불러옵니다.", Toast.LENGTH_LONG).show();

                if (btnNext.getText().toString().equals("처방데이터 불러오기")) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {
                            "application/vnd.ms-excel", // .xls
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xlsx
                    });
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(Intent.createChooser(intent, "엑셀 파일 선택"), 100);

                }

            }
        });

        btnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://ptl.hira.or.kr/mainCert.do?pageType=certByJ&domain=https://www.hira.or.kr&uri=JTJGcmIlMkZjbW1uJTJGcmJDZXJ0UmV0dXJuLmRvJTNGc3RyUGFnZVR5cGUlM0REVVI=";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                // 👉 여기!!
                Intent intent = new Intent(this, LoadExcelActivity.class);   // ✅ 원하는 액티비티로
                intent.putExtra("fileUri", selectedFileUri.toString());
                startActivity(intent);

                // ❌ 이건 더 이상 필요 없음
                // readExcelFile(selectedFileUri);
            }
        }
    }

    private void readExcelFile(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            // Apache POI 사용해서 엑셀 파일 읽기 (HSSFWorkbook or XSSFWorkbook)
            // 예시: HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            // 데이터 추출해서 화면에 표시 등
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "엑셀 파일을 읽을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "권한 허용됨. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "파일을 읽으려면 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}