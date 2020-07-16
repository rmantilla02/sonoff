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
                    String command = result.get(0);

                    if(WORDS_TO_ON.contains(command.trim()) || WORDS_TO_OFF.contains(command.trim())){
                        textView.setText("Estado: ");
                        //callSonoffSimulator(dataAux);
                        // para simular el cambio de estado
                        changeState(command);
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
                        command = "instrucción no válida!";
                        textView.setText(command);
                    }



                }
                break;
            }
        }
    }

    /**
     * cambia el estado en la pantalla segun el parametro
     * @param command
     */
    private void changeState(String command){
        ImageView imageView = (ImageView)findViewById(R.id.statusImage);
        if(WORDS_TO_ON.contains(command)){
            Log.i("MSG", "prendiendo el dispositivo...");
            imageView.setImageResource(R.drawable.on);
        }else {
            Log.i("MSG", "apagando el dispositivo...");
            imageView.setImageResource(R.drawable.off);
        }
    }

    private void callSonoffSimulator(String data){
//        String urlString = "http://10.0.2.2:8080/zeroconf/switch";
        String urlString = "http://127.0.0.1:8080/zeroconf/switch";
        OutputStream out = null;
        String command = data.equalsIgnoreCase("prender") ? "on" : "off";
        //String body = "{\"data\": {\"switch\":\"+command+\"}";
        String body = "{\"data\": {\"switch\":\"on\"}";

       // JSONParser parser = new JSONParser();
        //JSONObject jsonObject = new JSONObject();
        //jsonObject.put("data", "arg_1");
        String response = "";
        try {
            System.out.println("inicio callSonoff.. ");
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
            Log.i("MSG", "invocando a sonoff");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(body);
            writer.flush();
            writer.close();
            //out.close();
            System.out.println("view responseCode... ");
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
            System.out.println("error al invocar a sonoff " + e.getMessage() + "causa: " + e.getCause());
        }
    }

    private void getStatus() {

        URL url = null;

        try {
            System.out.println("inicio callSonoff.. ");
//            String urlStr = "http://127.0.0.1:8080/zeroconf/info";
            String urlStr = "http://10.0.2.2:8080/zeroconf/info";
            String urlSwitch = "http://10.0.2.2:8080/zeroconf/switch";

            String data = URLEncoder.encode("data", "UTF-8")
                    + "=" + URLEncoder.encode("{\"switch\":\"on\"}", "UTF-8");
            url = new URL(urlSwitch);

            // Send POST data request

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write( data );
            wr.flush();
            wr.close();
            System.out.println("##### OK1.. ");
        } catch (MalformedURLException e) {
            System.out.println("##### OK2.. ");
            e.printStackTrace();

        } catch (IOException e) {
            System.out.println("##### OK3.. ");
            e.printStackTrace();

        }
        System.out.println("##### OK.. ");

    }

    public void run() {
        // TODO Auto-generated method stub
        URL myurl = null;
        String jsoncode = null;
        boolean threading;

        try {
            myurl = new URL("http://10.0.2.2/list.JSON");
        }
        catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            URLConnection myconn = myurl.openConnection();
            InputStream in = new BufferedInputStream(myconn.getInputStream());
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(reader);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line=br.readLine()) != null)
            {
                sb.append(line);
                //Toast.makeText(getApplicationContext(), "I enter here", Toast.LENGTH_LONG).show();
            }
            jsoncode = sb.toString();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        threading = true;
//        super.run();
    }
}
