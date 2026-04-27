package com.example.habittracker.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;
import com.example.habittracker.db.entity.Category;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.ui.adapter.ItemAdapter;
import com.example.habittracker.util.NotificationHelper;
import com.example.habittracker.util.Utils;
import com.example.habittracker.viewmodel.HabitViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class HabitListFragment extends Fragment {

    private HabitViewModel habitViewModel;
    private ItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habit_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewHabits);
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);

        adapter = new ItemAdapter(new ItemAdapter.OnHabitClickListener() {
            @Override
            public void onHabitClick(HabitModel habit) {
                Bundle bundle = new Bundle();
                bundle.putInt("habitId", habit.getId());
                Navigation.findNavController(view).navigate(R.id.action_list_to_details, bundle);
            }

            @Override
            public void onCompleteClick(HabitModel habit) {

                habitViewModel.getMarkDoneEvent().observe(getViewLifecycleOwner(), result -> {
                    if (result == null) return;

                    switch (result) {
                        case SUCCESS:
                            Snackbar.make(view, "Привычка выполнена!", Snackbar.LENGTH_SHORT).show();
                            break;
                        case GOAL_REACHED:
                            showConfettiAnimation();
                            NotificationHelper.cancelAlarm(view.getContext(), habit.getId());
                            Snackbar.make(view, "Цель достигнута!", Snackbar.LENGTH_SHORT).show();
                            break;
                        case ALREADY_DONE:
                            Snackbar.make(view, "Сегодня уже выполнялась!", Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                });

                habitViewModel.completeHabit(habit.getId());
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupSwipe(recyclerView);

        // observe the main Habit list (Use only the filtered list)
        habitViewModel.filteredHabits.observe(getViewLifecycleOwner(), habits -> {
            adapter.submitList(habits);
        });

        // dynamic Categories (Filtering)
        ChipGroup categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        habitViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryChipGroup.removeAllViews();

            // add "All" chip
            Chip allChip = new Chip(getContext());
            allChip.setText("Все");
            allChip.setCheckable(true);
            allChip.setId(R.id.chipAll); // Unique ID for selection
            allChip.setTag(null);
            allChip.setChecked(true);
            allChip.setChipIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_all_inclusive, null));
            allChip.setChipIconVisible(true);
            categoryChipGroup.addView(allChip);

            for (Category category : categories) {
                Chip chip = new Chip(getContext());
                chip.setText(category.name);
                chip.setTag(category.id);
                chip.setCheckable(true);
                chip.setChipIconVisible(true);
                chip.setId(View.generateViewId());
                chip.setChipIcon(ContextCompat.getDrawable(getContext(), Utils.drawableMap.get(category.icon)));
                categoryChipGroup.addView(chip);
            }
        });

        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);

            Chip checkedChip = group.findViewById(checkedId);
            if (checkedId == R.id.chipAll) {
                habitViewModel.setCategory(null); // Сброс фильтра
            } else {
                habitViewModel.setCategory((Long) checkedChip.getTag());
            }
        });

        // sorting Logic
        ChipGroup sortChipGroup = view.findViewById(R.id.sortChipGroup); // ensure this is a separate group
        sortChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipSortName) habitViewModel.setSortType(SortType.NAME);
            else if (id == R.id.chipSortDate) habitViewModel.setSortType(SortType.DATE);
        });

        MaterialButton btnDirection = view.findViewById(R.id.btnSortDirection);
        habitViewModel.getIsAscending().observe(getViewLifecycleOwner(), isAsc -> {
            btnDirection.setIconResource(isAsc ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);
        });
        btnDirection.setOnClickListener(v -> habitViewModel.toggleDirection());
    }

    private void setupSwipe(RecyclerView recyclerView) {
        ColorDrawable deleteBg = new ColorDrawable(Color.RED);
        ColorDrawable editBg = new ColorDrawable(Color.GREEN);
        Drawable deleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_delete);
        Drawable editIcon = ContextCompat.getDrawable(getContext(), R.drawable.edit_24px);

        int deleteWidth = deleteIcon.getIntrinsicWidth();
        int deleteHeight = deleteIcon.getIntrinsicHeight();
        int editWidth = editIcon.getIntrinsicWidth();
        int editHeight = editIcon.getIntrinsicHeight();

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return 0;

                HabitModel habit = adapter.getCurrentList().get(position);

                if (habit.isCompleted) {
                    // only allow swiping LEFT
                    return makeMovementFlags(0, ItemTouchHelper.LEFT);
                }

                // allow both directions for non-completed habits
                return super.getMovementFlags(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                // check for NO_POSITION to avoid crashes during animations
                if (position != RecyclerView.NO_POSITION) {
                    HabitModel habit = adapter.getCurrentList().get(position);

                    if (direction == ItemTouchHelper.RIGHT) {
                        // OPEN EDIT
                        AddHabitSheet.newInstance(habit.getId()).show(getChildFragmentManager(), "EditTag");
                        adapter.notifyItemChanged(position); // Reset the swiped item view
                    } else {
                        // DELETE/ARCHIVE (Left Swipe)
                        habitViewModel.archiveHabit(habit.getId());

                        Snackbar.make(
                                        requireActivity().findViewById(R.id.main_content),
                                        "Архивировано: " + (habit.getName().length() > 15 ?
                                                habit.getName().substring(0, 15) : habit.getName()),
                                        Snackbar.LENGTH_LONG)
                                .setAction("Отмена", v -> {
                                    // this triggers the LiveData observer to refresh the UI
                                    habitViewModel.restoreHabit(habit.getId());
                                })
                                .show();
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = vh.itemView;
                int itemHeight = itemView.getBottom() - itemView.getTop();

                int position = vh.getBindingAdapterPosition();
                HabitModel habit = (position != RecyclerView.NO_POSITION) ? adapter.getCurrentList().get(position) : null;

                if (dX > 0) {
                    if (habit != null && !habit.isCompleted) {
                        // swiping Right - Edit
                        // draw Green deleteBg for Edit
                        editBg.setBounds(itemView.getLeft(), itemView.getTop(),
                                itemView.getLeft() + (int) dX, itemView.getBottom());
                        editBg.draw(c);

                        int iconTop = itemView.getTop() + (itemHeight - editHeight) / 2;
                        int iconMargin = (itemHeight - editHeight) / 2;
                        int iconLeft = itemView.getLeft() + iconMargin;
                        int iconRight = itemView.getLeft() + iconMargin + editWidth;
                        int iconBottom = iconTop + editHeight;

                        editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        editIcon.draw(c);
                    } else {
                        // if it IS completed, don't draw anything for dX > 0
                        // and don't call super to completely block the visual offset
                        return;
                    }

                } else if (dX < 0) { //todo replace 'else if' with 'else'
                    // swiping Left (Delete)
                    // drawing the red deleteBg
                    deleteBg.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    deleteBg.draw(c);

                    // calculate icon position and draw it
                    int iconTop = itemView.getTop() + (itemHeight - deleteHeight) / 2;
                    int iconMargin = (itemHeight - deleteHeight) / 2;
                    int iconRight = itemView.getRight() - iconMargin;
                    int iconLeft = iconRight - deleteWidth;
                    int iconBottom = iconTop + deleteHeight;
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    deleteIcon.draw(c);
                }

                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }


    private void showConfettiAnimation() {
        final KonfettiView konfettiView = getView().findViewById(R.id.konfettiView);

        EmitterConfig emitterConfig = new Emitter(3L, TimeUnit.SECONDS).perSecond(50);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .angle(Angle.TOP)
                        .spread(Spread.WIDE)
                        .setSpeedBetween(10f, 30f)
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .position(new Position.Relative(0.5, 0.3)) // Top center
                        .build()
        );
    }
}
