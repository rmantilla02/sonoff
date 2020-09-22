package ar.fiuba.appsonoff;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private static final List<String> WORDS_TO_ON = Arrays.asList("prender", "encender", "iniciar");
    private static final List<String> WORDS_TO_OFF = Arrays.asList("apagar", "finalizar");

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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "hable para ingresar la instrucción");
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
                    String instruction = result.get(0);
                    instruction = instruction.trim().toLowerCase();
                    Log.i("MSG", "instruccion: " + instruction);
                    if(WORDS_TO_ON.contains(instruction.trim()) || WORDS_TO_OFF.contains(instruction.trim())){
                        textView.setText("Estado: ");
                        // TODO: revisar: sigue tirando error de permisos
                        callDeviceSonoff(instruction);
                        changeState(instruction);

//                        Thread thread = new Thread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                try  {
//                                    //Your code goes here
//                                    getStatus();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//
//                        thread.start();
                    }else {
                        instruction = "instrucción no válida!";
                        textView.setText(instruction);
                    }

                }
                break;
            }
        }
    }

    /**
     * cambia el estado en la pantalla segun el parametro
     * @param instruction
     */
    private void changeState(String instruction){
        ImageView imageView = (ImageView)findViewById(R.id.statusImage);
        if(WORDS_TO_ON.contains(instruction)){
            Log.i("MSG", "prendiendo el dispositivo...");
            imageView.setImageResource(R.drawable.on);
        }else {
            Log.i("MSG", "apagando el dispositivo...");
            imageView.setImageResource(R.drawable.off);
        }
    }

    /**
     * apaga/prende el simulador segun el parametro
     * @param instruction
     */
    private void callDeviceSonoff(String instruction){
//        String urlString = "http://10.0.2.2:8080/zeroconf/switch";
        String urlString = "http://127.0.0.1:8080/zeroconf/switch";
        OutputStream out = null;
        String command = WORDS_TO_ON.contains(instruction) ? "on" : "off";
        //String body = "{\"data\": {\"switch\":\"+command+\"}";
        String body = "{\"data\": {\"switch\":\"+command+\"}";

       // JSONParser parser = new JSONParser();
        //JSONObject jsonObject = new JSONObject();
        //jsonObject.put("data", "arg_1");
        String response = "";
        try {
            Log.i("MSG", "invocando al simulador...");
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept","application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            //connection.setReadTimeout(15000);
            //connection.setConnectTimeout(15000);

            out = new BufferedOutputStream(connection.getOutputStream());
            Log.i("JSON", body);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(body);
            writer.flush();
            writer.close();
            //out.close();
            Log.i("STATUS", String.valueOf(connection.getResponseCode()));
            Log.i("MSG" , connection.getResponseMessage());
            int responseCode=connection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }

            connection.connect();
        } catch (Exception e) {
            Log.e("MSG", "error al invocar al simulador: " + e.getMessage() +". "+ e.getCause());
        }
    }

    private void getStatus() {

        URL url = null;

        try {
            System.out.println("inicio callSonoff.. ");
//            String urlStr = "http://127.0.0.1:8080/zeroconf/info";
            String urlInfo = "http://10.0.2.2:8080/zeroconf/info";

            url = new URL(urlInfo);

            // Send POST data request
            String data = "{\"data\": {}";
            Log.i("MSG", "obteniendo el estado del dispositivo...");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write( data );
            wr.flush();
            wr.close();
        } catch (MalformedURLException e) {
            Log.e("MSG", "error al obtener el estado del dispositivo: " + e.getMessage());

        } catch (IOException e) {
            Log.e("MSG", "error al obtener el estado del dispositivo: " + e.getMessage());

        }

    }

    /**
     * Solo de prueba
     */
//    public void run() {
//        // TODO Auto-generated method stub
//        URL myurl = null;
//        String jsoncode = null;
//        boolean threading;
//
//        try {
//            myurl = new URL("http://10.0.2.2/list.JSON");
//        }
//        catch (MalformedURLException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        try {
//            URLConnection myconn = myurl.openConnection();
//            InputStream in = new BufferedInputStream(myconn.getInputStream());
//            InputStreamReader reader = new InputStreamReader(in);
//            BufferedReader br = new BufferedReader(reader);
//            String line;
//            StringBuilder sb = new StringBuilder();
//            while ((line=br.readLine()) != null)
//            {
//                sb.append(line);
//                //Toast.makeText(getApplicationContext(), "I enter here", Toast.LENGTH_LONG).show();
//            }
//            jsoncode = sb.toString();
//        }
//        catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        threading = true;
////        super.run();
//    }
}
