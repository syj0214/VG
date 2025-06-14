package com.example.veggiecoach;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    private ImageButton btnAddTodo;
    private LinearLayout todoListItems;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        btnAddTodo = root.findViewById(R.id.btnAddTodo);
        todoListItems = root.findViewById(R.id.todoListItems);

        // 할 일 추가 버튼 클릭 시 캘린더 액티비티 오픈
        btnAddTodo.setOnClickListener(v -> openCalendarActivity());

        return root;
    }

    // 캘린더 액티비티 열기
    private void openCalendarActivity() {
        Intent intent = new Intent(requireContext(), CalendarActivity.class);
        startActivity(intent);
    }

    // 할 일 리스트에 아이템 추가 (예시)
    public void addTodoItem(String dateStr) {
        TextView todoItem = new TextView(requireContext());
        todoItem.setText("작물 심기 날짜: " + dateStr + " - 할 일: 잡초 뽑기");
        todoItem.setTextSize(16);
        todoItem.setPadding(8, 8, 8, 8);
        todoListItems.addView(todoItem);
    }
}
