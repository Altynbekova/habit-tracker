package com.example.habittracker.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.db.entity.Category;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.util.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.Map;

public class ItemAdapter extends ListAdapter<HabitModel, ItemAdapter.HabitViewHolder> {

    private static final DiffUtil.ItemCallback<HabitModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<HabitModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull HabitModel oldItem, @NonNull HabitModel newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull HabitModel oldItem, @NonNull HabitModel newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.isArchived() == newItem.isArchived() &&
                    oldItem.isCompleted() == newItem.isCompleted() &&
                    oldItem.getCategoryId().equals(newItem.getCategoryId());
        }
    };
    private static final Map<Long, Category> categoryMap = new HashMap<>(
            Map.of(
                    1L, Utils.categories().get(0),
                    2L, Utils.categories().get(1),
                    3L, Utils.categories().get(2),
                    4L, Utils.categories().get(3)
            )
    );

    private final OnHabitClickListener listener;

    public ItemAdapter(OnHabitClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        HabitModel currentHabit = getItem(position);

        if (currentHabit.isCompleted()) {
            MaterialButton completion = holder.cardView.findViewById(R.id.buttonComplete);
            completion.setIconResource(R.drawable.outline_check_24);
        }
        holder.textViewName.setText(currentHabit.getName());

        if (currentHabit.categoryId != null && categoryMap.get(currentHabit.categoryId) != null) {
            holder.imageViewCategory.setImageResource(
                    Utils.drawableMap.get(categoryMap.get(currentHabit.categoryId).getIcon())
            );
            holder.imageViewCategory.setVisibility(View.VISIBLE);

        } else {
            holder.imageViewCategory.setVisibility(View.GONE);
        }

        // toggle icon based on status
        /*if (currentHabit.isCompleted) {
            holder.buttonComplete.setIconResource(R.drawable.ic_check_circle);
            // Optional: Change color to green when done
            holder.buttonComplete.setIconTintResource(R.color.streak_fire);
        } else {
            holder.buttonComplete.setIconResource(R.drawable.ic_circle_outline);
            holder.buttonComplete.setIconTintResource(R.color.streak_color);
        }*/

        holder.itemView.setOnClickListener(v -> listener.onHabitClick(currentHabit));
        holder.buttonComplete.setOnClickListener(v -> listener.onCompleteClick(currentHabit));
    }

    public interface OnHabitClickListener {
        void onHabitClick(HabitModel habit);

        void onCompleteClick(HabitModel habit);
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewCategory;
        private final TextView textViewName;
        private final MaterialButton buttonComplete;
        private final MaterialCardView cardView;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCategory = itemView.findViewById(R.id.imageViewHabitCategory);
            textViewName = itemView.findViewById(R.id.textViewHabitName);
            buttonComplete = itemView.findViewById(R.id.buttonComplete);
            cardView = itemView.findViewById(R.id.habitCard);
        }
    }

    /*@Override
    public HabitModel getItem(int position) {
        return super.getItem(position);
    }*/
}

