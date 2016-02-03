package com.ice.toaqui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ice.toaqui.pojo.Area;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


public class SendLocalActivity extends AppCompatActivity {

    static List<String> mapas;
    static List<String> cities;
    private static String url = "http://ehlogoali.eduardobarrere.com/consulta/";
    private static String urlArea = "xml6.php";
    String datapadrao = "26-10-1989";
    List<Area> areas;
    List<String> ultimosupdates;
    ProgressDialog mProgressDialog;
    static boolean msgLocais = true;
    String statusUpdate;
    public static final String VERSION_PREFS = "VersionsFile";
    public static final String MAPA_PREF = "MapaEscolhido";
    public static final String CITY_PREF = "CidadeEscolhida";
    public static final String ALL_CITY_PREFS = "CidadesExistentes";

    public static String mapaescolhido;
    static boolean ehArea = true;
    public static String mapaescolhido2;
    static boolean ableToContinue = false;
    static TextView textArea;
    static TextView textCidade;

    // Progress Dialog
    private ProgressDialog pDialog;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_Message = "message";
    private static EditText inputName;
    private static EditText inputDesc;
    JSONParser jsonParser = new JSONParser();
    private static String url_create_product = "http://ehlogoali.eduardobarrere.com/joao";
    Double lat;
    Double lng;
    public static PerfilData perfil;
    private static String cidadeAtual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_local);
        perfil = MainActivity.getPerfil();
        TextView textLocation = (TextView)findViewById(R.id.infoLL);
        LatLng latLng = MainActivity.getLatLng();
        lat = latLng.latitude;
        lng = latLng.longitude;
        textLocation.setText("Latitude: "+lat.toString()+"\nLongitude: "+lng.toString());

        textArea = (TextView)findViewById(R.id.area);
        textCidade = (TextView)findViewById(R.id.cidade);

        inputName = (EditText) findViewById(R.id.nome);
        inputDesc = (EditText) findViewById(R.id.link);
        perfil.setSuperUser(true);



        inputName.setText(MainActivity.getNomeLocal());
        inputDesc.setText(MainActivity.getLinkLocal());
        textArea.setText(perfil.getMapaAtual());
        textArea.setVisibility(View.VISIBLE);
        textCidade.setText(perfil.getCidadeAtual());
        textCidade.setVisibility(View.VISIBLE);
        //--------------------------------------------
        mapas = new ArrayList<String>();
        cities = new ArrayList<String>();
        ultimosupdates = new ArrayList<String>();
        statusUpdate = "Checar Atualizações";


        //Carrega e salva preferencias
        loadAllPrefs();
        loadMapaPref();
        loadCityPref();
        loadAllCityPrefs();
        boolean conexao = verificaConexao();
        boolean atualiza = true;
        if(!(conexao) && mapas.isEmpty()){
            //gera??o do dialog para avisar a ausencia de conex?o
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Para a primeira vez que a aplicação é aberta, o uso de internet é necessário.\n Conecte a internet e reabra a aplicação")
                    .setTitle("Sem Conexão");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }



        mProgressDialog = new ProgressDialog(SendLocalActivity.this);
        mProgressDialog.setMessage("Carregando...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_send_local, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    ///////Metodos relacionados as preferencias

    /*Jo?o-10/09-carregar no inicio da app a escolha de mapa do usu?rio*/
    public void loadMapaPref(){
        // Restore preferences
        SharedPreferences mapa = getSharedPreferences(MAPA_PREF, 0);

        String aux = mapa.getString("mapaescolhido", null);
        if(aux==null){

        }else{
            mapaescolhido=aux;
        }
    }
    /*Jo?o-10/09-carregar no inicio da app os mapas que tem registrado e qual a data de atualiza??o*/
    //
    public void loadAllPrefs(){
        // Restore preferences
        SharedPreferences versoes = getSharedPreferences(VERSION_PREFS, 0);
        Map<String, ?> infos= versoes.getAll();
        for (Map.Entry<String, ?> entry : infos.entrySet())
        {
            if(mapas.indexOf(entry.getKey())==-1){
                mapas.add(entry.getKey());
                ultimosupdates.add((String)entry.getValue());
            }
        }
        for(int i=0;i<mapas.size();i++){
            String aux = versoes.getString(mapas.get(i), ultimosupdates.get(i));
            savePrefs(mapas.get(i),aux);
        }
    }
    /*Jo?o-10/09-Salva a escolha de mapa do usu?rio, deve ser chamado no bot?o de salvar as configura??es*/
    public void saveMapaPref(String varPref, String valor){
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(MAPA_PREF, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(varPref, valor);

        // Commit the edits!
        editor.commit();
    }
    /*Jo?o-10/09-Salva mapas e datas de atualiza??o nas preferencias, caso seja um novo mapa ? passado o nome e uma datapadr?o=26-10-1989*/
    //N?O USADO AINDA
    public void savePrefs(String name, String valor){
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(VERSION_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, valor);

        // Commit the edits!
        editor.commit();
        int i = mapas.indexOf(name);
        ultimosupdates.set(i, valor);
    }

    ///////////////////////c?digos inseridos a partir do dia 16/09

    public void loadCityPref() {
        // Restore preferences
        SharedPreferences cidade = getSharedPreferences(CITY_PREF, 0);

        String aux = cidade.getString("CidadeEscolhida", null);
        if (aux == null) {

        } else {
            cidadeAtual = aux;
        }
    }

    /*Jo�o-10/09-carregar no inicio da app os mapas que tem registrado e qual a data de atualiza��o*/
    //
    public void loadAllCityPrefs() {
        // Restore preferences
        SharedPreferences versoes = getSharedPreferences(ALL_CITY_PREFS, 0);
        Map<String, ?> infos = versoes.getAll();
        for (Map.Entry<String, ?> entry : infos.entrySet()) {
            if (cities.indexOf(entry.getKey()) == -1) {
                cities.add(entry.getKey());
                ultimosupdates.add((String) entry.getValue());
            }
        }
        for (int i = 0; i < cities.size(); i++) {
            String aux = versoes.getString(cities.get(i), ultimosupdates.get(i));
            saveCityPrefs(cities.get(i), aux);

        }
    }

    /*Jo�o-10/09-Salva a escolha de mapa do usu�rio, deve ser chamado no bot�o de salvar as configura��es*/
    public void saveCityPref(String varPref, String valor) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(CITY_PREF, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(varPref, valor);

        // Commit the edits!
        editor.commit();
    }

    /*Jo�o-10/09-Salva mapas e datas de atualiza��o nas preferencias, caso seja um novo mapa � passado o nome e uma datapadr�o=26-10-1989*/
    //N�O USADO AINDA
    public void saveCityPrefs(String name, String valor) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(ALL_CITY_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, valor);

        // Commit the edits!
        editor.commit();
        int i = cities.indexOf(name);
    }


    /* Fun??o para verificar exist?ncia de conex?o com a internet*/
    public  boolean verificaConexao() {
        boolean conectado;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            conectado = true;
        } else {
            conectado = false;
        }
        return conectado;
    }



    public void escolhaCidade(View view)
    {

        CharSequence[] opt = cities.toArray(new CharSequence[cities.size()]);


        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2
                .setTitle("Áreas")
                .setIcon(R.drawable.map)
                .setItems(opt, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        //which eh o elemento selecionado
                        String s = cities.get(which).toString();
                        textCidade.setText(s);
                        textCidade.setVisibility(View.VISIBLE);
                        if (verificaConexao()) {


                        } else
                            Toast.makeText(getApplicationContext(), "Você esta sem conexão no momento!\n Ative a internet para baixar / atualizar as àreas", Toast.LENGTH_SHORT).show();


                        msgLocais = true;

                    }
                });

        AlertDialog dialog2 = builder2.create();
        dialog2.show();
    }

    public void escolhaCidade2(View view){
        if(perfil.isSuperUser()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Cidades");
            alert.setMessage("Digite uma nova cidade");

// Set an EditText view to get user input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String s = input.getText().toString();
                    textCidade.setText(s);
                    textCidade.setVisibility(View.VISIBLE);
                    // Do something with value!

                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            AlertDialog dialog2 = alert.create();
            dialog2.show();
        }else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Cidades");
            alert.setMessage("Você não possui permissão para criar nova cidade");

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });

            AlertDialog dialog2 = alert.create();
            dialog2.show();
        }
        if(verificaConexao()){
        }else Toast.makeText(getApplicationContext(), "Você esta sem conexão no momento!\n Ative a internet para baixar / atualizar as àreas", Toast.LENGTH_SHORT).show();

    }

    public void escolhaMapa(View view)
    {

        CharSequence[] opt = mapas.toArray(new CharSequence[mapas.size()]);


        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2
                .setTitle("Áreas")
                .setIcon(R.drawable.map)
                .setItems(opt, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        //which eh o elemento selecionado
                        String s = mapas.get(which).toString();
                        textArea.setText(s);
                        textArea.setVisibility(View.VISIBLE);
                        if(verificaConexao()){



                        }else Toast.makeText(getApplicationContext(), "Você esta sem conexão no momento!\n Ative a internet para baixar / atualizar as àreas", Toast.LENGTH_SHORT).show();


                        msgLocais = true;

                    }
                });

        AlertDialog dialog2 = builder2.create();
        dialog2.show();
    }

    public void escolhaMapa2(View view){
        if(perfil.isSuperUser()) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Áreas");
            alert.setMessage("Digite uma nova área");

// Set an EditText view to get user input
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String s = input.getText().toString();
                    textArea.setText(s);
                    textArea.setVisibility(View.VISIBLE);
                    // Do something with value!

                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            AlertDialog dialog2 = alert.create();
            dialog2.show();
        }else{
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Áreas");
            alert.setMessage("Você não possui permissão para criar nova área");

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });

            AlertDialog dialog2 = alert.create();
            dialog2.show();
        }
        if(verificaConexao()){
        }else Toast.makeText(getApplicationContext(), "Você esta sem conexão no momento!\n Ative a internet para baixar / atualizar as àreas", Toast.LENGTH_SHORT).show();

    }


    public void enviaBanco(View view){
        textArea = (TextView)findViewById(R.id.area);
        inputName = (EditText) findViewById(R.id.nome);
        inputDesc = (EditText) findViewById(R.id.link);
        if(inputName.getText().toString()!="" && inputName.getText().toString()!=null) {
            if(textArea.getText().toString()!="" && textArea.getText().toString()!=null) {
                new CreateNewProduct().execute();
            }else Toast.makeText(getApplicationContext(), "Você não definiu a área do local!\n Campo Obrigatório!", Toast.LENGTH_LONG).show();
        }else Toast.makeText(getApplicationContext(), "Você não deu nome ao local!\n Campo Obrigatório!", Toast.LENGTH_LONG).show();
    }


    // / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
    /// / / / / / / / / / / / / / / / / / / / / / / / / / / / /
    // / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
    /// / / / / / / / / / / / / / / / / / / / / / / / / / / / /
    // / / / / / / / / / / / / / / / / / / / / / / / / / / / / /
    /// / / / / / / / / / / / / / / / / / / / / / / / / / / / /


    class CreateNewProduct extends AsyncTask<String, String, String> {

        private int success;
        private String message;
        private JSONObject json;
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SendLocalActivity.this);
            pDialog.setMessage("Enviando Dados...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            String name = inputName.getText().toString();
            String area = textArea.getText().toString();
            String cidade = textCidade.getText().toString();
            String link = inputDesc.getText().toString();
            String lat = SendLocalActivity.this.lat.toString();
            String lng = SendLocalActivity.this.lng.toString();
            String email = perfil.getEmail();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("nome", name.toString()));
            params.add(new BasicNameValuePair("area", area.toString()));
            params.add(new BasicNameValuePair("link", link.toString()));
            params.add(new BasicNameValuePair("latitude", lat.toString()));
            params.add(new BasicNameValuePair("longitude", lng.toString()));
            params.add(new BasicNameValuePair("cidade", cidade.toString()));
            params.add(new BasicNameValuePair("email", email.toString()));


            // getting JSON Object
            // Note that create product url accepts POST method
            json = jsonParser.makeHttpRequest(url_create_product,
                    "GET", params);

            try {
                // check log cat fro response
                Log.d("Create Response", json.toString());
            }catch (Exception e){
                e.printStackTrace();
            }



            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            // check for success tag
            try {
                success = json.getInt(TAG_SUCCESS);
                message = json.getString(TAG_Message);

                if (success == 1) {
                    // successfully created product
                    //Looper.prepare();

                    successMensage();

                } else {
                    // failed to create product
                    //Looper.prepare();
                    errorMensage();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                errorMensage();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                errorMensage();
            }
        }

        public void successMensage() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SendLocalActivity.this);
            builder.setMessage("Os dados foram salvos!"+message)
                    .setTitle("Sucesso!")
                    .setIcon(R.drawable.success);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            // closing this screen
            //finish();
        }

        public void errorMensage() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SendLocalActivity.this);
            builder.setMessage("Ocorreu erro de envio!\nTente mais tarde.\nERRO: "+message)
                    .setTitle("Falha!")
                    .setIcon(R.drawable.erro);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            // closing this screen
            //finish();
        }

    }

    public static PerfilData getPerfil(){
        return perfil;
    }

    public static void setPerfil(PerfilData perfilNovo){
        perfil = perfilNovo;
    }

}
//http://www.androidhive.info/2012/05/how-to-connect-android-with-php-mysql/