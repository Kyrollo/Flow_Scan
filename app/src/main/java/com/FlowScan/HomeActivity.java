package com.FlowScan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.FlowScan.Table.Data;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.FlowScan.DataBase.AppDatabase;
import com.FlowScan.menuPages.DataDisplayActivity;
import com.google.android.material.navigation.NavigationView;

import java.io.FileInputStream;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int PICK_EXCEL_FILE = 1;
    private DrawerLayout drawerLayout;
    private AppDatabase db;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupNavigationDrawer();
        setupLanguageSwitching();

        Button btnDataDisplay = findViewById(R.id.btnDataDisplay);
        Button btnDeleteAll = findViewById(R.id.btnDeleteAll);
        Button btnImport = findViewById(R.id.btnImport);

        // Initialize the database
        db = AppDatabase.getDatabase(this);

        btnDataDisplay.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, DataDisplayActivity.class);
            startActivity(intent);
        });
        btnDeleteAll.setOnClickListener(view -> showDeleteDialog());
        btnImport.setOnClickListener(view -> openFilePicker());

    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.deleteAllDialog))
                .setMessage(getString(R.string.confirmDeleteDialog))
                .setPositiveButton(getString(R.string.yesDeleteDialog), (dialog, which) -> deleteAllData())
                .setNegativeButton(getString(R.string.noDeleteDialog), (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteAllData() {
        db.dataDao().deleteAll();
        Toast.makeText(HomeActivity.this, getString(R.string.dataDeleted), Toast.LENGTH_SHORT).show();
    }

    private void importExcelFile(Uri fileUri) {
        try {
            FileInputStream inputStream = (FileInputStream) getContentResolver().openInputStream(fileUri);
            assert inputStream != null;
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                if (row == null){
                    // Skip creating loop if row is empty
                    break;
                }
                else{
                    String barCode = getCellValueAsString(row.getCell(0));
                    String description = getCellValueAsString(row.getCell(1));
                    String category = getCellValueAsString(row.getCell(2));
                    String status = getCellValueAsString(row.getCell(3));
                    String location = getCellValueAsString(row.getCell(4));

                    // Skip creating an item if the category description is empty or null
                    Data checkDataExist = db.dataDao().getDataByBarcode(barCode);

                    // Check if data already exists
                    if (checkDataExist == null) {
                        Data data = new Data(barCode, description, category, status, location);
                        db.dataDao().insert(data);
                    }
                }
            }
            workbook.close();
            inputStream.close();

            Toast.makeText(HomeActivity.this, getString(R.string.importSucceded), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(HomeActivity.this, getString(R.string.importFailed), Toast.LENGTH_SHORT).show();
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        startActivityForResult(intent, PICK_EXCEL_FILE);
    }

    @SuppressLint("SetTextI18n")
    private void setupNavigationDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Retrieve the username from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        // Find the TextView and set the username
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderTitle = headerView.findViewById(R.id.nav_header_title);
        navHeaderTitle.setText(getString(R.string.welcome) + username);

        Button btnDeleteAll = findViewById(R.id.btnDeleteAll);


        // Check if the username is equal to "user"
        if ("user".equals(username)) {
            // Hide the "Delete All" button
            btnDeleteAll.setVisibility(View.GONE);
        }
    }

    private void setupLanguageSwitching() {
        ImageView iconArabic = findViewById(R.id.icon_arabic);
        ImageView iconEnglish = findViewById(R.id.icon_english);

        String currentLocale = getResources().getConfiguration().locale.getLanguage();

        if (currentLocale.equals("ar")) {
            iconArabic.setVisibility(View.GONE);
        } else if (currentLocale.equals("en")) {
            iconEnglish.setVisibility(View.GONE);
        }

        iconArabic.setOnClickListener(view -> setLocale("ar"));
        iconEnglish.setOnClickListener(view -> setLocale("en"));
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = new Configuration();
        config.setLocale(locale);
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(config, dm);

        // Restart the current activity
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_EXCEL_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri fileUri = data.getData();
                importExcelFile(fileUri);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}