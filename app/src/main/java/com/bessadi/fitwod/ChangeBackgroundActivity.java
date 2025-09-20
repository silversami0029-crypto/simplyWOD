package com.bessadi.fitwod;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bessadi.fitwod.R;

import java.util.Arrays;
import java.util.List;

public class ChangeBackgroundActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 101;
    private static final int PICK_IMAGE_REQUEST = 1;
    private LinearLayout chooseFromGallery;
    private LinearLayout defaultBackgrounds;
    private LinearLayout removeBackground;
    private PremiumManager premiumManager;

    // Array of default background drawable resources
    private final int[] defaultBackgroundsArray = {
            R.drawable.default_bg_1,
            R.drawable.default_bg_2,
            R.drawable.default_bg_3,
            R.drawable.default_bg_4,
            R.drawable.default_bg_5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_background);
        premiumManager = new PremiumManager(this);
        // Initialize the layout items
        chooseFromGallery = findViewById(R.id.chooseFromGallery);
        defaultBackgrounds = findViewById(R.id.defaultBackgrounds);
        removeBackground = findViewById(R.id.removeBackground);


        // Set click listeners
        setupClickListeners();
       // showSimpleTestDialog();

    }
    private void checkPremiumStatus() {
        if (!premiumManager.isPremiumUser()) {
            showPremiumLock();
        } else {
            enablePremiumFeatures();
        }
    }

    private void showPremiumLock() {
        // Add lock icons or disable functionality
        chooseFromGallery.setAlpha(0.5f);
        defaultBackgrounds.setAlpha(0.5f);

        chooseFromGallery.setOnClickListener(v -> showPremiumDialog());
        defaultBackgrounds.setOnClickListener(v -> showPremiumDialog());
    }
    private void enablePremiumFeatures() {
        chooseFromGallery.setAlpha(1.0f);
        defaultBackgrounds.setAlpha(1.0f);

        chooseFromGallery.setOnClickListener(v -> openGallery());
        defaultBackgrounds.setOnClickListener(v -> showDefaultBackgroundsDialog());
    }

    private void showPremiumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Premium Feature");
        builder.setMessage("Unlock analytics and custom backgrounds with our premium package!");
        builder.setPositiveButton("Upgrade Now", (dialog, which) -> {
           // premiumManager.launchPurchaseFlow(this);
        });
        builder.setNegativeButton("Later", null);
        builder.show();
    }





    private void setupClickListeners() {
        // Choose from Gallery click listener
        chooseFromGallery.setOnClickListener(v -> openGallery());

        // Default Backgrounds click listener
        defaultBackgrounds.setOnClickListener(v -> showDefaultBackgroundsDialog());

        // Remove Background click listener
        removeBackground.setOnClickListener(v -> removeBackground());
    }
    // Add this method for smooth selection animations
    private void animateSelection(View itemView, boolean selected) {
        if (selected) {
            itemView.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .alpha(1.0f)
                    .setDuration(200)
                    .start();
        } else {
            itemView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(0.7f)
                    .setDuration(200)
                    .start();
        }
    }

    private void updateSelectionWithAnimation(LinearLayout container, int selectedPosition) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View itemView = container.getChildAt(i);
            ImageView checkIcon = itemView.findViewById(R.id.selectionIndicator);
            View border = itemView.findViewById(R.id.borderIndicator);

            if (i == selectedPosition) {
                // Selected item
                checkIcon.setVisibility(View.VISIBLE);
                border.setVisibility(View.VISIBLE);
                animateSelection(itemView, true);
            } else {
                // Unselected items
                checkIcon.setVisibility(View.GONE);
                border.setVisibility(View.GONE);
                animateSelection(itemView, false);
            }
        }
    }
    private void showDefaultBackgroundsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Default Background");

        // Main container
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(40, 30, 40, 30);

        // Horizontal ScrollView for the backgrounds
        HorizontalScrollView horizontalScroll = new HorizontalScrollView(this);
        horizontalScroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Container for background items
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        horizontalScroll.addView(container);

        List<Integer> backgroundList = Arrays.asList(
                R.drawable.default_bg_1, R.drawable.default_bg_2, R.drawable.default_bg_3,
                R.drawable.default_bg_4, R.drawable.default_bg_5
        );

        for (int i = 0; i < backgroundList.size(); i++) {
            // Inflate the item layout
            View itemView = getLayoutInflater().inflate(R.layout.item_background, null);
            ImageView imageView = itemView.findViewById(R.id.backgroundImage);
            ImageView checkIcon = itemView.findViewById(R.id.selectionIndicator);
            View border = itemView.findViewById(R.id.borderIndicator);

            // Set the background image
            imageView.setImageResource(backgroundList.get(i));

            // Set layout parameters
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(180, 180);
            params.setMargins(12, 12, 12, 12);
            itemView.setLayoutParams(params);

            final int position = i;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("BackgroundSelect", "Background selected: " + position);

                    // Update visual selection
                    updateSelection(container, position);

                    // Apply the background
                    setDefaultBackground(position);

                    Toast.makeText(ChangeBackgroundActivity.this,
                            "Background applied! ✓", Toast.LENGTH_SHORT).show();
                }
            });

            container.addView(itemView);
        }

        mainContainer.addView(horizontalScroll);


        builder.setView(mainContainer);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateSelection(LinearLayout container, int selectedPosition) {
        // Update visual selection across all items
        for (int i = 0; i < container.getChildCount(); i++) {
            View itemView = container.getChildAt(i);
            ImageView checkIcon = itemView.findViewById(R.id.selectionIndicator);
            View border = itemView.findViewById(R.id.borderIndicator);

            if (i == selectedPosition) {
                // Selected item
                checkIcon.setVisibility(View.VISIBLE);
                border.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f);
            } else {
                // Unselected items
                checkIcon.setVisibility(View.GONE);
                border.setVisibility(View.GONE);
                itemView.setAlpha(0.7f);
            }
        }
    }

    private void applySelectedBackground() {
        // Implement this to apply the selected background
        // You might want to track the last selected position
        Toast.makeText(this, "Background applied successfully!", Toast.LENGTH_SHORT).show();
    }
    private void showAlternativeBackgroundDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Default Background");

        // Use a horizontal ScrollView with a LinearLayout
        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(50, 50, 50, 50);

        ScrollView horizontalScroll = new ScrollView(this);
        horizontalScroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        horizontalScroll.addView(container);

        List<Integer> backgroundList = Arrays.asList(
                R.drawable.default_bg_1, R.drawable.default_bg_2, R.drawable.default_bg_3,
                R.drawable.gradient_background, R.drawable.default_bg_5, R.drawable.default_bg_fitness
        );

        for (int i = 0; i < backgroundList.size(); i++) {
            FrameLayout itemLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.item_background, null);
            ImageView imageView = itemLayout.findViewById(R.id.backgroundImage);
            ImageView checkIcon = itemLayout.findViewById(R.id.selectionIndicator);
            View border = itemLayout.findViewById(R.id.borderIndicator);

            imageView.setImageResource(backgroundList.get(i));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
            params.setMargins(8, 8, 8, 8);
            itemLayout.setLayoutParams(params);

            final int position = i;
            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("AlternativeClick", "Background clicked: " + position);
                    setDefaultBackground(position);
                }
            });

            container.addView(itemLayout);
        }

        mainContainer.addView(horizontalScroll);
        builder.setView(mainContainer);
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }
    private void setDefaultBackground(int backgroundIndex) {
        if (backgroundIndex >= 0 && backgroundIndex < defaultBackgroundsArray.length) {
            int selectedDrawable = defaultBackgroundsArray[backgroundIndex];

            // Save the drawable resource ID instead of URI
            saveDefaultBackgroundPreference(selectedDrawable);

            Toast.makeText(this, "Default background applied!", Toast.LENGTH_SHORT).show();

            // Finish activity to return to MenuActivity
            finish();
        }
    }

    private void debugViewHierarchy(View view, int depth) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }

        Log.d("ViewHierarchy", indent.toString() + view.getClass().getSimpleName() +
                " [clickable=" + view.isClickable() +
                ", enabled=" + view.isEnabled() +
                ", visibility=" + getVisibilityString(view.getVisibility()) + "]");

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                debugViewHierarchy(group.getChildAt(i), depth + 1);
            }
        }
    }

    private String getVisibilityString(int visibility) {
        switch (visibility) {
            case View.VISIBLE: return "VISIBLE";
            case View.INVISIBLE: return "INVISIBLE";
            case View.GONE: return "GONE";
            default: return "UNKNOWN";
        }
    }


    private void postGridViewCheck(final GridView gridView, final DefaultBackgroundsAdapter adapter) {
        gridView.post(new Runnable() {
            @Override
            public void run() {
                Log.d("BackgroundDebug", "GridView visibility: " + gridView.getVisibility());
                Log.d("BackgroundDebug", "GridView width: " + gridView.getWidth() + ", height: " + gridView.getHeight());
                Log.d("BackgroundDebug", "GridView child count: " + gridView.getChildCount());
                Log.d("BackgroundDebug", "Adapter count: " + adapter.getCount());

                // Check if items are actually visible
                for (int i = 0; i < gridView.getChildCount(); i++) {
                    View child = gridView.getChildAt(i);
                    if (child != null) {
                        Log.d("BackgroundDebug", "Child " + i + " visibility: " + child.getVisibility());
                        Log.d("BackgroundDebug", "Child " + i + " width: " + child.getWidth() + ", height: " + child.getHeight());
                    }
                }
            }
        });
    }
    private void saveDefaultBackgroundPreference(int drawableResourceId) {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Remove any custom gallery image
        editor.remove("background_uri");

        // Save the default background resource ID
        editor.putInt("default_background_id", drawableResourceId);
        editor.putBoolean("is_default_background", true);
        editor.apply();

        Log.d("Background", "Default background saved: " + drawableResourceId);
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Use the new photo picker (Android 13+)
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            // Use traditional method for older versions
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            String[] mimeTypes = {"image/jpeg", "image/png", "image/jpg"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }
    }

    private void removeBackground() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Remove both custom and default background preferences
        editor.remove("background_uri");
        editor.remove("default_background_id");
        editor.remove("is_default_background");
        editor.apply();

        Toast.makeText(this, "Background removed! Using default.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                saveGalleryBackgroundPreference(selectedImageUri);
                Toast.makeText(this, "Custom background applied!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    private void showSimpleTestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Simple Test Dialog");

        // Create a simple layout with clickable views
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 50, 50, 50);

        for (int i = 0; i < 3; i++) {
            Button button = new Button(this);
            button.setText("Test Button " + (i + 1));
            button.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
           // button.setMargin(10);

            final int index = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("SimpleTest", "Button " + index + " clicked in dialog");
                    Toast.makeText(ChangeBackgroundActivity.this,
                            "Button " + index + " works!", Toast.LENGTH_SHORT).show();
                }
            });

            container.addView(button);
        }

        builder.setView(container);
        builder.setNegativeButton("Close", null);
        builder.show();
    }
    private void saveGalleryBackgroundPreference(Uri imageUri) {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Remove default background preference
        editor.remove("default_background_id");
        editor.remove("is_default_background");

        // Save the gallery image URI
        editor.putString("background_uri", imageUri.toString());
        editor.apply();

        Log.d("Background", "Gallery background saved: " + imageUri.toString());
    }
}