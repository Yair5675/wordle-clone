package com.example.wordle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class NormalWordleActivity extends AppCompatActivity implements View.OnClickListener {

    private Button[][] wordleBoard; /* The game board, where the guessed words will be shown */

    private Button btnEnter; /* The 'enter' button which is not a regular keyboard button */
    private Button[] keyboard; /* An array containing every button in our keyboard */
    private Button btnBackspace; /* The 'backspace' button which is not a regular keyboard button */
    private ConstraintLayout clKeyboard; /* The constraint layout containing the keyboard */

    private GameManager gm; /* A GameManager object which handles the logistics and graphics of the
                               game */
    private long startTime; /* The time when the user began the game (in milliseconds), used to
                               calculate the duration of the game later on */

    // Constants:
    private final static int KEYS_COUNT = 26; /* The amount of keys in our keyboard (excluding the
                                                 buttons 'enter' and 'backspace') */
    private final static int ROWS_COUNT = 6; /* The amount of rows in the board (i.e: the amount of
                                                guesses the user can make) */
    private final static int COL_COUNT = 5; /* The amount of columns in the board (i.e: the amount
                                               of letters in every word in the game) */

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_wordle);

        // Setting the keyboard's layout:
        clKeyboard = findViewById(R.id.keyboardLayout);
        // Setting the keyboard's length to the amount of keys:
        keyboard = new Button[KEYS_COUNT];

        // Saving the resources in a variable so that we won't need to constantly get them:
        Resources res = getResources(), keyboardRes = clKeyboard.getResources();

        // Saving the letter buttons first:
        int id;
        Button btn;
        for (char c = 'A'; c <= 'Z'; c++) {
            // Finding the right button by iterating through the alphabet and searching for the
            // string:
            id = keyboardRes.getIdentifier("btn" + c, "id",
                                                         this.getPackageName());
            btn = clKeyboard.findViewById(id);

            // Setting the buttons tag as its letter, this way we can find it later:
            btn.setTag(Character.toString(c));

            // Setting the OnClickListener of the button to our function:
            btn.setOnClickListener(this);

            // Adding the button to our array:
            keyboard[c - 'A'] = btn;
        }

        // Saving the 'enter' and 'backspace' buttons:
        btnEnter = findViewById(R.id.btnEnter);
        btnBackspace = findViewById(R.id.btnBackspace);

        // Setting the OnClickListener of the two buttons to our function:
        btnEnter.setOnClickListener(this);
        btnBackspace.setOnClickListener(this);

        // Loading the buttons of the board:
        wordleBoard = new Button[ROWS_COUNT][COL_COUNT];
        for (int row = 0; row < ROWS_COUNT; row++) {
            for (int col = 0; col < COL_COUNT; col++) {
                // Getting the id's of every button in the board using the row and column:
                id = res.getIdentifier(String.format("btnGuess%d_%d", row, col),
                                                  "id", getPackageName());
                // Loading the buttons into the board array using the id:
                wordleBoard[row][col] = findViewById(id);
            }
        }

        // Setting the game manager after every other attribute is defined:
        gm = new GameManager(this);

    }

    /**
     * A method used to reset the colors of the different letters in the keyboard.
     */
    public void resetKeyboardColor() {
        // Saving the resources in a variable so that we won't need to constantly get them:
        Resources res = getResources();
        // Changing the letters' colors:
        for (Button key : keyboard) {
            key.setBackgroundColor(res.getColor(R.color.normal_btn_background_clr));
        }
        // Changing the colors of the 'enter' button:
        btnEnter.setBackgroundColor(res.getColor(R.color.normal_btn_background_clr));
        btnEnter.setTextColor(res.getColor(R.color.btn_text_clr));

        // Changing the colors of the 'backspace' button:
        btnBackspace.setBackgroundColor(res.getColor(R.color.normal_btn_background_clr));
        btnBackspace.setTextColor(res.getColor(R.color.btn_text_clr));
    }

    /**
     * A method to reset every GUI part in the game, used when the activity is started/restarted.
     */
    public void clear() {
        // Clearing the colors in the keyboard:
        resetKeyboardColor();
        // Clearing the Wordle board:
        for (int row = 0; row < wordleBoard.length; row++) {
            clearBoardRow(row);
        }
        // Opening the keyboard:
        openKeyboard();
        // Setting the time to the current second the activity started:
        startTime = System.currentTimeMillis();
    }

    /**
     * A method for opening the keyboard when the game starts/restarts.
     */
    public void openKeyboard() {
        this.clKeyboard.setVisibility(View.VISIBLE);
    }

    /**
     * A method for closing the keyboard when the user won/lost.
     */
    public void closeKeyboard() {
        this.clKeyboard.setVisibility(View.GONE);
    }

    /**
     * Updates the colors of the letters upon the board on the current row.
     * @param guessed An array of Letter Statuses, according to which the letters in the current
     *                guess will be updated.
     */
    public void updateBoardLetters(Word.LetterStatus[] guessed) {
        // The current row is the amount of guesses, so we save that as a variable:
        int row = gm.getAttempts();
        // Updating the colors of the guessed letter in its row:
        for (int col = 0; col < guessed.length; col++) {
            changeBoardLetterColor(row, col, guessed[col]);
        }
    }

    /**
     * Updates a single letter in the array depending on its status within the board. The letter
     * will be black if it's not in the word, yellow if it's in the word but not in the correct
     * place, and green if it's in the word and in the correct place.
     * @param row The row index of the board where the letter is written.
     * @param col The column index of the board where the letter is written.
     * @param status The status of the current letter: INCORRECT/DISPLACED/CORRECT
     */
    public void changeBoardLetterColor(int row, int col, Word.LetterStatus status) {
        // Saving the resources in a variable so that we won't need to constantly get them:
        Resources res = getResources();
        switch (status) {
            case CORRECT: {
                wordleBoard[row][col].setBackgroundColor(res.getColor(R.color.correct_btn_bg_clr));
                break;
            }
            case DISPLACED: {
                wordleBoard[row][col].setBackgroundColor(res.getColor(R.color.displaced_btn_bg_clr));
                break;
            }
            case INCORRECT: {
                wordleBoard[row][col].setBackgroundColor(res.getColor(R.color.incorrect_btn_bg_clr));
                break;
            }
        }
    }

    /**
     * Given an array of Letter Statuses in the length of the abc (where every index corresponds to
     * a letter), the method changes the color of the letters in the keyboard according to their
     * status.
     * @param statuses An array of Letter Statuses in the length of the abc.
     */
    public void updateKeyBoardColor(Word.LetterStatus[] statuses) {
        for (char c = 'A'; c <= 'Z'; c++) {
            changeKeyColor(c, statuses[c - 'A']);
        }
    }

    /**
     * Changes the color of a single key in the keyboard depending on the given status.
     * @param letter The letter in the keyboard whose status needs to change.
     * @param status The status of the current letter: INCORRECT/DISPLACED/CORRECT
     */
    public void changeKeyColor(char letter, Word.LetterStatus status) {
        // The index of the button in our keyboard array:
        int btnIndex = letter - 'A';

        // Changing the color based on the status:

        // Saving the resources in a variable so that we won't need to constantly get them:
        Resources res = getResources();

        // Changing the color of the background:
        if (status == Word.LetterStatus.CORRECT) {
            keyboard[btnIndex].setBackgroundColor(res.getColor(R.color.correct_btn_bg_clr));
        }
        else if (status == Word.LetterStatus.DISPLACED) {
            keyboard[btnIndex].setBackgroundColor(res.getColor(R.color.displaced_btn_bg_clr));
        }
        else if (status == Word.LetterStatus.INCORRECT) {
            keyboard[btnIndex].setBackgroundColor(res.getColor(R.color.incorrect_btn_bg_clr));
        }
        // Setting the text color:
        keyboard[btnIndex].setTextColor(res.getColor(R.color.btn_text_clr));
    }

    public void clearBoardRow(int row) {
        // Saving the resources in a variable so that we won't need to constantly get them:
        Resources res = getResources();
        for (Button button : wordleBoard[row]) {
            button.setText("");
            button.setBackgroundColor(res.getColor(R.color.normal_btn_background_clr));
        }
    }

    public void wordNotInDictMsg(int row, String word) {
        String msg = getResources().getString(R.string.unknown_word_msg);
        Toast.makeText(this, String.format(msg, word),
                Toast.LENGTH_SHORT).show();
        clearBoardRow(row);
    }

    public void duplicateWordMsg(int row) {
        String msg = getResources().getString(R.string.duplicate_word_msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        clearBoardRow(row);
    }

    public void goToStats(GameManager.Status status) {
        // Loading the intent and saving all the stats for the next activity:
        Intent intent = new Intent(this, StatisticsActivity.class);
        Bundle stats = new Bundle();

        // Saving the game status (if the user won or lost):
        stats.putInt("gameStatus", status.getValue());

        // Saving the correct word:
        stats.putString("correctWord", gm.getWord());

        // Saving the amount of times the user guessed:
        stats.putInt("guesses", gm.getAttempts());

        // Saving the time it took the user to guess the word:
        final long duration = (System.currentTimeMillis() - startTime) / 1000;
        stats.putLong("timeSecs", duration);

        // Telling the statistics activity that it was activated from the game:
        stats.putBoolean("isPostGame", true);

        // Putting all the stats in the intent:
        intent.putExtras(stats);

        // Waiting 1.5 seconds before going to the the next activity:
        final int DELAY_MILLISECONDS = 1500;
        Handler handler = new Handler();
        handler.postDelayed(() -> statsActivity.launch(intent), DELAY_MILLISECONDS);
    }

    // A variable for launching the stats activity once the game is finished:
    ActivityResultLauncher<Intent> statsActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result != null) {
                        // Checking if the user wanted to go back to the menu or play again:
                        if (result.getResultCode() == 1) { // Menu option
                            setResult(2);
                            finish();
                        }
                        else { // Restart option
                            gm.restart();
                        }
                    }
                }
            }
    );
    
    @Override
    public void onClick(View view) {
        if (view != null) {
            String letter;
            final int id = view.getId();
            // Going over every key in the keyboard:
            for (Button key : keyboard) {
                // Checking if a key in the keyboard was clicked:
                if (key.getId() == id) {
                    letter = key.getText().toString();
                    // If one of the letters on the game board is empty, write the letter of the
                    // current key on it:
                    for (int col = 0; col < wordleBoard[gm.getAttempts()].length; col++) {
                        if (wordleBoard[gm.getAttempts()][col].getText().toString().isEmpty()) {
                            wordleBoard[gm.getAttempts()][col].setText(letter);
                            break;
                        }
                    }
                    break;
                }
            }
            // If the backspace button was clicked:
            if (id == R.id.btnBackspace) {
                // Going from the end of the current word backwards and searching for a letter:
                for (int col = wordleBoard[gm.getAttempts()].length - 1; col >= 0; col--) {
                    // If there is a letter, delete it:
                    if (!wordleBoard[gm.getAttempts()][col].getText().toString().isEmpty()) {
                        wordleBoard[gm.getAttempts()][col].setText("");
                        break;
                    }
                }
            }
            // If the enter button was clicked:
            else if (id == R.id.btnEnter) {
                // Build the word from the current row in the game board:
                StringBuilder guess = new StringBuilder();
                for (int col = 0; col < wordleBoard[gm.getAttempts()].length; col++) {
                    guess.append(wordleBoard[gm.getAttempts()][col].getText().toString());
                }
                guess = new StringBuilder(guess.toString().toUpperCase());

                // If the word is of length 5, then the user filled all the row:
                if (guess.length() == 5) {
                    // Checking the given word using the Game Manager:
                    gm.checkWord(guess.toString());
                    // Checking the status of the game:
                    GameManager.Status status = gm.getGameStatus();
                    // If the game is no longer in progress, close the keyboard and go to the stats
                    // activity:
                    if (status != GameManager.Status.IN_PROGRESS) {
                        closeKeyboard();
                        goToStats(status);
                    }
                }
            }
        }
    }
}