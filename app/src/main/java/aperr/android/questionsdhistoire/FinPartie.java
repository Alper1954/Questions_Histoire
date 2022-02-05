package aperr.android.questionsdhistoire;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class FinPartie extends Activity {

    private int units;
    private int score;
    private int maxScore;
    private int nbRep;
    private int nbRepOk;
    private static final int scoreCadeau = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fin_partie);

        Intent intent = getIntent();
        units = intent.getIntExtra("units", 0);
        score = intent.getIntExtra("score", 0);
        maxScore = intent.getIntExtra("maxScore", 0);
        nbRep = intent.getIntExtra("nbRep", 0);
        nbRepOk = intent.getIntExtra("nbRepOk", 0);

        TextView finPartie = (TextView) findViewById(R.id.finPartie);
        if(units == 0){
            if (Locale.getDefault().getLanguage().equals("fr")) {
                finPartie.setText("[Unités à 0]");
            } else {
                finPartie.setText("[0 Unit]");
            }
        }else{
            if (Locale.getDefault().getLanguage().equals("fr")) {
                finPartie.setText("[temps écoulé]");
            } else {
                finPartie.setText("[time elapsed]");
            }
        }

        TextView textScore =(TextView) findViewById(R.id.textScore);
        if(score >= scoreCadeau){
            textScore.setText(Integer.toString(score)+ "   [>=" + Integer.toString(scoreCadeau)+ "]");
        }else{
            textScore.setText(Integer.toString(score));
        }

        TextView reponses =(TextView) findViewById(R.id.reponses);
        reponses.setText(Integer.toString(nbRepOk)+ "/" + Integer.toString(nbRep));

        TextView record =(TextView) findViewById(R.id.record);
        record.setText(Integer.toString(maxScore));

        LinearLayout repOK =(LinearLayout) findViewById(R.id.repOK);
        LinearLayout bestScore =(LinearLayout) findViewById(R.id.bestScore);

        TextView cadeau =(TextView) findViewById(R.id.cadeau);
        TextView winner =(TextView) findViewById(R.id.winner);
        TextView winner2 =(TextView) findViewById(R.id.winner2);
        TextView looser =(TextView) findViewById(R.id.looser);

        if(score >= scoreCadeau){

            repOK.setVisibility(View.GONE);
            bestScore.setVisibility(View.GONE);

            cadeau.setVisibility(View.VISIBLE);
        }else{
            repOK.setVisibility(View.VISIBLE);
            bestScore.setVisibility(View.VISIBLE);

            if(score > maxScore){
                cadeau.setVisibility(View.GONE);
                winner.setVisibility(View.VISIBLE);
                winner2.setVisibility(View.GONE);
                looser.setVisibility(View.GONE);
            }else if(score == maxScore){
                cadeau.setVisibility(View.GONE);
                winner.setVisibility(View.GONE);
                winner2.setVisibility(View.VISIBLE);
                looser.setVisibility(View.GONE);
            }else{
                cadeau.setVisibility(View.GONE);
                winner.setVisibility(View.GONE);
                winner2.setVisibility(View.GONE);
                looser.setVisibility(View.VISIBLE);
            }
        }

        Button buttonJouez = (Button)findViewById(R.id.buttonRejouez);

        buttonJouez.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(FinPartie.this, Question.class);
                startActivity(intent);
                finish();
            }
        });

        Button buttonParam = (Button)findViewById(R.id.buttonParam);

        buttonParam.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(FinPartie.this, Parameters.class);
                startActivity(intent);
            }
        });

        Button share = (Button)findViewById(R.id.share);
        Button share_en = (Button)findViewById(R.id.share_en);

        if (Locale.getDefault().getLanguage().equals("fr")) {
            share.setVisibility(View.VISIBLE);
            share_en.setVisibility(View.GONE);
        }else{
            share.setVisibility(View.GONE);
            share_en.setVisibility(View.VISIBLE);
        }

        share.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.setType("text/plain");

                String subject = getResources().getString(R.string.mailSubject);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                String message = getResources().getString(R.string.mailText);
                intent.putExtra(Intent.EXTRA_TEXT, message);

                startActivity(intent);
            }
        });

        share_en.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.setType("text/plain");

                String subject = getResources().getString(R.string.mailSubject);
                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                String message = getResources().getString(R.string.mailText);
                intent.putExtra(Intent.EXTRA_TEXT, message);

                startActivity(intent);
            }
        });
    }

}
