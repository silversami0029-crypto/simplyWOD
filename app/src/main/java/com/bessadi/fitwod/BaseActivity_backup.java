package com.bessadi.fitwod;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity_backup extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        String language = LanguageManager.getLanguage(newBase);
        Context context = LanguageManager.setLocale(newBase, language);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Reset title to ensure it gets translated
        try {
            int label = getPackageManager().getActivityInfo(getComponentName(), 0).labelRes;
            if (label != 0) {
                setTitle(label);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update UI if language was changed while activity was in background
        String currentLanguage = LanguageManager.getLanguage(this);
        if (!currentLanguage.equals(getResources().getConfiguration().getLocales().get(0).getLanguage())) {
            recreate();
        }
    }
}
