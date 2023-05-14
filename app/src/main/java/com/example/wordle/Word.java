package com.example.wordle;

import java.util.Arrays;


public class Word {

    public enum LetterStatus {
        INCORRECT, /* If the letter is not in the word at all */
        DISPLACED, /* If the letter is in the word but in the wrong place */
        CORRECT    /* If the letter is both in the word and in the right place */
    }

    private final String word; /* The correct word which the user needs to guess */

    public Word(final String word) {
        this.word = word;
    }

    public String getWord() {
        return this.word;
    }

    public LetterStatus[] checkWord(String word) {
        // Counting the letters in the secret word:
        final int[] lettersCount = new int[26];

        Arrays.fill(lettersCount, 0);
        for (char c : this.word.toCharArray()) {
            lettersCount[c - 'A']++;
        }

        LetterStatus[] letterStatuses = new LetterStatus[this.word.length()];

        // First, we set the letters' statuses not regarding the other letters:
        for (int i = 0; i < letterStatuses.length; i++) {
            letterStatuses[i] = this.checkLetterStatus(Character.toString(word.charAt(i)), i);
        }

        // Second, we subtract the letters that were guessed:
        for (int i = 0; i < letterStatuses.length; i++) {
            if (letterStatuses[i] == LetterStatus.CORRECT) {
                // Subtract the amount of letters that should be by one:
                lettersCount[word.charAt(i) - 'A']--;
            }
        }

        // Third, we check for every displaced letter if it hadn't already been guessed, and we set it to INCORRECT if
        // it was already guessed:
        for (int i = 0; i < letterStatuses.length; i++) {
            if (letterStatuses[i] == LetterStatus.DISPLACED) {
                if (lettersCount[word.charAt(i) - 'A'] > 0) {
                    // Subtract the amount of letters that should be by one:
                    lettersCount[word.charAt(i) - 'A']--;
                }
                else {
                    letterStatuses[i] = LetterStatus.INCORRECT;
                }
            }
        }

        return letterStatuses;
    }

    public LetterStatus checkLetterStatus(String letter, int index) {
        int letterIdx = this.word.indexOf(letter);

        LetterStatus status = letterIdx >= 0 ? LetterStatus.DISPLACED : LetterStatus.INCORRECT;
        while ((letterIdx = this.word.indexOf(letter, letterIdx)) >= 0) {
            if (letterIdx == index) {
                status = LetterStatus.CORRECT;
                break;
            }
            else {
                letterIdx++;
            }
        }
        return status;
    }
}
