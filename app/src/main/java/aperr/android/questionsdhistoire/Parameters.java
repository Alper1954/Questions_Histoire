package aperr.android.questionsdhistoire;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Locale;

public class Parameters extends Activity {

    private int gametime, capUnits, maxSkip;
    private Boolean erreur;
    private int MY_DATA_CHECK_CODE = 0;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);


        final SharedPreferences sharedPref = getSharedPreferences("QH_preferences", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        gametime = sharedPref.getInt("gametime", 2);
        final EditText edit_gametime = (EditText) findViewById(R.id.edit_gametime);
        edit_gametime.setText(Integer.toString(gametime));

        capUnits = sharedPref.getInt("capUnits", 20);
        final EditText edit_capUnits = (EditText) findViewById(R.id.edit_capUnits);
        edit_capUnits.setText(Integer.toString(capUnits));

        maxSkip = sharedPref.getInt("maxSkip", 10);
        final EditText edit_skipQuestion = (EditText) findViewById(R.id.edit_skipQuestion);
        edit_skipQuestion.setText(Integer.toString(maxSkip));

        final Switch soundSwitch = (Switch)findViewById(R.id.soundSwitch);
        Boolean soundSwitchStatus = sharedPref.getBoolean("soundSwitch", true);
        soundSwitch.setChecked(soundSwitchStatus);


        Button buttonValider = (Button) findViewById(R.id.buttonValider);
        buttonValider.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                erreur = false;

                try {
                    String sgametime = edit_gametime.getText().toString();
                    gametime = Integer.parseInt(sgametime);
                    if(gametime == 0){
                        edit_gametime.setTextColor(0xffff0000);
                        erreur = true;
                    }else{
                        edit_gametime.setTextColor(0xff000000);
                        editor.putInt("gametime", gametime);
                        editor.commit();
                    }
                }catch (Exception e) {
                    erreur = true;
                }


                try {
                    String scapUnits = edit_capUnits.getText().toString();
                    capUnits = Integer.parseInt(scapUnits);
                    if(capUnits == 0){
                        edit_capUnits.setTextColor(0xffff0000);
                        erreur = true;
                    }else{
                        edit_capUnits.setTextColor(0xff000000);
                        editor.putInt("capUnits", capUnits);
                        editor.commit();
                    }
                }catch (Exception e) {
                    erreur = true;
                }


                try {
                    String smaxSkip = edit_skipQuestion.getText().toString();
                    maxSkip = Integer.parseInt(smaxSkip);
                    if(maxSkip == 0){
                        edit_skipQuestion.setTextColor(0xffff0000);
                        erreur = true;
                    }else{
                        edit_skipQuestion.setTextColor(0xff000000);
                        editor.putInt("maxSkip", maxSkip);
                        editor.commit();
                    }
                }catch (Exception e) {
                    erreur = true;
                }
                

                if(erreur) {
                    if (Locale.getDefault().getLanguage().equals("fr")) {
                        toast = Toast.makeText(Parameters.this, "Erreurs de saisie", Toast.LENGTH_SHORT);
                    } else {
                        toast = Toast.makeText(Parameters.this, "Bad input", Toast.LENGTH_SHORT);
                    }
                    toast.show();
                }else{
                    finish();
                }


                editor.putBoolean("soundSwitch", soundSwitch.isChecked());
                editor.commit();


                Switch razScore =(Switch) findViewById(R.id.razScore);
                if(razScore.isChecked()){
                    editor.putInt("maxScore", 0);
                    editor.commit();
                    razScore.setChecked(false);
                }

            }
        });


        Button buttonAnnuler = (Button)findViewById(R.id.buttonAnnuler);
        buttonAnnuler.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });

    }

    public void onSwitchClicked(View view){
        boolean on =((Switch) view).isChecked();
        if(on){
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode != TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {

                Switch soundSwitch = (Switch)findViewById(R.id.soundSwitch);
                soundSwitch.setChecked(false);


                Toast toast = Toast.makeText(Parameters.this, "La synthèse vocale n'est pas disponible", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
