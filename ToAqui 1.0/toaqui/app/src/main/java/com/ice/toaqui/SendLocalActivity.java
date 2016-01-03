package com.ice.toaqui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
    public static String mapaescolhido;
    static boolean ehArea = true;
    public static String mapaescolhido2;
    static boolean ableToContinue = false;
    static TextView textArea;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_local);
        TextView textLocation = (TextView)findViewById(R.id.infoLL);
        LatLng latLng = MainActivity.getLatLng();
        lat = latLng.latitude;
        lng = latLng.longitude;
        textLocation.setText("Latitude: "+lat.toString()+"\nLongitude: "+lng.toString());

        textArea = (TextView)findViewById(R.id.area);
        inputName = (EditText) findViewById(R.id.nome);
        inputDesc = (EditText) findViewById(R.id.link);

                //--------------------------------------------
        mapas = new ArrayList<String>();
        ultimosupdates = new ArrayList<String>();
        statusUpdate = "Checar Atualizações";


        //Carrega e salva preferencias
        loadAllPrefs();
        loadMapaPref();
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
        else{
//			carregamento dos dados e atualiza??o e exibi??o do tutorial
            if(mapas.isEmpty()){
                //showTutorial(0);
            }else{
                //aqui ele atualiza ou carrega as áreas existentes
                verificaAreas(true);
                atualiza = false;
            }
            //SendHttpRequestTask t = new SendHttpRequestTask();
            //String[] params = new String[]{url+urlArea, null, null};
            //t.execute(params);
            ehArea = true;

        }
        //aqui ele vai baixar as áreas caso seja o primeiro acesso
        if(atualiza)
        {
            verificaAreas(false);

            ehArea = true;
        }
        msgLocais = true;

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


    public void showTutorial(int i)
    {
        LayoutInflater factory = LayoutInflater.from(this);
        final View view;

        ImageView image;
        ImageView image2;

        TextView text;
        TextView text2;
        TextView text3;

        final String title;
        String voltar = "<< Voltar";
        String sair = "Sair";
        String proximo = "Próximo >>";

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(i){
            case 0:
                //geração do dialog para o sobre

                view = factory.inflate(R.layout.image, null);
                image = (ImageView) view.findViewById(R.id.image);
                text= (TextView) view.findViewById(R.id.textView);

                image.setImageResource(R.drawable.tutorial);

                title = "Tutorial";
                text.setText("Gostaria de fazer o tutorial agora?");

                builder.setView(view)
                        //.setMessage(R.string.string_array_help_newmapa)
                        .setIcon(R.drawable.tut)
                        .setTitle(title);
                proximo = "Vamos lá!";
                voltar = "Agora não";

                builder.setPositiveButton(proximo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(1);
                    }
                });
                builder.setNegativeButton(voltar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                    }
                });

                break;

            case 1:
                //geração do dialog para o sobre
                view = factory.inflate(R.layout.image, null);
                image = (ImageView) view.findViewById(R.id.image);
                text= (TextView) view.findViewById(R.id.textView);



                image.setImageResource(R.drawable.menu);
                //image2.setImageResource(R.drawable.up);
                title = "Tutorial - Menu";
                text.setText("A aplicação tem um menu com as opções a baixo:");


                builder.setView(view)
                        //.setMessage(R.string.string_array_help_newmapa)
                        .setIcon(R.drawable.tut)
                        .setTitle(title);

                builder.setPositiveButton(proximo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(2);
                    }
                });
                builder.setNegativeButton(sair, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                    }
                });
                builder.setNeutralButton(voltar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(0);
                    }
                });


                break;
            case 2:
                //geração do dialog para o sobre
                view = factory.inflate(R.layout.image, null);
                image = (ImageView) view.findViewById(R.id.image);
                text= (TextView) view.findViewById(R.id.textView);



                image.setImageResource(R.drawable.tut3);
                //image2.setImageResource(R.drawable.up);
                title = "Tutorial - Atualizar Mapa";
                text.setText("Você deve selecionar um mapa em \"Atualizar Mapa\" para que possa utilizá-lo." +
                        "Só assim poderá selecioná-lo na opção \"Mapas\".");

                builder.setView(view)
                        //.setMessage(R.string.string_array_help_newmapa)
                        .setIcon(R.drawable.tut)
                        .setTitle(title);

                builder.setPositiveButton(proximo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(3);
                    }
                });
                builder.setNegativeButton(sair, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                    }
                });
                builder.setNeutralButton(voltar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(1);
                    }
                });

                break;
            case 3:
                //geração do dialog para o sobre
                view = factory.inflate(R.layout.dialog1, null);
                image = (ImageView) view.findViewById(R.id.imageView);
                image2 = (ImageView) view.findViewById(R.id.imageView2);

                text= (TextView) view.findViewById(R.id.textView);
                text2= (TextView) view.findViewById(R.id.textView2);
                text3= (TextView) view.findViewById(R.id.textView3);



                image.setImageResource(R.drawable.tut3);
                image2.setImageResource(R.drawable.orangedot);

