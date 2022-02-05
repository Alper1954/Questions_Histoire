package aperr.android.questionsdhistoire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import java.util.Locale;

public class Question extends Activity implements TextToSpeech.OnInitListener {

    private static final int nbQuestions = 1000;  //nombre total de questions du Quiz

    private Boolean afterValidation;
    private String easy,medium,difficult,question,nbAnswers,nbOk,answer1,answer2,answer3,answer4;
    private int nbAnswered;
    private Boolean goodAnswer = true;
    private String answerMessage = "";
    private String answerText = "";
    private TextToSpeech myTTS;
    private Boolean firstStart = true;

    private int gametime; //durée de la partie
    private int maxSkip; //nombre de changements de question autorisé
    private int skipQuestion; //nombre de changements de question restants
    private int tmin;  //nombre de minutes restantes
    private int tsec;  //nombre de secondes restantes
    private int secs; //temps restant en secondes
    private boolean running; //compteur de temps actif
    private boolean wasRunning; //le compteur était actif quand l'activité n'a plus été visibbe

    private int score; //score courant
    private int maxScore; //score maximum obtenu

    private int capUnits; //capital initial d'unités
    private int units; //nombre d'unités restantes

    private int nbRep; //compteur nombre de réponses
    private int nbRepOk; //compteur nombre de réponses OK

    private Boolean ttsInstalled;
    private Boolean ttsEnabled;

