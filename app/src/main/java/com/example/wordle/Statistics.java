package com.example.wordle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
// import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Statistics {

    private int winCount; /* The amount of time the user guessed the word correctly */
    private int lossCount; /* The amount of time the user couldn't guess the word correctly */
    private final int winPercent; /* The chance the user has to win based on past games */
    private final int played; /* The amount of times the user had played the app */
    private int streak; /* The current amount of games the user won in a row */
    private int maxStreak; /* The largest amount of games the user ever won in a row */
    private int bestTime; /* The shortest amount of time the user ever guessed a word */

    private final StatisticsActivity statsUI; /* UI object, allows us to access files */
    private final static String PATH = "statistics.txt"; /* The path to the statistics file */
    private final static int MAX_MINUTES = 20; /* The maximum amount of minutes displayed */

    public Statistics(StatisticsActivity statsActivity, GameManager.Status status, int time) {
        // Loading the statistics activity:
        this.statsUI = statsActivity;

        // Loading the rest of the values from the file:
        this.loadFromFile();
        // If the activity was launched through a game:
        if (status != GameManager.Status.IN_PROGRESS) {
            switch (status) {
                case VICTORY: {
                    // Adding 1 to the win count:
                    this.winCount++;
                    // Adding 1 to the streak:
                    this.streak++;
                    // Changing the best time if the current time is lower and if the user won:
                    if (this.bestTime > 0) {
                        this.bestTime = Math.min(this.bestTime, time);
                        // Making sure the time is below/equal to the max minute limit:
                        this.bestTime = Math.min(this.bestTime, MAX_MINUTES);
                    }
                    // If the best time is not defined yet:
                    else {
                        this.bestTime = time;
                    }
                    break;
                }
                case LOSS: {
                    this.lossCount++;
                    // Resetting the streak if the user lost:
                    this.streak = 0;
                    break;
                }
            }
        }
        // Changing the max streak if the current streak is higher:
        this.maxStreak = Math.max(this.streak, this.maxStreak);

        // Calculating the times the user played:
        played = winCount + lossCount;

        // Calculating the win percent:
        if (this.played != 0) {
            this.winPercent = (int) Math.round(100 * this.winCount / (double) this.played);
        }
        else {
            this.winPercent = 0;
        }
    }

    /**
     * Generates default statistics in case no former statistics were found
     */
    public void generateDefault() {
        this.winCount = 0;
        this.lossCount = 0;
        this.streak = 0;
        this.maxStreak = 0;
        this.bestTime = 0;
    }

    /**
     * Loads the values saved in statistics.txt by this format:
     * Line 1 - win count
     * Line 2 - loss count
     * Line 3 - current streak
     * Line 4 - max streak
     * Line 5 - best time (in seconds)
     */
    private void loadFromFile() {
        try {
            // Reading the file statistics.txt:
            FileInputStream preReader = this.statsUI.openFileInput(PATH);
            BufferedReader reader = new BufferedReader(new InputStreamReader(preReader));
            String dataLine;
            boolean loaded = false;
            int line = 1;
            while ((dataLine = reader.readLine()) != null && line <= 5) {
                // The first 5 lines are the wins count, loss count, the current streak, the highest
                // streak ever achieved, and the best time ever achieved (in seconds):
                switch (line++) {
                    case 1: {
                        winCount = Integer.parseInt(dataLine);
                        break;
                    }
                    case 2: {
                        lossCount = Integer.parseInt(dataLine);
                        break;
                    }
                    case 3: {
                        streak = Integer.parseInt(dataLine);
                        break;
                    }
                    case 4: {
                        maxStreak = Integer.parseInt(dataLine);
                        break;
                    }
                    case 5: {
                        bestTime = Integer.parseInt(dataLine);
                        // Preventing the best time from ever being Integer.MAX_VALUE:
                        if (bestTime == Integer.MAX_VALUE) {
                            bestTime = 0;
                            Log.e("statistics", "Best time appeared as Integer.MAX_VALUE, was reset to 0");
                        }
                        loaded = true;
                        break;
                    }
                }
            }
            if (!loaded) {
                Log.e("statistics", "Insufficient amount of data found, generated default");
                this.generateDefault();
            }
            reader.close();
            preReader.close();
        } catch (IOException e) {
            Log.e("statistics", "Couldn't find any data, generated default");
            this.generateDefault();
        }
    }

    /**
     * Generating a message which represents the time given to us.
     * @param timeSecs The amount of time specified in seconds (can be more than 60).
     * @return A message describing the amount of minutes and seconds of the time given.
     */
    @SuppressLint("DefaultLocale")
    public static String generateTimeMsg(int timeSecs) {
        String message;
        int minutes = timeSecs / 60, seconds = timeSecs % 60;
        // If the given time is more than a quarter of an hour, just say 'over 20 minutes':
        if (minutes >= MAX_MINUTES) {
            message = String.format("Over %d minutes", MAX_MINUTES);
        }
        // Checking if the message should say 'minutes' or 'minute':
        else if (minutes > 1) {
            message = minutes + " minutes";
            if (seconds > 1) {
                message += " and " + seconds + " seconds";
            }
            else if (seconds == 1) {
                message += " and 1 second";
            }
        }
        else if (minutes == 1) {
            message = "1 minute";
            if (seconds > 1) {
                message += " and " + seconds + " seconds";
            }
            else if (seconds == 1) {
                message += " and 1 second";
            }
        }
        else if (seconds > 1) {
            message = seconds + " seconds";
        }
        else if (seconds == 1) {
            message = "1 second";
        }
        else {
            message = "0 seconds";
        }
        return message;
    }

    public int getWinPercent() {
        return winPercent;
    }

    public int getPlayed() {
        return played;
    }

    public int getStreak() {
        return streak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public int getBestTime() {
        return bestTime;
    }

    private void writeData() {
        Log.d("stats", "starting to write");
        // Building all the info we want to write to the file:
        StringBuilder data = new StringBuilder();
        // Adding the win-count:
        data.append(this.winCount).append("\n");
        // Adding the loss-count:
        data.append(this.lossCount).append("\n");
        // Adding the current streak:
        data.append(this.streak).append("\n");
        // Adding the max streak:
        data.append(this.maxStreak).append("\n");
        // Adding the best time:
        data.append(this.bestTime).append("\n");

        try {
            // Creating the writer:
            FileOutputStream writer = this.statsUI.openFileOutput(PATH, Context.MODE_PRIVATE);
            // Writing the data we gathered before:
            writer.write(data.toString().getBytes());

            // Making a toast specifying the location of the file (optional):
            // Toast.makeText(statsUI, "Written to location: " + statsUI.getFilesDir() + "/" + PATH, Toast.LENGTH_LONG).show();

            // Closing the writer:
            writer.flush();
            writer.close();

            Log.d("stats", "Finished writing, should work");

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("stats", "Writing failed");
            throw new RuntimeException("Could not write to file");
        }
    }

    public void close() {
        // Writing the data we received to the file:
        this.writeData();
    }
}
