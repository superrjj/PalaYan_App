package com.example.palayan.UserActivities;

import android.app.DatePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.example.palayan.Adapter.TaskHistoryAdapter;
import com.example.palayan.Helper.CropCalendarTask;
import com.example.palayan.Helper.FertilizerScheduleEntry;
import com.example.palayan.Helper.NonScrollableRecyclerView;
import com.example.palayan.Helper.RicePlanting;
import com.example.palayan.Helper.JournalStorageHelper;
import com.example.palayan.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
import java.util.TimeZone;

public class AddPlantingActivity extends AppCompatActivity {

    private static final int TOTAL_CALENDAR_WEEKS = 17;

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
    private TextInputEditText etSeedWeight, etAreaHectares;
    private TextView tvTotalKilograms, tvTotalSacks;
    private RadioGroup rgPlantingMethod, rgFertilizerStrategy, rgFertilizerCombo;
    private RadioButton rbSabogTanim, rbLipatTanim, rbAbonongSwak, rbSarilingDiskarte;
    private RadioButton rbCombo1, rbCombo2, rbCombo3, rbCombo4;
    private Button btnSave, btnNext, btnBack;
    private RecyclerView rvCropCalendar, rvFertilizerSchedule;
    private MaterialCardView cardCurrentWeekTasks;
    private LinearLayout layoutCurrentWeekTasks, layoutFullCalendar, layoutTaskHistory, layoutCalendarButtons;
    private TextView tvCurrentWeekRange, tvCurrentWeekEmpty, tvShowFullCalendar, tvHideFullCalendar;
    private TextView tvShowTaskHistory, tvHideTaskHistory, tvHistoryEmpty;
    private NonScrollableRecyclerView rvTaskHistory;
    private TaskHistoryAdapter taskHistoryAdapter;
    private boolean isHistoryVisible = false;
    private boolean isFullCalendarVisible = false;
    private int highlightedWeekNumber = 1;
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
    private String initialFertilizerStrategy;
    private String initialFertilizerCombo;
    private List<CropCalendarTask> initialCropCalendarTasks = new ArrayList<>();

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
        etAreaHectares = findViewById(R.id.etAreaHectares);
        tvTotalKilograms = findViewById(R.id.tvTotalKilograms);
        tvTotalSacks = findViewById(R.id.tvTotalSacks);
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

        cardCurrentWeekTasks = findViewById(R.id.cardCurrentWeekTasks);
        layoutCurrentWeekTasks = findViewById(R.id.layoutCurrentWeekTasks);
        tvCurrentWeekRange = findViewById(R.id.tvCurrentWeekRange);
        tvCurrentWeekEmpty = findViewById(R.id.tvCurrentWeekEmpty);
        layoutFullCalendar = findViewById(R.id.layoutFullCalendar);
        layoutCalendarButtons = findViewById(R.id.layoutCalendarButtons);
        tvShowFullCalendar = findViewById(R.id.tvShowFullCalendar);
        tvHideFullCalendar = findViewById(R.id.tvHideFullCalendar);
        if (tvShowFullCalendar != null) {
            tvShowFullCalendar.setOnClickListener(v -> setFullCalendarVisibility(true));
        }
        if (tvHideFullCalendar != null) {
            tvHideFullCalendar.setOnClickListener(v -> setFullCalendarVisibility(false));
        }
        
        // Task History views
        layoutTaskHistory = findViewById(R.id.layoutTaskHistory);
        tvShowTaskHistory = findViewById(R.id.tvShowTaskHistory);
        tvHideTaskHistory = findViewById(R.id.tvHideTaskHistory);
        tvHistoryEmpty = findViewById(R.id.tvHistoryEmpty);
        rvTaskHistory = findViewById(R.id.rvTaskHistory);
        
        // Initialize task history RecyclerView
        rvTaskHistory.setLayoutManager(new LinearLayoutManager(this));
        taskHistoryAdapter = new TaskHistoryAdapter(this, new ArrayList<>());
        rvTaskHistory.setAdapter(taskHistoryAdapter);
        
        if (tvShowTaskHistory != null) {
            tvShowTaskHistory.setOnClickListener(v -> setTaskHistoryVisibility(true));
        }
        if (tvHideTaskHistory != null) {
            tvHideTaskHistory.setOnClickListener(v -> setTaskHistoryVisibility(false));
        }
        
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
        isFullCalendarVisible = isPreviewMode;
        
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
        
        // Set input filter: letters and numbers only for rice variety
        etRiceVariety.setFilters(new InputFilter[] { new LettersAndNumbersOnlyFilter() });

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
                                
