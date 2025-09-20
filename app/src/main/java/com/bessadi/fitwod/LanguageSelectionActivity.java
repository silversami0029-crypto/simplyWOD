package com.bessadi.fitwod;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bessadi.fitwod.R;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelectionActivity extends BaseActivity {
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        listView = findViewById(R.id.language_list);

        // Configure window size
        Window window = getWindow();
        if (window != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);

            // Set width to match parent and height to 250dp
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    350,
                    getResources().getDisplayMetrics()
            );

            window.setLayout(width, height);

            // Remove default dialog background/padding
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Optional: Position the dialog if needed
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP; // Or whatever position you prefer
            window.setAttributes(params);
        }

        // Create list of available languages
        List<LanguageItem> languages = new ArrayList<>();
        languages.add(new LanguageItem("English", ""));
     //   languages.add(new LanguageItem("Español", "es"));
        languages.add(new LanguageItem("Français", "fr"));
       // languages.add(new LanguageItem("Deutsch", "de"));
        languages.add(new LanguageItem("Turkish", "tr"));
        languages.add(new LanguageItem("العربية", "ar"));
       // languages.add(new LanguageItem("हिन्दी", "hi"));

        LanguageAdapter adapter = new LanguageAdapter(this, languages);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LanguageItem selectedLanguage = (LanguageItem) parent.getItemAtPosition(position);
                changeLanguage(selectedLanguage.getCode());
            }
        });
    }

    private void changeLanguage(String languageCode) {
        LanguageManager.setLocale(this, languageCode);

        // Restart the app to apply language changes
        Intent intent = new Intent(this, Menu_Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Language item class
    public static class LanguageItem {
        private String name;
        private String code;

        public LanguageItem(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Custom adapter for language list
    public static class LanguageAdapter extends ArrayAdapter<LanguageItem> {
        public LanguageAdapter(LanguageSelectionActivity context, List<LanguageItem> languages) {
            super(context, android.R.layout.simple_list_item_1, languages);
        }
    }
}
