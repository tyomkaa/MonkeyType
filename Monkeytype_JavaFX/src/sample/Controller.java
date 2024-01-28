package sample;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private View view;
    private Model model;
    private StringBuilder userInput = new StringBuilder();

    private int correctWords = 0;
    private int extraChars = 0;
    private int correctChars = 0;
    private int incorrectChars = 0;
    private int totalChars = 0;
    private int totalSeconds = 0;

    private int testDurationSeconds = 0;

    private int totalWPM = 0;

    private String currentLanguage = "English";

    private boolean isTestReadyToStart = false;

    private int initialTestDuration;


    public Controller(Model model) {
        this.model = model;
        this.isTestReadyToStart = true;
    }

    public void restartTest() {
        model.stopTimer();

        userInput.setLength(0);
        correctChars = 0;
        incorrectChars = 0;
        extraChars = 0;
        wordCompletionTimes.clear();

        totalSeconds = initialTestDuration;
        model.setTimeSeconds(initialTestDuration);

        String dictionaryFileName = currentLanguage.toLowerCase() + ".txt";
        loadTextForTyping(dictionaryFileName);

        view.setTextForTyping(model.getExpectedText());
        view.updateTimer(totalSeconds);
        view.updateTextFlow(userInput.toString(), model.getExpectedText());

        isTestReadyToStart = true;
    }



    public void setCurrentLanguage(String language) {
        this.currentLanguage = language;
        String dictionaryFileName = language.toLowerCase() + ".txt";
        loadTextForTyping(dictionaryFileName);
        view.setTextForTyping(model.getExpectedText());
    }

    public void pauseTest() {
        System.out.println("paused");

        if (model.isTimerStarted()) {
            model.stopTimer();
        } else {
            model.startTimer(() -> view.updateTimer(model.getTimeSeconds()), true);
        }
    }

    public void endTest() {
        model.stopTimer();

        int averageWPM = calculateAverageWPM();
        float accuracy = accuracy();

        view.updateChart(testDurationSeconds, calculateWPM());

        Platform.runLater(() -> {
            view.showChart(averageWPM, accuracy, currentLanguage, testDurationSeconds, correctChars, incorrectChars, extraChars);
        });

        for (String word : userInput.toString().split("\\s+")) {
            recordWordCompletionTime();
        }
        writeResultsToFile();
    }


    private float accuracy(){
        if (totalChars == 0) {
            return 0;
        }
        return (float) correctChars * 100 / totalChars;
    }

    private int calculateAverageWPM() {
        if (testDurationSeconds == 0) return 0;
        return totalWPM / testDurationSeconds;
    }

    private int calculateWPM() {
        if (totalSeconds == 0) return 0;
        double timeInMinutes = testDurationSeconds / 60.0;
        return (int) (correctWords / timeInMinutes);
    }

    private void updateStatistics() {
        String expectedText = model.getExpectedText();
        String enteredText = userInput.toString();

        correctWords = 0;
        correctChars = 0;
        incorrectChars = 0;
        extraChars = 0;
        totalChars = 0;

        String[] expectedWords = expectedText.split("\\s+");
        String[] enteredWords = enteredText.split("\\s+", -1);

        for (int i = 0; i < expectedWords.length; i++) {
            if (i < enteredWords.length) {
                String expectedWord = expectedWords[i];
                String enteredWord = enteredWords[i];

                int wordLength = Math.min(expectedWord.length(), enteredWord.length());
                for (int j = 0; j < wordLength; j++) {
                    if (enteredWord.charAt(j) == expectedWord.charAt(j)) {
                        correctChars++;
                        totalChars++;
                    } else {
                        incorrectChars++;
                        totalChars++;
                    }
                }

                if (enteredWord.length() > expectedWord.length()) {
                    extraChars += enteredWord.length() - expectedWord.length();
                }

                if (enteredWords[i].equals(expectedWords[i])){
                    correctWords++;
                }
            }
        }
        totalChars += extraChars;

    }

    public void setTime(int seconds) {
        initialTestDuration = seconds;
        model.setTimeSeconds(seconds);
        model.startTimer(() -> view.updateTimer(model.getTimeSeconds()), false);
    }


    public void loadTextForTyping(String filename) {
        model.loadAndSetExpectedText(filename, 30);
        view.setTextForTyping(model.getExpectedText());
    }

    public void startTest() {
        if (isTestReadyToStart && !model.isTimerStarted()) {
            totalSeconds = initialTestDuration;
            totalWPM = 0;
            isTestReadyToStart = false;
            testDurationSeconds = 0;
            model.startTimer(() -> {
                view.updateTimer(model.getTimeSeconds());
                totalSeconds--;
                int currentWPM = calculateWPM();
                totalWPM += currentWPM;
                testDurationSeconds++;
                view.updateChart(testDurationSeconds, currentWPM);
                if (model.getTimeSeconds() <= 0) {
                    model.stopTimer();
                    endTest();
                }
            }, true);
        }
    }

    public void setView(View view) {
        this.view = view;
    }

    public void onUserInput(String input) {

        if (!model.isTimerStarted() && isTestReadyToStart) {
            model.startTimer(() -> view.updateTimer(model.getTimeSeconds()), true);
        }

        if (input.equals(KeyCode.BACK_SPACE.toString()) && userInput.length() > 0) {
            userInput.deleteCharAt(userInput.length() - 1);
        } else {
            String[] expectedWords = model.getExpectedText().split("\\s+");
            String[] enteredWords = userInput.toString().split("\\s+", -1);
            int lastEnteredWordIndex = enteredWords.length - 1;

            String lastWordEntered = lastEnteredWordIndex >= 0 ? enteredWords[lastEnteredWordIndex] : "";

            boolean atWordEnd = lastEnteredWordIndex < expectedWords.length && lastWordEntered.length() >= expectedWords[lastEnteredWordIndex].length();

            if (input.equals(" ") && atWordEnd) {
                recordWordCompletionTime();
                userInput.append(input);
            } else if (!input.equals(" ")) {
                userInput.append(input);
            }

            if (enteredWords.length >= expectedWords.length) {
                if (model.isTimerStarted()) {
                    String dictionaryFileName = currentLanguage.toLowerCase() + ".txt";
                    model.appendNewWords(dictionaryFileName, 30);
                }
            }
        }

        updateStatistics();
        updateTextFlow();


    }

    private void updateTextFlow() {
        String expectedText = model.getExpectedText();

        view.updateTextFlow(userInput.toString(), expectedText);
    }

    private List<Integer> wordCompletionTimes = new ArrayList<>();

    private void recordWordCompletionTime() {
        wordCompletionTimes.add(testDurationSeconds);
    }

    private void writeResultsToFile() {
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_typing_test_results.txt";
        Path filePath = Paths.get(fileName);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("Typing Test Results:");
            writer.newLine();

            String[] expectedWords = model.getExpectedText().split("\\s+");
            String[] enteredWords = userInput.toString().split("\\s+");
            for (int i = 0; i < enteredWords.length; i++) {
                if (i < expectedWords.length && enteredWords[i].equals(expectedWords[i])) {
                    int wpmForWord = calculateWPMForWord(i, enteredWords[i]);
                    writer.write(enteredWords[i] + " -> " + wpmForWord + " wpm");
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int calculateWPMForWord(int wordIndex, String word) {
        if (wordIndex >= wordCompletionTimes.size()) {
            return 0;
        }
        int timeTakenForWordInSeconds = (wordIndex > 0 ? wordCompletionTimes.get(wordIndex) - wordCompletionTimes.get(wordIndex - 1) : wordCompletionTimes.get(0));
        if (timeTakenForWordInSeconds <= 0) {
            return 0;
        }
        double timeTakenForWordInMinutes = timeTakenForWordInSeconds / 60.0;
        double wordsPerMinute = (word.length()) / timeTakenForWordInMinutes;
        return (int) (wordsPerMinute);
    }


}
