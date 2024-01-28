package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Model {
    private Timeline timeline;
    private int timeSeconds;
    private String expectedText = "";
    private View view;

    public Model() {
        this.timeSeconds = 0;
    }

    public void setTimeSeconds(int timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public int getTimeSeconds() {
        return timeSeconds;
    }

    public void startTimer(Runnable onTick, boolean startNow) {
        if (startNow) {
            if (timeline != null) {
                timeline.stop();
            }
            timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
                timeSeconds--;
                onTick.run();
                if (timeSeconds <= 0) {
                    timeline.stop();
                }
            }));
            timeline.playFromStart();
        }
    }

    public boolean isTimerStarted() {
        return timeline != null && timeline.getStatus() == Animation.Status.RUNNING;
    }

    public void stopTimer() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    public void loadAndSetExpectedText(String filename, int numberOfWords) {
        try {
            List<String> lines = Files.readAllLines(Path.of("src/dictionary", filename), StandardCharsets.UTF_8);
            Random random = new Random();
            expectedText = random.ints(0, lines.size())
                    .limit(numberOfWords)
                    .mapToObj(lines::get)
                    .collect(Collectors.joining(" "));
        } catch (Exception e) {
            e.printStackTrace();
            expectedText = "";
        }
    }

    public String getExpectedText() {
        return expectedText;
    }

    public void setExpectedText(String text) {
        this.expectedText = text;
    }

    public void appendNewWords(String filename, int numberOfWords) {
        try {
            List<String> lines = Files.readAllLines(Path.of("src/dictionary", filename), StandardCharsets.UTF_8);
            Random random = new Random();
            expectedText += " " + random.ints(0, lines.size())
                    .limit(numberOfWords)
                    .mapToObj(lines::get)
                    .collect(Collectors.joining(" "));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
