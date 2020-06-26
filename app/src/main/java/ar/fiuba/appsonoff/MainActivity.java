package ar.fiuba.appsonoff;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    //vistas
    TextView textView;
    ImageButton button;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textTV);
        button = findViewById(R.id.voiceBtn);

        //button click to show speech to text dialog
        button.setOnClickListener(new View.OnClickListener(){
             @Override
             public void onClick(View view) {
                speak();
             }
            }
        );
    }

    private void speak() {
        // intent to show speech to text dialog
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak something");

        // start intent
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        }
        catch (Exception e){
            Toast.makeText(this, ""+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // receive voice input and handle it


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQUEST_CODE_SPEECH_INPUT: {
                if(resultCode == RESULT_OK && data != null){
                    // get text array from voice intent
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //set to text view
                    String dataAux = result.get(0);

                    if(dataAux.equalsIgnoreCase("apagar") || dataAux.equalsIgnoreCase("prender")){
                        callSonoffSimulator(dataAux);
                    }else {
                        dataAux = "instrucción no válida!";
                    }
                    textView.setText(dataAux);


                }
                break;
            }
        }
    }

    private void callSonoffSimulator(String data){
//        String urlString = "http://127.0.0.1:8080/zeroconf/switch";
        String urlString = "https://10.0.2.2:8080/zeroconf/switch";

        OutputStream out = null;
        String command = data.equalsIgnoreCase("prender") ? "on" : "off";
        String body = "{\"data\": {\"switch\":\"+command+\"}";

       // JSONParser parser = new JSONParser();
        //JSONObject jsonObject = new JSONObject();
        //jsonObject.put("data", "arg_1");
        String response = "";
        try {
            System.out.println("inicio callSonoff.. ");
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            out = new BufferedOutputStream(urlConnection.getOutputStream());
            //Log.i("info", "invocando a sonoff");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(body);
            writer.flush();
            writer.close();
            out.close();
            System.out.println("view responseCode... ");
            int responseCode=urlConnection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }

            urlConnection.connect();
        } catch (Exception e) {
            System.out.println("error al invocar a sonoff " + e.getMessage() + "causa: " + e.getCause());
        }
    }
}