//				text.setText("");
                text.setText("Em \"Mapas\" você escolhe um mapa para visualizar.");
                text2.setText("Depois no marker amarelo:");
                text3.setText("Você escolhe um local que aparecerá no mapa do App.");
                title = "Tutorial - Mapas";

                builder.setView(view)
                        //.setMessage(R.string.string_array_help_newmapa)
                        .setIcon(R.drawable.tut)
                        .setTitle(title);

                builder.setPositiveButton(proximo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(4);
                    }
                });
                builder.setNegativeButton(sair, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                    }
                });
                builder.setNeutralButton(voltar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(2);
                    }
                });

                break;
            case 4:
                //geração do dialog para o sobre
                view = factory.inflate(R.layout.dialog1, null);
                image = (ImageView) view.findViewById(R.id.imageView);
                image2 = (ImageView) view.findViewById(R.id.imageView2);

                text= (TextView) view.findViewById(R.id.textView);
                text2= (TextView) view.findViewById(R.id.textView2);
                text3= (TextView) view.findViewById(R.id.textView3);



                //image.setImageResource(R.drawable.opt);
                image.setImageResource(R.drawable.tut4);

//				text.setText("");
                text.setText("Clicando em \"Satélite / Normal\"");
                text2.setText("Você transita entre as imagens de Satélite e a padrão.");
                text3.setText("");

                title = "Tutorial - Satelite / Normal";

                builder.setView(view)
                        //.setMessage(R.string.string_array_help_newmapa)
                        .setIcon(R.drawable.tut)
                        .setTitle(title);

                builder.setPositiveButton(proximo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(5);
                    }
                });
                builder.setNegativeButton(sair, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                    }
                });
                builder.setNeutralButton(voltar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(3);
                    }
                });

                break;
            case 5:
                //geração do dialog para o sobre
                view = factory.inflate(R.layout.dialog1, null);
                image = (ImageView) view.findViewById(R.id.imageView);
                image2 = (ImageView) view.findViewById(R.id.imageView2);

                text= (TextView) view.findViewById(R.id.textView);
                text2= (TextView) view.findViewById(R.id.textView2);
                text3= (TextView) view.findViewById(R.id.textView3);



                image.setImageResource(R.drawable.tut5);
                //image2.setImageResource();

                text.setText("Ao clicar no marker do mapa, você terá mais infomações sobre o local. ");
                text2.setText("Em destaque com seta e caixa vermelha aparecerá a opção de rota. ");
                text3.setText("Clicando nele, você será redirecionado ao Google Maps App e será requisitado" +
                        " a permissão para o uso de GPS para se construir a rota de onde você está até o local." +
                        " A outra opção ao lado redireciona você para o Google Maps App.");



                title = "Tutorial - Informações e Rota";

                builder.setView(view)
                        //.setMessage(R.string.string_array_help_newmapa)
                        .setIcon(R.drawable.tut)
                        .setTitle(title);

                builder.setPositiveButton(proximo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(6);
                    }
                });
                builder.setNegativeButton(sair, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                    }
                });
                builder.setNeutralButton(voltar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(4);
                    }
                });

                break;
            case 6:
                view = factory.inflate(R.layout.image, null);
                image = (ImageView) view.findViewById(R.id.image);
                text= (TextView) view.findViewById(R.id.textView);

                image.setImageResource(R.drawable.tutorial);

                title = "Tutorial";
                text.setText("Pronto! Boa Sorte!");

                builder.setView(view)
                        //.setMessage(R.string.string_array_help_newmapa)
                        .setIcon(R.drawable.tut)
                        .setTitle(title);
                proximo = "Ir para a aplicação";


                builder.setPositiveButton(proximo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                    }
                });
                builder.setNegativeButton(voltar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(5);
                    }
                });

                break;
        }