                                // Fix weekRange format for all tasks (in case they have old format)
                                fixWeekRangeForAllTasks(p.getPlantingDate());
                                
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
        // Week 1: Pag aararo, Pag-aayos ng kanal at pilapil, Pagbabad ng lupa, Paghahanda ng lupang pagpupunlaan
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pag-aararo", "preparation"),
                new TaskTemplate("Pag-aayos ng kanal at pilapil", "preparation"),
                new TaskTemplate("Pagbabad ng lupa", "preparation"),
                new TaskTemplate("Paghahanda ng lupang pagpupunlaan", "preparation")
        ));
        // Week 2: Pagbababad ng binhi, paggawa ng kamang-punlaan, pagkulob ng binhi, pagpupunla (SEEDLING WEEK)
        templates.add(new WeekTaskTemplate(true,
                new TaskTemplate("Pagbababad ng binhi", "seed"),
                new TaskTemplate("Paggawa ng kamang-punlaan", "seed"),
                new TaskTemplate("Pagkulob ng binhi", "seed"),
                new TaskTemplate("Pagpupunla", "seed")
        ));
        // Week 3: Ika-1 pagsusuyod, pagpapatubig sa punlaan, pagpapataba sa punlaan
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Ika-1 pagsusuyod", "weed_control"),
                new TaskTemplate("Pagpapatubig sa punlaan", "irrigation"),
                new TaskTemplate("Pagpapataba sa punlaan", "fertilizer")
        ));
        // Week 4: Ika-2 pagsusuyod
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Ika-2 pagsusuyod", "weed_control")
        ));
        // Week 5: Pagpapatag, pagbubunot ng punla, pagkakalat ng punla, paglilipat tanim, pamamahala sa damo, pamamahala sa kuhol, paghuhulip, pagpapatubig
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatag", "land_preparation"),
                new TaskTemplate("Pagbubunot ng punla", "planting"),
                new TaskTemplate("Pagkakalat ng punla", "planting"),
                new TaskTemplate("Paglilipat tanim", "planting"),
                new TaskTemplate("Pamamahala sa damo", "weed_control"),
                new TaskTemplate("Pamamahala sa kuhol", "pest_control"),
                new TaskTemplate("Paghuhulip", "pest_control"),
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        // Week 6: Ika-1 pagpapataba, pagpapatubig
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Ika-1 pagpapataba", "fertilizer"),
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        // Week 7: pagpapatubig, pamamahala sa damo
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation"),
                new TaskTemplate("Pamamahala sa damo", "weed_control")
        ));
        // Week 8: paglalagay ng observation well (awd), ikaw-2 pagpapataba, pagpapatubig
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Paglalagay ng observation well (awd)", "irrigation"),
                new TaskTemplate("Ika-2 pagpapataba", "fertilizer"),
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        // Week 9: pagpapatubig
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        // Week 10: pagpapatubig, ika-3 pagpapataba
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation"),
                new TaskTemplate("Ika-3 pagpapataba", "fertilizer")
        ));
        // Week 11: pagpapatubig
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        // Week 12: pagpapatubig
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        // Week 13: pagpapatubig
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        // Week 14: pagpapatubig
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        // Week 15: pagpapatubig
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatubig", "irrigation")
        ));
        // Week 16: pagpapatuyo ng palayan
        templates.add(new WeekTaskTemplate(
                new TaskTemplate("Pagpapatuyo ng palayan", "harvest_prep")
        ));
        // Week 17: pag-aani
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
            checkForChanges();
        });

        // Combo selection change
        rgFertilizerCombo.setOnCheckedChangeListener((group, checkedId) -> {
            updateFertilizerScheduleTable();
            checkForChanges();
        });
        
        // Track changes in text fields
        etRiceVariety.setOnItemClickListener((parent, view, position, id) -> checkForChanges());
        etSeedWeight.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                checkForChanges();
            }
        });
        
        // Track planting method changes
        rgPlantingMethod.setOnCheckedChangeListener((group, checkedId) -> checkForChanges());
        
        // Yield computation - automatic calculation when area changes
        if (etAreaHectares != null) {
            etAreaHectares.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(android.text.Editable s) {
                    computeYield();
                }
            });
            // Prevent leading zeros (like 01, 02, etc.)
            etAreaHectares.setFilters(new InputFilter[] { new NoLeadingZeroFilter() });
        }
        
        // Auto-compute yield when variety or method changes
        etRiceVariety.setOnItemClickListener((parent, view, position, id) -> {
            checkForChanges();
            computeYield();
        });
        
        rgPlantingMethod.setOnCheckedChangeListener((group, checkedId) -> {
            checkForChanges();
            computeYield();
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
            showSnackBar("Piliin ang petsa ng pagpupunla.", false, null);
            return false;
        }
        if (seedWeight.isEmpty()) {
            showSnackBar("Ilalagay ang kabuuang timbang ng binhi.", false, null);
            return false;
        }
        
        // Validate area hectares (required, must not be empty or 0)
        if (etAreaHectares != null) {
            String areaStr = etAreaHectares.getText() != null ? etAreaHectares.getText().toString().trim() : "";
            if (areaStr.isEmpty()) {
                showSnackBar("Ilalagay ang sukat (hektarya).", false, null);
                return false;
            }
            try {
                double area = Double.parseDouble(areaStr);
                if (area <= 0) {
                    showSnackBar("Ang sukat (hektarya) ay dapat mas malaki sa 0.", false, null);
                    return false;
                }
            } catch (NumberFormatException e) {
                showSnackBar("Hindi wasto ang sukat (hektarya).", false, null);
                return false;
            }
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
        // Use Philippines timezone (Asia/Manila, UTC+8)
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String displayDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    tvPlantingDate.setText(displayDate);
                    // Regenerate tasks if we're on crop calendar step
                    if (currentStep == 2) {
                        generateCropCalendarTasks();
                    }
                    checkForChanges();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        // Prevent selecting past dates
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
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

            // Use Philippines timezone (Asia/Manila, UTC+8)
            Calendar plantingCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
            plantingCal.setTime(plantingDate);

            cropCalendarTasks.clear();
            List<WeekTaskTemplate> weekTemplates = buildWeekTaskTemplates();
            int seedlingWeekIndex = 2; // Week 2 is the seedling week
            
            // Anchor Week 2 (seedling week) to the selected date (adjusted to Monday)
            Calendar anchorWeekStart = (Calendar) plantingCal.clone();
            int dayOfWeek = anchorWeekStart.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
            anchorWeekStart.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
            // Go back 1 week to get Week 1 start (since Week 2 is the selected date)
            anchorWeekStart.add(Calendar.DAY_OF_YEAR, -7);
            
            // Ensure we're in preview mode when generating new tasks
            // This ensures all tasks are created as new, not updated
            boolean wasPreviewMode = isPreviewMode;
            boolean isNewPlanting = (plantingId == null || plantingId.isEmpty());
            isPreviewMode = true; // Force preview mode during generation to always create new tasks
            
            for (int weekIndex = 0; weekIndex < weekTemplates.size(); weekIndex++) {
                WeekTaskTemplate template = weekTemplates.get(weekIndex);
                int weekNumber = weekIndex + 1;
                int offsetFromSeedlingWeek = weekNumber - seedlingWeekIndex;
                Calendar weekStart = (Calendar) anchorWeekStart.clone();
                weekStart.add(Calendar.DAY_OF_YEAR, offsetFromSeedlingWeek * 7);
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
            if (cardCurrentWeekTasks != null) {
                cardCurrentWeekTasks.setVisibility(View.GONE);
            }
            setFullCalendarVisibility(true);
        } else {
            if (rvCropCalendar.getAdapter() != cropCalendarAdapter) {
                rvCropCalendar.setAdapter(cropCalendarAdapter);
            }
            cropCalendarAdapter.updateTasks(cropCalendarTasks);
            refreshCropCalendarUIState();
        }

        rvCropCalendar.post(() -> {
            if (rvCropCalendar instanceof NonScrollableRecyclerView) {
                ((NonScrollableRecyclerView) rvCropCalendar).forceMeasure();
            }
            rvCropCalendar.requestLayout();
        });
    }

    private void refreshCropCalendarUIState() {
        if (isPreviewMode) {
            setFullCalendarVisibility(true);
            return;
        }

        if (cardCurrentWeekTasks == null || layoutCurrentWeekTasks == null || tvShowFullCalendar == null || layoutCalendarButtons == null) {
            return;
        }

        Map<Integer, List<CropCalendarTask>> groupedTasks = groupTasksByWeek();
        if (groupedTasks.isEmpty()) {
            cardCurrentWeekTasks.setVisibility(View.GONE);
            rvCropCalendar.setVisibility(View.GONE);
            return;
        }

        int currentWeekByDate = getCurrentWeekNumberByDate(groupedTasks);
        int firstIncompleteWeek = getFirstIncompleteWeekNumber(groupedTasks);
        int targetWeek = currentWeekByDate > 0 ? currentWeekByDate
                : (firstIncompleteWeek > 0 ? firstIncompleteWeek : groupedTasks.keySet().iterator().next());

        highlightedWeekNumber = targetWeek;

        if (cropCalendarAdapter != null) {
            cropCalendarAdapter.setMaxUnlockedWeek(targetWeek);
        }

        updateCurrentWeekCard(groupedTasks, targetWeek);

        if (!isFullCalendarVisible) {
            setFullCalendarVisibility(false);
        } else {
            setFullCalendarVisibility(true);
        }
    }

    private void updateCurrentWeekCard(Map<Integer, List<CropCalendarTask>> groupedTasks, int weekNumber) {
        if (cardCurrentWeekTasks == null || layoutCurrentWeekTasks == null) {
            return;
        }

        List<CropCalendarTask> tasksForWeek = groupedTasks.get(weekNumber);
        if (tasksForWeek == null || tasksForWeek.isEmpty()) {
            layoutCurrentWeekTasks.removeAllViews();
            tvCurrentWeekEmpty.setVisibility(View.VISIBLE);
            cardCurrentWeekTasks.setVisibility(View.VISIBLE);
            return;
        }

        tvCurrentWeekRange.setText(tasksForWeek.get(0).getWeekRange());
        layoutCurrentWeekTasks.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (CropCalendarTask task : tasksForWeek) {
            View taskView = inflater.inflate(R.layout.item_crop_calendar_task_item, layoutCurrentWeekTasks, false);
            CheckBox cbTask = taskView.findViewById(R.id.cbTaskCompleted);
            TextView tvTaskName = taskView.findViewById(R.id.tvTaskName);

            cbTask.setChecked(task.isCompleted());
            tvTaskName.setText("➤ " + task.getTaskName());

            cbTask.setOnClickListener(v -> {
                cbTask.setChecked(task.isCompleted());
                showTaskCompletionDialog(task);
            });

            layoutCurrentWeekTasks.addView(taskView);
        }

        tvCurrentWeekEmpty.setVisibility(View.GONE);
        cardCurrentWeekTasks.setVisibility(isFullCalendarVisible ? View.GONE : View.VISIBLE);
    }

    private Map<Integer, List<CropCalendarTask>> groupTasksByWeek() {
        Map<Integer, List<CropCalendarTask>> grouped = new LinkedHashMap<>();
        if (cropCalendarTasks == null) {
            return grouped;
        }

        for (CropCalendarTask task : cropCalendarTasks) {
            int weekNumber = task.getWeekNumber();
            if (!grouped.containsKey(weekNumber)) {
                grouped.put(weekNumber, new ArrayList<>());
            }
            grouped.get(weekNumber).add(task);
        }

        return grouped;
    }

    private int getFirstIncompleteWeekNumber(Map<Integer, List<CropCalendarTask>> groupedTasks) {
        for (Map.Entry<Integer, List<CropCalendarTask>> entry : groupedTasks.entrySet()) {
            List<CropCalendarTask> tasks = entry.getValue();
            if (tasks == null) continue;
            for (CropCalendarTask task : tasks) {
                if (!task.isCompleted()) {
                    return entry.getKey();
                }
            }
        }
        return -1;
    }

    private int getCurrentWeekNumberByDate(Map<Integer, List<CropCalendarTask>> groupedTasks) {
        // Use Philippines timezone (Asia/Manila, UTC+8)
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
        // Set today to start of day for accurate comparison
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        int firstWeekAfterToday = -1;
        int lastWeek = -1;

        for (Map.Entry<Integer, List<CropCalendarTask>> entry : groupedTasks.entrySet()) {
            List<CropCalendarTask> tasks = entry.getValue();
            if (tasks == null || tasks.isEmpty()) {
                continue;
            }

            CropCalendarTask referenceTask = tasks.get(0);
            String scheduledDate = referenceTask.getScheduledDate();
            if (TextUtils.isEmpty(scheduledDate)) {
                if (lastWeek == -1) {
                    lastWeek = entry.getKey();
                }
                continue;
            }

            try {
                Date referenceDate = isoDateFormat.parse(scheduledDate);
                if (referenceDate != null) {
                    // Use Philippines timezone for consistency
                    Calendar weekStart = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
                    weekStart.setTime(referenceDate);
                    weekStart.add(Calendar.DAY_OF_YEAR, -3);
                    // Set time to start of day for accurate comparison
                    weekStart.set(Calendar.HOUR_OF_DAY, 0);
                    weekStart.set(Calendar.MINUTE, 0);
                    weekStart.set(Calendar.SECOND, 0);
                    weekStart.set(Calendar.MILLISECOND, 0);

                    Calendar weekEnd = (Calendar) weekStart.clone();
                    weekEnd.add(Calendar.DAY_OF_YEAR, 6);
                    // Set time to end of day for accurate comparison
                    weekEnd.set(Calendar.HOUR_OF_DAY, 23);
                    weekEnd.set(Calendar.MINUTE, 59);
                    weekEnd.set(Calendar.SECOND, 59);
                    weekEnd.set(Calendar.MILLISECOND, 999);

                    // Check if today falls within this week (weekStart <= today <= weekEnd)
                    if (!today.before(weekStart) && !today.after(weekEnd)) {
                        return entry.getKey(); // Today is within this week - return immediately
                    }
                    
                    // Track first week that starts after today (for fallback)
                    if (today.before(weekStart) && firstWeekAfterToday == -1) {
                        firstWeekAfterToday = entry.getKey();
                    }
                    
                    // Track last week
                    lastWeek = entry.getKey();
                }
            } catch (Exception e) {
                if (lastWeek == -1) {
                    lastWeek = entry.getKey();
                }
                continue;
            }
        }

        // If today is before all weeks, return first week
        // If today is after all weeks, return last week
        // Otherwise return first week after today
        if (firstWeekAfterToday != -1) {
            return firstWeekAfterToday;
        }
        return lastWeek != -1 ? lastWeek : -1;
    }

    private void setFullCalendarVisibility(boolean visible) {
        isFullCalendarVisible = visible;
        if (layoutFullCalendar != null) {
            layoutFullCalendar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if (rvCropCalendar != null) {
            rvCropCalendar.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible && !isPreviewMode) {
                rvCropCalendar.post(() -> rvCropCalendar.smoothScrollToPosition(Math.max(0, highlightedWeekNumber - 1)));
            }
        }
        if (cardCurrentWeekTasks != null) {
            cardCurrentWeekTasks.setVisibility(visible ? View.GONE : View.VISIBLE);
        }
        if (layoutCalendarButtons != null) {
            layoutCalendarButtons.setVisibility(visible ? View.GONE : View.VISIBLE);
        }
        if (tvShowFullCalendar != null) {
            tvShowFullCalendar.setVisibility(visible ? View.GONE : View.VISIBLE);
        }
        if (tvHideFullCalendar != null) {
            tvHideFullCalendar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        
        // Hide history when showing full calendar
        if (visible) {
            setTaskHistoryVisibility(false);
        }
    }
    
    private void setTaskHistoryVisibility(boolean visible) {
        isHistoryVisible = visible;
        if (layoutTaskHistory != null) {
            layoutTaskHistory.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        
        // Hide current week card when showing history (like full calendar)
        if (cardCurrentWeekTasks != null) {
            cardCurrentWeekTasks.setVisibility(visible ? View.GONE : View.VISIBLE);
        }
        
        if (layoutCalendarButtons != null) {
            layoutCalendarButtons.setVisibility(visible ? View.GONE : View.VISIBLE);
        }
        
        if (tvShowTaskHistory != null) {
            tvShowTaskHistory.setVisibility(visible ? View.GONE : View.VISIBLE);
        }
        
        if (tvHideTaskHistory != null) {
            tvHideTaskHistory.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        
        // Hide full calendar when showing history
        if (visible) {
            setFullCalendarVisibility(false);
            updateTaskHistory();
        }
    }
    
    private void updateTaskHistory() {
        if (taskHistoryAdapter == null || cropCalendarTasks == null) {
            return;
        }
        
        // Filter completed tasks
        List<CropCalendarTask> completedTasks = new ArrayList<>();
        for (CropCalendarTask task : cropCalendarTasks) {
            if (task.isCompleted()) {
                completedTasks.add(task);
            }
        }
        
        // Sort by completion date (newest first - descending order)
        completedTasks.sort((t1, t2) -> {
            String date1Str = t1.getActualCompletionDate();
            String date2Str = t2.getActualCompletionDate();
            
            if (date1Str == null || date1Str.isEmpty()) {
                return 1; // Put null dates at the end
            }
            if (date2Str == null || date2Str.isEmpty()) {
                return -1;
            }
            
            // Parse dates and compare as Date objects for accurate comparison
            try {
                Date date1 = isoDateFormat.parse(date1Str);
                Date date2 = isoDateFormat.parse(date2Str);
                
                if (date1 == null || date2 == null) {
                    // Fallback to string comparison if parsing fails
                    return date2Str.compareTo(date1Str);
                }
                
                // Compare dates - descending order (newest first)
                int dateCompare = date2.compareTo(date1);
                
                // If dates are the same, sort by task ID (creation timestamp) - newest first
                // Task ID is System.currentTimeMillis() when created, so higher ID = newer task
                if (dateCompare == 0) {
                    try {
                        long id1 = Long.parseLong(t1.getId());
                        long id2 = Long.parseLong(t2.getId());
                        return Long.compare(id2, id1); // Descending order (newest first)
                    } catch (NumberFormatException e) {
                        // Fallback to week number if ID parsing fails
                        return Integer.compare(t2.getWeekNumber(), t1.getWeekNumber());
                    }
                }
                
                return dateCompare; // Descending order (newest first)
            } catch (Exception e) {
                // Fallback to string comparison if parsing fails
                return date2Str.compareTo(date1Str);
            }
        });
        
        // Update adapter
        taskHistoryAdapter = new TaskHistoryAdapter(this, completedTasks);
        rvTaskHistory.setAdapter(taskHistoryAdapter);
        
        // Show/hide empty message
        if (tvHistoryEmpty != null) {
            tvHistoryEmpty.setVisibility(completedTasks.isEmpty() ? View.VISIBLE : View.GONE);
        }
        
        // Force RecyclerView to measure and show all items (similar to crop calendar)
        rvTaskHistory.post(() -> {
            if (rvTaskHistory instanceof NonScrollableRecyclerView) {
                ((NonScrollableRecyclerView) rvTaskHistory).forceMeasure();
            }
            rvTaskHistory.requestLayout();
        });
    }

    private void addWeekTask(Calendar weekStart, int weekNumber, int taskOrder, String taskName, String taskType) {
        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        // Format: "Nov 18–24, 2025" or "Dec 29 - Jan 4, 2025" if spanning different months
        String weekRange;
        int startMonth = weekStart.get(Calendar.MONTH);
        int endMonth = weekEnd.get(Calendar.MONTH);
        int startYear = weekStart.get(Calendar.YEAR);
        int endYear = weekEnd.get(Calendar.YEAR);
        
        if (startMonth != endMonth || startYear != endYear) {
            // Different months or years - show both month names
            weekRange = monthFormat.format(weekStart.getTime()) + " " + 
                       dayFormat.format(weekStart.getTime()) + " - " + 
                       monthFormat.format(weekEnd.getTime()) + " " + 
                       dayFormat.format(weekEnd.getTime()) + ", " + 
                       yearFormat.format(weekEnd.getTime());
        } else {
            // Same month - show single month name
            weekRange = monthFormat.format(weekStart.getTime()) + " " + 
                       dayFormat.format(weekStart.getTime()) + "–" + 
                       dayFormat.format(weekEnd.getTime()) + ", " + 
                       yearFormat.format(weekStart.getTime());
        }

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

        // Default to current date (today) in Philippines timezone
        String initialDate;
        if (!TextUtils.isEmpty(task.getActualCompletionDate())) {
            // If task already has a completion date, use it
            initialDate = task.getActualCompletionDate();
        } else {
            // Otherwise, use current date (today) - format directly from Calendar to avoid timezone issues
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
            initialDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                    today.get(Calendar.YEAR), 
                    today.get(Calendar.MONTH) + 1, 
                    today.get(Calendar.DAY_OF_MONTH));
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
            // Get current week date range for this task
            Calendar weekStart = null;
            Calendar weekEnd = null;
            
            String scheduledDate = task.getScheduledDate();
            if (!TextUtils.isEmpty(scheduledDate)) {
                try {
                    Date referenceDate = isoDateFormat.parse(scheduledDate);
                    if (referenceDate != null) {
                        // Calculate week start (3 days before scheduled date) and end (3 days after)
                        weekStart = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
                        weekStart.setTime(referenceDate);
                        weekStart.add(Calendar.DAY_OF_YEAR, -3);
                        weekStart.set(Calendar.HOUR_OF_DAY, 0);
                        weekStart.set(Calendar.MINUTE, 0);
                        weekStart.set(Calendar.SECOND, 0);
                        weekStart.set(Calendar.MILLISECOND, 0);
                        
                        weekEnd = (Calendar) weekStart.clone();
                        weekEnd.add(Calendar.DAY_OF_YEAR, 6);
                        weekEnd.set(Calendar.HOUR_OF_DAY, 23);
                        weekEnd.set(Calendar.MINUTE, 59);
                        weekEnd.set(Calendar.SECOND, 59);
                        weekEnd.set(Calendar.MILLISECOND, 999);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Use Philippines timezone (Asia/Manila, UTC+8)
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
            String current = etTaskDate.getText() != null ? etTaskDate.getText().toString() : "";
            if (!TextUtils.isEmpty(current)) {
                try {
                    // Parse date string directly (yyyy-MM-dd format) to avoid timezone issues
                    // Extract year, month, day directly from string instead of parsing as Date
                    String[] parts = current.split("-");
                    if (parts.length == 3) {
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]) - 1; // Calendar.MONTH is 0-based
                        int day = Integer.parseInt(parts[2]);
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, day);
                    } else {
                        // If format is wrong, use current date (today)
                        calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
                    }
                } catch (Exception ignored) {
                    // If parsing fails, use current date (today)
                    calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
                }
            } else {
                // If empty, use current date (today) in Philippines timezone
                calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
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
            
            // Always prevent selecting past dates - use same approach as planting date picker
            // This will disable past dates in the date picker UI itself
            long minDateMillis = System.currentTimeMillis() - 1000;
            
            // Limit date picker to current week only
            if (weekStart != null && weekEnd != null) {
                // Ensure min date is at least today (to disable past dates)
                // But allow week start if it's in the future
                long minDate = minDateMillis;
                if (weekStart.getTimeInMillis() > minDateMillis) {
                    minDate = weekStart.getTimeInMillis();
                }
                
                dialog.getDatePicker().setMinDate(minDate);
                dialog.getDatePicker().setMaxDate(weekEnd.getTimeInMillis());
            } else {
                // Fallback: prevent selecting past dates - same as planting date picker
                dialog.getDatePicker().setMinDate(minDateMillis);
            }
            
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
        }
        
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        ivCloseDialog.setOnClickListener(v -> dialog.dismiss());
        
        // Show dialog first to get window, then set layout parameters
        dialog.show();
        
        if (window != null) {
            // Set layout parameters to center the dialog (90% width, wider)
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenWidth = displayMetrics.widthPixels;
            
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = (int) (screenWidth * 0.90); // 90% of screen width (wider)
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = android.view.Gravity.CENTER; // Center the dialog
            layoutParams.x = 0; // Ensure centered horizontally
            layoutParams.y = 0; // Ensure centered vertically
            window.setAttributes(layoutParams);
            
            // Dim the background
            window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.5f); // 50% dim
        }

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
            checkForChanges();
            
            // Update task history if visible
            if (isHistoryVisible) {
                updateTaskHistory();
            }
            
            Toast.makeText(this, "Na-save ang detalye ng gawain.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
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
        checkForChanges();
        
        // Update task history if visible
        if (isHistoryVisible) {
            updateTaskHistory();
        }
        
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
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_keycheck_recommendation, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvKeycheckTitle);
        TextView tvContent = dialogView.findViewById(R.id.tvKeycheckContent);
        MaterialButton btnOkay = dialogView.findViewById(R.id.btnKeycheckOkay);

        // Get Keycheck content based on category
        String keycheckContent = getKeycheckContent(category);
        tvContent.setText(keycheckContent);

        // Ensure button is visible
        btnOkay.setVisibility(View.VISIBLE);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Set dialog width to 90% of screen (wider)
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        
        dialog.show(); // Show dialog first to get window
        
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (screenWidth * 0.90); // 90% width (wider)
        // Set fixed height to ensure all dialogs are the same size
        int dialogHeight = (int) (displayMetrics.heightPixels * 0.65); // 65% of screen height
        layoutParams.height = dialogHeight;
        layoutParams.gravity = android.view.Gravity.CENTER;
        layoutParams.x = 0; // Ensure centered horizontally
        layoutParams.y = 0; // Ensure centered vertically
        dialog.getWindow().setAttributes(layoutParams);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setDimAmount(0.5f);

        btnOkay.setOnClickListener(v -> dialog.dismiss());
    }

    private String getKeycheckContent(String category) {
        if (TextUtils.isEmpty(category)) {
            return "Walang available na keycheck para sa kategoryang ito.";
        }

        // Normalize category name for comparison
        String normalizedCategory = category.trim().toLowerCase(Locale.getDefault());

        // Keycheck content for "Paghahanda ng lupa"
        if (normalizedCategory.contains("paghahanda") && normalizedCategory.contains("lupa")) {
            return "• Mag-araro 21-30 araw bago magtanim gamit ang tractor o rotovator.\n\n" +
                   "• Ayusin ang mga pilapil at kanal bago pa magsimula ang taniman upang maipon ang tubig at gumawa ng mga kanal.\n\n" +
                   "• Araruhin minsan at suyurin nang 3 beses (paayon, pahalang, at linang) ang bukid nang may tig-5 o 7 araw na pagitan. Patagin ang lupa na may 5 cm lalim ng tubig nang mapadali ang pagpapatag.";
        }

        // Keycheck content for "Pagtatanim"
        if (normalizedCategory.contains("pagtatanim")) {
            return "• Isagawa ang sabayang pagtatanim ayon sa dating ng patubig sa iyong lugar.\n\n" +
                   "• Protektahan ang itinanim na binhi laban sa mga peste.\n\n" +
                   "• Magsabog ng 1 kilong pinasibol na ekstrana.\n\n" +
                   "• Para sa manu-manong lipat-tanim, maaaring isagawa ito 18-21 DAS.\n\n" +
                   "• Magtanim ng 2-3 punla sa bawat tundos na may agwat na 20x20 cm sa tag-ulan at 20x15 cm naman sa tag-araw.";
        }

        // Keycheck content for "Pamamahala sa damo at peste" (combined)
        if ((normalizedCategory.contains("pamamahala") && normalizedCategory.contains("peste")) ||
            (normalizedCategory.contains("pamamahala") && normalizedCategory.contains("damo"))) {
            return "• Gumamit ng barayting napatunayang matibay sa sakit at peste, at angkop sa inyong lugar.\n\n" +
                   "• Huwag mag-spray ng pamatay insekto sa unang 30 araw pagkalipat-tanim o 40 araw pagkasabog-tanim. Ito ay upang maparami ang mga kaibigang insekto at kakamping organismo sa palayan na pumapatay sa mga peste.\n\n" +
                   "• Agapan ang damo at kuhol sa unang 3 araw pagkasabog-tanim o lipat-tanim. Maglagay ng kaukulang pamuksa laban sa mga daga at ibon lalo na sa panahon ng pagbubuntis at pag-uusbong ng palay.\n\n" +
                   "• Linisin ang mga kanal at pilapil upang hindi pamahayan ng mga mapanirang insekto o organismo.\n\n" +
                   "• Palagiang bisitahin ang palayan upang maagang matukoy at maagapan ang mga potensiyal na problema dulot ng peste.\n\n" +
                   "• Alamin ang mga mapanirang organismo sa bawat yugto ng palay at sugpuin bago makapaminsala.";
        }

        // Keycheck content for "Pagpapatubig"
        if (normalizedCategory.contains("pagpapatubig")) {
            return "• Sa unang pagpapatubig, maglagay ng 2-3cm babaw ng tubig bago ang unang pagpapataba (10-14 DAS/DAT).\n\n" +
                   "• Panatilihin ang 2-3cm babaw ng tubig tuwing magpapataba.\n\n" +
                   "• Panatilihin ang 5cm lalim ng tubig sa panahon ng pamumulaklak hanggang paghinog ng palay.\n\n" +
                   "• Kung kulang sa tubig, isagawa ang controlled irrigation o Alternate Wetting and Drying (AWD) technology sa panahon ng vegetative stage.\n\n" +
                   "• Alisan o ihinto ang pagpapatubig: (1) isang lingo bago mag-ani para sa mga galas na lupa o sa tag-araw; (2) 2 linggo bago mag-ani para sa lagkiting lupa o sa tag-ulan.";
        }

        // Keycheck content for "Pagpapataba"
        if (normalizedCategory.contains("pagpapataba")) {
            return "• Alamin ang tamang ELEMENT na kailangan ng palay.\n\n" +
                   "• Alamin ang tamang AMOUNT o dami ng patabang ilalagay.\n\n" +
                   "• Alamin ang tamang TIMING o tiyempo nang mapakinabangan ng palay ang sustansiyang ilalagay.";
        }

        // Keycheck content for "Pag-aani"
        if (normalizedCategory.contains("pag-aani") || normalizedCategory.contains("pagsisinop")) {
            return "• Patuyuan ang palayan 1-2 linggo bago ang inaasahang araw ng pag-aani.\n\n" +
                   "• Anihin ang palay kapag 85-90% (ginapas) o 90-95% (combine harvester) ng butil ay hinog.\n\n" +
                   "• Mag-ani sa tamang moisture content o porsyento ng halumigmig ng butil.\n\n" +
                   "• Pagkapag-gapas, giikin ang palay nang hindi lalampas sa isang araw kapag tag-ulan; 2 araw kung tag-araw.\n\n" +
                   "• Ikamada sa paleta ang nakasakong pinatuyong palay nang hindi mabasa o magpawis bago linisin.\n\n" +
                   "• Linisin ang pinatuyong palay sa loob ng 2-3 araw.\n\n" +
                   "• Kung hindi agad ibebenta, isalansan ang pinatuyong palay sa malinis na bodega o imbakan.\n\n" +
                   "• Sa mga palay na gagamiting binhi, patuyuin sa loob ng 12-14 oras pagkatapos magiik bago ilagay sa sisidlan.";
        }

        // Add more categories here as needed
        return "Walang available na keycheck para sa kategoryang ito.";
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
        
        // Save area data (yield is computed automatically based on variety + method + area)
        String areaHectares = etAreaHectares != null && etAreaHectares.getText() != null 
                ? etAreaHectares.getText().toString().trim() : "";
        planting.setAreaHectares(areaHectares);
        
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
        
        // Make "Pagpili ng Binhi" section read-only when editing existing planting
        etRiceVariety.setEnabled(false);
        etRiceVariety.setFocusable(false);
        etRiceVariety.setClickable(false);
        rbSabogTanim.setEnabled(false);
        rbLipatTanim.setEnabled(false);
        layoutPlantingDate.setClickable(false);
        layoutPlantingDate.setEnabled(false);
        etSeedWeight.setEnabled(false);
        etSeedWeight.setFocusable(false);
        
        // Make "Pamamahala sa Pataba" section read-only when editing existing planting
        rbAbonongSwak.setEnabled(false);
        rbSarilingDiskarte.setEnabled(false);
        rbCombo1.setEnabled(false);
        rbCombo2.setEnabled(false);
        rbCombo3.setEnabled(false);
        rbCombo4.setEnabled(false);
        
        // Make yield computation fields read-only when editing existing planting
        if (etAreaHectares != null) {
            etAreaHectares.setEnabled(false);
            etAreaHectares.setFocusable(false);
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
                            
                            // Load area data (yield is computed automatically)
                            if (p.getAreaHectares() != null && !p.getAreaHectares().isEmpty() && etAreaHectares != null) {
                                etAreaHectares.setText(p.getAreaHectares());
                            }
                            // Compute yield to update display (based on variety + method + area)
                            computeYield();
                            
                            if (p.getCropCalendarTasks() != null && !p.getCropCalendarTasks().isEmpty()) {
                                cropCalendarTasks = p.getCropCalendarTasks();
                                android.util.Log.d("CropCalendar", "Loaded " + cropCalendarTasks.size() + " existing tasks from storage");
                                
                                // Fix weekRange format for all tasks (in case they have old format)
                                fixWeekRangeForAllTasks(p.getPlantingDate());
                                
                                // Store initial state for change tracking
                                initialCropCalendarTasks = new ArrayList<>();
                                for (CropCalendarTask t : cropCalendarTasks) {
                                    CropCalendarTask copy = new CropCalendarTask(t.getTaskName(), t.getTaskName(), 0, t.getTaskType());
                                    copy.setCompleted(t.isCompleted());
                                    copy.setActualCompletionDate(t.getActualCompletionDate());
                                    copy.setAdditionalNotes(t.getAdditionalNotes());
                                    copy.setTaskCategory(t.getTaskCategory());
                                    copy.setWeekNumber(t.getWeekNumber());
                                    initialCropCalendarTasks.add(copy);
                                }
                                
                                // Check if we have all weeks, if not, regenerate
                                if (!hasCompleteCalendar(cropCalendarTasks) && p.getPlantingDate() != null && !p.getPlantingDate().isEmpty()) {
                                    android.util.Log.d("CropCalendar", "Incomplete tasks found (" + cropCalendarTasks.size() + "), regenerating full schedule");
                                    tvPlantingDate.setText(p.getPlantingDate());
                                    generateCropCalendarTasks();
                                    // Update initial state after regeneration
                                    initialCropCalendarTasks = new ArrayList<>();
                                    for (CropCalendarTask t : cropCalendarTasks) {
                                        CropCalendarTask copy = new CropCalendarTask(t.getTaskName(), t.getTaskName(), 0, t.getTaskType());
                                        copy.setCompleted(t.isCompleted());
                                        copy.setActualCompletionDate(t.getActualCompletionDate());
                                        copy.setAdditionalNotes(t.getAdditionalNotes());
                                        copy.setTaskCategory(t.getTaskCategory());
                                        copy.setWeekNumber(t.getWeekNumber());
                                        initialCropCalendarTasks.add(copy);
                                    }
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
                                // Store initial state (empty tasks)
                                initialCropCalendarTasks = new ArrayList<>();
                            }
                            
                            // Store initial fertilizer state
                            initialFertilizerStrategy = p.getFertilizerStrategy();
                            initialFertilizerCombo = p.getFertilizerCombo();
                            
                            // Check for changes after loading
                            checkForChanges();
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

    private void fixWeekRangeForAllTasks(String plantingDate) {
        if (plantingDate == null || plantingDate.isEmpty() || cropCalendarTasks == null || cropCalendarTasks.isEmpty()) {
            return;
        }
        
        try {
            // Parse planting date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date plantingDateObj = sdf.parse(plantingDate);
            if (plantingDateObj == null) {
                return;
            }
            
            // Use Philippines timezone (Asia/Manila, UTC+8)
            Calendar plantingCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
            plantingCal.setTime(plantingDateObj);
            
            // Find seedling week (week 2)
            Calendar anchorWeekStart = (Calendar) plantingCal.clone();
            int dayOfWeek = anchorWeekStart.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
            anchorWeekStart.add(Calendar.DAY_OF_YEAR, -daysFromMonday);
            // Go back 1 week to get Week 1 start (since Week 2 is the selected date)
            anchorWeekStart.add(Calendar.DAY_OF_YEAR, -7);
            
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
            
            // Fix weekRange for each task
            for (CropCalendarTask task : cropCalendarTasks) {
                int weekNumber = task.getWeekNumber();
                if (weekNumber < 1) {
                    continue;
                }
                
                // Calculate week start based on week number
                int offsetFromSeedlingWeek = weekNumber - 2; // Week 2 is seedling week
                Calendar weekStart = (Calendar) anchorWeekStart.clone();
                weekStart.add(Calendar.DAY_OF_YEAR, offsetFromSeedlingWeek * 7);
                
                Calendar weekEnd = (Calendar) weekStart.clone();
                weekEnd.add(Calendar.DAY_OF_YEAR, 6);
                
                // Format weekRange with proper month/year handling
                int startMonth = weekStart.get(Calendar.MONTH);
                int endMonth = weekEnd.get(Calendar.MONTH);
                int startYear = weekStart.get(Calendar.YEAR);
                int endYear = weekEnd.get(Calendar.YEAR);
                
                String weekRange;
                if (startMonth != endMonth || startYear != endYear) {
                    // Different months or years - show both month names
                    weekRange = monthFormat.format(weekStart.getTime()) + " " + 
                               dayFormat.format(weekStart.getTime()) + " - " + 
                               monthFormat.format(weekEnd.getTime()) + " " + 
                               dayFormat.format(weekEnd.getTime()) + ", " + 
                               yearFormat.format(weekEnd.getTime());
                } else {
                    // Same month - show single month name
                    weekRange = monthFormat.format(weekStart.getTime()) + " " + 
                               dayFormat.format(weekStart.getTime()) + "–" + 
                               dayFormat.format(weekEnd.getTime()) + ", " + 
                               yearFormat.format(weekStart.getTime());
                }
                
                task.setWeekRange(weekRange);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkForChanges() {
        // Only check for changes when editing
        boolean isEditing = plantingId != null && !plantingId.isEmpty();
        if (!isEditing) {
            return;
        }
        
        boolean hasChanges = false;
        
        // Check variety
        String currentVariety = etRiceVariety.getText() != null ? etRiceVariety.getText().toString().trim() : "";
        if (!currentVariety.equals(existingVariety != null ? existingVariety : "")) {
            hasChanges = true;
        }
        
        // Check planting method
        String currentMethod = "";
        if (rbSabogTanim.isChecked()) {
            currentMethod = "Sabog-tanim";
        } else if (rbLipatTanim.isChecked()) {
            currentMethod = "Lipat-tanim";
        }
        if (!currentMethod.equals(existingMethod != null ? existingMethod : "")) {
            hasChanges = true;
        }
        
        // Check planting date
        String currentDate = tvPlantingDate.getText().toString();
        if (!currentDate.equals(existingDate != null ? existingDate : "")) {
            hasChanges = true;
        }
        
        // Check seed weight
        String currentSeedWeight = etSeedWeight.getText() != null ? etSeedWeight.getText().toString().trim() : "";
        if (!currentSeedWeight.equals(existingSeedWeight != null ? existingSeedWeight : "")) {
            hasChanges = true;
        }
        
        // Check fertilizer strategy
        String currentFertilizerStrategy = "";
        if (rbAbonongSwak.isChecked()) {
            currentFertilizerStrategy = "Abonong Swak";
        } else if (rbSarilingDiskarte.isChecked()) {
            currentFertilizerStrategy = "Sariling diskarte";
        }
        String initialStrategy = initialFertilizerStrategy != null ? initialFertilizerStrategy : "";
        if (!currentFertilizerStrategy.equals(initialStrategy)) {
            hasChanges = true;
        }
        
        // Check fertilizer combo
        String currentFertilizerCombo = "";
        if (rbCombo1.isChecked()) {
            currentFertilizerCombo = "Combo 1 (3-4 tons)";
        } else if (rbCombo2.isChecked()) {
            currentFertilizerCombo = "Combo 2 (5-6 tons)";
        } else if (rbCombo3.isChecked()) {
            currentFertilizerCombo = "Combo 3 (7-8 tons)";
        } else if (rbCombo4.isChecked()) {
            currentFertilizerCombo = "Combo 4 + Biofertilizer (6-7 tons)";
        }
        String initialCombo = initialFertilizerCombo != null ? initialFertilizerCombo : "";
        if (!currentFertilizerCombo.equals(initialCombo)) {
            hasChanges = true;
        }
        
        // Check crop calendar tasks completion status
        if (initialCropCalendarTasks.size() != cropCalendarTasks.size()) {
            hasChanges = true;
        } else {
            // Match tasks by week number and task name for accurate comparison
            for (CropCalendarTask current : cropCalendarTasks) {
                CropCalendarTask initial = null;
                for (CropCalendarTask init : initialCropCalendarTasks) {
                    if (init.getWeekNumber() == current.getWeekNumber() && 
                        init.getTaskName().equals(current.getTaskName())) {
                        initial = init;
                        break;
                    }
                }
                
                if (initial == null) {
                    hasChanges = true;
                    break;
                }
                
                // Compare completion status
                if (current.isCompleted() != initial.isCompleted()) {
                    hasChanges = true;
                    break;
                }
                
                // Compare completion details if completed
                if (current.isCompleted()) {
                    String currentTaskDate = current.getActualCompletionDate() != null ? current.getActualCompletionDate() : "";
                    String initialTaskDate = initial.getActualCompletionDate() != null ? initial.getActualCompletionDate() : "";
                    if (!currentTaskDate.equals(initialTaskDate)) {
                        hasChanges = true;
                        break;
                    }
                    
                    String currentNotes = current.getAdditionalNotes() != null ? current.getAdditionalNotes() : "";
                    String initialNotes = initial.getAdditionalNotes() != null ? initial.getAdditionalNotes() : "";
                    if (!currentNotes.equals(initialNotes)) {
                        hasChanges = true;
                        break;
                    }
                }
            }
        }
        
        // Update button state
        if (hasChanges) {
            btnSave.setEnabled(true);
            btnSave.setAlpha(1.0f);
        } else {
            btnSave.setEnabled(false);
            btnSave.setAlpha(0.5f);
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

    /**
     * Get default yield per hectare in kg/ha based on variety name
     * Returns default value if variety not found in mapping
     */
    private double getDefaultYieldPerHectare(String varietyName) {
        if (varietyName == null || varietyName.trim().isEmpty()) {
            return 4500.0; // Standard default: 4.5 tons/ha = 4500 kg/ha
        }
        
        String variety = varietyName.trim().toUpperCase();
        
        // Predefined varieties from sample_variety_array and their typical yields (in kg/ha)
        // Based on average yields from Philippine rice varieties
        
        // NSIC Rc 222
        if (variety.contains("RC 222") || variety.contains("RC222") || variety.contains("NSIC RC 222") || variety.contains("NSIC RC222")) {
            return 4500.0; // ~4.5 tons/ha
        }
        // NSIC Rc 160
        else if (variety.contains("RC 160") || variety.contains("RC160") || variety.contains("NSIC RC 160") || variety.contains("NSIC RC160")) {
            return 5000.0; // ~5.0 tons/ha
        }
        // NSIC Rc 216
        else if (variety.contains("RC 216") || variety.contains("RC216") || variety.contains("NSIC RC 216") || variety.contains("NSIC RC216")) {
            return 4800.0; // ~4.8 tons/ha
        }
        // RC 480 (Tubigan 28)
        else if (variety.contains("RC 480") || variety.contains("RC480") || variety.contains("TUBIGAN 28") || variety.contains("TUBIGAN28")) {
            return 4700.0; // ~4.7 tons/ha
        }
        // RC 518
        else if (variety.contains("RC 518") || variety.contains("RC518")) {
            return 4600.0; // ~4.6 tons/ha
        }
        // RC 300
        else if (variety.contains("RC 300") || variety.contains("RC300") || variety.contains("NSIC RC 300") || variety.contains("NSIC RC300")) {
            return 4600.0; // ~4.6 tons/ha
        }
        // Other common NSIC varieties
        else if (variety.contains("RC 218") || variety.contains("RC218") || variety.contains("NSIC RC 218") || variety.contains("NSIC RC218")) {
            return 4700.0; // ~4.7 tons/ha
        }
        else if (variety.contains("RC 154") || variety.contains("RC154") || variety.contains("NSIC RC 154") || variety.contains("NSIC RC154")) {
            return 4900.0; // ~4.9 tons/ha
        }
        else if (variety.contains("RC 158") || variety.contains("RC158") || variety.contains("NSIC RC 158") || variety.contains("NSIC RC158")) {
            return 4800.0; // ~4.8 tons/ha
        }
        else if (variety.contains("RC 238") || variety.contains("RC238") || variety.contains("NSIC RC 238") || variety.contains("NSIC RC238")) {
            return 4600.0; // ~4.6 tons/ha
        }
        
        // Standard default for unknown/custom varieties: 4.5 tons/ha (4500 kg/ha)
        // This is the average yield for most Philippine rice varieties
        return 4500.0;
    }
    
    private void computeYield() {
        if (tvTotalKilograms == null || tvTotalSacks == null) {
            return;
        }
        
        try {
            // Get area
            String areaStr = etAreaHectares != null && etAreaHectares.getText() != null 
                    ? etAreaHectares.getText().toString().trim() : "";
            
            if (areaStr.isEmpty()) {
                tvTotalKilograms.setText("0 kg");
                tvTotalSacks.setText("0 sako");
                return;
            }
            
            double area = Double.parseDouble(areaStr);
            if (area <= 0) {
                tvTotalKilograms.setText("0 kg");
                tvTotalSacks.setText("0 sako");
                return;
            }
            
            // Get variety name
            String varietyName = etRiceVariety != null && etRiceVariety.getText() != null 
                    ? etRiceVariety.getText().toString().trim() : "";
            
            // Get base yield per hectare (in kg/ha) based on variety
            double baseYieldPerHectareKg = getDefaultYieldPerHectare(varietyName);
            
            // Apply method adjustment
            double adjustedYieldPerHectareKg = baseYieldPerHectareKg;
            if (rgPlantingMethod != null) {
                int selectedMethodId = rgPlantingMethod.getCheckedRadioButtonId();
                if (selectedMethodId == R.id.rbLipatTanim) {
                    // Lipat-tanim: +10%
                    adjustedYieldPerHectareKg = baseYieldPerHectareKg * 1.10;
                } else if (selectedMethodId == R.id.rbSabogTanim) {
                    // Sabog-tanim: -5%
                    adjustedYieldPerHectareKg = baseYieldPerHectareKg * 0.95;
                }
            }
            
            // Calculate total yield in kg: adjusted yield per hectare × area
            double totalKilograms = adjustedYieldPerHectareKg * area;
            
            // Calculate total sacks: total kg ÷ 50 kg per sack
            double totalSacks = totalKilograms / 50.0;
            
            // Format and display
            if (totalKilograms >= 1000) {
                // Show in tons if >= 1000 kg
                double tons = totalKilograms / 1000.0;
                tvTotalKilograms.setText(String.format(Locale.getDefault(), "%.2f tonelada (%.0f kg)", tons, totalKilograms));
            } else {
                tvTotalKilograms.setText(String.format(Locale.getDefault(), "%.0f kg", totalKilograms));
            }
            
            tvTotalSacks.setText(String.format(Locale.getDefault(), "%.1f sako", totalSacks));
            
        } catch (NumberFormatException e) {
            tvTotalKilograms.setText("0 kg");
            tvTotalSacks.setText("0 sako");
        }
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

    // InputFilter for letters and numbers only (for rice variety)
    private static class LettersAndNumbersOnlyFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source == null || source.length() == 0) {
                return null; // Accept deletion
            }

            StringBuilder sb = new StringBuilder();
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
                    sb.append(c);
                }
            }

            // If all characters are valid, return null to accept the input
            if (sb.length() == (end - start)) {
                return null;
            }

            // Return only valid characters
            return sb.toString();
        }
    }

    // InputFilter to prevent leading zeros (except for decimal like 0.5)
    private static class NoLeadingZeroFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source == null || source.length() == 0) {
                return null; // Accept deletion
            }

            String currentText = dest.toString();
            String newValue = currentText.substring(0, dstart) + source.toString() + currentText.substring(dend);
            
            // Allow empty or just decimal point
            if (newValue.isEmpty() || newValue.equals(".")) {
                return null;
            }
            
            // Allow decimal point and numbers after it (like 0.5, 0.1)
            if (newValue.contains(".")) {
                String[] parts = newValue.split("\\.");
                if (parts.length == 2) {
                    // If there's a decimal part, allow "0." or single "0" before decimal
                    if (parts[0].isEmpty() || parts[0].equals("0")) {
                        return null; // Allow 0.5, 0.1, etc.
                    }
                    // Check if integer part starts with 0 and has more digits (like 01.5)
                    if (parts[0].length() > 1 && parts[0].startsWith("0")) {
                        return ""; // Reject 01.5, 02.5, etc.
                    }
                }
            } else {
                // No decimal point - check for leading zeros
                // Allow single "0" but reject "01", "02", "03", etc.
                if (newValue.length() > 1 && newValue.startsWith("0")) {
                    return ""; // Reject 01, 02, 03, etc.
                }
            }
            
            return null; // Accept the input
        }
    }

}

