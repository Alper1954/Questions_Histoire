package aperr.android.questionsdhistoire;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Locale;

import android.widget.Toast;


/**
 * Created by perrault on 02/06/2016.
 */
class QhistDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "qhist";
    private static final int DB_VERSION = 1;
    private static Context mycontext;

    QhistDatabaseHelper(Context context){

        super(context, DB_NAME, null, DB_VERSION);
        this.mycontext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //Log.i("alain", "QhistDatabaseHelper - onCreate");
        QhistCreateDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVerion){
        db.execSQL("DROP TABLE QUESTIONS;");
        QhistCreateDatabase(db);
    }

    private static void QhistCreateDatabase(SQLiteDatabase db){

        db.execSQL("CREATE TABLE QUESTIONS (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "EASY TEXT, "
                + "MEDIUM TEXT, "
                + "DIFFICULT TEXT, "
                + "QUESTION TEXT, "
                + "NB_ANSWERS TEXT, "
                + "NBOK TEXT, "
                + "ANSWER1 TEXT, "
                + "ANSWER2 TEXT, "
                + "ANSWER3 TEXT, "
                + "ANSWER4 TEXT);");

        String line;
        InputStream iStream;
        String easy, medium, difficult,question, nbok;
        String answer1 = "?";
        String answer2 = "?";
        String answer3 = "?";
        String answer4 = "?";

        int nb_answers, length;


        if (Locale.getDefault().getLanguage().equals("fr")) {
            iStream = mycontext.getResources().openRawResource(R.raw.quiz_histoire);
        } else {
            iStream = mycontext.getResources().openRawResource(R.raw.quiz_histoire_en);
        }

        BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream, Charset.forName("UTF8")));

        try {
            while ((line=bReader.readLine()) != null){

                String[] lineFields = line.split(";");
                length = lineFields.length;

                easy = lineFields[1];
                medium = lineFields[2];
                difficult = lineFields[3];
                question = lineFields[4];
                nb_answers = Integer.parseInt(lineFields[5]);
                nbok = lineFields[6];


                if(length >= (nb_answers + 5)){
                    if(nb_answers == 4){
                        answer1 = lineFields[7];
                        answer2 = lineFields[8];
                        answer3 = lineFields[9];
                        answer4 = lineFields[10];
                    }else if(nb_answers == 3){
                        answer1 = lineFields[7];
                        answer2 = lineFields[8];
                        answer3 = lineFields[9];
                    }else{
                        answer1 = lineFields[7];
                        answer2 = lineFields[8];
                    }
                }else{
                    Log.i("alain", line);
                }

                insertQuestion(db, easy, medium, difficult, question, nb_answers, nbok, answer1, answer2, answer3, answer4);
            }

        }catch (IOException e){
            Toast toast = Toast.makeText(mycontext, "Invalid file", Toast.LENGTH_SHORT);
        }


    }

    private static void insertQuestion(SQLiteDatabase db,
                                       String easy,
                                       String medium,
                                       String difficult,
                                       String question,
                                       int nb_answers,
                                       String nbok,
                                       String answer1,
                                       String answer2,
                                       String answer3,
                                       String answer4){


        ContentValues questionValues = new ContentValues();

        questionValues.put("EASY", easy);
        questionValues.put("MEDIUM", medium);
        questionValues.put("DIFFICULT", difficult);
        questionValues.put("QUESTION", question);
        questionValues.put("NB_ANSWERS", nb_answers);
        questionValues.put("NBOK", nbok);
        questionValues.put("ANSWER1", answer1);
        questionValues.put("ANSWER2", answer2);
        questionValues.put("ANSWER3", answer3);
        questionValues.put("ANSWER4", answer4);

        db.insert("QUESTIONS", null, questionValues);
    }

}