//		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//	           public void onClick(DialogInterface dialog, int id) {
//	               // User clicked OK button
//
//	           }
//	       });
//		builder.setNeutralButton("Voltar", new DialogInterface.OnClickListener() {
//	           public void onClick(DialogInterface dialog, int id) {
//	               // User clicked OK button
//
//	           }
//	       });
//		builder.setNegativeButton("sair", new DialogInterface.OnClickListener() {
//	           public void onClick(DialogInterface dialog, int id) {
//	               // User clicked OK button
//
//	           }
//	       });

        AlertDialog dialog = builder.create();
        dialog.show();

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
        if(verificaConexao()){
        }else Toast.makeText(getApplicationContext(), "Você esta sem conexão no momento!\n Ative a internet para baixar / atualizar as àreas", Toast.LENGTH_SHORT).show();

    }



    public void verificaAreas(boolean teste)
    {
        if(!teste){
            teste = verificaConexao();
        }
        if(teste) {
            final DownloadTask2 dt = new DownloadTask2(SendLocalActivity.this);
            dt.execute(url + urlArea);
        }else Toast.makeText(this, "Você esta sem conexão no momento!", Toast.LENGTH_SHORT).show();
    }

    //inner class que efetua o download e instala os dados na aplica??o
    public class DownloadTask2 extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;


        public DownloadTask2(Context context) {
            this.context = context;
        }

        //salva os dados dentro da aplica??o
        @Override
        protected String doInBackground(String... sUrl) {
            String s = null;

            //metodo para salvar dentro do app o arquivo
            s = INTERNALmethod(sUrl);


            return s;
        }

        //metodo q insere os dados dentro do app
        protected String INTERNALmethod(String... sUrl)
        {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();

                String s = context.getFilesDir().toString();

                output = new FileOutputStream(context.getFilesDir()+"/Areas.xml");


                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally { //por fim fecha os arquivos de leitura / escrita
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;

        }
        //metodo q grava no cart?o (n?o est? sendo usado
        protected String EXTERNALmethod(String... sUrl)
        {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                //String mapaescolhido = Main.getMapaEscolhido();

                output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/"+mapaescolhido2+".xml");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;

        }
        //prepara??o para executa o salvamento dos arquivos na mem?ria do aparelho
        // @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            //mProgressDialog.show();
        }
        //aqui mostra um progresso do download
        //@Override
        //protected void onProgressUpdate(Integer... progress) {
        //    super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        //    mProgressDialog.setIndeterminate(false);
        //    mProgressDialog.setMax(100);
        //    mProgressDialog.setProgress(progress[0]);
        //    if (progress[0] == 100) {
        //        mProgressDialog.dismiss();
        //    }
        // }

        //execu??o posterior ao download
        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