    private String smaxScore;
    private String temps;
    private String textButton;
    private Toast toast;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        
        if (savedInstanceState == null) {
            //Log.i("alain", "savedInstanceState is null");

            SharedPreferences sharedPref = getSharedPreferences("QH_preferences", Context.MODE_PRIVATE);

            TextView textScore = (TextView) findViewById(R.id.textScore);
            textScore.setText("Score: 0");

            maxScore = sharedPref.getInt("maxScore", 0);


            if (Locale.getDefault().getLanguage().equals("fr")) {
                smaxScore = String.format("[Dernier meilleur Score: %d]",maxScore);
            } else {
                smaxScore = String.format("[Last highest Score: %d]",maxScore);
            }
            TextView tvmaxScore = (TextView)findViewById(R.id.maxScore);
            tvmaxScore.setText(smaxScore);

            ttsInstalled = sharedPref.getBoolean("soundSwitch", true);
            ttsEnabled = ttsInstalled;

            capUnits = sharedPref.getInt("capUnits", 20);
            units = capUnits;

            TextView textUnits = (TextView) findViewById(R.id.textUnits);
            if (Locale.getDefault().getLanguage().equals("fr")) {
                textUnits.setText(String.format("Unités: %d", units));
            } else {
                textUnits.setText(String.format("Units: %d", units));
            }

            gametime = sharedPref.getInt("gametime", 2);
            secs = gametime * 60;

            maxSkip = sharedPref.getInt("maxSkip", 10);
            skipQuestion = maxSkip;

            Button buttonAutre = (Button) findViewById(R.id.buttonAutre);
            if (Locale.getDefault().getLanguage().equals("fr")) {
                textButton = "Autre (" + Integer.toString(maxSkip) + ")";
            } else {
                textButton = "Other (" + Integer.toString(maxSkip) + ")";
            }
            buttonAutre.setText(textButton);

            afterValidation = false;
            running = true;

        } else {
            //Log.i("alain", "savedInstanceState is not null");
            
            easy = savedInstanceState.getString("easy");
            medium = savedInstanceState.getString("medium");
            difficult = savedInstanceState.getString("difficult");
            question = savedInstanceState.getString("question");
            nbAnswers = savedInstanceState.getString("nbAnswers");
            nbOk = savedInstanceState.getString("nbOk");
            answer1 = savedInstanceState.getString("answer1");
            answer2 = savedInstanceState.getString("answer2");
            answer3 = savedInstanceState.getString("answer3");
            answer4 = savedInstanceState.getString("answer4");

            afterValidation = savedInstanceState.getBoolean("afterValidation");
            goodAnswer = savedInstanceState.getBoolean("goodAnswer");
            answerText = savedInstanceState.getString("answerText");
            nbAnswered = savedInstanceState.getInt("nbAnswered");
            firstStart = savedInstanceState.getBoolean("firstStart");

            score = savedInstanceState.getInt("score");
            maxScore = savedInstanceState.getInt("maxScore");
            units = savedInstanceState.getInt("units");

            nbRep = savedInstanceState.getInt("nbRep");
            nbRepOk = savedInstanceState.getInt("nbRepOk");

            secs = savedInstanceState.getInt("secs");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");

            skipQuestion = savedInstanceState.getInt("skipQuestion");

            ttsInstalled = savedInstanceState.getBoolean("ttsInstalled");
            ttsEnabled = savedInstanceState.getBoolean("ttsEnabled");

            TextView textScore = (TextView) findViewById(R.id.textScore);
            textScore.setText(String.format("Score: %d", score));

            if (Locale.getDefault().getLanguage().equals("fr")) {
                smaxScore = String.format("[Dernier meilleur Score: %d]",maxScore);
            } else {
                smaxScore = String.format("[Last highest Score: %d]",maxScore);
            }
            TextView tvmaxScore = (TextView)findViewById(R.id.maxScore);
            tvmaxScore.setText(smaxScore);

            TextView textUnits = (TextView) findViewById(R.id.textUnits);
            if (Locale.getDefault().getLanguage().equals("fr")) {
                textUnits.setText(String.format("Unités: %d", units));
            } else {
                textUnits.setText(String.format("Units: %d", units));
            }

            Button buttonAutre = (Button) findViewById(R.id.buttonAutre);
            if (Locale.getDefault().getLanguage().equals("fr")) {
                textButton = "Autre (" + Integer.toString(skipQuestion) + ")";
            } else {
                textButton = "Other (" + Integer.toString(skipQuestion) + ")";
            }
            buttonAutre.setText(textButton);


            RelativeLayout layoutQuestion = (RelativeLayout) findViewById(R.id.layoutQuestion);
            LinearLayout layoutAnswers = (LinearLayout) findViewById(R.id.layoutAnswers);
            LinearLayout layoutResult = (LinearLayout) findViewById(R.id.layoutResult);

            if ((layoutQuestion == null)||(!afterValidation)) {

                if(layoutQuestion != null){
                    layoutQuestion.setVisibility(View.VISIBLE);
                }

                TextView textEasy =(TextView)findViewById(R.id.textEasy);
                TextView textMedium =(TextView)findViewById(R.id.textMedium);
                TextView textDifficult =(TextView)findViewById(R.id.textDifficult);
                TextView textLoad =(TextView) findViewById(R.id.textLoad);

                textLoad.setVisibility(View.INVISIBLE);

                if (easy.equals("1")){
                    textEasy.setVisibility(View.VISIBLE);
                    textMedium.setVisibility(View.INVISIBLE);
                    textDifficult.setVisibility(View.INVISIBLE);
                }else if (medium.equals("1")){
                    textEasy.setVisibility(View.INVISIBLE);
                    textMedium.setVisibility(View.VISIBLE);
                    textDifficult.setVisibility(View.INVISIBLE);
                }else{
                    textEasy.setVisibility(View.INVISIBLE);
                    textMedium.setVisibility(View.INVISIBLE);
                    textDifficult.setVisibility(View.VISIBLE);
                }

                TextView textQuestion =(TextView)findViewById(R.id.textQuestion);
                textQuestion.setText(question);

            }else{
                layoutQuestion.setVisibility(View.GONE);
            }


            if (afterValidation) {

                layoutAnswers.setVisibility(View.GONE);
                layoutResult.setVisibility(View.VISIBLE);

                TextView textSolutionBad = (TextView) findViewById(R.id.textSolutionBad);
                ImageView image_ok = (ImageView) findViewById(R.id.image_ok);
                ImageView image_nok = (ImageView) findViewById(R.id.image_nok);
                ImageView image_ok_en = (ImageView) findViewById(R.id.image_ok_en);
                ImageView image_nok_en = (ImageView) findViewById(R.id.image_nok_en);

                if (goodAnswer) {
                    if (Locale.getDefault().getLanguage().equals("fr")) {
                        textSolutionBad.setVisibility(View.GONE);
                        image_ok.setVisibility(View.VISIBLE);
                        image_nok.setVisibility(View.GONE);
                        image_ok_en.setVisibility(View.GONE);
                        image_nok_en.setVisibility(View.GONE);
                    } else {
                        textSolutionBad.setVisibility(View.GONE);
                        image_ok_en.setVisibility(View.VISIBLE);
                        image_nok_en.setVisibility(View.GONE);
                        image_ok.setVisibility(View.GONE);
                        image_nok.setVisibility(View.GONE);
                    }
                } else {
                    textSolutionBad.setText(answerText);
                    textSolutionBad.setVisibility(View.VISIBLE);
                    if (Locale.getDefault().getLanguage().equals("fr")) {
                        image_ok.setVisibility(View.GONE);
                        image_nok.setVisibility(View.VISIBLE);
                        image_ok_en.setVisibility(View.GONE);
                        image_nok_en.setVisibility(View.GONE);
                    } else {
                        image_ok_en.setVisibility(View.GONE);
                        image_nok_en.setVisibility(View.VISIBLE);
                        image_ok.setVisibility(View.GONE);
                        image_nok.setVisibility(View.GONE);
                    }
                }

            }else{

                layoutAnswers.setVisibility(View.VISIBLE);
                layoutResult.setVisibility(View.GONE);

                TextView textAnswer1 = (TextView) findViewById(R.id.answer1);
                TextView textAnswer2 = (TextView) findViewById(R.id.answer2);
                TextView textAnswer3 = (TextView) findViewById(R.id.answer3);
                TextView textAnswer4 = (TextView) findViewById(R.id.answer4);

                textAnswer1.setBackgroundColor(0xFFFFFFFF);
                textAnswer2.setBackgroundColor(0xFFFFFFFF);
                textAnswer3.setBackgroundColor(0xFFFFFFFF);
                textAnswer4.setBackgroundColor(0xFFFFFFFF);

                if(nbAnswered == 1){
                    textAnswer1.setBackgroundColor(0xFF00FF7F);
                }else if(nbAnswered == 2){
                    textAnswer2.setBackgroundColor(0xFF00FF7F);
                }else if(nbAnswered == 3) {
                    textAnswer3.setBackgroundColor(0xFF00FF7F);
                }else if(nbAnswered == 4) {
                    textAnswer4.setBackgroundColor(0xFF00FF7F);
                }

                int nb = Integer.parseInt(nbAnswers);

                if (nb == 4) {
                    textAnswer1.setText(answer1);
                    textAnswer1.setVisibility(View.VISIBLE);

                    textAnswer2.setText(answer2);
                    textAnswer2.setVisibility(View.VISIBLE);

                    textAnswer3.setText(answer3);
                    textAnswer3.setVisibility(View.VISIBLE);

                    textAnswer4.setText(answer4);
                    textAnswer4.setVisibility(View.VISIBLE);

                } else if (nb == 3) {
                    textAnswer1.setText(answer1);
                    textAnswer1.setVisibility(View.VISIBLE);

                    textAnswer2.setText(answer2);
                    textAnswer2.setVisibility(View.VISIBLE);

                    textAnswer3.setText(answer3);
                    textAnswer3.setVisibility(View.VISIBLE);


                    textAnswer4.setVisibility(View.GONE);

                } else {
                    textAnswer1.setText(answer1);
                    textAnswer1.setVisibility(View.VISIBLE);

                    textAnswer2.setText(answer2);
                    textAnswer2.setVisibility(View.VISIBLE);

                    textAnswer3.setVisibility(View.GONE);

                    textAnswer4.setVisibility(View.GONE);
                }
            }
        }


