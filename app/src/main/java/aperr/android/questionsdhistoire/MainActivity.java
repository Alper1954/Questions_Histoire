package aperr.android.questionsdhistoire;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {

    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Log.i("alain", "MainActivity");


        Button buttonParam = (Button)findViewById(R.id.buttonParam);

        buttonParam.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Parameters.class);
                startActivity(intent);
            }
        });

        Button description = (Button)findViewById(R.id.buttonApropos);

        description.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Description.class);
                startActivity(intent);
            }
        });


        Button buttonJouez = (Button)findViewById(R.id.buttonJouez);

        buttonJouez.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, Question.class);
                startActivity(intent);
            }
        });

    }

}
