package com.example.wordle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class OpeningActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnNormalMode;
    Button btnStatsMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);

        // Loading the buttons:
        btnNormalMode = findViewById(R.id.btn_normal_mode);
        btnStatsMode = findViewById(R.id.btn_stats_mode);

        // Setting the OnClickListener of the two buttons to our function:
        btnNormalMode.setOnClickListener(this);
        btnStatsMode.setOnClickListener(this);
    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {}
    );

    @Override
    public void onClick(View view) {
        if (view != null) {
            final int id = view.getId();
            if (id == R.id.btn_normal_mode) {
                // If the normal mode was clicked, launch the normal mode:
                Intent intent = new Intent(this, NormalWordleActivity.class);
                launcher.launch(intent);
            }
            else if (id == R.id.btn_stats_mode) {
                // If the statistics mode was clicked, launch the stats activity:
                Intent intent = new Intent(this, StatisticsActivity.class);
                // Telling the statistics activity it was NOT launched through a game:
                intent.putExtra("isPostGame", false);
                launcher.launch(intent);
            }
        }
    }
}