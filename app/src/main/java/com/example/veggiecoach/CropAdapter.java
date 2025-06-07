package com.example.veggiecoach;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {

    private List<String> cropList;
    private OnCropDeleteListener deleteListener;
    private OnCropClickListener clickListener;

    public CropAdapter(List<String> cropList, OnCropDeleteListener deleteListener, OnCropClickListener clickListener) {
        this.cropList = cropList;
        this.deleteListener = deleteListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crop, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        String cropName = cropList.get(position);
        holder.tvCropName.setText(cropName);

        // 삭제 버튼 눌렀을 때
        holder.btnDeleteCrop.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (deleteListener != null && currentPos != RecyclerView.NO_POSITION) {
                deleteListener.onDelete(currentPos);
            }
        });

        // 아이템(작물 카드) 클릭했을 때
        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (clickListener != null && currentPos != RecyclerView.NO_POSITION) {
                clickListener.onClick(cropList.get(currentPos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    // 삭제용 인터페이스
    public interface OnCropDeleteListener {
        void onDelete(int position);
    }

    // 클릭용 인터페이스 (추가)
    public interface OnCropClickListener {
        void onClick(String cropName);
    }

    public static class CropViewHolder extends RecyclerView.ViewHolder {
        TextView tvCropName;
        ImageButton btnDeleteCrop;

        public CropViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCropName = itemView.findViewById(R.id.tvCropName);
            btnDeleteCrop = itemView.findViewById(R.id.btnDeleteCrop);
        }
    }
}
