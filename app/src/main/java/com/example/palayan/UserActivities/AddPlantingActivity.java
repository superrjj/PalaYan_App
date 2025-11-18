package com.example.palayan.UserActivities;

import android.app.DatePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.palayan.Adapter.CropCalendarAdapter;
import com.example.palayan.Adapter.CropCalendarPreviewAdapter;
import com.example.palayan.Helper.CropCalendarTask;
import com.example.palayan.Helper.FertilizerScheduleEntry;
import com.example.palayan.Helper.NonScrollableRecyclerView;
import com.example.palayan.Helper.RicePlanting;
import com.example.palayan.Helper.JournalStorageHelper;
import com.example.palayan.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddPlantingActivity extends AppCompatActivity {

    private static final int TOTAL_CALENDAR_WEEKS = 20;

    private TextView tvTitle, tvRiceFieldInfo, tvPlantingDate, tvRemove;
    private TextView tabSeedSelection, tabFertilizer, tabCropCalendar;
    private View indicatorSeedSelection, indicatorFertilizer, indicatorCropCalendar;
    private LinearLayout layoutSeedSelection, layoutFertilizer, layoutCropCalendar;
    private ImageView ivBack;
    private LinearLayout layoutBack, layoutPlantingDate, layoutCustomFertilizer;
    private View layoutFertilizerScheduleTableCombo1;
    private View layoutFertilizerScheduleTableCombo2;
    private View layoutFertilizerScheduleTableCombo3;
    private View layoutFertilizerScheduleTableCombo4;
    private View layoutFertilizerScheduleTableSarilingDiskarte;
    private LinearLayout layoutTableRows;
    private AutoCompleteTextView etRiceVariety;
    private TextInputEditText etSeedWeight;
    private RadioGroup rgPlantingMethod, rgFertilizerStrategy, rgFertilizerCombo;
    private RadioButton rbSabogTanim, rbLipatTanim, rbAbonongSwak, rbSarilingDiskarte;
    private RadioButton rbCombo1, rbCombo2, rbCombo3, rbCombo4;
    private Button btnSave, btnNext, btnBack;
    private RecyclerView rvCropCalendar, rvFertilizerSchedule;
    private CropCalendarAdapter cropCalendarAdapter;
    private CropCalendarPreviewAdapter cropCalendarPreviewAdapter;
    private int currentStep = 0; // 0 = Seed, 1 = Fertilizer, 2 = Crop Calendar
    private List<CropCalendarTask> cropCalendarTasks = new ArrayList<>();
    private boolean isPreviewMode = true; // true for new planting, false for editing
    private final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final Map<String, List<String>> categoryTaskOptions = new LinkedHashMap<>();
    private static final String[] TASK_CATEGORY_ORDER = new String[] {
            "Paghahanda ng lupa",
            "Pagtatanim",
            "Pagpapataba",
            "Pagpapatubig",
            "Pamamahala sa damo",
            "Pamamahala sa peste",
            "Pag-aani at paghahanda",
            "Iba pang gawain"
    };

    private String riceFieldId;
    private String riceFieldName;
    private String plantingId; // For editing/deleting existing planting
    private String existingVariety;
    private String existingMethod;
    private String existingDate;
    private String existingSeedWeight;
    private String existingFertilizerUsed;
    private String existingFertilizerAmount;
    private String existingNotes;
    private boolean showCropCalendar = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_planting);

        riceFieldId = getIntent().getStringExtra("riceFieldId");
        riceFieldName = getIntent().getStringExtra("riceFieldName");
        plantingId = getIntent().getStringExtra("plantingId");
        existingVariety = getIntent().getStringExtra("plantingVariety");
        existingMethod = getIntent().getStringExtra("plantingMethod");
        existingDate = getIntent().getStringExtra("plantingDate");
        existingSeedWeight = getIntent().getStringExtra("seedWeight");
        existingFertilizerUsed = getIntent().getStringExtra("fertilizerUsed");
        existingFertilizerAmount = getIntent().getStringExtra("fertilizerAmount");
        existingNotes = getIntent().getStringExtra("notes");
        showCropCalendar = getIntent().getBooleanExtra("showCropCalendar", false);

        if (riceFieldId == null || riceFieldId.isEmpty()) {
            showSnackBar("Invalid rice field.", false, null);
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvRiceFieldInfo = findViewById(R.id.tvRiceFieldInfo);
        tvPlantingDate = findViewById(R.id.tvPlantingDate);
        ivBack = findViewById(R.id.ivBack);
        layoutBack = findViewById(R.id.layoutBack);
        layoutPlantingDate = findViewById(R.id.layoutPlantingDate);
        etRiceVariety = findViewById(R.id.etRiceVariety);
        etSeedWeight = findViewById(R.id.etSeedWeight);
        rgPlantingMethod = findViewById(R.id.rgPlantingMethod);
        rbSabogTanim = findViewById(R.id.rbSabogTanim);
        rbLipatTanim = findViewById(R.id.rbLipatTanim);
        btnSave = findViewById(R.id.btnSave);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);
        tvRemove = findViewById(R.id.tvRemove);

        // Tabs
        tabSeedSelection = findViewById(R.id.tabSeedSelection);
        tabFertilizer = findViewById(R.id.tabFertilizer);
        tabCropCalendar = findViewById(R.id.tabCropCalendar);
        indicatorSeedSelection = findViewById(R.id.indicatorSeedSelection);
        indicatorFertilizer = findViewById(R.id.indicatorFertilizer);
        indicatorCropCalendar = findViewById(R.id.indicatorCropCalendar);

        // Layouts
        layoutSeedSelection = findViewById(R.id.layoutSeedSelection);
        layoutFertilizer = findViewById(R.id.layoutFertilizer);
        layoutCropCalendar = findViewById(R.id.layoutCropCalendar);
        layoutCustomFertilizer = findViewById(R.id.layoutCustomFertilizer);

        // Fertilizer
        rgFertilizerStrategy = findViewById(R.id.rgFertilizerStrategy);
        rgFertilizerCombo = findViewById(R.id.rgFertilizerCombo);
        rbAbonongSwak = findViewById(R.id.rbAbonongSwak);
        rbSarilingDiskarte = findViewById(R.id.rbSarilingDiskarte);
        rbCombo1 = findViewById(R.id.rbCombo1);
        rbCombo2 = findViewById(R.id.rbCombo2);
        rbCombo3 = findViewById(R.id.rbCombo3);
        rbCombo4 = findViewById(R.id.rbCombo4);
        layoutFertilizerScheduleTableCombo1 = findViewById(R.id.layoutFertilizerScheduleTableCombo1);
        layoutFertilizerScheduleTableCombo2 = findViewById(R.id.layoutFertilizerScheduleTableCombo2);
        layoutFertilizerScheduleTableCombo3 = findViewById(R.id.layoutFertilizerScheduleTableCombo3);
        layoutFertilizerScheduleTableCombo4 = findViewById(R.id.layoutFertilizerScheduleTableCombo4);
        layoutFertilizerScheduleTableSarilingDiskarte = findViewById(R.id.layoutFertilizerScheduleTableSarilingDiskarte);
        layoutTableRows = findViewById(R.id.layoutTableRows);

        // RecyclerViews
        rvCropCalendar = findViewById(R.id.rvCropCalendar);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvCropCalendar.setLayoutManager(layoutManager);
        rvCropCalendar.setHasFixedSize(false);
        rvCropCalendar.setNestedScrollingEnabled(false);
        
        // Preview adapter (for new plantings - informational only)
        cropCalendarPreviewAdapter = new CropCalendarPreviewAdapter();
        
        // Interactive adapter (for editing - with checkboxes)
        cropCalendarAdapter = new CropCalendarAdapter((task, targetState) -> {
            if (targetState) {
                showTaskCompletionDialog(task);
            } else {
                handleTaskUncheck(task);
            }
        });
        
        // Determine if editing or adding new
        boolean isEditing = plantingId != null && !plantingId.isEmpty();
        isPreviewMode = !isEditing;
        
        // Set initial adapter
        if (isPreviewMode) {
            rvCropCalendar.setAdapter(cropCalendarPreviewAdapter);
        } else {
            rvCropCalendar.setAdapter(cropCalendarAdapter);
        }

        if (riceFieldName != null && !riceFieldName.isEmpty()) {
            tvRiceFieldInfo.setText("Para sa: " + riceFieldName);
        }

        ArrayAdapter<CharSequence> varietyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sample_variety_array,
                android.R.layout.simple_list_item_1
        );
        etRiceVariety.setAdapter(varietyAdapter);
        etRiceVariety.setThreshold(1);

        initTaskCategoryOptions();
        populateExistingPlanting();
        
        // If showing crop calendar directly (from clicking planting card), go to step 2
        if (showCropCalendar && plantingId != null && !plantingId.isEmpty()) {
            // Load tasks first, then show step 2
            JournalStorageHelper.loadRicePlantings(this, riceFieldId, new JournalStorageHelper.OnPlantingsLoadedListener() {
                @Override
                public void onSuccess(List<RicePlanting> plantings) {
                    for (RicePlanting p : plantings) {
                        if (p.getId().equals(plantingId)) {
                            // Switch to interactive mode when viewing from card
                            isPreviewMode = false;
                            rvCropCalendar.setAdapter(cropCalendarAdapter);
                            
                            if (p.getCropCalendarTasks() != null && !p.getCropCalendarTasks().isEmpty()) {
                                cropCalendarTasks = p.getCropCalendarTasks();
                                android.util.Log.d("CropCalendar", "Loaded " + cropCalendarTasks.size() + " tasks from card click");
                                
                                // Check if we have all weeks, if not, regenerate
                                if (!hasCompleteCalendar(cropCalendarTasks) && p.getPlantingDate() != null && !p.getPlantingDate().isEmpty()) {
                                    android.util.Log.d("CropCalendar", "Incomplete tasks found (" + cropCalendarTasks.size() + "), regenerating full schedule");
                                    tvPlantingDate.setText(p.getPlantingDate());
                                    if (p.getPlantingMethod() != null) {
                                        if ("Sabog-tanim".equalsIgnoreCase(p.getPlantingMethod())) {
                                            rbSabogTanim.setChecked(true);
                                        } else if ("Lipat-tanim".equalsIgnoreCase(p.getPlantingMethod())) {
                                            rbLipatTanim.setChecked(true);
                                        }
                                    }
                                    generateCropCalendarTasks();
                                } else {
                                    displayCropCalendarTasks();
                                }
                            } else if (p.getPlantingDate() != null && !p.getPlantingDate().isEmpty()) {
                                // Generate tasks if not yet generated
                                android.util.Log.d("CropCalendar", "No tasks found, generating new ones from card click");
                                tvPlantingDate.setText(p.getPlantingDate());
                                if (p.getPlantingMethod() != null) {
                                    if ("Sabog-tanim".equalsIgnoreCase(p.getPlantingMethod())) {
                                        rbSabogTanim.setChecked(true);
                                    } else if ("Lipat-tanim".equalsIgnoreCase(p.getPlantingMethod())) {
                                        rbLipatTanim.setChecked(true);
                                    }
                                }
                                generateCropCalendarTasks();
                            }
                            break;
                        }
                    }
                    showStep(2); // Show crop calendar
                }

                @Override
                public void onFailure(String error) {
                    showStep(0); // Default to step 1 if error
                }
            });
        } else {
            showStep(0); // Start with step 1
        }
    }

    private List<WeekTaskTemplate> buildWeekTaskTemplates() {
        List<WeekTaskTemplate> templates = new ArrayList<>();
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pag-aararo", "preparation"),
                new TaskTemplate("Pag-aayos ng kanal at pilapil", "preparation")
        ));
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagbabababad ng lupa", "preparation"),
                new TaskTemplate("Paghahanda ng lupang pagpupunlaan", "preparation")
        ));
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagbabababad ng binhi", "seed"),
                new TaskTemplate("Paggawa ng kamang-punlaan", "seed"),
                new TaskTemplate("Pagkukolob ng binhi", "seed"),
                new TaskTemplate("Pagpupunla", "seed"),
                new TaskTemplate("Ika-1 pagsusuyod", "weed_control"),
                new TaskTemplate("Pagpapatubig sa punlaan", "irrigation"),
                new TaskTemplate("Pagpapataba sa punlaan", "fertilizer"),
                new TaskTemplate("Ika-2 pagsusuyod", "weed_control")
        ));
        templates.add(new WeekTaskTemplate(true,
                new TaskTemplate("Paglilinang/Pagpapatag", "land_preparation"),
                new TaskTemplate("Pagbubunot ng punla", "planting"),
                new TaskTemplate("Pagkakalat ng punla", "planting"),
                new TaskTemplate("Paglilipat-tanim", "planting")
        ));
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pamamahala sa damo", "weed_control"),
                new TaskTemplate("Pamamahala sa kuhol", "pest_control"),
                new TaskTemplate("Pamamahala sa daga", "pest_control"),
                new TaskTemplate("Paghuhulip", "pest_control"),
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Ika-1 pagpapataba", "fertilizer")
        ));
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation"),
                new TaskTemplate("Pamamahala sa damo", "weed_control"),
                new TaskTemplate("Paglalagay ng observation well (AWD)", "irrigation")
        ));
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Ika-2 pagpapataba", "fertilizer"),
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation"),
                new TaskTemplate("Ika-3 pagpapataba", "fertilizer")
        ));
        templates.add(new WeekTaskTemplate(new TaskTemplate("Pagpapatubig", "irrigation")));
        templates.add(new WeekTaskTemplate(new TaskTemplate("Pagpapatubig", "irrigation")));
        templates.add(new WeekTaskTemplate(new TaskTemplate("Pagpapatubig", "irrigation")));
        templates.add(new WeekTaskTemplate(new TaskTemplate("Pagpapatubig", "irrigation")));
        templates.add(new WeekTaskTemplate(new TaskTemplate("Pagpapatubig", "irrigation")));
        templates.add(new WeekTaskTemplate(new TaskTemplate("Pagpapatubig", "irrigation")));
        templates.add(new WeekTaskTemplate(new TaskTemplate("Pagpapatubig", "irrigation")));
        templates.add(new WeekTaskTemplate(new TaskTemplate("Pagpapatubig", "irrigation")));
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation"),
                new TaskTemplate("Pagpapatuyo ng palayan", "harvest_prep")
        ));
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pag-aani", "harvest")
        ));
        return templates;
    }

    private static class WeekTaskTemplate {
        private final List<TaskTemplate> tasks = new ArrayList<>();
        private final boolean plantingWeek;

        WeekTaskTemplate(TaskTemplate... taskTemplates) {
            this(false, taskTemplates);
        }

        WeekTaskTemplate(boolean plantingWeek, TaskTemplate... taskTemplates) {
            this.plantingWeek = plantingWeek;
            if (taskTemplates != null) {
                for (TaskTemplate template : taskTemplates) {
                    tasks.add(template);
                }
            }
        }

        List<TaskTemplate> getTasks() {
            return tasks;
        }

        boolean isPlantingWeek() {
            return plantingWeek;
        }
    }

    private static class TaskTemplate {
        private final String name;
        private final String type;

        TaskTemplate(String name, String type) {
            this.name = name;
            this.type = type;
        }

        String getName() {
            return name;
        }

        String getType() {
            return type;
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        layoutBack.setOnClickListener(v -> finish());

        layoutPlantingDate.setOnClickListener(v -> showDatePicker());
        btnNext.setOnClickListener(v -> handleNext());
        btnBack.setOnClickListener(v -> handleBack());
        btnSave.setOnClickListener(v -> savePlanting());
        tvRemove.setOnClickListener(v -> {
            if (plantingId != null && !plantingId.isEmpty()) {
                showDeleteConfirmationDialog();
            }
        });

        // Tab clicks
        tabSeedSelection.setOnClickListener(v -> showStep(0));
        tabFertilizer.setOnClickListener(v -> showStep(1));
        tabCropCalendar.setOnClickListener(v -> {
            showStep(2);
            // Generate tasks if empty when clicking crop calendar tab
            if (cropCalendarTasks.isEmpty()) {
                generateCropCalendarTasks();
            } else {
                displayCropCalendarTasks();
            }
        });

        // Fertilizer strategy change
        rgFertilizerStrategy.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSarilingDiskarte) {
                layoutCustomFertilizer.setVisibility(View.GONE);
                rgFertilizerCombo.setVisibility(View.GONE);
                // Hide all combo schedules
                hideAllScheduleTables();
                // Show Sariling diskarte schedule
                showSarilingDiskarteSchedule();
            } else if (checkedId == R.id.rbAbonongSwak) {
                layoutCustomFertilizer.setVisibility(View.GONE);
                rgFertilizerCombo.setVisibility(View.VISIBLE);
                // Hide Sariling diskarte schedule
                layoutFertilizerScheduleTableSarilingDiskarte.setVisibility(View.GONE);
                // Show schedule if a combo is already selected
                updateFertilizerScheduleTable();
            } else {
                layoutCustomFertilizer.setVisibility(View.GONE);
                rgFertilizerCombo.setVisibility(View.GONE);
                hideAllScheduleTables();
            }
        });

        // Combo selection change
        rgFertilizerCombo.setOnCheckedChangeListener((group, checkedId) -> {
            updateFertilizerScheduleTable();
        });

        // Initialize fertilizer UI state
        if (rbAbonongSwak.isChecked()) {
            rgFertilizerCombo.setVisibility(View.VISIBLE);
        }
    }

    private void handleNext() {
        if (currentStep == 0) {
            if (validateStep1()) {
                showStep(1);
            }
        } else if (currentStep == 1) {
            if (validateStep2()) {
                showStep(2);
                // Generate tasks when moving to crop calendar step
                if (cropCalendarTasks.isEmpty()) {
                    generateCropCalendarTasks();
                } else {
                    displayCropCalendarTasks();
                }
            }
        }
    }

    private void handleBack() {
        if (currentStep > 0) {
            showStep(currentStep - 1);
        }
    }

    private void showStep(int step) {
        currentStep = step;

        // Update tab colors and indicators
        updateTabIndicators(step);

        // Show/hide layouts
        layoutSeedSelection.setVisibility(step == 0 ? View.VISIBLE : View.GONE);
        layoutFertilizer.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        layoutCropCalendar.setVisibility(step == 2 ? View.VISIBLE : View.GONE);

        // Update buttons - hide all first, then show appropriate ones
        btnNext.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);
        
        if (step < 2) {
            btnNext.setVisibility(View.VISIBLE);
            if (step > 0) {
                btnBack.setVisibility(View.VISIBLE);
            }
        } else {
            // Step 2 (Crop Calendar) - only show Save button, no Back button
            btnSave.setVisibility(View.VISIBLE);
            // Hide Back button in crop calendar step
            btnBack.setVisibility(View.GONE);
        }
    }

    private void updateTabIndicators(int activeStep) {
        // Reset all tabs
        tabSeedSelection.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
        tabFertilizer.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
        tabCropCalendar.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));

        indicatorSeedSelection.setVisibility(View.GONE);
        indicatorFertilizer.setVisibility(View.GONE);
        indicatorCropCalendar.setVisibility(View.GONE);

        // Set active tab
        if (activeStep == 0) {
            tabSeedSelection.setTextColor(ContextCompat.getColor(this, R.color.orange));
            indicatorSeedSelection.setVisibility(View.VISIBLE);
        } else if (activeStep == 1) {
            tabFertilizer.setTextColor(ContextCompat.getColor(this, R.color.orange));
            indicatorFertilizer.setVisibility(View.VISIBLE);
        } else if (activeStep == 2) {
            tabCropCalendar.setTextColor(ContextCompat.getColor(this, R.color.orange));
            indicatorCropCalendar.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateStep1() {
        String variety = etRiceVariety.getText() != null ? etRiceVariety.getText().toString().trim() : "";
        String plantingDate = tvPlantingDate.getText().toString();
        String seedWeight = etSeedWeight.getText() != null ? etSeedWeight.getText().toString().trim() : "";

        if (variety.isEmpty()) {
            showSnackBar("Ilalagay ang barayti ng palay.", false, null);
            return false;
        }
        if (!rbSabogTanim.isChecked() && !rbLipatTanim.isChecked()) {
            showSnackBar("Piliin ang paraan ng pagtatanim.", false, null);
            return false;
        }
        if (plantingDate == null || plantingDate.isEmpty() || plantingDate.equals("Piliin ang petsa")) {
            showSnackBar("Piliin ang petsa ng pagtatanim.", false, null);
            return false;
        }
        if (seedWeight.isEmpty()) {
            showSnackBar("Ilalagay ang kabuuang timbang ng binhi.", false, null);
            return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (rbAbonongSwak.isChecked()) {
            if (!rbCombo1.isChecked() && !rbCombo2.isChecked() && !rbCombo3.isChecked() && !rbCombo4.isChecked()) {
                showSnackBar("Piliin ang combo ng abono.", false, null);
                return false;
            }
        }
        return true;
    }
    
    private void deletePlanting() {
        if (plantingId == null || plantingId.isEmpty() || riceFieldId == null || riceFieldId.isEmpty()) {
            showSnackBar("Hindi ma-tanggal ang taniman.", false, null);
            return;
        }
        
        JournalStorageHelper.deleteRicePlanting(this, riceFieldId, plantingId, new JournalStorageHelper.OnDeleteListener() {
            @Override
            public void onSuccess() {
                showSnackBar("Matagumpay na natanggal ang taniman!", true, AddPlantingActivity.this::finish);
            }

            @Override
            public void onFailure(String error) {
                showSnackBar("Hindi natanggal: " + error, false, null);
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String displayDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    tvPlantingDate.setText(displayDate);
                    // Regenerate tasks if we're on crop calendar step
                    if (currentStep == 2) {
                        generateCropCalendarTasks();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void generateCropCalendarTasks() {
        android.util.Log.d("CropCalendar", "generateCropCalendarTasks() called");
        String plantingDateStr = tvPlantingDate.getText().toString();
        android.util.Log.d("CropCalendar", "Planting date string: '" + plantingDateStr + "'");
        if (plantingDateStr == null || plantingDateStr.isEmpty() || plantingDateStr.equals("Piliin ang petsa")) {
            android.util.Log.d("CropCalendar", "Early return: No planting date set");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date plantingDate = sdf.parse(plantingDateStr);
            if (plantingDate == null) return;

            Calendar plantingCal = Calendar.getInstance();
            plantingCal.setTime(plantingDate);

            cropCalendarTasks.clear();
            List<WeekTaskTemplate> weekTemplates = buildWeekTaskTemplates();
            int plantingWeekIndex = 1;
            for (int i = 0; i < weekTemplates.size(); i++) {
                if (weekTemplates.get(i).isPlantingWeek()) {
                    plantingWeekIndex = i + 1;
                    break;
                }
            }
            
            // Anchor the planting week to the selected planting date (adjusted to Monday)
            Calendar anchorWeekStart = (Calendar) plantingCal.clone();
            int dayOfWeek = anchorWeekStart.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
            anchorWeekStart.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
            
            // Ensure we're in preview mode when generating new tasks
            // This ensures all tasks are created as new, not updated
            boolean wasPreviewMode = isPreviewMode;
            boolean isNewPlanting = (plantingId == null || plantingId.isEmpty());
            isPreviewMode = true; // Force preview mode during generation to always create new tasks
            
            for (int weekIndex = 0; weekIndex < weekTemplates.size(); weekIndex++) {
                WeekTaskTemplate template = weekTemplates.get(weekIndex);
                int weekNumber = weekIndex + 1;
                int offsetFromPlantingWeek = weekNumber - plantingWeekIndex;
                Calendar weekStart = (Calendar) anchorWeekStart.clone();
                weekStart.add(Calendar.DAY_OF_YEAR, offsetFromPlantingWeek * 7);
                List<TaskTemplate> tasksForWeek = template.getTasks();
                for (int taskIndex = 0; taskIndex < tasksForWeek.size(); taskIndex++) {
                    TaskTemplate taskTemplate = tasksForWeek.get(taskIndex);
                    addWeekTask(weekStart, weekNumber, taskIndex, taskTemplate.getName(), taskTemplate.getType());
                }
            }

            sortCropCalendarTasks();
            
            // Debug log - check how many tasks were actually added
            android.util.Log.d("CropCalendar", "Generated " + cropCalendarTasks.size() + " tasks across " + weekTemplates.size() + " weeks");
            for (CropCalendarTask task : cropCalendarTasks) {
                android.util.Log.d("CropCalendar", "Week " + task.getWeekNumber() + " (" + task.getTaskOrder() + "): " + task.getTaskName());
            }
            
            // Restore preview mode state - but keep preview mode for new plantings
            if (isNewPlanting) {
                // New planting - always use preview mode
                isPreviewMode = true;
            } else {
                // Editing - restore original state
                isPreviewMode = wasPreviewMode;
            }

            displayCropCalendarTasks();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayCropCalendarTasks() {
        if (cropCalendarTasks == null) {
            cropCalendarTasks = new ArrayList<>();
        }
        sortCropCalendarTasks();
        if (isPreviewMode) {
            if (rvCropCalendar.getAdapter() != cropCalendarPreviewAdapter) {
                rvCropCalendar.setAdapter(cropCalendarPreviewAdapter);
            }
            cropCalendarPreviewAdapter.updateTasks(cropCalendarTasks);
        } else {
            if (rvCropCalendar.getAdapter() != cropCalendarAdapter) {
                rvCropCalendar.setAdapter(cropCalendarAdapter);
            }
            cropCalendarAdapter.updateTasks(cropCalendarTasks);
        }

        rvCropCalendar.post(() -> {
            if (rvCropCalendar instanceof NonScrollableRecyclerView) {
                ((NonScrollableRecyclerView) rvCropCalendar).forceMeasure();
            }
            rvCropCalendar.requestLayout();
        });
    }

    private void addWeekTask(Calendar weekStart, int weekNumber, int taskOrder, String taskName, String taskType) {
        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        // Format: "Nov 18–24, 2025"
        String weekRange = monthFormat.format(weekStart.getTime()) + " " + 
                          dayFormat.format(weekStart.getTime()) + "–" + 
                          dayFormat.format(weekEnd.getTime()) + ", " + 
                          yearFormat.format(weekStart.getTime());

        // Use middle of week as scheduled date
        Calendar taskDate = (Calendar) weekStart.clone();
        taskDate.add(Calendar.DAY_OF_YEAR, 3); // Wednesday of the week
        String scheduledDate = sdf.format(taskDate.getTime());

        // Check if task already exists (when editing) - only check if we're not in preview mode
        CropCalendarTask existingTask = null;
        if (!isPreviewMode) {
            for (CropCalendarTask t : cropCalendarTasks) {
                if (t.getWeekNumber() == weekNumber && t.getTaskName().equalsIgnoreCase(taskName)) {
                    existingTask = t;
                    break;
                }
            }
        }

        CropCalendarTask task;
        if (existingTask != null) {
            // Update existing task dates but keep completion status
            task = existingTask;
            task.setScheduledDate(scheduledDate);
            task.setWeekRange(weekRange);
            task.setTaskName(taskName);
            task.setTaskType(taskType);
            task.setWeekNumber(weekNumber);
            android.util.Log.d("CropCalendar", "Updated existing task for week " + weekNumber);
        } else {
            // Create new task - always add new task in preview mode
            task = new CropCalendarTask(taskName, taskName, 0, taskType);
            task.setScheduledDate(scheduledDate);
            task.setWeekRange(weekRange);
            task.setWeekNumber(weekNumber);
            task.setTaskOrder(taskOrder);
            task.setTaskType(taskType);
            cropCalendarTasks.add(task);
            android.util.Log.d("CropCalendar", "Added new task for week " + weekNumber + " (" + taskName + "). Total tasks: " + cropCalendarTasks.size());
            return;
        }

        task.setTaskOrder(taskOrder);
        task.setTaskCategory(getCategoryLabel(taskType));
    }

    private void initTaskCategoryOptions() {
        if (!categoryTaskOptions.isEmpty()) {
            return;
        }

        for (String category : TASK_CATEGORY_ORDER) {
            if (!categoryTaskOptions.containsKey(category)) {
                categoryTaskOptions.put(category, new ArrayList<>());
            }
        }

        List<WeekTaskTemplate> templates = buildWeekTaskTemplates();
        for (WeekTaskTemplate template : templates) {
            for (TaskTemplate taskTemplate : template.getTasks()) {
                String category = getCategoryLabel(taskTemplate.getType());
                List<String> tasks = categoryTaskOptions.get(category);
                if (tasks == null) {
                    tasks = new ArrayList<>();
                    categoryTaskOptions.put(category, tasks);
                }
                if (!tasks.contains(taskTemplate.getName())) {
                    tasks.add(taskTemplate.getName());
                }
            }
        }
    }

    private void fillTaskNameOptions(List<String> targetList, ArrayAdapter<String> adapter, String category, String fallbackName) {
        targetList.clear();
        if (!TextUtils.isEmpty(category)) {
            List<String> predefined = categoryTaskOptions.get(category);
            if (predefined != null) {
                targetList.addAll(predefined);
            }
        }
        if (!TextUtils.isEmpty(fallbackName) && !targetList.contains(fallbackName)) {
            targetList.add(0, fallbackName);
        }
        adapter.notifyDataSetChanged();
    }

    private String getCategoryLabel(String taskType) {
        if (taskType == null) {
            return "Iba pang gawain";
        }
        switch (taskType) {
            case "preparation":
            case "land_preparation":
                return "Paghahanda ng lupa";
            case "seed":
            case "planting":
                return "Pagtatanim";
            case "fertilizer":
                return "Pagpapataba";
            case "irrigation":
                return "Pagpapatubig";
            case "weed_control":
                return "Pamamahala sa damo";
            case "pest_control":
                return "Pamamahala sa peste";
            case "harvest":
            case "harvest_prep":
                return "Pag-aani at paghahanda";
            default:
                return "Iba pang gawain";
        }
    }

    private void showTaskCompletionDialog(CropCalendarTask task) {
        if (task == null) {
            return;
        }
        initTaskCategoryOptions();

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_crop_task_completion, null);
        AutoCompleteTextView actCategory = dialogView.findViewById(R.id.actTaskCategory);
        AutoCompleteTextView actTaskName = dialogView.findViewById(R.id.actTaskName);
        TextInputLayout layoutTaskCategory = dialogView.findViewById(R.id.layoutTaskCategory);
        TextInputLayout layoutTaskName = dialogView.findViewById(R.id.layoutTaskName);
        TextInputLayout layoutTaskDate = dialogView.findViewById(R.id.layoutTaskDate);
        TextInputEditText etTaskDetails = dialogView.findViewById(R.id.etTaskDetails);
        TextInputEditText etTaskDate = dialogView.findViewById(R.id.etTaskDate);
        MaterialButton btnKeycheck = dialogView.findViewById(R.id.btnKeycheck);
        MaterialButton btnSaveTask = dialogView.findViewById(R.id.btnSaveTask);
        ImageView ivCloseDialog = dialogView.findViewById(R.id.ivCloseDialog);

        List<String> categoryOptions = new ArrayList<>(categoryTaskOptions.keySet());
        String defaultCategory = !TextUtils.isEmpty(task.getTaskCategory()) ? task.getTaskCategory() : getCategoryLabel(task.getTaskType());
        if (!TextUtils.isEmpty(defaultCategory) && !categoryOptions.contains(defaultCategory)) {
            categoryOptions.add(0, defaultCategory);
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryOptions);
        actCategory.setAdapter(categoryAdapter);
        actCategory.setOnClickListener(v -> actCategory.showDropDown());
        if (!TextUtils.isEmpty(defaultCategory)) {
            actCategory.setText(defaultCategory, false);
        }

        List<String> taskNameOptions = new ArrayList<>();
        ArrayAdapter<String> taskNameAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskNameOptions);
        actTaskName.setAdapter(taskNameAdapter);
        actTaskName.setOnClickListener(v -> actTaskName.showDropDown());
        fillTaskNameOptions(taskNameOptions, taskNameAdapter, defaultCategory, task.getTaskName());
        if (!TextUtils.isEmpty(task.getTaskName())) {
            actTaskName.setText(task.getTaskName(), false);
        } else if (!taskNameOptions.isEmpty()) {
            actTaskName.setText(taskNameOptions.get(0), false);
        }

        if (!TextUtils.isEmpty(task.getAdditionalNotes())) {
            etTaskDetails.setText(task.getAdditionalNotes());
        }

        String initialDate = !TextUtils.isEmpty(task.getActualCompletionDate())
                ? task.getActualCompletionDate()
                : task.getScheduledDate();
        if (TextUtils.isEmpty(initialDate)) {
            initialDate = isoDateFormat.format(new Date());
        }
        etTaskDate.setText(initialDate);

        updateKeycheckButtonLabel(btnKeycheck, defaultCategory);

        actCategory.setOnItemClickListener((parent, view, position, id) -> {
            layoutTaskCategory.setError(null);
            String selectedCategory = parent.getItemAtPosition(position).toString();
            fillTaskNameOptions(taskNameOptions, taskNameAdapter, selectedCategory, task.getTaskName());
            if (!taskNameOptions.isEmpty()) {
                actTaskName.setText(taskNameOptions.get(0), false);
            } else {
                actTaskName.setText("", false);
            }
            updateKeycheckButtonLabel(btnKeycheck, selectedCategory);
        });

        actTaskName.setOnItemClickListener((parent, view, position, id) -> layoutTaskName.setError(null));

        View.OnClickListener dateClickListener = v -> {
            Calendar calendar = Calendar.getInstance();
            String current = etTaskDate.getText() != null ? etTaskDate.getText().toString() : "";
            if (!TextUtils.isEmpty(current)) {
                try {
                    Date parsed = isoDateFormat.parse(current);
                    if (parsed != null) {
                        calendar.setTime(parsed);
                    }
                } catch (Exception ignored) { }
            }

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        String newDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        etTaskDate.setText(newDate);
                        layoutTaskDate.setError(null);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        };

        etTaskDate.setOnClickListener(dateClickListener);
        layoutTaskDate.setEndIconOnClickListener(dateClickListener);

        btnKeycheck.setOnClickListener(v -> {
            String selectedCategory = actCategory.getText() != null ? actCategory.getText().toString().trim() : "";
            if (TextUtils.isEmpty(selectedCategory)) {
                layoutTaskCategory.setError("Piliin ang kategorya");
                actCategory.requestFocus();
                return;
            }
            String selectedTask = actTaskName.getText() != null ? actTaskName.getText().toString().trim() : task.getTaskName();
            showKeycheckDialog(selectedCategory, selectedTask);
        });
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            
            // Set layout parameters to center the dialog
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            int dialogWidth = (int) (screenWidth * 0.9); // 90% of screen width
            
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = dialogWidth;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = android.view.Gravity.CENTER; // Center the dialog
            layoutParams.verticalMargin = 0.05f; // 5% margin from top and bottom
            window.setAttributes(layoutParams);
            
            // Dim the background
            window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.5f); // 50% dim
        }
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        ivCloseDialog.setOnClickListener(v -> dialog.dismiss());

        btnSaveTask.setOnClickListener(v -> {
            String selectedCategory = actCategory.getText() != null ? actCategory.getText().toString().trim() : "";
            String selectedTaskName = actTaskName.getText() != null ? actTaskName.getText().toString().trim() : "";
            String details = etTaskDetails.getText() != null ? etTaskDetails.getText().toString().trim() : "";
            String dateValue = etTaskDate.getText() != null ? etTaskDate.getText().toString().trim() : "";

            boolean hasError = false;
            if (TextUtils.isEmpty(selectedCategory)) {
                layoutTaskCategory.setError("Piliin ang kategorya");
                hasError = true;
            } else {
                layoutTaskCategory.setError(null);
            }

            if (TextUtils.isEmpty(selectedTaskName)) {
                layoutTaskName.setError("Piliin ang gawain");
                hasError = true;
            } else {
                layoutTaskName.setError(null);
            }

            if (TextUtils.isEmpty(dateValue)) {
                layoutTaskDate.setError("Piliin ang petsa");
                hasError = true;
            } else {
                layoutTaskDate.setError(null);
            }

            if (hasError) {
                return;
            }

            task.setTaskCategory(selectedCategory);
            if (!TextUtils.isEmpty(selectedTaskName)) {
                task.setTaskName(selectedTaskName);
            }
            task.setAdditionalNotes(details);
            task.setActualCompletionDate(dateValue);
            task.setCompleted(true);

            displayCropCalendarTasks();
            updateCropCalendarTasks();
            Toast.makeText(this, "Na-save ang detalye ng gawain.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void handleTaskUncheck(CropCalendarTask task) {
        if (task == null) {
            return;
        }
        task.setCompleted(false);
        task.setActualCompletionDate("");
        task.setAdditionalNotes("");

        displayCropCalendarTasks();
        updateCropCalendarTasks();
        Toast.makeText(this, "Tinanggal ang pagkaka-check ng gawain.", Toast.LENGTH_SHORT).show();
    }

    private void updateKeycheckButtonLabel(MaterialButton button, String category) {
        if (button == null) {
            return;
        }
        String labelCategory = TextUtils.isEmpty(category)
                ? "GAWAIN"
                : category.toUpperCase(Locale.getDefault());
        button.setText("BASAHIN ANG KEYCHECK SA " + labelCategory);
    }

    private void showKeycheckDialog(String category, String taskName) {
        String safeCategory = TextUtils.isEmpty(category) ? "gawain" : category;
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Mga susi at paalala para sa ");
        messageBuilder.append(safeCategory);
        if (!TextUtils.isEmpty(taskName)) {
            messageBuilder.append("\n\nGawain: ").append(taskName);
        }
        messageBuilder.append("\n\nIlalagay dito ang kumpletong keycheck guide. Samantala, siguraduhing sundin ang mga rekomendasyon ng Sistemang PalaYan.");

        new AlertDialog.Builder(this)
                .setTitle("Keycheck Guide")
                .setMessage(messageBuilder.toString())
                .setPositiveButton("Sige", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateCropCalendarTasks() {
        // Tasks are updated automatically through adapter listener
        // Save tasks immediately when checked
        if (plantingId != null && !plantingId.isEmpty() && riceFieldId != null && !riceFieldId.isEmpty()) {
            // Load current planting, update tasks, and save
            JournalStorageHelper.loadRicePlantings(this, riceFieldId, new JournalStorageHelper.OnPlantingsLoadedListener() {
                @Override
                public void onSuccess(List<RicePlanting> plantings) {
                    for (RicePlanting p : plantings) {
                        if (p.getId().equals(plantingId)) {
                            p.setCropCalendarTasks(cropCalendarTasks);
                            JournalStorageHelper.saveRicePlanting(AddPlantingActivity.this, riceFieldId, p, null);
                            break;
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    // Ignore error
                }
            });
        }
    }

    private void sortCropCalendarTasks() {
        if (cropCalendarTasks == null || cropCalendarTasks.isEmpty()) {
            return;
        }
        cropCalendarTasks.sort((t1, t2) -> {
            int weekCompare = Integer.compare(t1.getWeekNumber(), t2.getWeekNumber());
            if (weekCompare != 0) {
                return weekCompare;
            }
            return Integer.compare(t1.getTaskOrder(), t2.getTaskOrder());
        });
    }

    private boolean hasCompleteCalendar(List<CropCalendarTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return false;
        }
        boolean[] weeksCovered = new boolean[TOTAL_CALENDAR_WEEKS];
        for (CropCalendarTask task : tasks) {
            int weekNum = task.getWeekNumber();
            if (weekNum >= 1 && weekNum <= TOTAL_CALENDAR_WEEKS) {
                weeksCovered[weekNum - 1] = true;
            }
        }
        for (boolean covered : weeksCovered) {
            if (!covered) {
                return false;
            }
        }
        return true;
    }

    private void savePlanting() {
        boolean isEditing = plantingId != null && !plantingId.isEmpty();
        boolean isOnCropCalendarStep = currentStep == 2;
        
        // Validate step 1 (seed selection) - always required
        if (!validateStep1()) {
            return;
        }
        
        // Only validate fertilizer if:
        // 1. It's a new planting (not editing), OR
        // 2. We're not on crop calendar step when saving
        // If editing and on crop calendar step, skip fertilizer validation (just updating tasks)
        if (!(isEditing && isOnCropCalendarStep)) {
            if (!validateStep2()) {
                return;
            }
        }

        String variety = etRiceVariety.getText() != null ? etRiceVariety.getText().toString().trim() : "";
        String plantingDate = tvPlantingDate.getText().toString();
        String seedWeight = etSeedWeight.getText() != null ? etSeedWeight.getText().toString().trim() : "";

        RicePlanting planting = new RicePlanting();
        planting.setRiceFieldId(riceFieldId);
        planting.setRiceVarietyName(variety);
        planting.setPlantingDate(plantingDate);
        planting.setSeedWeight(seedWeight);
        planting.setPlantingMethod(rbSabogTanim.isChecked() ? "Sabog-tanim" : "Lipat-tanim");
        
        // Fertilizer info - if editing and on crop calendar step, load existing fertilizer info
        if (isEditing && isOnCropCalendarStep) {
            // Load existing planting to get fertilizer info
            JournalStorageHelper.loadRicePlantings(this, riceFieldId, new JournalStorageHelper.OnPlantingsLoadedListener() {
                @Override
                public void onSuccess(List<RicePlanting> plantings) {
                    for (RicePlanting p : plantings) {
                        if (p.getId().equals(plantingId)) {
                            // Use existing fertilizer info
                            planting.setFertilizerStrategy(p.getFertilizerStrategy());
                            planting.setFertilizerCombo(p.getFertilizerCombo());
                            // Continue with save
                            continueSave(planting);
                            break;
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    // If loading fails, try to get fertilizer from UI
                    setFertilizerInfo(planting);
                    continueSave(planting);
                }
            });
            return; // Exit early, continueSave will be called from callback
        } else {
            // New planting or not on crop calendar step - get fertilizer from UI
            setFertilizerInfo(planting);
        }
        
        continueSave(planting);
    }
    
    private void setFertilizerInfo(RicePlanting planting) {
        if (rbAbonongSwak.isChecked()) {
            planting.setFertilizerStrategy("Abonong Swak");
            if (rbCombo1.isChecked()) planting.setFertilizerCombo("Combo 1 (3-4 tons)");
            else if (rbCombo2.isChecked()) planting.setFertilizerCombo("Combo 2 (5-6 tons)");
            else if (rbCombo3.isChecked()) planting.setFertilizerCombo("Combo 3 (7-8 tons)");
            else if (rbCombo4.isChecked()) planting.setFertilizerCombo("Combo 4 + Biofertilizer (6-7 tons)");
        } else {
            planting.setFertilizerStrategy("Sariling diskarte");
        }
    }
    
    private void continueSave(RicePlanting planting) {
        planting.setNotes(""); // Notes are now part of history
        
        // Set crop calendar tasks
        if (cropCalendarTasks.isEmpty()) {
            generateCropCalendarTasks();
        }
        planting.setCropCalendarTasks(cropCalendarTasks);
        
        if (plantingId != null && !plantingId.isEmpty()) {
            planting.setId(plantingId);
        }

        JournalStorageHelper.saveRicePlanting(this, riceFieldId, planting, new JournalStorageHelper.OnSaveListener() {
            @Override
            public void onSuccess() {
                showSnackBar("Matagumpay na na-save ang taniman!", true, AddPlantingActivity.this::finish);
            }

            @Override
            public void onFailure(String error) {
                showSnackBar("Hindi na-save: " + error, false, null);
            }
        });
    }


    private void populateExistingPlanting() {
        boolean isEditing = plantingId != null && !plantingId.isEmpty();
        tvRemove.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        
        // Switch to interactive mode when editing
        if (isEditing) {
            isPreviewMode = false;
            rvCropCalendar.setAdapter(cropCalendarAdapter);
        }

        if (!isEditing) {
            return;
        }

        tvTitle.setText("I-EDIT ANG TANIMAN");
        btnSave.setText("I-UPDATE ANG TANIMAN");

        if (existingVariety != null && !existingVariety.isEmpty()) {
            etRiceVariety.setText(existingVariety, false);
        }
        if (existingMethod != null && !existingMethod.isEmpty()) {
            if ("Sabog-tanim".equalsIgnoreCase(existingMethod)) {
                rbSabogTanim.setChecked(true);
            } else if ("Lipat-tanim".equalsIgnoreCase(existingMethod)) {
                rbLipatTanim.setChecked(true);
            }
        }
        if (existingDate != null && !existingDate.isEmpty()) {
            tvPlantingDate.setText(existingDate);
        }
        if (existingSeedWeight != null && !existingSeedWeight.isEmpty()) {
            etSeedWeight.setText(existingSeedWeight);
        }
        // Load existing crop calendar tasks and fertilizer info if editing
        if (plantingId != null && !plantingId.isEmpty()) {
            JournalStorageHelper.loadRicePlantings(this, riceFieldId, new JournalStorageHelper.OnPlantingsLoadedListener() {
                @Override
                public void onSuccess(List<RicePlanting> plantings) {
                    for (RicePlanting p : plantings) {
                        if (p.getId().equals(plantingId)) {
                            // Restore fertilizer strategy and combo
                            if (p.getFertilizerStrategy() != null) {
                                if ("Abonong Swak".equalsIgnoreCase(p.getFertilizerStrategy())) {
                                    rbAbonongSwak.setChecked(true);
                                    rgFertilizerCombo.setVisibility(View.VISIBLE);
                                    if (p.getFertilizerCombo() != null) {
                                        if (p.getFertilizerCombo().contains("Combo 1")) {
                                            rbCombo1.setChecked(true);
                                        } else if (p.getFertilizerCombo().contains("Combo 2")) {
                                            rbCombo2.setChecked(true);
                                        } else if (p.getFertilizerCombo().contains("Combo 3")) {
                                            rbCombo3.setChecked(true);
                                        } else if (p.getFertilizerCombo().contains("Combo 4")) {
                                            rbCombo4.setChecked(true);
                                        }
                                        updateFertilizerScheduleTable();
                                    }
                                } else if ("Sariling diskarte".equalsIgnoreCase(p.getFertilizerStrategy())) {
                                    rbSarilingDiskarte.setChecked(true);
                                    layoutCustomFertilizer.setVisibility(View.GONE);
                                    showSarilingDiskarteSchedule();
                                }
                            }
                            
                            if (p.getCropCalendarTasks() != null && !p.getCropCalendarTasks().isEmpty()) {
                                cropCalendarTasks = p.getCropCalendarTasks();
                                android.util.Log.d("CropCalendar", "Loaded " + cropCalendarTasks.size() + " existing tasks from storage");
                                
                                // Check if we have all weeks, if not, regenerate
                                if (!hasCompleteCalendar(cropCalendarTasks) && p.getPlantingDate() != null && !p.getPlantingDate().isEmpty()) {
                                    android.util.Log.d("CropCalendar", "Incomplete tasks found (" + cropCalendarTasks.size() + "), regenerating full schedule");
                                    tvPlantingDate.setText(p.getPlantingDate());
                                    generateCropCalendarTasks();
                                } else {
                                    // Switch to interactive mode
                                    isPreviewMode = false;
                                    rvCropCalendar.setAdapter(cropCalendarAdapter);
                                    displayCropCalendarTasks();
                                }
                            } else if (p.getPlantingDate() != null && !p.getPlantingDate().isEmpty()) {
                                // No tasks saved yet, generate them
                                android.util.Log.d("CropCalendar", "No tasks found, generating new ones");
                                tvPlantingDate.setText(p.getPlantingDate());
                                generateCropCalendarTasks();
                            }
                            break;
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    // Ignore error for now
                }
            });
        }
    }

    private void showDeleteConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmation, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        MaterialButton btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        MaterialButton btnDialogConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        tvDialogTitle.setText("Tanggalin ang Taniman");
        tvDialogMessage.setText("Sigurado ka bang gusto mong tanggalin ang taniman na ito?");
        btnDialogConfirm.setText("Tanggalin");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        btnDialogConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            deletePlanting();
        });

        dialog.show();
    }

    private void hideAllScheduleTables() {
        layoutFertilizerScheduleTableCombo1.setVisibility(View.GONE);
        layoutFertilizerScheduleTableCombo2.setVisibility(View.GONE);
        layoutFertilizerScheduleTableCombo3.setVisibility(View.GONE);
        layoutFertilizerScheduleTableCombo4.setVisibility(View.GONE);
        layoutFertilizerScheduleTableSarilingDiskarte.setVisibility(View.GONE);
    }

    private void updateFertilizerScheduleTable() {
        if (!rbAbonongSwak.isChecked()) {
            hideAllScheduleTables();
            return;
        }

        // Hide all schedule tables first
        hideAllScheduleTables();

        List<FertilizerScheduleEntry> schedule = null;
        View targetTable = null;
        LinearLayout targetTableRows = null;

        if (rbCombo1.isChecked()) {
            schedule = getCombo1Schedule();
            targetTable = layoutFertilizerScheduleTableCombo1;
            targetTableRows = layoutFertilizerScheduleTableCombo1.findViewById(R.id.layoutTableRows);
        } else if (rbCombo2.isChecked()) {
            schedule = getCombo2Schedule();
            targetTable = layoutFertilizerScheduleTableCombo2;
            targetTableRows = layoutFertilizerScheduleTableCombo2.findViewById(R.id.layoutTableRows);
        } else if (rbCombo3.isChecked()) {
            schedule = getCombo3Schedule();
            targetTable = layoutFertilizerScheduleTableCombo3;
            targetTableRows = layoutFertilizerScheduleTableCombo3.findViewById(R.id.layoutTableRows);
        } else if (rbCombo4.isChecked()) {
            schedule = getCombo4Schedule();
            targetTable = layoutFertilizerScheduleTableCombo4;
            targetTableRows = layoutFertilizerScheduleTableCombo4.findViewById(R.id.layoutTableRows);
        }

        if (schedule != null && !schedule.isEmpty() && targetTable != null && targetTableRows != null) {
            populateFertilizerTable(schedule, targetTableRows);
            targetTable.setVisibility(View.VISIBLE);
        }
    }

    private void showSarilingDiskarteSchedule() {
        List<FertilizerScheduleEntry> schedule = getSarilingDiskarteSchedule();
        if (schedule != null && !schedule.isEmpty()) {
            LinearLayout targetTableRows = layoutFertilizerScheduleTableSarilingDiskarte.findViewById(R.id.layoutTableRows);
            populateFertilizerTable(schedule, targetTableRows);
            layoutFertilizerScheduleTableSarilingDiskarte.setVisibility(View.VISIBLE);
        } else {
            layoutFertilizerScheduleTableSarilingDiskarte.setVisibility(View.GONE);
        }
    }

    private List<FertilizerScheduleEntry> getSarilingDiskarteSchedule() {
        List<FertilizerScheduleEntry> schedule = new ArrayList<>();
        schedule.add(new FertilizerScheduleEntry("Punlaan", "Zinc sulfate at 14-14-14", "7-10 DAS"));
        schedule.add(new FertilizerScheduleEntry("Ika-1", "14-14-14", "10-14 DAT"));
        schedule.add(new FertilizerScheduleEntry("Ika-2", "46-0-0", "24-28 DAT"));
        schedule.add(new FertilizerScheduleEntry("Ika-3", "46-0-0", "40-45 DAT"));
        return schedule;
    }

    private void populateFertilizerTable(List<FertilizerScheduleEntry> schedule, LinearLayout targetTableRows) {
        if (targetTableRows == null) {
            targetTableRows = layoutTableRows;
        }
        targetTableRows.removeAllViews();

        for (FertilizerScheduleEntry entry : schedule) {
            View rowView = LayoutInflater.from(this).inflate(R.layout.item_fertilizer_table_row, targetTableRows, false);
            TextView tvFertilizer = rowView.findViewById(R.id.tvFertilizer);
            TextView tvTiming = rowView.findViewById(R.id.tvTiming);

            tvFertilizer.setText(entry.getFertilizer());
            tvTiming.setText(entry.getTiming());

            targetTableRows.addView(rowView);
        }
    }

    private List<FertilizerScheduleEntry> getCombo1Schedule() {
        List<FertilizerScheduleEntry> schedule = new ArrayList<>();
        schedule.add(new FertilizerScheduleEntry("Punlaan", "73-146 kg 14-14-14", "10-14 DAS"));
        schedule.add(new FertilizerScheduleEntry("Ika-1", "54.75 sako 14-14-14", "0-14 DAT"));
        schedule.add(new FertilizerScheduleEntry("Ika-2", "36.5 sako 46-0-0 at 18.25 bag 0-0-60", "36-40 DAT"));
        return schedule;
    }

    private List<FertilizerScheduleEntry> getCombo2Schedule() {
        List<FertilizerScheduleEntry> schedule = new ArrayList<>();
        schedule.add(new FertilizerScheduleEntry("Punlaan", "36.5-73 kg zinc sulfate", "7-10 DAS"));
        schedule.add(new FertilizerScheduleEntry("Punlaan", "73-146 kg 14-14-14", "10-14 DAS"));
        schedule.add(new FertilizerScheduleEntry("Ika-1", "73 sako 14-14-14 o 16-20-0", "0-14 DAT"));
        schedule.add(new FertilizerScheduleEntry("Ika-2", "36.5 sako 46-0-0", "26-31 DAT"));
        schedule.add(new FertilizerScheduleEntry("Ika-3", "36.5 sako 46-0-0 at 18.25 bag 0-0-60", "36-40 DAT"));
        return schedule;
    }

    private List<FertilizerScheduleEntry> getCombo3Schedule() {
        List<FertilizerScheduleEntry> schedule = new ArrayList<>();
        schedule.add(new FertilizerScheduleEntry("Punlaan", "36.5-73 kg zinc sulfate", "7-10 DAS"));
        schedule.add(new FertilizerScheduleEntry("Punlaan", "146 kg 14-14-14", "10-14 DAS"));
        schedule.add(new FertilizerScheduleEntry("Ika-1", "146 sako 14-14-14", "0-14 DAT"));
        schedule.add(new FertilizerScheduleEntry("Ika-2", "73 sako 46-0-0", "26-31 DAT"));
        schedule.add(new FertilizerScheduleEntry("Ika-3", "73 sako 46-0-0 at 36.5 bag 0-0-60", "36-40 DAT"));
        return schedule;
    }

    private List<FertilizerScheduleEntry> getCombo4Schedule() {
        List<FertilizerScheduleEntry> schedule = new ArrayList<>();
        schedule.add(new FertilizerScheduleEntry("Biofertilizer", "I-apply ayon sa manufacturer", "--"));
        schedule.add(new FertilizerScheduleEntry("Punlaan", "36.5-73 kg ZnSO4", "7-10 DAS"));
        schedule.add(new FertilizerScheduleEntry("Punlaan", "146 kg 14-14-14", "10-14 DAS"));
        schedule.add(new FertilizerScheduleEntry("Ika-1", "109.5 bag 16-20-0", "0-14 DAT"));
        schedule.add(new FertilizerScheduleEntry("Ika-2", "36.5 bag 46-0-0", "26-31 DAT"));
        schedule.add(new FertilizerScheduleEntry("Ika-3", "36.5 bag 46-0-0 at 18.25 bag 0-0-60", "36-40 DAT"));
        return schedule;
    }

    private void showSnackBar(String message, boolean isSuccess, @Nullable Runnable onDismiss) {
        View root = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        int backgroundColor = ContextCompat.getColor(this, isSuccess ? R.color.green : R.color.dark_red);
        snackbar.setBackgroundTint(backgroundColor);
        snackbar.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins__regular));
        }

        if (onDismiss != null) {
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    onDismiss.run();
                }
            });
        }

        snackbar.show();
    }
}

