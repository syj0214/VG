package com.example.veggiecoach;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CalendarView calendarView;
    private String selectedDate;
    private RecyclerView rvSchedules;
    private ScheduleAdapter scheduleAdapter;
    private List<ScheduleItem> currentScheduleList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = FirebaseFirestore.getInstance();
        calendarView = findViewById(R.id.calendarView);
        ImageButton btnAddSchedule = findViewById(R.id.btnAddSchedule);
        rvSchedules = findViewById(R.id.rvSchedules);

        // RecyclerView 세팅
        scheduleAdapter = new ScheduleAdapter(currentScheduleList);
        rvSchedules.setLayoutManager(new LinearLayoutManager(this));
        rvSchedules.setAdapter(scheduleAdapter);

        // 오늘 날짜 선택 초기화
        selectedDate = getDateFromMillis(calendarView.getDate());

        // 오늘 날짜 일정 불러오기
        loadSchedulesByDate(selectedDate, new OnSchedulesLoadedListener() {
            @Override
            public void onLoaded(List<ScheduleItem> schedules) {
                currentScheduleList.clear();
                currentScheduleList.addAll(schedules);
                scheduleAdapter.updateList(currentScheduleList);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CalendarActivity.this, "일정 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 날짜 변경 시
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            // 선택한 날짜 일정 불러오기
            loadSchedulesByDate(selectedDate, new OnSchedulesLoadedListener() {
                @Override
                public void onLoaded(List<ScheduleItem> schedules) {
                    currentScheduleList.clear();
                    currentScheduleList.addAll(schedules);
                    scheduleAdapter.updateList(currentScheduleList);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(CalendarActivity.this, "일정 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 일정 추가 버튼 클릭 시 다이얼로그로 텍스트 입력
        btnAddSchedule.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(this, "날짜를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("일정 추가");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("추가", (dialog, which) -> {
                String content = input.getText().toString().trim();
                if (content.isEmpty()) {
                    Toast.makeText(this, "일정을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                addSchedule(selectedDate, content);
            });

            builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

            builder.show();
        });
    }

    private String getDateFromMillis(long millis) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar.get(java.util.Calendar.YEAR) + "-"
                + (calendar.get(java.util.Calendar.MONTH) + 1) + "-"
                + calendar.get(java.util.Calendar.DAY_OF_MONTH);
    }

    // 일정 추가 함수
    public void addSchedule(String date, String content) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference scheduleRef = db.collection("users")
                .document(uid)
                .collection("schedules");

        ScheduleItem newSchedule = new ScheduleItem(date, content);

        scheduleRef.add(newSchedule)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "일정 추가 완료", Toast.LENGTH_SHORT).show();
                    // 일정 추가 후 새로고침
                    loadSchedulesByDate(date, new OnSchedulesLoadedListener() {
                        @Override
                        public void onLoaded(List<ScheduleItem> schedules) {
                            currentScheduleList.clear();
                            currentScheduleList.addAll(schedules);
                            scheduleAdapter.updateList(currentScheduleList);
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(CalendarActivity.this, "일정 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "일정 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 일정 조회 함수 (날짜별)
    public void loadSchedulesByDate(String date, OnSchedulesLoadedListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users")
                .document(uid)
                .collection("schedules")
                .whereEqualTo("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ScheduleItem> schedules = new ArrayList<>();
                        QuerySnapshot snapshots = task.getResult();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            ScheduleItem item = doc.toObject(ScheduleItem.class);
                            item.setId(doc.getId());
                            schedules.add(item);
                        }
                        listener.onLoaded(schedules);
                    } else {
                        listener.onError(task.getException());
                    }
                });
    }

    public interface OnSchedulesLoadedListener {
        void onLoaded(List<ScheduleItem> schedules);
        void onError(Exception e);
    }

    // 일정 데이터 모델
    public static class ScheduleItem {
        private String id;
        private String date;
        private String content;

        public ScheduleItem() {}  // Firestore용 빈 생성자

        public ScheduleItem(String date, String content) {
            this.date = date;
            this.content = content;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
