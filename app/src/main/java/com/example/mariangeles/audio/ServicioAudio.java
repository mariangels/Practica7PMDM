package com.example.mariangeles.audio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;


public class ServicioAudio extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mp;
    private enum Estados{
        idle,
        initialized,
        prepairing,
        prepared,
        started,
        paused,
        completed,
        sttoped,
        end,
        error
    };

    private Estados estado;
    public static final String PLAY="play";
    public static final String STOP="stop";
    public static final String ADD="add";
    public static final String PAUSE="pause";
    public static final String CHANGE="change";
    private String rutaCancion=null;
    private boolean reproducir;
    private ArrayList<String> cancionesPath;
    private int nCancion;

    /* ******************************************************* */
    // METODOS SOBREESCRITOS //
    /* ****************************************************** */

    @Override
    public void onCreate() {
        super.onCreate();
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int r = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if(r==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            // normal
            mp = new MediaPlayer();
            mp.setOnPreparedListener(this);
            mp.setOnCompletionListener(this);
            mp.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            estado = Estados.idle;
        } else {
            stopSelf();
        }
        cancionesPath= new ArrayList<String>();
    }

    @Override
    public void onDestroy() {
        mp.reset();
        mp.release();
        mp = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if(action.equals(PLAY)){
            play();
        }else if(action.equals(ADD)){
            String dato = intent.getStringExtra("cancion");
            add(dato);
        }else if(action.equals(STOP)){
            stop();
        }else if(action.equals(PAUSE)) {
            pause();
        }else if(action.equals(CHANGE)) {
            String dato = intent.getStringExtra("cancion");
            change(dato);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /* ****************************************************** */
    // INTERFAZ PREPARED LISTENER //
    /* ****************************************************** */

    @Override
    public void onPrepared(MediaPlayer mp) {
        estado = Estados.prepared;
        if(reproducir){
            mp.start();
            estado = Estados.started;
        }
    }

    /* ****************************************************** */
    // INTERFAZ COMPLETED LISTENER //
    /* ****************************************************** */

    @Override
    public void onCompletion(MediaPlayer mp) {
        estado = Estados.completed;
        //mp.seekTo(0);
        //mp.start();
        /*
        if(cancionesPath.size()!=nCancion + 1) {
            nCancion ++;
        }else
            nCancion = 0;
        this.rutaCancion = cancionesPath.get(nCancion);
        mp.reset();
        play();*/
    }

    /* ****************************************************** */
    // INTERFAZ AUDIO FOCUS CHANGED //
    /* ****************************************************** */

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                play();
                mp.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mp.setVolume(0.1f, 0.1f);
                break;
        }
    }

    /* ****************************************************** */
    // METODOS DE AUDIO //
    /* ****************************************************** */

    private void play(){
        if(rutaCancion != null){
            if(estado == Estados.error){
                estado = Estados.idle;
            }
            if(estado == Estados.idle){
                reproducir = true;
                try {
                    mp.setDataSource(rutaCancion);
                    estado = Estados.initialized;
                } catch (IOException e) {
                    estado= Estados.error;
                }
            }
            if(estado == Estados.initialized ||
                    estado == Estados.sttoped){
                reproducir = true;
                mp.prepareAsync();
                estado = Estados.prepairing;
            } else if(estado == Estados.prepairing) {
                reproducir = true;
            }
            if(estado == Estados.prepared ||
                    estado == Estados.paused ||
                    estado == Estados.completed ||
                    estado == Estados.started) {
                mp.start();
                estado = Estados.started;
            }
        }
    }

    private void stop(){
        if(estado == Estados.prepared ||
                estado == Estados.started ||
                estado == Estados.paused ||
                estado == Estados.completed){
            mp.seekTo(0); // Para volver al principio sino comentar para pause
            mp.stop();
            estado = Estados.sttoped;
        }
        reproducir = false;
    }

    private void pause() {
        if(estado == Estados.paused ||
                estado == Estados.started) {
            mp.pause();
            estado = Estados.paused;
        }
    }

    private void add(String cancion){
        cancionesPath.add(cancion);
    }

    private void change(String cancion){
        this.rutaCancion = cancion;
        mp.reset();
        estado = Estados.idle;
        play();
    }
}