        TextView sound = (TextView) findViewById(R.id.sound);
        TextView noSound = (TextView) findViewById(R.id.nosound);
        if(ttsInstalled){
            if(ttsEnabled){
                sound.setVisibility(View.VISIBLE);
                noSound.setVisibility(View.GONE);
            }else{
                sound.setVisibility(View.GONE);
                noSound.setVisibility(View.VISIBLE);
            }
        }else{
            sound.setVisibility(View.GONE);
            noSound.setVisibility(View.GONE);
        }

        //restoreContextHistory();

        if(ttsInstalled){
            myTTS = new TextToSpeech(this, this);
        }else{
            if (firstStart) {
                firstStart = false;
                newQuestion();
            }
        }

        runTimer();


        Button buttonValider = (Button) findViewById(R.id.buttonValider);
        buttonValider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if((secs == 0)||(units == 0)) {
                    running = false; // stop timer

                    finPartie();

                }else{
                    if (nbAnswered == 0) {
                        Toast toast = Toast.makeText(Question.this, "Veuillez sélectionner une réponse", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {

                        RelativeLayout layoutQuestion = (RelativeLayout) findViewById(R.id.layoutQuestion);
                        if (layoutQuestion != null) {
                            layoutQuestion.setVisibility(View.GONE);
                        }

                        LinearLayout layoutAnswers = (LinearLayout) findViewById(R.id.layoutAnswers);
                        layoutAnswers.setVisibility(View.GONE);

                        LinearLayout layoutResult = (LinearLayout) findViewById(R.id.layoutResult);
                        layoutResult.setVisibility(View.VISIBLE);



                        int inbok = Integer.parseInt(nbOk);
                        nbRep++;

                        if (nbAnswered == inbok) {
                            nbRepOk++;
                            goodAnswer = true;

                            TextView textSolutionBad = (TextView) findViewById(R.id.textSolutionBad);
                            textSolutionBad.setVisibility(View.GONE);

                            ImageView image_ok = (ImageView) findViewById(R.id.image_ok);
                            ImageView image_nok = (ImageView) findViewById(R.id.image_nok);
                            ImageView image_ok_en = (ImageView) findViewById(R.id.image_ok_en);
                            ImageView image_nok_en = (ImageView) findViewById(R.id.image_nok_en);

                            if (Locale.getDefault().getLanguage().equals("fr")) {
                                answerMessage = "Bravo!";
                                image_ok.setVisibility(View.VISIBLE);
                                image_nok.setVisibility(View.GONE);
                                image_ok_en.setVisibility(View.GONE);
                                image_nok_en.setVisibility(View.GONE);
                            } else {
                                answerMessage = "Good!";
                                image_ok.setVisibility(View.GONE);
                                image_nok.setVisibility(View.GONE);
                                image_ok_en.setVisibility(View.VISIBLE);
                                image_nok_en.setVisibility(View.GONE);
                            }

                            if(easy.equals("1")){
                                score++;
                                units++;
                            }else if(medium.equals("1")){
                                score = score + 2;
                                units = units + 2;
                            }else{
                                score = score + 3;
                                units = units + 3;
                            }


                        } else {
                            if (inbok == 1) {
                                TextView textAnswer1 = (TextView) findViewById(R.id.answer1);
                                if (Locale.getDefault().getLanguage().equals("fr")) {
                                    answerMessage = "Erreur! La bonne réponse est: \n" + textAnswer1.getText().toString();
                                    answerText = "La bonne réponse est: \n\n" + textAnswer1.getText().toString();
                                } else {
                                    answerMessage = "Error! The correct answer is: \n" + textAnswer1.getText().toString();
                                    answerText = "The correct answer is: \n\n" + textAnswer1.getText().toString();
                                }

                            } else if (inbok == 2) {
                                TextView textAnswer2 = (TextView) findViewById(R.id.answer2);
                                if (Locale.getDefault().getLanguage().equals("fr")) {
                                    answerMessage = "Erreur! La bonne réponse est: \n" + textAnswer2.getText().toString();
                                    answerText = "La bonne réponse est: \n\n" + textAnswer2.getText().toString();
                                } else {
                                    answerMessage = "Error! The correct answer is: \n" + textAnswer2.getText().toString();
                                    answerText = "The correct answer is: \n\n" + textAnswer2.getText().toString();
                                }

                            } else if (inbok == 3) {
                                TextView textAnswer3 = (TextView) findViewById(R.id.answer3);
                                if (Locale.getDefault().getLanguage().equals("fr")) {
                                    answerMessage = "Erreur! La bonne réponse est: \n" + textAnswer3.getText().toString();
                                    answerText = "La bonne réponse est: \n\n" + textAnswer3.getText().toString();
                                } else {
                                    answerMessage = "Error! The correct answer is: \n" + textAnswer3.getText().toString();
                                    answerText = "The correct answer is: \n\n" + textAnswer3.getText().toString();
                                }

                            } else {
                                TextView textAnswer4 = (TextView) findViewById(R.id.answer4);
                                if (Locale.getDefault().getLanguage().equals("fr")) {
                                    answerMessage = "Erreur! La bonne réponse est: \n" + textAnswer4.getText().toString();
                                    answerText = "La bonne réponse est: \n\n" + textAnswer4.getText().toString();
                                } else {
                                    answerMessage = "Error! The correct answer is: \n" + textAnswer4.getText().toString();
                                    answerText = "The correct answer is: \n\n" + textAnswer4.getText().toString();
                                }

                            }

                            TextView textSolutionBad = (TextView) findViewById(R.id.textSolutionBad);
                            textSolutionBad.setText(answerText);
                            textSolutionBad.setVisibility(View.VISIBLE);

                            ImageView image_ok = (ImageView) findViewById(R.id.image_ok);
                            ImageView image_nok = (ImageView) findViewById(R.id.image_nok);
                            ImageView image_ok_en = (ImageView) findViewById(R.id.image_ok_en);
                            ImageView image_nok_en = (ImageView) findViewById(R.id.image_nok_en);

                            if (Locale.getDefault().getLanguage().equals("fr")) {
                                image_ok.setVisibility(View.GONE);
                                image_nok.setVisibility(View.VISIBLE);
                                image_ok_en.setVisibility(View.GONE);
                                image_nok_en.setVisibility(View.GONE);
                            } else {
                                image_ok.setVisibility(View.GONE);
                                image_nok.setVisibility(View.GONE);
                                image_ok_en.setVisibility(View.GONE);
                                image_nok_en.setVisibility(View.VISIBLE);
                            }

                            goodAnswer = false;

                            if(easy.equals("1")){
                                units--;
                                if(units < 0) {units = 0;}
                            }else if(medium.equals("1")){
                                units = units - 2;
                                if(units < 0) {units = 0;}
                            }else{
                                units = units - 3;
                                if(units < 0) {units = 0;}
                            }
                        }

                        TextView textScore = (TextView) findViewById(R.id.textScore);
                        textScore.setText(String.format("Score: %d", score));

                        TextView textUnits = (TextView) findViewById(R.id.textUnits);
                        if (Locale.getDefault().getLanguage().equals("fr")) {
                            textUnits.setText(String.format("Unités: %d", units));
                        } else {
                            textUnits.setText(String.format("Units: %d", units));
                        }

                        initQueue(answerMessage);

                        afterValidation = true;
                    }
                }
            }
        });


        Button buttonAutre = (Button) findViewById(R.id.buttonAutre);
        buttonAutre.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                if((secs == 0)||(units == 0)){
                    running = false;
                    finPartie();
                }else if(skipQuestion == 0) {
                    if (Locale.getDefault().getLanguage().equals("fr")) {
                        toast = Toast.makeText(Question.this, "Nombre maximum de changements de question atteint", Toast.LENGTH_SHORT);
                    } else {
                        toast = Toast.makeText(Question.this, "Maximum number of changes is reached", Toast.LENGTH_SHORT);
                    }
                    toast.show();
                }else{
                    skipQuestion--;
                    Button buttonAutre = (Button) findViewById(R.id.buttonAutre);
                    if (Locale.getDefault().getLanguage().equals("fr")) {
                        textButton = "Autre (" + Integer.toString(skipQuestion) + ")";
                    } else {
                        textButton = "Other (" + Integer.toString(skipQuestion) + ")";
                    }
                    buttonAutre.setText(textButton);

                    newQuestion();
                }

                afterValidation = false;
            }
        });


        Button buttonNext = (Button) findViewById(R.id.buttonSuivant);
        buttonNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if((secs == 0)||(units == 0)){
                    running = false; // stop timer
                    finPartie();
                    //Toast toast = Toast.makeText(Question.this, "La partie est terminée", Toast.LENGTH_SHORT);
                    //toast.show();
                }else{
                    newQuestion();

                    running = true; //restart timer

                    afterValidation = false;
                }
            }
        });



        Button buttonPause = (Button) findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                running = false; // stop timer
            }
        });

    }


    private void finPartie(){
        SharedPreferences sharedPref = getSharedPreferences("QH_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (score > maxScore){
            editor.putInt("maxScore", score);
            editor.commit();
        }

        Intent intent = new Intent(Question.this, FinPartie.class);
        intent.putExtra("units", units);
        intent.putExtra("score", score);
        intent.putExtra("maxScore", maxScore);
        intent.putExtra("nbRep", nbRep);
        intent.putExtra("nbRepOk", nbRepOk);

        startActivity(intent);
        finish();
    }


    private void runTimer(){
        final android.os.Handler handler = new android.os.Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                tmin = secs/60;
                tsec = secs%60;

                if (Locale.getDefault().getLanguage().equals("fr")) {
                    temps = String.format("Temps: %dmn %ds",tmin,tsec);
                } else {
                    temps = String.format("Time: %dmn %ds",tmin,tsec);
                }
                TextView textTemps = (TextView)findViewById(R.id.textTemps);
                textTemps.setText(temps);

                if(running){
                    secs--;
                    if(secs <= 0){
                        secs = 0;
                        running = false;
                    }
                }
                handler.postDelayed(this,1000);
            }
        });
    }


    public void clickanswer1(View view){
        selectedAnswer(1);
    }

    public void clickanswer2(View view){
        selectedAnswer(2);
    }

    public void clickanswer3(View view){
        selectedAnswer(3);
    }

    public void clickanswer4(View view){
        selectedAnswer(4);
    }

    private void selectedAnswer(Integer answer){
        TextView selanswer1 = (TextView) findViewById(R.id.answer1);
        TextView selanswer2 = (TextView) findViewById(R.id.answer2);
        TextView selanswer3 = (TextView) findViewById(R.id.answer3);
        TextView selanswer4 = (TextView) findViewById(R.id.answer4);

        selanswer1.setBackgroundColor(0xFFFFFFFF);
        selanswer2.setBackgroundColor(0xFFFFFFFF);
        selanswer3.setBackgroundColor(0xFFFFFFFF);
        selanswer4.setBackgroundColor(0xFFFFFFFF);

        if(answer == 1){
            selanswer1.setBackgroundColor(0xFF00FF7F);
        }else if(answer == 2){
            selanswer2.setBackgroundColor(0xFF00FF7F);
        }else if(answer == 3) {
            selanswer3.setBackgroundColor(0xFF00FF7F);
        }else {
            selanswer4.setBackgroundColor(0xFF00FF7F);
        }

        nbAnswered = answer;
    }

    public void clicksound(View view){
        TextView sound = (TextView) findViewById(R.id.sound);
        TextView noSound = (TextView) findViewById(R.id.nosound);
        sound.setVisibility(View.GONE);
        noSound.setVisibility(View.VISIBLE);

        initQueue(" ");
        ttsEnabled = false;
    }

    public void clicknosound(View view){
        TextView sound = (TextView) findViewById(R.id.sound);
        TextView noSound = (TextView) findViewById(R.id.nosound);
        sound.setVisibility(View.VISIBLE);
        noSound.setVisibility(View.GONE);

        ttsEnabled = true;
    }


    private void onBackgroundTaskDataObtained(String[] questionData) {
        easy = questionData[0];
        medium = questionData[1];
        difficult = questionData[2];
        question = questionData[3];
        nbAnswers = questionData[4];
        nbOk = questionData[5];
        answer1 = questionData[6];
        answer2 = questionData[7];
        answer3 = questionData[8];
        answer4 = questionData[9];

        nbAnswered = 0;

        initQueue(question);

        int nb = Integer.parseInt(nbAnswers);

        if(nb == 4){
            if (Locale.getDefault().getLanguage().equals("fr")) {
                addQueue("réponse une:");
                addQueue(answer1);
                addQueue("réponse deux:");
                addQueue(answer2);
                addQueue("réponse trois:");
                addQueue(answer3);
                addQueue("réponse quatre:");
                addQueue(answer4);
            } else {
                addQueue("answer one:");
                addQueue(answer1);
                addQueue("answer two:");
                addQueue(answer2);
                addQueue("answer three:");
                addQueue(answer3);
                addQueue("answer four:");
                addQueue(answer4);
            }
        }else if(nb == 3){
            if (Locale.getDefault().getLanguage().equals("fr")) {
                addQueue("réponse une:");
                addQueue(answer1);
                addQueue("réponse deux:");
                addQueue(answer2);
                addQueue("réponse trois:");
                addQueue(answer3);
            } else {
                addQueue("answer one:");
                addQueue(answer1);
                addQueue("answer two:");
                addQueue(answer2);
                addQueue("answer three:");
                addQueue(answer3);
            }

        }else{
            if (Locale.getDefault().getLanguage().equals("fr")) {
                addQueue("réponse une:");
                addQueue(answer1);
                addQueue("réponse deux:");
                addQueue(answer2);
            } else {
                addQueue("answer one:");
                addQueue(answer1);
                addQueue("answer two:");
                addQueue(answer2);
            }
        }
    }


    public void onSaveInstanceState(Bundle savedInstanceState) { //appelé avant la destruction de l'activité
        //saveContextHistory();

        savedInstanceState.putString("easy",easy);
        savedInstanceState.putString("medium", medium);
        savedInstanceState.putString("difficult", difficult);
        savedInstanceState.putString("question", question);
        savedInstanceState.putString("nbAnswers", nbAnswers);
        savedInstanceState.putString("nbOk", nbOk);
        savedInstanceState.putString("answer1", answer1);
        savedInstanceState.putString("answer2", answer2);
        savedInstanceState.putString("answer3", answer3);
        savedInstanceState.putString("answer4", answer4);

        savedInstanceState.putBoolean("afterValidation", afterValidation);
        savedInstanceState.putBoolean("goodAnswer", goodAnswer);
        savedInstanceState.putString("answerText", answerText);
        savedInstanceState.putInt("nbAnswered", nbAnswered);
        savedInstanceState.putBoolean("firstStart", firstStart);

        savedInstanceState.putInt("score", score);
        savedInstanceState.putInt("maxScore", maxScore);
        savedInstanceState.putInt("units", units);

        savedInstanceState.putInt("nbRep", nbRep);
        savedInstanceState.putInt("nbRepOk", nbRepOk);

        savedInstanceState.putInt("secs", secs);
        savedInstanceState.putBoolean("running", running);
        savedInstanceState.putBoolean("wasRunning", wasRunning);

        savedInstanceState.putInt("skipQuestion", skipQuestion);

        savedInstanceState.putBoolean("ttsInstalled", ttsInstalled);
        savedInstanceState.putBoolean("ttsEnabled", ttsEnabled);

        super.onSaveInstanceState(savedInstanceState);
    }


    private class ReadQuestion extends AsyncTask<Integer, Void, Boolean> {

        private String questionData[];
        private SQLiteDatabase db;
        private Cursor cursor;

        protected void onPreExecute() {

            questionData = new String[10];

            TextView textLoad = (TextView) findViewById(R.id.textLoad);
            textLoad.setVisibility(View.VISIBLE);

            LinearLayout layoutResult = (LinearLayout) findViewById(R.id.layoutResult);
            if (layoutResult != null) {
                layoutResult.setVisibility(View.GONE);
            }
        }


        protected Boolean doInBackground(Integer... questionId){
            String qId = Integer.toString(questionId[0]);
            try {
                SQLiteOpenHelper qhistDataBaseHelper = new QhistDatabaseHelper(Question.this);
                db = qhistDataBaseHelper.getReadableDatabase();

                cursor = db.query("QUESTIONS",
                        new String[] {"EASY","MEDIUM","DIFFICULT","QUESTION","NB_ANSWERS","NBOK","ANSWER1","ANSWER2","ANSWER3","ANSWER4"},
                        "_id = ?",
                        new String[]{qId},
                        null, null, null);
                if (cursor.moveToFirst()){
                    questionData[0] = cursor.getString(0);
                    questionData[1] = cursor.getString(1);
                    questionData[2] = cursor.getString(2);
                    questionData[3] = cursor.getString(3);
                    questionData[4] = cursor.getString(4);
                    questionData[5] = cursor.getString(5);
                    questionData[6] = cursor.getString(6);
                    questionData[7] = cursor.getString(7);
                    questionData[8] = cursor.getString(8);
                    questionData[9] = cursor.getString(9);
                }
                cursor.close();
                db.close();
                return true;
            }catch (SQLiteException e){
                //Log.i("alain", "erreur BD = "+ e.toString());
                return false;
            }
        }

        protected void onPostExecute(Boolean success){
            if (!success){
                Toast toast=Toast.makeText(Question.this, "Database invalide", Toast.LENGTH_SHORT);
                toast.show();
            }else {

                RelativeLayout layoutQuestion = (RelativeLayout) findViewById(R.id.layoutQuestion);
                if (layoutQuestion != null) {
                    layoutQuestion.setVisibility(View.VISIBLE);
                }

                TextView textEasy =(TextView)findViewById(R.id.textEasy);
                TextView textMedium =(TextView)findViewById(R.id.textMedium);
                TextView textDifficult =(TextView)findViewById(R.id.textDifficult);

                if (questionData[0].equals("1")){
                    textEasy.setVisibility(View.VISIBLE);
                    textMedium.setVisibility(View.INVISIBLE);
                    textDifficult.setVisibility(View.INVISIBLE);
                }else if (questionData[1].equals("1")){
                    textEasy.setVisibility(View.INVISIBLE);
                    textMedium.setVisibility(View.VISIBLE);
                    textDifficult.setVisibility(View.INVISIBLE);
                }else{
                    textEasy.setVisibility(View.INVISIBLE);
                    textMedium.setVisibility(View.INVISIBLE);
                    textDifficult.setVisibility(View.VISIBLE);
                }
                TextView textLoad = (TextView)findViewById(R.id.textLoad);
                textLoad.setVisibility(View.INVISIBLE);

                TextView textQuestion =(TextView)findViewById(R.id.textQuestion);
                textQuestion.setText(questionData[3]);

                LinearLayout layout = (LinearLayout) findViewById(R.id.layoutAnswers);
                layout.setVisibility(View.VISIBLE);

                TextView textAnswer1 =(TextView) findViewById(R.id.answer1);
                TextView textAnswer2 =(TextView) findViewById(R.id.answer2);
                TextView textAnswer3 =(TextView) findViewById(R.id.answer3);
                TextView textAnswer4 =(TextView) findViewById(R.id.answer4);

                textAnswer1.setBackgroundColor(0xFFFFFFFF);
                textAnswer2.setBackgroundColor(0xFFFFFFFF);
                textAnswer3.setBackgroundColor(0xFFFFFFFF);
                textAnswer4.setBackgroundColor(0xFFFFFFFF);


                int nb = Integer.parseInt(questionData[4]);
                if(nb == 4){
                    textAnswer1.setText(questionData[6]);
                    textAnswer1.setVisibility(View.VISIBLE);

                    textAnswer2.setText(questionData[7]);
                    textAnswer2.setVisibility(View.VISIBLE);

                    textAnswer3.setText(questionData[8]);
                    textAnswer3.setVisibility(View.VISIBLE);

                    textAnswer4.setText(questionData[9]);
                    textAnswer4.setVisibility(View.VISIBLE);

                }else if(nb == 3){
                    textAnswer1.setText(questionData[6]);
                    textAnswer1.setVisibility(View.VISIBLE);

                    textAnswer2.setText(questionData[7]);
                    textAnswer2.setVisibility(View.VISIBLE);

                    textAnswer3.setText(questionData[8]);
                    textAnswer3.setVisibility(View.VISIBLE);

                    textAnswer4.setVisibility(View.GONE);

                }else {
                    textAnswer1.setText(questionData[6]);
                    textAnswer1.setVisibility(View.VISIBLE);

                    textAnswer2.setText(questionData[7]);
                    textAnswer2.setVisibility(View.VISIBLE);

                    textAnswer3.setVisibility(View.GONE);

                    textAnswer4.setVisibility(View.GONE);
                }


                Question.this.onBackgroundTaskDataObtained(questionData);
            }
        }
    }

    private void newQuestion(){
        Random r = new Random();
        int questionId = r.nextInt(nbQuestions-1) + 1;

        new ReadQuestion().execute(questionId);
    }


    public void onInit(int initStatus){

        if(initStatus == TextToSpeech.SUCCESS){
            //Log.i("alain", "TTS init OK");

            if (Locale.getDefault().getLanguage().equals("fr")) {
                myTTS.setLanguage(Locale.FRANCE);
            } else {
                myTTS.setLanguage(Locale.US);
            }

            if (firstStart) {
                firstStart = false;
                newQuestion();
            }

        }else if (initStatus == TextToSpeech.ERROR){
            //Log.i("alain", "TTS init ERROR!!!!");
        }
    }

    private void initQueue(String text){
        if(ttsEnabled){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                //Log.i("alain", "Use TTS speak new version");
                myTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }else{
                //Log.i("alain", "Use TTS speak deprecated version");
                myTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    private void addQueue(String text){
        if(ttsEnabled){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                //Log.i("alain", "Use TTS speak new version");
                myTTS.speak(text, TextToSpeech.QUEUE_ADD, null, null);
            }else{
                //Log.i("alain", "Use TTS speak deprecated version");
                myTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }


    @Override
    protected void onStop(){ // appelé quand l'activité n'est plus visible
        //Log.i("alain", "call onStop");
        super.onStop();
        wasRunning = running;
        running = false;
    }


    @Override
    protected void onStart(){ // appelé après onCreate quand l'activité devient visible
        //Log.i("alain", "call onStart");
        super.onStart();
        if(wasRunning){
            running = true;
        }
    }

    @Override
    public void onDestroy(){ // appelé quand l'activité est détruite
        //Log.i("alain", "call onDestroy");
        if(myTTS != null){
            myTTS.stop();
            myTTS.shutdown();
        }
        super.onDestroy();
    }

}
