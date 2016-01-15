package com.ice.toaqui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;

import com.ice.toaqui.pojo.Area;

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
import android.content.SharedPreferences;


/**
 * Created by Bolsa on 26/11/2015.
 */
public class PerfilData extends Activity {

    private String email;
    public static List<String> mapas = new ArrayList<String>();

    public static String mapaescolhido2;

    static boolean ableToContinue = false;

    static boolean ehArea = true;
    List<Area> areas;
    List<String> ultimosupdates;
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
    String datapadrao = "26-10-1989";
    static boolean msgLocais = true;
    String statusUpdate;
    public static final String VERSION_PREFS = "VersionsFile";
    public static final String MAPA_PREF = "MapaEscolhido";
    public static String mapaescolhido;
    private static String url = "http://ehlogoali.eduardobarrere.com/consulta/";
    private static String urlArea = "xml6.php";
    ProgressDialog mProgressDialog;



    public PerfilData() {

    }

    public PerfilData(String email){
        this.email = email;
    }

    public PerfilData(String email, List<String> mapas){
        this.email = email;
        this.mapas.addAll(mapas);
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String email){
        this.email=email;
    }

    public static List<String> getMapas(){
        return mapas;
    }

    public static void restartMapas(List<String> novosMapas){
        mapas.clear();
        mapas.addAll(novosMapas);
    }

    public void upload(){
        //loadAllPrefs();
        //loadMapaPref();
        getApplicationContext();
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

        mProgressDialog = new ProgressDialog(PerfilData.this);
        mProgressDialog.setMessage("Carregando...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
    }



    public void verificaAreas(boolean teste)
    {
        if(!teste){
            teste = verificaConexao();
        }
        if(teste) {
            final DownloadTask2 dt = new DownloadTask2(PerfilData.this);
            dt.execute(url + urlArea);
        }else Toast.makeText(this, "Você esta sem conexão no momento!", Toast.LENGTH_SHORT).show();
    }



    /*Jo�o-10/09-carregar no inicio da app a escolha de mapa do usu�rio*/
    public void loadMapaPref(){
        // Restore preferences
        SharedPreferences mapa = getSharedPreferences(MAPA_PREF, 0);

        String aux = mapa.getString("mapaescolhido", null);
        if(aux==null){

        }else{
            mapaescolhido=aux;
        }
    }
    /*Jo�o-10/09-carregar no inicio da app os mapas que tem registrado e qual a data de atualiza��o*/
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
    /*Jo�o-10/09-Salva a escolha de mapa do usu�rio, deve ser chamado no bot�o de salvar as configura��es*/
    public void saveMapaPref(String varPref, String valor){
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(MAPA_PREF, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(varPref, valor);

        // Commit the edits!
        editor.commit();
    }
    /*Jo�o-10/09-Salva mapas e datas de atualiza��o nas preferencias, caso seja um novo mapa � passado o nome e uma datapadr�o=26-10-1989*/
    //N�O USADO AINDA
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

    ///////////////////////c�digos inseridos a partir do dia 16/09


    /* Fun��o para verificar exist�ncia de conex�o com a internet*/
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


    //-------------------------------------------------//------------------------------------------------//

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
                Toast.makeText(context, "Erro de Download", Toast.LENGTH_LONG).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(PerfilData.this);
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

}
