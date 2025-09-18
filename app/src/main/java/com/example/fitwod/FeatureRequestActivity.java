package com.example.fitwod;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bessadi.fitwod.R;

public class FeatureRequestActivity extends AppCompatActivity {

    private CheckBox willingCheckbox;
    private LinearLayout optionalFields;
    private EditText featureRequestEditText, contextEditText, nameEditText, emailEditText;
    private Button submitButton, clearButton;
    ImageButton goback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_feature);

        // Initialize views
        willingCheckbox = findViewById(R.id.willingCheckbox);
        optionalFields = findViewById(R.id.optionalFields);
        featureRequestEditText = findViewById(R.id.featureRequestEditText);
        contextEditText = findViewById(R.id.contextEditText);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        submitButton = findViewById(R.id.submitButton);
        clearButton = findViewById(R.id.clearButton);
        goback = findViewById(R.id.ib_back_db_request_feature);

        // Set up checkbox listener
        willingCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                optionalFields.setVisibility(View.VISIBLE);
            } else {
                optionalFields.setVisibility(View.GONE);
            }
        });

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goback_to_mainMenu();// goes back to main menu

            }
        });



        // Set up clear button
        clearButton.setOnClickListener(v -> clearForm());

        // Set up submit button
        submitButton.setOnClickListener(v -> submitForm());
    }

    private void clearForm() {
        featureRequestEditText.setText("");
        contextEditText.setText("");
        nameEditText.setText("");
        emailEditText.setText("");
        willingCheckbox.setChecked(false);
    }

    private void goback_to_mainMenu() {
        startActivity(new Intent(FeatureRequestActivity.this, Menu_Activity.class));
    }

    private void submitForm() {
        String featureRequest = featureRequestEditText.getText().toString().trim();
        String context = contextEditText.getText().toString().trim();
        boolean isWilling = willingCheckbox.isChecked();
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        // Validate form
        if (featureRequest.isEmpty()) {
            featureRequestEditText.setError("Please describe your feature request");
            return;
        }

        if (isWilling && email.isEmpty()) {
            emailEditText.setError("Please provide your email address");
            return;
        }

        // Send email intent
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"silversami0029@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feature Request for SmartWOD App");

        String emailBody = "Feature Request: " + featureRequest + "\n\n";
        emailBody += "Additional Context: " + context + "\n\n";
        emailBody += "Willing to answer more questions: " + (isWilling ? "Yes" : "No") + "\n";

        if (isWilling) {
            emailBody += "Name: " + name + "\n";
            emailBody += "Email: " + email + "\n";
        }

        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