//	      mProgressDialog.dismiss();
            if (result != null){ //aviso caso tenha ocorrido um erro
                Toast.makeText(context,"Erro de Download", Toast.LENGTH_LONG).show();
                statusUpdate = "Checar Atualizações";
            }
            else{//aviso q ocorreu tudo certo
                Toast.makeText(context,"Mapas baixados", Toast.LENGTH_SHORT).show();

                //informa q est? atualizado

                //statusUpdate = "Atualizado";
                //Toast.makeText(context, statusUpdate, Toast.LENGTH_LONG).show();
                statusUpdate = "Checar Atualizações";
                //ultimosupdates.contains(formattedDate);

            }
            useDownload();
        }

        public void useDownload(){

            File f = new File(context.getFilesDir()+ "/Areas.xml");


            InputSource is = new InputSource(f.toURI().toString());
            if(ehArea) {
                areas = RssParserHelper.parseArea(is);

                //caso mapas esteja vazio ele é preenchido já q será utilizado para fornecer os dados
                if (mapas.isEmpty()) {
                    for (Area area : areas) {
                        mapas.add(area.getNome());
                        ultimosupdates.add(datapadrao);
                        savePrefs(area.getNome(), datapadrao);
                    }
                } else {//se n?o atualiza o q interessa
                    for (Area area : areas) {
                        int index = mapas.indexOf(area.getNome());
                        if (index == -1) {
                            mapas.add(area.getNome());
                            ultimosupdates.add(datapadrao);
                            savePrefs(area.getNome(), datapadrao);
                        }
                        String codigo = (String)area.getIdarea().toString();
                        //ehArea=false;
                        //SendHttpRequestTask t = new SendHttpRequestTask();//ENVIA AO SERVIDOR UMA REQUISI??O ASYNCTASK, OU SEJA, EM PARALELO PARA N?O TRAVAR A APLICA??O;
                        //String[] params = new String[]{url+urlLocal+codigo/*+codigo da ?rea*/, null, null};
                        //t.execute(params);

                    }
                }
                ArrayList<String> mapasToDelete = new ArrayList<String>();
                for (String mapa : mapas) {
                    int index = -1;
                    for (Area area : areas) {

                        if(area.getNome().indexOf(mapa) != -1){
                            index = 1;
                        }
                    }
                    if (index == -1) {
                        mapasToDelete.add(mapa);
                    }
                }
                for (String mapa : mapasToDelete) {
                    mapas.remove(mapa);
                }
                // se areas e mapas estiverem vazios significa q ? primeira vez q a aplica??o ? carregada
                // e n?o h? comunica??o com o servidor
                // logo deve se informar ao usu?rio q a aplica??o vai ter q ser executada mais tarde
                if (areas.isEmpty() && mapas.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SendLocalActivity.this);
                    builder.setMessage("Para a primeira vez que a aplicação é aberta, " +
                            "a conexão com o servidor é necessária.\n Desculpe-nos o trantorno " +
                            "e reabra a aplicação mais tarde")
                            .setTitle("Servidor OFF");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            finish();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                } else {
                    ableToContinue = true;

                    ///Faz o mesmo na configura??o em lista
                    //if (mapasWasEmpty || firstClick) {
                    Toast.makeText(getApplicationContext(), "Opções de Mapas Carregadas", Toast.LENGTH_LONG).show();
                    //}
                }
            }
            statusUpdate = "Checar Atualizações";
        }

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

    /**
     * Background Async Task to Create new product
     * */
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
            String link = inputDesc.getText().toString();
            String lat = SendLocalActivity.this.lat.toString();
            String lng = SendLocalActivity.this.lng.toString();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("nome", name.toString()));
            params.add(new BasicNameValuePair("area", area.toString()));
            params.add(new BasicNameValuePair("link", link.toString()));
            params.add(new BasicNameValuePair("latitude", lat.toString()));
            params.add(new BasicNameValuePair("longitude", lng.toString()));

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