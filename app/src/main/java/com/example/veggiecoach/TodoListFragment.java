package com.example.veggiecoach;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TodoListFragment extends Fragment {

    // newInstance() 정적 메서드 추가
    public static TodoListFragment newInstance() {
        return new TodoListFragment();
    }

    private ImageButton btnAddSchedule;
    private RecyclerView rvSchedules;
    private ScheduleAdapter scheduleAdapter;
    private List<ScheduleItem> scheduleList = new ArrayList<>();

    private FirebaseFirestore db;
    private String selectedDate;  // 현재 선택한 날짜 (YYYY-MM-DD)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_todo_list, container, false);

        btnAddSchedule = root.findViewById(R.id.btnAddSchedule);
        rvSchedules = root.findViewById(R.id.rvSchedules);

        db = FirebaseFirestore.getInstance();

        // 기본 날짜를 오늘 날짜로 설정 (YYYY-MM-DD)
        selectedDate = getTodayDate();

        // RecyclerView 세팅
        scheduleAdapter = new ScheduleAdapter(scheduleList);
        rvSchedules.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSchedules.setAdapter(scheduleAdapter);

        loadSchedulesByDate(selectedDate);

        btnAddSchedule.setOnClickListener(v -> showAddScheduleDialog());

        return root;
    }

    // 오늘 날짜 가져오기 (간단 포맷)
    private String getTodayDate() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        return calendar.get(java.util.Calendar.YEAR) + "-" +
                (calendar.get(java.util.Calendar.MONTH) + 1) + "-" +
                calendar.get(java.util.Calendar.DAY_OF_MONTH);
    }

    private void showAddScheduleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("일정 추가 (" + selectedDate + ")");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("추가", (dialog, which) -> {
            String content = input.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "일정을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            addSchedule(selectedDate, content);
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addSchedule(String date, String content) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference scheduleRef = db.collection("users")
                .document(uid)
                .collection("schedules");

        ScheduleItem newSchedule = new ScheduleItem(date, content);

        scheduleRef.add(newSchedule)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "일정 추가 완료", Toast.LENGTH_SHORT).show();
                    loadSchedulesByDate(date);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "일정 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadSchedulesByDate(String date) {
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
                        scheduleList.clear();
                        scheduleList.addAll(schedules);
                        scheduleAdapter.updateList(scheduleList);
                    } else {
                        Toast.makeText(requireContext(), "일정 불러오기 실패: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 일정 데이터 모델 (TodoListFragment 내 중첩 클래스)
    public static class ScheduleItem {
        private String id;
        private String date;
        private String content;

        public ScheduleItem() {}

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
