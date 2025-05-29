package com.example.project_pendidikan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
        
        // Initialize toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Initialize views
        textViewQuestionNumber = findViewById(R.id.textViewQuestionNumber);
        textViewScore = findViewById(R.id.textViewScore);
        progressBar = findViewById(R.id.progressBar);
        textViewQuizWord = findViewById(R.id.textViewQuizWord);
        
        // Initialize option cards and their components
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
        
        // Load user progress
        loadUserProgress();
        
        // Initialize dictionary service
        dictionaryService = DictionaryApiClient.getClient().create(WordsApiService.class);
        
        // Set up quiz words (common English words for the quiz)
        setupQuizWords();
        
        // Generate quiz questions
        generateQuizQuestions();
        
        // Set up option card click listeners
        for (int i = 0; i < optionCards.length; i++) {
            final int index = i;
            optionCards[i].setOnClickListener(v -> {
                selectOption(index);
            });
        }
        
        // Set up button listeners
        buttonSubmit.setOnClickListener(v -> checkAnswer());
        buttonNext.setOnClickListener(v -> showNextQuestion());
    }
    
    private void loadUserProgress() {
        SharedPreferences prefs = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE);
        userProgress = new UserProgress();
        userProgress.setLevel(prefs.getInt("level", 1));
        userProgress.setTotalCorrectAnswers(prefs.getInt("totalCorrectAnswers", 0));
        userProgress.setQuizzesTaken(prefs.getInt("quizzesTaken", 0));
        userProgress.setCurrentStreak(prefs.getInt("currentStreak", 0));
    }
    
    private void saveUserProgress() {
        SharedPreferences.Editor editor = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE).edit();
        editor.putInt("level", userProgress.getLevel());
        editor.putInt("totalCorrectAnswers", userProgress.getTotalCorrectAnswers());
        editor.putInt("quizzesTaken", userProgress.getQuizzesTaken());
        editor.putInt("currentStreak", userProgress.getCurrentStreak());
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
        // Start with the first word
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
                // If API call fails, use a fallback definition
                createFallbackQuestion(word);
            }
        });
    }
    
    private void createQuizQuestion(WordResponse wordResponse) {
        String word = wordResponse.getWord();
        String correctDefinition = "";
        
        // Get a definition from the word response
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
        
        // Generate incorrect options in Indonesian
        List<String> options = new ArrayList<>();
        options.add(correctDefinition);
        
        // Add some fake definitions as distractors in Indonesian
        options.add("Lawan kata dari " + word);
        options.add("Jenis makanan yang berhubungan dengan " + word);
        options.add("Tindakan yang dilakukan dengan " + word);
        
        // Shuffle options
        Collections.shuffle(options);
        
        // Create quiz question
        QuizQuestion question = new QuizQuestion(word, correctDefinition, options);
        quizQuestions.add(question);
        
        // If we need more questions and have more words, continue fetching
        if (quizQuestions.size() < QUIZ_SIZE && currentQuestionIndex + quizQuestions.size() < quizWords.size()) {
            fetchWordDefinition(quizWords.get(currentQuestionIndex + quizQuestions.size()));
        } else {
            // Start the quiz
            displayCurrentQuestion();
        }
    }
    
    private void createFallbackQuestion(String word) {
        // Create a fallback question with predefined definitions in Indonesian
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
        
        // Generate incorrect options in Indonesian
        List<String> options = new ArrayList<>();
        options.add(correctDefinition);
        options.add("Lawan kata dari " + word);
        options.add("Jenis makanan yang berhubungan dengan " + word);
        options.add("Tindakan yang dilakukan dengan " + word);
        
        // Shuffle options
        Collections.shuffle(options);
        
        // Create quiz question
        QuizQuestion question = new QuizQuestion(word, correctDefinition, options);
        quizQuestions.add(question);
        
        // If we need more questions and have more words, continue fetching
        if (quizQuestions.size() < QUIZ_SIZE && currentQuestionIndex + quizQuestions.size() < quizWords.size()) {
            fetchWordDefinition(quizWords.get(currentQuestionIndex + quizQuestions.size()));
        } else {
            // Start the quiz
            displayCurrentQuestion();
        }
    }
    
    /**
     * Handle option selection
     */
    private void selectOption(int index) {
        // Reset all options first
        for (int i = 0; i < optionCards.length; i++) {
            optionCards[i].setCardBackgroundColor(getResources().getColor(android.R.color.transparent));
            optionImages[i].setImageResource(android.R.drawable.radiobutton_off_background);
        }
        
        // Highlight the selected option
        selectedOptionIndex = index;
        optionCards[index].setCardBackgroundColor(getResources().getColor(android.R.color.holo_blue_light, null));
        optionImages[index].setImageResource(android.R.drawable.radiobutton_on_background);
    }
    
    private void displayCurrentQuestion() {
        if (currentQuestionIndex < quizQuestions.size()) {
            QuizQuestion currentQuestion = quizQuestions.get(currentQuestionIndex);
            
            // Update question number and progress
            textViewQuestionNumber.setText((currentQuestionIndex + 1) + "/" + QUIZ_SIZE);
            textViewScore.setText(String.valueOf(score));
            progressBar.setProgress((currentQuestionIndex + 1) * 100 / QUIZ_SIZE);
            
            // Display word
            textViewQuizWord.setText(currentQuestion.getWord());
            
            // Set options
            List<String> options = currentQuestion.getOptions();
            for (int i = 0; i < optionCards.length; i++) {
                if (i < options.size()) {
                    textOptions[i].setText(options.get(i));
                }
            }
            
            // Reset UI state
            selectedOptionIndex = -1;
            for (int i = 0; i < optionCards.length; i++) {
                optionCards[i].setCardBackgroundColor(getResources().getColor(android.R.color.transparent));
                optionImages[i].setImageResource(android.R.drawable.radiobutton_off_background);
            }
            buttonSubmit.setEnabled(true);
            cardFeedback.setVisibility(View.GONE);
        } else {
            // Quiz completed
            finishQuiz();
        }
    }
    
    private void checkAnswer() {
        if (selectedOptionIndex == -1) {
            Toast.makeText(this, "Silakan pilih jawaban", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String selectedAnswer = textOptions[selectedOptionIndex].getText().toString();
        
        // Get current question
        QuizQuestion currentQuestion = quizQuestions.get(currentQuestionIndex);
        boolean isCorrect = currentQuestion.isCorrectAnswer(selectedAnswer);
        
        // Update score and progress
        if (isCorrect) {
            score++;
            textViewScore.setText(String.valueOf(score));
            textViewFeedback.setText("Benar! Bagus sekali.");
            textViewFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            
            // Update user progress
            userProgress.incrementCorrectAnswers();
        } else {
            textViewFeedback.setText("Salah. Jawaban yang benar adalah: " + currentQuestion.getCorrectDefinition());
            textViewFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            
            // Reset streak on wrong answer
            userProgress.resetStreak();
        }
        
        // Save progress
        saveUserProgress();
        
        // Show feedback
        cardFeedback.setVisibility(View.VISIBLE);
        buttonSubmit.setEnabled(false);
        
        // Disable option cards
        for (MaterialCardView card : optionCards) {
            card.setEnabled(false);
        }
    }
    
    private void showNextQuestion() {
        currentQuestionIndex++;
        
        // Re-enable option cards
        for (MaterialCardView card : optionCards) {
            card.setEnabled(true);
        }
        
        if (currentQuestionIndex < quizQuestions.size()) {
            displayCurrentQuestion();
        } else {
            finishQuiz();
        }
    }
    
    private void finishQuiz() {
        // Update user progress
        userProgress.incrementQuizzesTaken();
        saveUserProgress();
        
        // Show completion message
        Toast.makeText(this, "Quiz completed! Your score: " + score + "/" + QUIZ_SIZE, Toast.LENGTH_LONG).show();
        
        // Return to dashboard
        finish();
    }
}
