package com.example.designpattern.UI.learning;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.designpattern.Adapter.ListItemAdapter;
import com.example.designpattern.Interface.IClickItemListener;
import com.example.designpattern.Models.Bookmark;
import com.example.designpattern.Models.Pattern;
import com.example.designpattern.NamActivity;
import com.example.designpattern.R;
import com.example.designpattern.Services.BookmarkService;
import com.example.designpattern.Services.PatternService;
import com.example.designpattern.ShowDesignPatternInfoActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class LearningFragment extends Fragment {

    Set<String> selectedTypeChips = new HashSet<>();
    List<Integer> checkedTypeIds = new ArrayList<>();
    ChipGroup group1, group2;
    TextView textLan, textType;
    PatternService patternService;
    BookmarkService bookmarkService;
    RecyclerView recyclerView;
    AlertDialog customFilterDialog;
    ListItemAdapter adapter;
    ChipGroup chipContainer2;
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Lưu trữ trạng thái
        outState.putStringArrayList("selectedTypeChips", new ArrayList<>(selectedTypeChips));
        outState.putIntegerArrayList("checkedTypeIds", new ArrayList<>(checkedTypeIds));

        List<Integer> checkedIds = new ArrayList<>();
        for (int i = 0; i < chipContainer2.getChildCount(); i++) {
            Chip chip = (Chip) chipContainer2.getChildAt(i);
            if (chip.isChecked()) {
                checkedIds.add(chip.getId());
            }
        }
        outState.putIntegerArrayList("chipContainer2", new ArrayList<>(checkedIds));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            // Khôi phục trạng thái
            selectedTypeChips.clear();
            selectedTypeChips.addAll(savedInstanceState.getStringArrayList("selectedTypeChips"));
            checkedTypeIds.clear();
            checkedTypeIds.addAll(savedInstanceState.getIntegerArrayList("checkedTypeIds"));

            LayoutInflater inflater2 = LayoutInflater.from(getContext());

            if (!selectedTypeChips.isEmpty()) {
                textType.setVisibility(View.VISIBLE);
            }
            else {
                textType.setVisibility(View.GONE);
            }
            group2.removeAllViews();
            for (String text : selectedTypeChips) {
                Chip chip = (Chip) inflater2.inflate(R.layout.item_chip, group2, false);
                chip.setText(text);
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String chipText = chip.getText().toString();
                        if (selectedTypeChips.contains(chipText)) {
                            selectedTypeChips.remove(chipText);
                            if (!selectedTypeChips.isEmpty()) {
                                textType.setVisibility(View.VISIBLE);
                            }
                            else {
                                textType.setVisibility(View.GONE);
                            }
                            List<String> typeList = new ArrayList<>(selectedTypeChips);
                            loadPattern(typeList);
                            group2.removeView(chip);
                            List<Integer> savedCheckedIds = savedInstanceState.getIntegerArrayList("chipContainer2");
                            if (savedCheckedIds != null) {
                                for (int chipId : savedCheckedIds) {
                                    Chip chipInContainer = chipContainer2.findViewById(chipId);
                                    if (chipInContainer.getText().toString().equals(chip.getText().toString())) {
                                        if (checkedTypeIds.contains(chipInContainer.getId())) {
                                            checkedTypeIds.remove(Integer.valueOf(chipInContainer.getId()));
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
                group2.addView(chip);
            }
            List<String> typeList = new ArrayList<>(selectedTypeChips);
            loadPattern(typeList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learning, container, false);
        group1 = view.findViewById(R.id.chipContainerLanguage);
        group2 = view.findViewById(R.id.chipContainerType);
        textLan = view.findViewById(R.id.textLanguage);
        textType = view.findViewById(R.id.textType);

        process();

        setHasOptionsMenu(true);
        recyclerView = view.findViewById(R.id.recyclerView1);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        patternService = new PatternService(getContext());
        bookmarkService = new BookmarkService(getContext());

        loadPattern(new ArrayList<>());
        return view;
    }

    void process() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View customFilterView = getLayoutInflater().inflate(R.layout.custom_filter, null);

        builder.setView(customFilterView);

        customFilterDialog = builder.create();

        customFilterDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        customFilterDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        chipContainer2 = customFilterView.findViewById(R.id.chipContainer2);

        chipContainer2.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> list) {
                for (int i = 0; i < chipContainer2.getChildCount(); i++) {
                    Chip chipInContainer = (Chip) chipContainer2.getChildAt(i);
                    boolean check = false;
                    for (Integer index : list) {
                        Chip chip = chipGroup.findViewById(index);
                        if (chipInContainer.getText() == chip.getText()) {
                            if (!checkedTypeIds.contains(chipInContainer.getId())) {
                                checkedTypeIds.add(chipInContainer.getId());
                                selectedTypeChips.add(chip.getText().toString());
                            }
                            check = true;
                        }
                    }
                    if (!check) {
                        if (checkedTypeIds.contains(chipInContainer.getId())) {
                            checkedTypeIds.remove(Integer.valueOf(chipInContainer.getId()));
                            selectedTypeChips.remove(chipInContainer.getText().toString());
                        }
                    }
                }
            }
        });

        customFilterDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                List<Integer> checkedIds = new ArrayList<>(checkedTypeIds);
                List<String> c = new ArrayList<>(selectedTypeChips);
                for (int i = 0; i < chipContainer2.getChildCount(); i++) {
                    Chip chipInContainer = (Chip) chipContainer2.getChildAt(i);
                    if (chipInContainer != null) {
                        if (checkedIds.contains(chipInContainer.getId())) {
                            chipInContainer.setChecked(true); // Set checked nếu có
                        } else {
                            chipInContainer.setChecked(false); // Set false nếu không có
                        }
                    }
                }
            }
        });

        customFilterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

                LayoutInflater inflater = LayoutInflater.from(getContext());

                if (!selectedTypeChips.isEmpty()) {
                    textType.setVisibility(View.VISIBLE);
                }
                else {
                    textType.setVisibility(View.GONE);
                }
                group2.removeAllViews();
                for (String text : selectedTypeChips) {
                    Chip chip = (Chip) inflater.inflate(R.layout.item_chip, group2, false);
                    chip.setText(text);
                    chip.setCloseIconVisible(true);
                    chip.setOnCloseIconClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String chipText = chip.getText().toString();
                            if (selectedTypeChips.contains(chipText)) {
                                selectedTypeChips.remove(chipText);
                                if (!selectedTypeChips.isEmpty()) {
                                    textType.setVisibility(View.VISIBLE);
                                }
                                else {
                                    textType.setVisibility(View.GONE);
                                }
                                List<String> typeList = new ArrayList<>(selectedTypeChips);
                                loadPattern(typeList);
                                group2.removeView(chip);
                                for (int i = 0; i < chipContainer2.getChildCount(); i++) {
                                    Chip chipInContainer = (Chip) chipContainer2.getChildAt(i);
                                    if (chipInContainer.getText().toString().equals(chip.getText().toString())) {
                                        if (checkedTypeIds.contains(chipInContainer.getId())) {
                                            checkedTypeIds.remove(Integer.valueOf(chipInContainer.getId()));
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    });
                    group2.addView(chip);
                }
                List<String> typeList = new ArrayList<>(selectedTypeChips);
                loadPattern(typeList);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_home, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            customFilterDialog.show();
        }
        return true;
    }
    void loadPattern(List<String> types) {
        ArrayList<Pattern> result = new ArrayList<>();
        for (String type : types) {
            ArrayList<Pattern> patterns = patternService.GetAllByCatalog(type);
            result.addAll(patterns);
        }
        if (result.isEmpty()) {
            result = patternService.GetAll(Pattern.class);
        }
        Map<String, Integer> catalogOrder = new HashMap<>();
        catalogOrder.put("Creational Patterns", 1);
        catalogOrder.put("Structural Patterns", 2);
        catalogOrder.put("Behavioral Patterns", 3);

        List<Bookmark> bookmarks = bookmarkService.GetAll(Bookmark.class);

        // Extract bookmarked PatternIds
        List<Integer> bookmarkedPatternIds = new ArrayList<>();
        for (Bookmark bookmark : bookmarks) {
            bookmarkedPatternIds.add(bookmark.getPatternId());
        }

        Collections.sort(result, new Comparator<Pattern>() {
            @Override
            public int compare(Pattern pattern1, Pattern pattern2) {
                boolean isPattern1Bookmarked = bookmarkedPatternIds.contains(pattern1.getId());
                boolean isPattern2Bookmarked = bookmarkedPatternIds.contains(pattern2.getId());

                if (isPattern1Bookmarked && !isPattern2Bookmarked) {
                    return -1; // pattern1 should come before pattern2
                } else if (!isPattern1Bookmarked && isPattern2Bookmarked) {
                    return 1; // pattern2 should come before pattern1
                }

                // Compare catalog order if both patterns are either bookmarked or not bookmarked
                int catalogCompare = catalogOrder.get(pattern1.getCatalog()).compareTo(catalogOrder.get(pattern2.getCatalog()));
                if (catalogCompare != 0) {
                    return catalogCompare;
                } else {
                    // If catalogs are the same, compare by name
                    return pattern1.getName().compareTo(pattern2.getName());
                }
            }
        });

        adapter = new ListItemAdapter(getContext(), result, new IClickItemListener() {
            @Override
            public void onClickItem(String itemType) {
                onClickGoToHomePage(itemType);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    public void onClickGoToHomePage(String PatternName){
        Intent intent = new Intent(getContext(), ShowDesignPatternInfoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("PatternName", PatternName);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}