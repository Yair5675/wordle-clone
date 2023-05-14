package com.example.wordle;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class StatisticsActivity extends AppCompatActivity implements View.OnClickListener {
    // The TextViews that present the data to the user:
    private TextView tvPlayedData; /* Presenting how many times the user played */
    private TextView tvWinPercData; /* Presenting the win percent of the user */
    private TextView tvStreakData; /* Presenting the current streak of the user */
    private TextView tvMaxStreakData; /* Presenting the largest ever streak of the user */
    private TextView tvCorrectWord; /* Presenting the correct word from the current game */
    private TextView tvGuessesData; /* Presenting the amount of words the user guessed this game */
    private TextView tvTimeData; /* Presenting the amount of time the current game lasted */
    private TextView tvBestTimeData; /* Presenting the shortest ever time a game lasted */

    // The buttons in the screen:
    private Button btnRestart; /* Restarts a game with a new word */
    private Button btnToMenu; /* Takes the user back to the menu */
    private Button btnClearStats; /* Clears the statistics which were already saved */

    // A Statistics object, allows us to write and access info from previous games:
    private Statistics stats;

    private boolean IS_POST_GAME; /* Whether the statistics activity is after a game or not */

    // In order to test reading/writing files, there is a button to reset the stored data. It will
    // be available only for programmers who edit the code, hence this boolean variable:
    private final static boolean SHOW_RESET_STATS_BTN = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        // If stats is null, then the activity is started for the first time:
        if (stats == null) {
            Log.i("onCreate", "OnCreate called for the first time");
            Intent intent = getIntent();

            // Checking whether the activity was started through the game or the menu:
            IS_POST_GAME = intent.getBooleanExtra("isPostGame", true);

            // Loading all the textViews:
            tvPlayedData = findViewById(R.id.tv_played_data);
            tvWinPercData = findViewById(R.id.tv_win_perc_data);
            tvStreakData = findViewById(R.id.tv_streak_data);
            tvMaxStreakData = findViewById(R.id.tv_max_streak_data);
            tvCorrectWord = findViewById(R.id.tv_correct_word);
            tvGuessesData = findViewById(R.id.tv_guess_data);
            tvTimeData = findViewById(R.id.tv_time_data);
            tvBestTimeData = findViewById(R.id.tv_best_time);

            // Loading the buttons:
            btnRestart = findViewById(R.id.btn_restart);
            btnToMenu = findViewById(R.id.btn_back_to_menu);
            btnClearStats = findViewById(R.id.btn_clear_stats);

            // Setting the buttons' onClickListener:
            btnRestart.setOnClickListener(this);
            btnToMenu.setOnClickListener(this);
            btnClearStats.setOnClickListener(this);

            // Setting the stats attribute only if the activity was started through the game:
            if (IS_POST_GAME) {
                final boolean won = intent.getIntExtra("gameStatus", -1) > 0;
                final int time = (int) intent.getLongExtra("timeSecs", 0);
                stats = new Statistics(this,
                        won ? GameManager.Status.VICTORY : GameManager.Status.LOSS, time);
                // Showing all the text-views and buttons:
                tvCorrectWord.setVisibility(View.VISIBLE);
                tvGuessesData.setVisibility(View.VISIBLE);
                tvTimeData.setVisibility(View.VISIBLE);
                btnRestart.setVisibility(View.VISIBLE);
            } else {
                // Creating a new statistics object and telling it the activity wasn't started through
                // the game:
                stats = new Statistics(this, GameManager.Status.IN_PROGRESS, -1);
                // Hiding all the text-views and buttons:
                tvCorrectWord.setVisibility(View.GONE);
                tvGuessesData.setVisibility(View.GONE);
                tvTimeData.setVisibility(View.GONE);
                btnRestart.setVisibility(View.GONE);
            }

            // Setting the values in the textViews:
            setTextViewsStatistics(intent);

            // Showing the 'clear stats' button only if the programmer decided to:
            if (SHOW_RESET_STATS_BTN) {
                btnClearStats.setVisibility(View.VISIBLE);
            } else {
                btnClearStats.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stats.close();
    }

    @SuppressLint("DefaultLocale")
    private void setTextViewsStatistics(Intent intent) {
        // Saving the resources in a variable so that we won't need to constantly get them:
        Resources res = getResources();

        // Setting the amount of games played:
        int played = stats.getPlayed();
        tvPlayedData.setText(String.format("%d", played));

        // Setting the win percent:
        int winPerc = stats.getWinPercent();
        tvWinPercData.setText(String.format("%d", winPerc));

        // Setting the Current streak:
        int streak = stats.getStreak();
        tvStreakData.setText(String.format("%d", streak));

        // Setting the max-streak:
        int maxStreak = stats.getMaxStreak();
        tvMaxStreakData.setText(String.format("%d", maxStreak));

        if (IS_POST_GAME) {
            // Setting the correct word only if the activity was started through the game:
            String correctWord = intent.getStringExtra("correctWord");
            String correctWordMsg = res.getString(R.string.correct_word_txt);
            Log.d("stats", String.format(correctWordMsg, correctWord));
            tvCorrectWord.setText(String.format(correctWordMsg, correctWord));

            // Setting the number of guesses only if the activity was started through the game:
            int guesses = intent.getIntExtra("guesses", 0);
            String guessesMsg = res.getString(R.string.guesses_data);
            tvGuessesData.setText(String.format(guessesMsg, guesses));

            // Setting the time only if the activity was started through the game:
            long timeSecs = intent.getLongExtra("timeSecs", 0);
            String time = Statistics.generateTimeMsg((int) timeSecs);
            String timeMsg = res.getString(R.string.time_data);
            tvTimeData.setText(String.format(timeMsg, time));
        }

        // Setting the best time:
        String bestTime = Statistics.generateTimeMsg(stats.getBestTime());
        String bestTimeMsg = res.getString(R.string.best_time);
        tvBestTimeData.setText(String.format(bestTimeMsg, bestTime));
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        // Checking the 'restart game' button:
        if (id == btnRestart.getId()) {
            // Closing the statistics object while saving the new data:
            stats.close();
            // Going to the wordle again:
            setResult(2);
            finish();
        }
        // Checking the 'back to menu' button:
        else if (id == btnToMenu.getId()) {
            // Closing the statistics object while saving the new data:
            stats.close();
            // Going back to the menu:
            setResult(1);
            finish();
        }
        // Checking the 'clear stats' button and making sure it should be visible:
        else if (id == btnClearStats.getId() && SHOW_RESET_STATS_BTN) {
            // Setting all the statistics to default:
            this.stats.generateDefault();
            Toast.makeText(this, "Cleared statistics!", Toast.LENGTH_SHORT).show();
        }
    }

}