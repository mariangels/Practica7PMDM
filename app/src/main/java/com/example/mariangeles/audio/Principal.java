package com.example.mariangeles.audio;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


public class Principal extends Activity {

    private final int GETMUSICA=1;
    private final int GRABADORA=2;
    private ArrayList<Cancion> canciones;
    private ArrayList<String> nombres;
    private ListView lista;
    private ArrayAdapter ad;
    private TextView nombre;
    public int nCancion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad);
        initComponents();
    }

    private void initComponents(){
        lista= (ListView)findViewById(R.id.listView);
        canciones = new ArrayList<Cancion>();
        nombres = new ArrayList<String>();
        nombre = (TextView) findViewById(R.id.tvNombre);
        ad = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,nombres);
        lista.setAdapter(ad);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                nCancion=position;
                cambiar();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_grabar) {
            Intent i= new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            startActivityForResult(i, GRABADORA);
            return true;
        }if (id == R.id.action_añadir) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GETMUSICA);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*********  botones  ************/

    public void play(View v){
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.setAction(ServicioAudio.PLAY);
        startService(intent);
    }

    public void stop(View v){
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.setAction(ServicioAudio.STOP);
        startService(intent);
    }

    public void pause(View v){
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.setAction(ServicioAudio.PAUSE);
        startService(intent);
    }

    public void next(View v){
        if(canciones.size()!=nCancion + 1) {
            nCancion ++;
        }else
            nCancion = 0;

        cambiar();
    }

    public void prev(View v){
        if(canciones.size()!=nCancion + 1) {
            nCancion ++;
        }else
            nCancion = 0;
        cambiar();
    }

    public void pararServicio(View v){
        stopService(new Intent(this, ServicioAudio.class));
    }

    public void cambiar(){
        Cancion c= canciones.get(nCancion);
        nombre.setText(c.getTitulo());
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.putExtra("cancion",c.getPath());
        intent.setAction(ServicioAudio.CHANGE);
        startService(intent);
    }

    /*********************************/

    public void onActivityResult(int requestCode,int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == GETMUSICA) {
                Uri uri = data.getData();

                Cancion c=getCancion(uri);
                canciones.add(getCancion(uri));
                nombres.add(c.getTitulo());
                ad.notifyDataSetChanged();

                Intent intent = new Intent(this, ServicioAudio.class);
                intent.putExtra("cancion",getPath(uri));
                intent.setAction(ServicioAudio.ADD);
                startService(intent);
            }
        }
    }

    public String getPath(Uri uri) {
        Cursor cur = getContentResolver().query(uri,null, null, null, null);
        cur.moveToFirst();
        String path = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
        cur.close();
        return path;
    }

    public Cancion getCancion(Uri uri){
        Cursor cur = getContentResolver().query(uri,null, null, null, null);
        cur.moveToFirst();
        Cancion c = new Cancion(
            cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA)),
            cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE))
        );
        cur.close();
        return c;
    }



    /**********************************/
    /**********   CANCION    **********/
    /**********************************/

    public class Cancion  {
        private String path;
        private String titulo;

        public Cancion() {
        }

        public Cancion(String path, String titulo) {
            this.path = path;
            this.titulo = titulo;
        }

        public String getPath() {
            return path;
        }

        public String getTitulo() {
            return titulo;
        }
    }
}
