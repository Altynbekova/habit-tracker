package com.example.habittracker.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.databinding.ItemHabitBinding;
import com.example.habittracker.db.HabitModel;
import com.example.habittracker.ui.OnClickItemInterface;

import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private final OnClickItemInterface onClickItemInterface;
    private List<HabitModel> habitList;

    public HabitAdapter(OnClickItemInterface onClickItemInterface) {
        this.onClickItemInterface = onClickItemInterface;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHabitBinding view = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_habit, parent, false
        );
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        if (habitList != null) {
            HabitModel habit = habitList.get(position);
            holder.binding.setHabitModel(habit);
            holder.binding.setClickListener(onClickItemInterface);
        }
    }

    @Override
    public int getItemCount() {
        if (habitList != null) {
            return habitList.size();
        } else
            return 0;
    }

    public void setHabits(List<HabitModel> habitModels) {
        this.habitList = habitModels;
        notifyDataSetChanged();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        public ItemHabitBinding binding;

        public HabitViewHolder(@NonNull ItemHabitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}