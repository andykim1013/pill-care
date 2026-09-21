package org.techtown.medicheck;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    private final List<MedicineItem> medicineList;
    private final List<Set<String>> prohibitedPairs;
    private final List<ViewHolder> viewHolders = new ArrayList<>();

    public MedicineAdapter(List<MedicineItem> medicineList, List<Set<String>> prohibitedPairs) {
        this.medicineList = medicineList;
        this.prohibitedPairs = prohibitedPairs;
    }

    private int getDayOfWeek(String day) {
        switch (day) {
            case "일": return Calendar.SUNDAY;
            case "월": return Calendar.MONDAY;
            case "화": return Calendar.TUESDAY;
            case "수": return Calendar.WEDNESDAY;
            case "목": return Calendar.THURSDAY;
            case "금": return Calendar.FRIDAY;
            case "토": return Calendar.SATURDAY;
            default: return Calendar.MONDAY;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineItem item = medicineList.get(position);

        String name = formatMedicineName(item.getName());
        holder.tvMedicineName.setText(name);

        String ingredient = formatIngredient(item.getIngredient());
        holder.tvIngredient.setText(ingredient);

        holder.cbM.setChecked(item.isMorning());
        holder.cbL.setChecked(item.isLunch());
        holder.cbD.setChecked(item.isDinner());
        holder.cbWakeup.setChecked(item.isWakeUp());
        holder.cbBeforeBed.setChecked(item.isBeforeBed());

        holder.itemView.setBackgroundColor(item.isConflict() ? Color.parseColor("#FFCDD2") : Color.TRANSPARENT);

        holder.radioGroupRepeat.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioByDay) {
                holder.dayButtonsLayout.setVisibility(View.VISIBLE);
                holder.cycleInputLayout.setVisibility(View.GONE);
                item.setCycleMode(false);
                item.setCycleDays("");
                updateRepeatDays(holder, item);
            } else if (checkedId == R.id.radioByCycle) {
                holder.dayButtonsLayout.setVisibility(View.GONE);
                holder.cycleInputLayout.setVisibility(View.VISIBLE);
                item.setCycleMode(true);
                item.setRepeatDays(new ArrayList<>());
                item.setCycleDays(holder.etCycleDays.getText().toString());
            }
        });

        holder.etCycleDays.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (holder.radioGroupRepeat.getCheckedRadioButtonId() == R.id.radioByCycle) {
                    item.setCycleDays(s.toString());
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        View.OnClickListener dayClickListener = v -> updateRepeatDays(holder, item);
        holder.btnMon.setOnClickListener(dayClickListener);
        holder.btnTue.setOnClickListener(dayClickListener);
        holder.btnWed.setOnClickListener(dayClickListener);
        holder.btnThu.setOnClickListener(dayClickListener);
        holder.btnFri.setOnClickListener(dayClickListener);
        holder.btnSat.setOnClickListener(dayClickListener);
        holder.btnSun.setOnClickListener(dayClickListener);

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, AlarmSetting.class);
            intent.putExtra("medicineList", new ArrayList<>(medicineList));
            context.startActivity(intent);
        });

        holder.cbM.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setMorning(isChecked);
        });

        holder.cbL.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setLunch(isChecked);
        });

        holder.cbD.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setDinner(isChecked);
        });

        holder.cbWakeup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setWakeUp(isChecked);
        });

        holder.cbBeforeBed.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setBeforeBed(isChecked);
        });

        holder.med_del_Layout.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;
            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < 500) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("약 삭제")
                            .setMessage("이 약을 삭제하시겠습니까?")
                            .setPositiveButton("네", (dialog, which) -> {
                                int position = holder.getAdapterPosition();
                                if (position == RecyclerView.NO_POSITION) return;
                                medicineList.remove(position);
                                notifyItemRemoved(position);
                                updateConflicts();
                            })
                            .setNegativeButton("취소", null)
                            .show();
                }
                lastClickTime = clickTime;
            }
        });

        if (!viewHolders.contains(holder)) {
            viewHolders.add(holder);
        }
        updateRepeatDays(holder, item);
    }

    private void updateConflicts() {
        for (MedicineItem item : medicineList) item.setConflict(false);
        for (int i = 0; i < medicineList.size(); i++) {
            MedicineItem a = medicineList.get(i);
            for (int j = i + 1; j < medicineList.size(); j++) {
                MedicineItem b = medicineList.get(j);
                for (Set<String> pair : prohibitedPairs) {
                    if (pair.contains(a.getIngredient()) && pair.contains(b.getIngredient()) && !a.getIngredient().equals(b.getIngredient())) {
                        a.setConflict(true);
                        b.setConflict(true);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    private String formatMedicineName(String name) {
        if (name.contains("정")) {
            int idx = name.indexOf("정");
            return name.substring(0, idx + 1);
        }
        return name;
    }

    private String formatIngredient(String raw) {
        raw = raw.replaceAll("\\([^)]*\\)", "")     // 괄호 제거
                .replaceAll("\\d+%\\s*", "")       // 숫자% 제거
                .replaceAll("\\bext\\b", "")       // 'ext' 제거
                .trim()
                .replaceAll("\\s+", " ");          // 다중 공백 → 단일 공백

        String[] parts = raw.split(" ");
        List<String> filtered = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) filtered.add(part);
        }

        String joined = String.join(", ", filtered);

        joined = joined.trim().replaceAll("[.,\\s]+$", "");

        return joined;
    }

    private void updateRepeatDays(ViewHolder holder, MedicineItem item) {
        List<String> days = new ArrayList<>();
        if (holder.btnMon.isChecked()) days.add("월");
        if (holder.btnTue.isChecked()) days.add("화");
        if (holder.btnWed.isChecked()) days.add("수");
        if (holder.btnThu.isChecked()) days.add("목");
        if (holder.btnFri.isChecked()) days.add("금");
        if (holder.btnSat.isChecked()) days.add("토");
        if (holder.btnSun.isChecked()) days.add("일");
        item.setRepeatDays(days);
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public boolean hasAnyUncheckedDays(Context context) {
        for (int i = 0; i < medicineList.size(); i++) {
            ViewHolder holder = viewHolders.get(i);
            MedicineItem item = medicineList.get(i);

            // 🔹 [1] 요일 반복인데 요일이 모두 해제된 경우
            if (!item.isCycleMode() && isAllDaysUnchecked(holder)) {
                new AlertDialog.Builder(context)
                        .setTitle("요일 선택 없음")
                        .setMessage(item.getName() + "\"의 요일을 최소 1개 이상 선택해주세요.")
                        .setPositiveButton("확인", null)
                        .show();
                return true; // 중단
            }

            // 🔹 [2] 주기 반복인데 입력값이 비어있는 경우
            if (item.isCycleMode()) {
                String cycleInput = item.getCycleDays();
                if (cycleInput == null || cycleInput.trim().isEmpty()) {
                    new AlertDialog.Builder(context)
                            .setTitle("주기 미입력")
                            .setMessage( item.getName() + "\"의 '주기 반복' 값을 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .show();
                    return true; // 중단
                }
            }
        }
        return false; // 모두 정상
    }

    @SuppressLint("ScheduleExactAlarm")
    public void saveAllToSharedPreferences(Context context) {
        SharedPreferences userPrefs = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences = context.getSharedPreferences("alarms", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int[] hours = { userPrefs.getInt("time1_hour", 7), userPrefs.getInt("time2_hour", 12), userPrefs.getInt("time3_hour", 18) };
        int[] minutes = { userPrefs.getInt("time1_minute", 0), userPrefs.getInt("time2_minute", 0), userPrefs.getInt("time3_minute", 0) };

        int hourMorning = userPrefs.getInt("time1_hour", 7);
        int minuteMorning = userPrefs.getInt("time1_minute", 0);

        int hourLunch = userPrefs.getInt("time2_hour", 12);
        int minuteLunch = userPrefs.getInt("time2_minute", 0);

        int hourDinner = userPrefs.getInt("time3_hour", 18);
        int minuteDinner = userPrefs.getInt("time3_minute", 0);

        int hourWakeup = userPrefs.getInt("time4_hour", 6);
        int minuteWakeup = userPrefs.getInt("time4_minute", 30);

        int hourBeforeBed = userPrefs.getInt("time5_hour", 23);
        int minuteBeforeBed = userPrefs.getInt("time5_minute", 0);

        for (int i = 0; i < medicineList.size(); i++) {
            MedicineItem item = medicineList.get(i);
            ViewHolder holder = viewHolders.get(i);

            if (!item.isCycleMode() && (item.getRepeatDays() == null || item.getRepeatDays().isEmpty())) {
                new AlertDialog.Builder(context)
                        .setTitle("요일 선택 없음")
                        .setMessage("약 \"" + item.getName() + "\"의 요일을 최소 1개 이상 선택해주세요.")
                        .setPositiveButton("확인", null)
                        .show();
                return;
            }

            String name = formatMedicineName(item.getName());
            String ingredient = formatIngredient(item.getIngredient());

            List<String> times = new ArrayList<>();
            List<int[]> timeData = new ArrayList<>();
            List<String> timeLabels = new ArrayList<>();

            if (item.isMorning()) {
                times.add(String.format("%02d:%02d", hourMorning, minuteMorning));
                timeData.add(new int[]{hourMorning, minuteMorning});
                timeLabels.add("아침");
            }
            if (item.isLunch()) {
                times.add(String.format("%02d:%02d", hourLunch, minuteLunch));
                timeData.add(new int[]{hourLunch, minuteLunch});
                timeLabels.add("점심");
            }
            if (item.isDinner()) {
                times.add(String.format("%02d:%02d", hourDinner, minuteDinner));
                timeData.add(new int[]{hourDinner, minuteDinner});
                timeLabels.add("저녁");
            }
            if (item.isWakeUp()) {
                times.add(String.format("%02d:%02d", hourWakeup, minuteWakeup));
                timeData.add(new int[]{hourWakeup, minuteWakeup});
                timeLabels.add("기상 후");
            }
            if (item.isBeforeBed()) {
                times.add(String.format("%02d:%02d", hourBeforeBed, minuteBeforeBed));
                timeData.add(new int[]{hourBeforeBed, minuteBeforeBed});
                timeLabels.add("취침 전");
            }

            String timeString = times.isEmpty() ? "없음" : String.join(", ", times);
            String repeat;
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (item.isCycleMode()) {
                String cycle = holder.etCycleDays.getText().toString().trim();
                repeat = cycle.isEmpty() ? "없음" : cycle + "일마다";
                try {
                    int intervalDays = Integer.parseInt(cycle);
                    for (int j = 0; j < timeData.size(); j++) {
                        int hour = timeData.get(j)[0];
                        int minute = timeData.get(j)[1];
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, minute);
                        cal.set(Calendar.SECOND, 0);
                        if (cal.before(Calendar.getInstance())) cal.add(Calendar.DATE, 1);

                        int requestCode = (name + j).hashCode();
                        Intent intent = new Intent(context, AlarmReceiver.class);
                        intent.putExtra("alarmName", name);
                        intent.putExtra("hour", hour);
                        intent.putExtra("minute", minute);
                        intent.putExtra("intervalDays", intervalDays);
                        intent.putExtra("requestCode", requestCode);

                        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                    }
                } catch (NumberFormatException ignored) {}
            } else {
                List<String> days = item.getRepeatDays();
                repeat = (days == null || days.isEmpty()) ? "없음" : String.join(" ", days);

                for (String day : days) {
                    int dayOfWeek = getDayOfWeek(day);
                    for (int j = 0; j < timeData.size(); j++) {
                        int hour = timeData.get(j)[0];
                        int minute = timeData.get(j)[1];
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, minute);
                        cal.set(Calendar.SECOND, 0);
                        if (cal.before(Calendar.getInstance())) cal.add(Calendar.WEEK_OF_YEAR, 1);

                        int requestCode = (name + day + j).hashCode();
                        Intent intent = new Intent(context, AlarmReceiver.class);
                        intent.putExtra("alarmName", name);
                        intent.putExtra("hour", hour);
                        intent.putExtra("minute", minute);
                        intent.putExtra("dayOfWeek", dayOfWeek);
                        intent.putExtra("requestCode", requestCode);

                        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                    }
                }
            }

            String key = name;
            String dateStr = new SimpleDateFormat("MM/dd", Locale.getDefault()).format(new Date());

            if (sharedPreferences.contains(key)) {
                key = name + " " + dateStr + "처방";
            }

            String value = "이름: " + name + "\n" +
                    "성분: " + ingredient + "\n" +
                    "시간: " + timeString + "\n" +
                    "반복: " + repeat;
            if (item.isCycleMode()) value += "\n시작일: " + today;

            editor.putString(key, value);
        }
        editor.apply();
    }

    private boolean isAllDaysUnchecked(ViewHolder holder) {
        return !holder.btnMon.isChecked() &&
                !holder.btnTue.isChecked() &&
                !holder.btnWed.isChecked() &&
                !holder.btnThu.isChecked() &&
                !holder.btnFri.isChecked() &&
                !holder.btnSat.isChecked() &&
                !holder.btnSun.isChecked();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicineName, tvIngredient;
        CheckBox cbM, cbL, cbD, cbWakeup, cbBeforeBed;
        RadioGroup radioGroupRepeat;
        LinearLayout cycleInputLayout, med_del_Layout;
        MaterialButton btnMon, btnTue, btnWed, btnThu, btnFri, btnSat, btnSun;
        EditText etCycleDays;
        MaterialButtonToggleGroup dayButtonsLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            tvIngredient = itemView.findViewById(R.id.tvIngredient);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            cbM = itemView.findViewById(R.id.cbM);
            cbL = itemView.findViewById(R.id.cbL);
            cbD = itemView.findViewById(R.id.cbD);
            cbWakeup = itemView.findViewById(R.id.cbWakeup);
            cbBeforeBed = itemView.findViewById(R.id.cbBeforeBed);
            radioGroupRepeat = itemView.findViewById(R.id.radioGroupRepeat);
            dayButtonsLayout = itemView.findViewById(R.id.toggleGroupDays);
            cycleInputLayout = itemView.findViewById(R.id.cycleInputLayout);
            etCycleDays = itemView.findViewById(R.id.etCycleDays);
            btnMon = itemView.findViewById(R.id.btnMon);
            btnTue = itemView.findViewById(R.id.btnTue);
            btnWed = itemView.findViewById(R.id.btnWed);
            btnThu = itemView.findViewById(R.id.btnThu);
            btnFri = itemView.findViewById(R.id.btnFri);
            btnSat = itemView.findViewById(R.id.btnSat);
            btnSun = itemView.findViewById(R.id.btnSun);
            med_del_Layout = itemView.findViewById(R.id.med_del_Layout);
        }
    }
}
