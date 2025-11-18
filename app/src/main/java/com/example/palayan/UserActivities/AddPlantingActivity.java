package com.example.palayan.UserActivities;

import android.app.DatePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.palayan.Adapter.CropCalendarAdapter;
import com.example.palayan.Adapter.CropCalendarPreviewAdapter;
import com.example.palayan.Helper.CropCalendarTask;
import com.example.palayan.Helper.RicePlanting;
import com.example.palayan.Helper.JournalStorageHelper;
import com.example.palayan.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddPlantingActivity extends AppCompatActivity {

    private TextView tvTitle, tvRiceFieldInfo, tvPlantingDate, tvRemove;
    private TextView tabSeedSelection, tabFertilizer, tabCropCalendar;
    private View indicatorSeedSelection, indicatorFertilizer, indicatorCropCalendar;
    private LinearLayout layoutSeedSelection, layoutFertilizer, layoutCropCalendar;
    private ImageView ivBack;
    private LinearLayout layoutBack, layoutPlantingDate, layoutCustomFertilizer;
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
        cropCalendarAdapter = new CropCalendarAdapter((task, isChecked) -> {
            // Task checked - update and save
            updateCropCalendarTasks();
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
                                
                                // Check if we have all 16 weeks, if not, regenerate
                                if (cropCalendarTasks.size() < 16 && p.getPlantingDate() != null && !p.getPlantingDate().isEmpty()) {
                                    android.util.Log.d("CropCalendar", "Only " + cropCalendarTasks.size() + " tasks found, regenerating all 16 weeks");
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
                                    // Sort by week number
                                    cropCalendarTasks.sort((t1, t2) -> Integer.compare(t1.getWeekNumber(), t2.getWeekNumber()));
                                    cropCalendarAdapter.updateTasks(cropCalendarTasks);
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
                // Update adapter with existing tasks
                if (isPreviewMode) {
                    cropCalendarPreviewAdapter.updateTasks(cropCalendarTasks);
                } else {
                    cropCalendarAdapter.updateTasks(cropCalendarTasks);
                }
            }
        });

        // Fertilizer strategy change
        rgFertilizerStrategy.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSarilingDiskarte) {
                layoutCustomFertilizer.setVisibility(View.VISIBLE);
            } else {
                layoutCustomFertilizer.setVisibility(View.GONE);
            }
        });
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
                    // Update adapter with existing tasks
                    if (isPreviewMode) {
                        cropCalendarPreviewAdapter.updateTasks(cropCalendarTasks);
                    } else {
                        cropCalendarAdapter.updateTasks(cropCalendarTasks);
                    }
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

            // Calculate week 1 start (7 days before planting for land preparation)
            Calendar week1Start = (Calendar) plantingCal.clone();
            week1Start.add(Calendar.DAY_OF_YEAR, -7);
            
            // Adjust to Monday of that week
            int dayOfWeek = week1Start.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
            week1Start.add(Calendar.DAY_OF_YEAR, -daysFromMonday);

            cropCalendarTasks.clear();
            
            // Ensure we're in preview mode when generating new tasks
            // This ensures all tasks are created as new, not updated
            boolean wasPreviewMode = isPreviewMode;
            boolean isNewPlanting = (plantingId == null || plantingId.isEmpty());
            isPreviewMode = true; // Force preview mode during generation to always create new tasks

            // Generate 16 weeks of tasks
            // Week 1: Land preparation
            addWeekTask(week1Start, 1, "Pag-aararo at paghahanda ng lupa", "preparation");

            // Week 2: Seed preparation
            Calendar week2Start = (Calendar) week1Start.clone();
            week2Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week2Start, 2, "Paghahanda at pagpapatubo ng binhi", "preparation");

            // Week 3: Planting
            Calendar week3Start = (Calendar) week2Start.clone();
            week3Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week3Start, 3, "Pagtatanim (transplanting o direct seeding)", "planting");

            // Week 4: Early weed and pest control
            Calendar week4Start = (Calendar) week3Start.clone();
            week4Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week4Start, 4, "Maagang pagkontrol ng damo at peste", "pest_control");

            // Week 5: First fertilization
            Calendar week5Start = (Calendar) week4Start.clone();
            week5Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week5Start, 5, "Unang abono (Topdress 1)", "fertilizer");

            // Week 6: Water management
            Calendar week6Start = (Calendar) week5Start.clone();
            week6Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week6Start, 6, "Pag-aalaga sa tubig (tamang level ng tubig)", "monitoring");

            // Week 7: Second fertilization
            Calendar week7Start = (Calendar) week6Start.clone();
            week7Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week7Start, 7, "Pangalawang abono (Topdress 2)", "fertilizer");

            // Week 8: Plant and nutrient inspection
            Calendar week8Start = (Calendar) week7Start.clone();
            week8Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week8Start, 8, "Pagsusuri ng tanim at nutrients", "monitoring");

            // Week 9: Potassium fertilizer
            Calendar week9Start = (Calendar) week8Start.clone();
            week9Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week9Start, 9, "Paglalagay ng potassium (K) fertilizer", "fertilizer");

            // Week 10: Panicle initiation monitoring
            Calendar week10Start = (Calendar) week9Start.clone();
            week10Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week10Start, 10, "Pagbabantay sa pamumuo ng usbong (panicle initiation)", "monitoring");

            // Week 11: Booting stage pest control
            Calendar week11Start = (Calendar) week10Start.clone();
            week11Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week11Start, 11, "Pagkontrol ng peste sa booting stage", "pest_control");

            // Week 12: Flowering water management
            Calendar week12Start = (Calendar) week11Start.clone();
            week12Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week12Start, 12, "Pag-aalaga sa tubig habang namumulaklak", "monitoring");

            // Week 13: Grain filling monitoring
            Calendar week13Start = (Calendar) week12Start.clone();
            week13Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week13Start, 13, "Pagbabantay sa pagpuno ng butil (grain filling)", "monitoring");

            // Week 14: Soil drying
            Calendar week14Start = (Calendar) week13Start.clone();
            week14Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week14Start, 14, "Pagpapatuyo ng lupa (bawasan ang tubig)", "monitoring");

            // Week 15: Pre-harvest inspection
            Calendar week15Start = (Calendar) week14Start.clone();
            week15Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week15Start, 15, "Pagsusuri bago ang pag-ani", "harvest");

            // Week 16: Harvest
            Calendar week16Start = (Calendar) week15Start.clone();
            week16Start.add(Calendar.DAY_OF_YEAR, 7);
            addWeekTask(week16Start, 16, "Pag-ani", "harvest");

            // Sort tasks by week number
            cropCalendarTasks.sort((t1, t2) -> Integer.compare(t1.getWeekNumber(), t2.getWeekNumber()));
            
            // Debug log - check how many tasks were actually added
            android.util.Log.d("CropCalendar", "Generated " + cropCalendarTasks.size() + " tasks. Expected: 16");
            for (CropCalendarTask task : cropCalendarTasks) {
                android.util.Log.d("CropCalendar", "Week " + task.getWeekNumber() + ": " + task.getTaskName());
            }
            
            // Restore preview mode state - but keep preview mode for new plantings
            if (isNewPlanting) {
                // New planting - always use preview mode
                isPreviewMode = true;
            } else {
                // Editing - restore original state
                isPreviewMode = wasPreviewMode;
            }
            
            // Ensure adapter is set correctly
            if (isPreviewMode) {
                if (rvCropCalendar.getAdapter() != cropCalendarPreviewAdapter) {
                    rvCropCalendar.setAdapter(cropCalendarPreviewAdapter);
                }
                cropCalendarPreviewAdapter.updateTasks(cropCalendarTasks);
                // Force RecyclerView to measure all items
                rvCropCalendar.post(() -> {
                    if (rvCropCalendar instanceof com.example.palayan.Helper.NonScrollableRecyclerView) {
                        ((com.example.palayan.Helper.NonScrollableRecyclerView) rvCropCalendar).forceMeasure();
                    }
                    rvCropCalendar.requestLayout();
                    android.util.Log.d("CropCalendar", "Forced layout request for preview adapter");
                });
            } else {
                if (rvCropCalendar.getAdapter() != cropCalendarAdapter) {
                    rvCropCalendar.setAdapter(cropCalendarAdapter);
                }
                cropCalendarAdapter.updateTasks(cropCalendarTasks);
                // Force RecyclerView to measure all items
                rvCropCalendar.post(() -> {
                    if (rvCropCalendar instanceof com.example.palayan.Helper.NonScrollableRecyclerView) {
                        ((com.example.palayan.Helper.NonScrollableRecyclerView) rvCropCalendar).forceMeasure();
                    }
                    rvCropCalendar.requestLayout();
                    android.util.Log.d("CropCalendar", "Forced layout request for interactive adapter");
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addWeekTask(Calendar weekStart, int weekNumber, String taskName, String taskType) {
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
                if (t.getWeekNumber() == weekNumber) {
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
            android.util.Log.d("CropCalendar", "Updated existing task for week " + weekNumber);
        } else {
            // Create new task - always add new task in preview mode
            task = new CropCalendarTask(taskName, taskName, 0, taskType);
            task.setScheduledDate(scheduledDate);
            task.setWeekRange(weekRange);
            task.setWeekNumber(weekNumber);
            cropCalendarTasks.add(task);
            android.util.Log.d("CropCalendar", "Added new task for week " + weekNumber + ". Total tasks: " + cropCalendarTasks.size());
        }
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
        // Load existing crop calendar tasks if editing
        if (plantingId != null && !plantingId.isEmpty()) {
            JournalStorageHelper.loadRicePlantings(this, riceFieldId, new JournalStorageHelper.OnPlantingsLoadedListener() {
                @Override
                public void onSuccess(List<RicePlanting> plantings) {
                    for (RicePlanting p : plantings) {
                        if (p.getId().equals(plantingId)) {
                            if (p.getCropCalendarTasks() != null && !p.getCropCalendarTasks().isEmpty()) {
                                cropCalendarTasks = p.getCropCalendarTasks();
                                android.util.Log.d("CropCalendar", "Loaded " + cropCalendarTasks.size() + " existing tasks from storage");
                                
                                // Check if we have all 16 weeks, if not, regenerate
                                if (cropCalendarTasks.size() < 16 && p.getPlantingDate() != null && !p.getPlantingDate().isEmpty()) {
                                    android.util.Log.d("CropCalendar", "Only " + cropCalendarTasks.size() + " tasks found, regenerating all 16 weeks");
                                    tvPlantingDate.setText(p.getPlantingDate());
                                    generateCropCalendarTasks();
                                } else {
                                    // Sort by week number
                                    cropCalendarTasks.sort((t1, t2) -> Integer.compare(t1.getWeekNumber(), t2.getWeekNumber()));
                                    // Switch to interactive mode
                                    isPreviewMode = false;
                                    rvCropCalendar.setAdapter(cropCalendarAdapter);
                                    cropCalendarAdapter.updateTasks(cropCalendarTasks);
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

