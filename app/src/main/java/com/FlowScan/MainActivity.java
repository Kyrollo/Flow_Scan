package com.FlowScan;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addListenerOnButton();
        setupLanguageSwitching();
    }

    public void addListenerOnButton() {
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.login_button);

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        String password = intent.getStringExtra("PASSWORD");
        usernameEditText.setText(username);
        passwordEditText.setText(password);

        loginButton.setOnClickListener(view -> doLogin());
    }

    private void setupLanguageSwitching() {
        ImageView iconArabic = findViewById(R.id.icon_arabic);
        ImageView iconEnglish = findViewById(R.id.icon_english);

        String currentLocale = getResources().getConfiguration().locale.getLanguage();

        iconArabic.setOnClickListener(view -> {
            if (!currentLocale.equals("ar")) {
                setLocale("ar");
            }
        });

        iconEnglish.setOnClickListener(view -> {
            if (!currentLocale.equals("en")) {
                setLocale("en");
            }
        });
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = new Configuration();
        config.setLocale(locale);
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(config, dm);

        // Restart the activity to apply the language change
        Intent intent = getIntent();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        intent.putExtra("USERNAME", username);
        intent.putExtra("PASSWORD", password);
        startActivity(intent);
    }

    private void doLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (username.equals("Admin") && password.equals("123")) {
            Toast.makeText(getApplicationContext(), getString(R.string.loginSucceeded), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        } else if (username.equals("user") && password.equals("456")) {
            Toast.makeText(getApplicationContext(), getString(R.string.loginSucceeded), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.loginFailed), Toast.LENGTH_LONG).show();
        }
    }
}