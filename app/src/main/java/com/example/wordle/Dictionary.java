package com.example.wordle;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;


public class Dictionary {
    private static final Random rnd = new Random(); /* Used to select random words */
    private final String[] WORDS; /* The database of words we have */

    public Dictionary(AssetManager assets) {
        // Reading the words from our database and saving them to WORDS:
        try {
            // Loading the file DATABASE.txt:
            Scanner reader = new Scanner(assets.open("DATABASE.txt"));
            // Adding all the words from the database:
            StringBuilder builder = new StringBuilder();
            while (reader.hasNextLine()) {
                builder.append(reader.nextLine()).append("\n");
            }
            WORDS = builder.toString().split("\n");
            // Sorting the database to later use binarySearch:
            Arrays.sort(WORDS);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize words - database not found");
        }
    }

    public String getRandomWord() {
        return WORDS[rnd.nextInt(WORDS.length)].toUpperCase();
    }

    public boolean isInDB(String word) {
        int idx = Arrays.binarySearch(WORDS, word.toLowerCase());
        Log.d("index", Integer.toString(idx));
        Log.d("Given word", word.toLowerCase());
        return idx >= 0;
    }
}
