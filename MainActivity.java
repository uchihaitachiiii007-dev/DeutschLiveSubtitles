package com.example.deutschlivesubtitles;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import com.google.mlkit.nl.translate.*;

public class MainActivity extends Activity {
    private SpeechRecognizer recognizer;
    private boolean listening=false;
    private TextView german, english, status;
    private Translator translator;
    private final int REQ_AUDIO=100;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        if(!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this,"Speech recognition isn't available on this phone.",Toast.LENGTH_LONG).show();
        }
        TranslatorOptions options = new TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.GERMAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH).build();
        translator = Translation.getClient(options);
        translator.downloadModelIfNeeded()
            .addOnSuccessListener(v -> statusText("Translation ready"))
            .addOnFailureListener(e -> statusText("Translation model download failed. Check internet."));
        makeUI();
    }
    private void makeUI() {
        LinearLayout layout=new LinearLayout(this); layout.setOrientation(1); layout.setPadding(32,40,32,24);
        layout.setBackgroundColor(0xFF101820);
        TextView title=new TextView(this); title.setText("Deutsch Live Subtitles"); title.setTextSize(26); title.setTextColor(0xFFFFFFFF);
        status=new TextView(this); status.setText("Ready"); status.setTextColor(0xFF90CAF9); status.setPadding(0,20,0,16);
        german=new TextView(this); german.setText("German speech will appear here."); german.setTextSize(22); german.setTextColor(0xFFFFFFFF); german.setPadding(0,10,0,20);
        english=new TextView(this); english.setText("English translation appears here."); english.setTextSize(18); english.setTextColor(0xFFFFD54F); english.setPadding(0,10,0,20);
        Button overlay=new Button(this); overlay.setText("Enable floating subtitles");
        overlay.setOnClickListener(v -> {
            if(!Settings.canDrawOverlays(this)) {
                Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
                startActivity(i);
            } else { startService(new Intent(this,SubtitleOverlayService.class)); Toast.makeText(this,"Overlay enabled. Start listening below.",Toast.LENGTH_SHORT).show(); }
        });
        Button start=new Button(this); start.setText("Start listening");
        start.setOnClickListener(v -> startListening());
        Button stop=new Button(this); stop.setText("Stop listening");
        stop.setOnClickListener(v -> stopListening());
        layout.addView(title); layout.addView(status); layout.addView(german); layout.addView(english);
        layout.addView(overlay); layout.addView(start); layout.addView(stop);
        setContentView(layout);
    }
    private void startListening() {
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQ_AUDIO); return;
        }
        if(!SpeechRecognizer.isRecognitionAvailable(this)) { statusText("Speech recognition unavailable."); return; }
        if(recognizer!=null) recognizer.destroy();
        recognizer=SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            public void onReadyForSpeech(Bundle b){statusText("Listening… speak/play German nearby");}
            public void onBeginningOfSpeech(){}
            public void onRmsChanged(float x){}
            public void onBufferReceived(byte[] b){}
            public void onEndOfSpeech(){statusText("Processing…");}
            public void onError(int e){ if(listening) { statusText("Listening paused; tap Start again."); listening=false; } }
            public void onResults(Bundle b){ update(b); restart(); }
            public void onPartialResults(Bundle b){ update(b); }
            public void onEvent(int e,Bundle b){}
        });
        listening=true; restart();
    }
    private void restart() {
        if(!listening || recognizer==null)return;
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"de-DE");
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"de-DE");
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
        i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        recognizer.startListening(i);
    }
    private void update(Bundle b) {
        ArrayList<String> results=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(results==null || results.isEmpty())return;
        String text=results.get(0);
        german.setText(text);
        SubtitleOverlayService.setGerman(text);
        translator.translate(text).addOnSuccessListener(t -> {
            english.setText(t);
            SubtitleOverlayService.setEnglish(t);
        }).addOnFailureListener(e -> english.setText("Translation unavailable"));
    }
    private void stopListening() { listening=false; if(recognizer!=null) recognizer.stopListening(); statusText("Stopped"); }
    private void statusText(String s){ if(status!=null)status.setText(s); }
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g) {
        super.onRequestPermissionsResult(r,p,g);
        if(r==REQ_AUDIO && g.length>0 && g[0]==PackageManager.PERMISSION_GRANTED) startListening();
    }
    @Override protected void onDestroy(){stopListening(); if(recognizer!=null)recognizer.destroy(); if(translator!=null)translator.close(); super.onDestroy();}
}
