package com.example.project_pendidikan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_pendidikan.api.DictionaryApiClient;
import com.example.project_pendidikan.api.WordsApiService;
import com.example.project_pendidikan.model.QuizQuestion;
import com.example.project_pendidikan.model.UserProgress;
import com.example.project_pendidikan.model.WordResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizActivity extends AppCompatActivity {

    private static final int QUIZ_SIZE = 10;
    private static final String PROGRESS_PREFS = "ProgressPrefs";
    
    private TextView textViewQuestionNumber;
    private TextView textViewScore;
    private ProgressBar progressBar;
    private TextView textViewQuizWord;
    private MaterialCardView[] optionCards = new MaterialCardView[4];
    private ImageView[] optionImages = new ImageView[4];
    private TextView[] textOptions = new TextView[4];
    private int selectedOptionIndex = -1;
    private MaterialButton buttonSubmit;
    private MaterialCardView cardFeedback;
    private TextView textViewFeedback;
    private MaterialButton buttonNext;
    private ImageView imageEmotionFeedback;
    
    // Popup feedback
    private AlertDialog feedbackDialog;
    private View dialogView;
    private ImageView imageFeedbackIcon;
    private TextView textFeedbackMessage;
    
    // Popup quiz completed
    private AlertDialog quizCompletedDialog;
    private View quizCompletedView;
    private TextView textFinalScore;
    
    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private UserProgress userProgress;
    private WordsApiService dictionaryService;
    private List<String> quizWords = new ArrayList<>();
    private Random random = new Random();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {

            new AlertDialog.Builder(this)
                .setTitle("Exit Quiz?")
                .setMessage("Are you sure you want to exit? Your quiz progress will not be saved.")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
        });
        

        textViewQuestionNumber = findViewById(R.id.textViewQuestionNumber);
        textViewScore = findViewById(R.id.textViewScore);
        progressBar = findViewById(R.id.progressBar);
        textViewQuizWord = findViewById(R.id.textViewQuizWord);
        

        optionCards[0] = findViewById(R.id.cardOption1);
        optionCards[1] = findViewById(R.id.cardOption2);
        optionCards[2] = findViewById(R.id.cardOption3);
        optionCards[3] = findViewById(R.id.cardOption4);
        
        optionImages[0] = findViewById(R.id.imageOption1);
        optionImages[1] = findViewById(R.id.imageOption2);
        optionImages[2] = findViewById(R.id.imageOption3);
        optionImages[3] = findViewById(R.id.imageOption4);
        
        textOptions[0] = findViewById(R.id.textOption1);
        textOptions[1] = findViewById(R.id.textOption2);
        textOptions[2] = findViewById(R.id.textOption3);
        textOptions[3] = findViewById(R.id.textOption4);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        cardFeedback = findViewById(R.id.cardFeedback);
        textViewFeedback = findViewById(R.id.textViewFeedback);
        buttonNext = findViewById(R.id.buttonNext);
        imageEmotionFeedback = findViewById(R.id.imageEmotionFeedback);
        

        loadUserProgress();

        dictionaryService = DictionaryApiClient.getClient().create(WordsApiService.class);
        

        initPopupFeedback();
        

        initQuizCompletedPopup();
        

        loadUserProgress();
        

        setupQuizWords();
        

        generateQuizQuestions();

        for (int i = 0; i < optionCards.length; i++) {
            final int index = i;
            optionCards[i].setOnClickListener(v -> {
                selectOption(index);
            });
        }
        

        buttonSubmit.setOnClickListener(v -> checkAnswer());
        buttonNext.setOnClickListener(v -> showNextQuestion());
    }
    
    private void loadUserProgress() {

        SharedPreferences userPrefs = getSharedPreferences("UserPref", MODE_PRIVATE);
        String userEmail = userPrefs.getString("email", "");
        

        SharedPreferences prefs = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE);
        userProgress = new UserProgress();
        userProgress.setUserEmail(userEmail);
        userProgress.setLevel(prefs.getInt(userEmail + "_level", 1));
        userProgress.setTotalCorrectAnswers(prefs.getInt(userEmail + "_totalCorrectAnswers", 0));
        userProgress.setQuizzesTaken(prefs.getInt(userEmail + "_quizzesTaken", 0));
        userProgress.setCurrentStreak(prefs.getInt(userEmail + "_currentStreak", 0));
    }
    
    private void saveUserProgress() {
        String userEmail = userProgress.getUserEmail();
        if (userEmail.isEmpty()) {

            SharedPreferences userPrefs = getSharedPreferences("UserPref", MODE_PRIVATE);
            userEmail = userPrefs.getString("email", "");
            userProgress.setUserEmail(userEmail);
        }
        

        SharedPreferences.Editor editor = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE).edit();
        editor.putInt(userEmail + "_level", userProgress.getLevel());
        editor.putInt(userEmail + "_totalCorrectAnswers", userProgress.getTotalCorrectAnswers());
        editor.putInt(userEmail + "_quizzesTaken", userProgress.getQuizzesTaken());
        editor.putInt(userEmail + "_currentStreak", userProgress.getCurrentStreak());
        editor.apply();
    }
    
    private void setupQuizWords() {
        // Common English words for the quiz
        quizWords.add("hello");
        quizWords.add("world");
        quizWords.add("book");
        quizWords.add("computer");
        quizWords.add("language");
        quizWords.add("education");
        quizWords.add("knowledge");
        quizWords.add("learning");
        quizWords.add("school");
        quizWords.add("teacher");
        quizWords.add("student");
        quizWords.add("dictionary");
        quizWords.add("vocabulary");
        quizWords.add("definition");
        quizWords.add("meaning");
        
        // Shuffle the words
        Collections.shuffle(quizWords);
    }
    
    private void generateQuizQuestions() {

        fetchWordDefinition(quizWords.get(0));
    }
    
    private void fetchWordDefinition(String word) {
        dictionaryService.getWord(word).enqueue(new Callback<List<WordResponse>>() {
            @Override
            public void onResponse(Call<List<WordResponse>> call, Response<List<WordResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    WordResponse wordResponse = response.body().get(0);
                    createQuizQuestion(wordResponse);
                } else {
                    // If API call fails, use a fallback definition
                    createFallbackQuestion(word);
                }
            }
            
            @Override
            public void onFailure(Call<List<WordResponse>> call, Throwable t) {

                createFallbackQuestion(word);
            }
        });
    }
    
    private void createQuizQuestion(WordResponse wordResponse) {
        String word = wordResponse.getWord();
        String correctDefinition = "";
        

        if (wordResponse.getMeanings() != null && !wordResponse.getMeanings().isEmpty()) {
            WordResponse.Meaning meaning = wordResponse.getMeanings().get(0);
            if (meaning.getDefinitions() != null && !meaning.getDefinitions().isEmpty()) {
                correctDefinition = meaning.getDefinitions().get(0).getDefinition();
            }
        }
        
        if (correctDefinition.isEmpty()) {
            createFallbackQuestion(word);
            return;
        }
        

        List<String> options = new ArrayList<>();
        options.add(correctDefinition);
        
        // Add some fake definitions
        options.add("Antonym of " + word);
        options.add("What type is related to " + word);
        options.add("Actions taken with " + word);
        

        Collections.shuffle(options);
        

        QuizQuestion question = new QuizQuestion(word, correctDefinition, options);
        quizQuestions.add(question);
        
        // If we need more questions and have more words, continue fetching
        if (quizQuestions.size() < QUIZ_SIZE && currentQuestionIndex + quizQuestions.size() < quizWords.size()) {
            fetchWordDefinition(quizWords.get(currentQuestionIndex + quizQuestions.size()));
        } else {

            displayCurrentQuestion();
        }
    }
    
    private void createFallbackQuestion(String word) {

        String correctDefinition = "";
        
        // Simple fallback definitions based on the word (in Indonesian)
        switch (word) {
            case "hello":
                correctDefinition = "Salam yang digunakan ketika bertemu seseorang";
                break;
            case "world":
                correctDefinition = "Bumi, bersama dengan semua negara dan penduduknya";
                break;
            case "book":
                correctDefinition = "Karya tulis atau cetak yang terdiri dari halaman-halaman";
                break;
            case "computer":
                correctDefinition = "Perangkat elektronik untuk menyimpan dan memproses data";
                break;
            case "language":
                correctDefinition = "Metode komunikasi manusia, baik lisan maupun tulisan";
                break;
            case "education":
                correctDefinition = "Proses menerima atau memberikan pengajaran sistematis";
                break;
            case "knowledge":
                correctDefinition = "Fakta, informasi, dan keterampilan yang diperoleh melalui pengalaman atau pendidikan";
                break;
            case "learning":
                correctDefinition = "Perolehan pengetahuan atau keterampilan melalui studi atau pengalaman";
                break;
            case "school":
                correctDefinition = "Lembaga untuk mendidik anak-anak";
                break;
            case "teacher":
                correctDefinition = "Orang yang mengajar, terutama di sekolah";
                break;
            case "student":
                correctDefinition = "Orang yang belajar di sekolah atau perguruan tinggi";
                break;
            case "dictionary":
                correctDefinition = "Buku atau sumber elektronik yang mencantumkan kata-kata dan artinya";
                break;
            case "vocabulary":
                correctDefinition = "Kumpulan kata yang digunakan dalam bahasa tertentu";
                break;
            case "definition":
                correctDefinition = "Pernyataan tentang arti yang tepat dari suatu kata";
                break;
            case "meaning":
                correctDefinition = "Apa yang dimaksud dengan kata, teks, konsep, atau tindakan";
                break;
            default:
                correctDefinition = "Kata umum dalam bahasa Inggris";
                break;
        }
        
        // Generate incorrect options
        List<String> options = new ArrayList<>();
        options.add(correctDefinition);
        options.add("Antonym of " + word);
        options.add("What type is related to " + word);
        options.add("Actions taken with " + word);
        

        Collections.shuffle(options);
        

        QuizQuestion question = new QuizQuestion(word, correctDefinition, options);
        quizQuestions.add(question);
        
        // If we need more questions and have more words, continue fetching
        if (quizQuestions.size() < QUIZ_SIZE && currentQuestionIndex + quizQuestions.size() < quizWords.size()) {
            fetchWordDefinition(quizWords.get(currentQuestionIndex + quizQuestions.size()));
        } else {

            displayCurrentQuestion();
        }
    }
    
    /**
     * Handle option selection
     */
    private void selectOption(int index) {

        for (int i = 0; i < optionCards.length; i++) {
            optionCards[i].setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            optionImages[i].setImageResource(android.R.drawable.radiobutton_off_background);
        }
        

        selectedOptionIndex = index;
        optionCards[index].setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        optionImages[index].setImageResource(android.R.drawable.radiobutton_on_background);
    }
    
    private void displayCurrentQuestion() {
        if (currentQuestionIndex < quizQuestions.size()) {
            QuizQuestion currentQuestion = quizQuestions.get(currentQuestionIndex);
            

            textViewQuestionNumber.setText((currentQuestionIndex + 1) + "/" + QUIZ_SIZE);
            textViewScore.setText(String.valueOf(score));
            progressBar.setProgress((currentQuestionIndex + 1) * 100 / QUIZ_SIZE);

            textViewQuizWord.setText(currentQuestion.getWord());
            

            List<String> options = currentQuestion.getOptions();
            for (int i = 0; i < optionCards.length; i++) {
                if (i < options.size()) {
                    textOptions[i].setText(options.get(i));
                }
            }
            

            selectedOptionIndex = -1;
            for (int i = 0; i < optionCards.length; i++) {
                optionCards[i].setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
                optionImages[i].setImageResource(android.R.drawable.radiobutton_off_background);
            }
            buttonSubmit.setEnabled(true);
            cardFeedback.setVisibility(View.GONE);
        } else {

            finishQuiz();
        }
    }
    
    /**
     * Initialize popup feedback
     */
    private void initPopupFeedback() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        

        LayoutInflater inflater = LayoutInflater.from(this);
        dialogView = inflater.inflate(R.layout.popup_feedback, null);
        

        imageFeedbackIcon = dialogView.findViewById(R.id.imageFeedbackIcon);
        textFeedbackMessage = dialogView.findViewById(R.id.textFeedbackMessage);
        

        MaterialButton buttonNextPopup = dialogView.findViewById(R.id.buttonNextPopup);
        buttonNextPopup.setOnClickListener(v -> {
            // Dismiss dialog dan show next question
            if (feedbackDialog != null && feedbackDialog.isShowing()) {
                feedbackDialog.dismiss();
            }
            showNextQuestion();
        });
        

        builder.setView(dialogView);
        

        feedbackDialog = builder.create();
        

        feedbackDialog.setCancelable(false);
        

        if (feedbackDialog.getWindow() != null) {
            feedbackDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    
    /**
     * Show popup feedback
     */
    private void showPopupFeedback(boolean isCorrect) {

        if (isCorrect) {
            imageFeedbackIcon.setImageResource(R.drawable.ic_thumbs_up);
            textFeedbackMessage.setText("Right! Very good.");
            textFeedbackMessage.setTextColor(ContextCompat.getColor(this, R.color.green_500));
            dialogView.findViewById(R.id.popupFeedback).setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.green_200));
            dialogView.findViewById(R.id.buttonNextPopup).setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.green_500));
        } else {

            imageFeedbackIcon.setImageResource(R.drawable.ic_wrong);
            textFeedbackMessage.setText("Wrong. The correct answer is:" +
                    quizQuestions.get(currentQuestionIndex).getCorrectDefinition());
            textFeedbackMessage.setTextColor(ContextCompat.getColor(this, android.R.color.white));

            dialogView.findViewById(R.id.popupFeedback).setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.red_700));

            dialogView.findViewById(R.id.buttonNextPopup).setBackgroundTintList(
                    ContextCompat.getColorStateList(this, R.color.red_900));
            

            imageFeedbackIcon.setBackground(null);
        }
        

        feedbackDialog.show();
        

        Window window = feedbackDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
    }
    
    private void checkAnswer() {
        if (selectedOptionIndex == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String selectedAnswer = textOptions[selectedOptionIndex].getText().toString();
        

        QuizQuestion currentQuestion = quizQuestions.get(currentQuestionIndex);
        boolean isCorrect = currentQuestion.isCorrectAnswer(selectedAnswer);
        
        // Update score and progress
        if (isCorrect) {
            score++;
            textViewScore.setText(String.valueOf(score));
            textViewFeedback.setText("Right! Very good.");
            textViewFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            imageEmotionFeedback.setImageResource(R.drawable.ic_emotion_happy);

            cardFeedback.setCardBackgroundColor(ContextCompat.getColor(this, R.color.green_200));
            
            // Update user progress
            userProgress.incrementCorrectAnswers();
        } else {
            textViewFeedback.setText("Wrong. The correct answer is: " + currentQuestion.getCorrectDefinition());
            textViewFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            imageEmotionFeedback.setImageResource(R.drawable.ic_emotion_sad);
            

            cardFeedback.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
            

            userProgress.resetStreak();
        }
        

        showPopupFeedback(isCorrect);
        
        // Save progress
        saveUserProgress();
        

        cardFeedback.setVisibility(View.GONE);
        buttonSubmit.setEnabled(false);
        

        for (MaterialCardView card : optionCards) {
            card.setEnabled(false);
        }
    }
    
    private void showNextQuestion() {
        currentQuestionIndex++;
        

        for (MaterialCardView card : optionCards) {
            card.setEnabled(true);
        }
        
        if (currentQuestionIndex < quizQuestions.size()) {
            displayCurrentQuestion();
        } else {
            finishQuiz();
        }
    }
    
    /**
     * Initialize popup for quiz completion
     */
    private void initQuizCompletedPopup() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        

        LayoutInflater inflater = LayoutInflater.from(this);
        quizCompletedView = inflater.inflate(R.layout.popup_quiz_completed, null);
        

        textFinalScore = quizCompletedView.findViewById(R.id.textFinalScore);
        

        MaterialButton buttonViewAchievements = quizCompletedView.findViewById(R.id.buttonViewAchievements);
        buttonViewAchievements.setOnClickListener(v -> {

            if (quizCompletedDialog != null && quizCompletedDialog.isShowing()) {
                quizCompletedDialog.dismiss();
            }
            

            Intent intent = new Intent(QuizActivity.this, AchievementsActivity.class);
            startActivity(intent);
            

            finish();
        });
        

        builder.setView(quizCompletedView);
        

        quizCompletedDialog = builder.create();
        

        quizCompletedDialog.setCancelable(false);
        

        if (quizCompletedDialog.getWindow() != null) {
            quizCompletedDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
    
    /**
     * Tampilkan popup quiz completed
     */
    private void showQuizCompletedPopup() {

        textFinalScore.setText("Final Score: " + score + "/" + QUIZ_SIZE);
        

        quizCompletedDialog.show();
        

        Window window = quizCompletedDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
    }
    
    private void finishQuiz() {
        // Update user progress
        userProgress.incrementQuizzesTaken();
        saveUserProgress();
        

        Log.d("QuizActivity", "Quiz completed, showing completed popup");
        

        new Handler(Looper.getMainLooper()).post(() -> {
            showQuizCompletedPopup();
        });
        

    }
}
