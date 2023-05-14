package com.example.wordle;

import android.util.Log;

import java.util.Arrays;


public class GameManager {
    public enum Status {
        IN_PROGRESS, /* When the game is still running and the user hadn't guessed the word yet */
        VICTORY,     /* When the user guessed the word correctly */
        LOSS;        /* When the user couldn't manage to guess the word in the attempts given */

        public int getValue() {
            switch (this) {
                case VICTORY: return 1;
                case IN_PROGRESS: return 0;
                case LOSS: return -1;
                default: return -2;
            }
        }
    }

    // The secret word which the user needs to guess:
    private Word secret;

    // The amount of times the user had tried to guess the word:
    private int attempts;

    // A dictionary containing every possible word for the game:
    private final Dictionary DICT;

    // The GUI for the game, handles every aspect of the UI:
    private final NormalWordleActivity wordleUI;

    // The status of the game, whether it's in progress/the user won/the user lost:
    private Status gameStatus;

    // The maximum amount of guesses the user has in game:
    private final static int ATTEMPTS_LIM = 6;

    // The words which the user had guessed:
    private final String[] guesses = new String[ATTEMPTS_LIM];

    // The status of each of the letters in the keyboard:
    private final Word.LetterStatus[] keyboardStatus = new Word.LetterStatus[26];

    public GameManager(NormalWordleActivity wordleUI) {
        this.wordleUI = wordleUI;
        DICT = new Dictionary(wordleUI.getAssets());
        this.restart();
    }

    public void restart() {
        this.wordleUI.clear();
        this.secret = new Word(DICT.getRandomWord());
        this.attempts = 0;
        this.gameStatus = Status.IN_PROGRESS;
        Arrays.fill(guesses, "");
        Arrays.fill(keyboardStatus, null);
        Log.v("Correct answer", this.secret.getWord());
    }

    /**
     * Receives a guess from the user, checks the guess and changes the UI and other app attributes
     * accordingly.
     * @param guess The guess of the user, should be exactly 5 letters long.
     */
    public void checkWord(String guess) {
        if (DICT.isInDB(guess)) {
            // Checking if the user already wrote this guess:
            if (!alreadyGuessedWord(guess)) {
                // Checking the state of the letters using the Word class and adding them to the
                // keyboard statuses:
                Word.LetterStatus[] letterStatuses = this.secret.checkWord(guess);
                updateKeyboardStatuses(guess.toCharArray(), letterStatuses);

                // Updating the board according to the word:
                this.wordleUI.updateBoardLetters(letterStatuses);

                // Updating the keyboard according to the word:
                this.wordleUI.updateKeyBoardColor(this.keyboardStatus);

                // Adding 1 to the amount of guesses:
                this.addAttempt();

                // Checking if the the user guessed the word right:
                if (guess.equals(this.secret.getWord())) {
                    this.gameStatus = Status.VICTORY;
                }
                else if (this.attempts >= ATTEMPTS_LIM) {
                    this.gameStatus = Status.LOSS;
                }

                // Adding the guess to the guesses:
                guesses[this.attempts - 1] = guess;
            }
            else {
                // If the user already guessed this word:
                wordleUI.duplicateWordMsg(this.attempts);
            }
        }
        else {
            // If the word isn't in the database:
            Log.d("gm", "word not in dict");
            wordleUI.wordNotInDictMsg(this.attempts, guess);
        }
    }

    public boolean alreadyGuessedWord(String guess) {
        for (String pastGuess : guesses) {
            if (pastGuess.equals(guess)) {
                return true;
            }
        }
        return false;
    }

    public void updateKeyboardStatuses(char[] letters, Word.LetterStatus[] letterStatuses) {
        for (int i = 0; i < letters.length; i++) {
            if (keyboardStatus[letters[i] - 'A'] != null) {
                switch (this.keyboardStatus[letters[i] - 'A']) {
                    case CORRECT: {
                        break;
                    }
                    case DISPLACED: {
                        if (letterStatuses[i] == Word.LetterStatus.CORRECT) {
                            this.keyboardStatus[letters[i] - 'A'] = letterStatuses[i];
                        }
                        break;
                    }
                    case INCORRECT: {
                        if (letterStatuses[i] != Word.LetterStatus.INCORRECT) {
                            this.keyboardStatus[letters[i] - 'A'] = letterStatuses[i];
                        }
                        break;
                    }
                }
            }
            else {
                this.keyboardStatus[letters[i] - 'A'] = letterStatuses[i];
            }
        }
    }

    public void addAttempt() {
        this.attempts = Math.min(this.attempts + 1, ATTEMPTS_LIM);
    }

    public int getAttempts() {
        return this.attempts;
    }

    public Status getGameStatus() {
        return this.gameStatus;
    }

    public String getWord() {
        return this.secret.getWord();
     }

}
