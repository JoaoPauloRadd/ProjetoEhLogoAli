package com.ice.toaqui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.ice.toaqui.pojo.Area;
import com.ice.toaqui.pojo.Usuarios;

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


public class GooglePlayServicesActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    ///////////////////////////////para uso do xml usuarios
    static List<String> mapas;
    private static String url = "http://ehlogoali.eduardobarrere.com/consulta/";
    private static String urlArea = "xml4.php";
    String datapadrao = "26-10-1989";
    List<Area> areas;
    ProgressDialog mProgressDialog;
    static boolean msgLocais = true;
    String statusUpdate;
    public static final String LOGIN_PREFS = "LoginFile";
    public static final String MAPA_PREF = "MapaEscolhido";
    public static String mapaescolhido;
    static boolean ehArea = true;
    public static String mapaescolhido2;
    static boolean ableToContinue = false;
    static TextView textArea;
    List<Usuarios> users;
    static String mail;
    /////////////////////////////////////////////////////////////


    private static final String TAG = "MainActivity";

    /* RequestCode for resolutions involving sign-in */
    private static final int RC_SIGN_IN = 9001;

    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_SHOULD_RESOLVE = "should_resolve";

    /* Client for accessing Google APIs */
    private GoogleApiClient mGoogleApiClient;

    /* View to display current status (signed-in, signed-out, disconnected, etc) */
    private TextView mStatus;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;

    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        users = new ArrayList<Usuarios>();


        if (verificaConexao()){
            verificaLogins(true);
        }else{
            loadAllPrefs();
            if(users.isEmpty()){
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
            }else{
                Toast.makeText(this, "Verifique a conexão!\nErros podem ocorrer!", Toast.LENGTH_LONG).show();
            }
        }


        // Restore from saved instance state
        // [START restore_saved_instance_state]
        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            mShouldResolve = savedInstanceState.getBoolean(KEY_SHOULD_RESOLVE);
        }
        // [END restore_saved_instance_state]

        // Set up button click listeners
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);

        // Large sign-in
        ((SignInButton) findViewById(R.id.sign_in_button)).setSize(SignInButton.SIZE_WIDE);

        // Start with sign-in button disabled until sign-in either succeeds or fails
        findViewById(R.id.sign_in_button).setEnabled(false);

        // Set up vionconew instances
        mStatus = (TextView) findViewById(R.id.status);

        // [START create_google_api_client]
        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();
        // [END create_google_api_client]

    }

    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            // Show signed-in user's name
            if(mGoogleApiClient.isConnected()) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

                mail = Plus.AccountApi.getAccountName(mGoogleApiClient);
                if (currentPerson != null) {
                    String name = currentPerson.getDisplayName();
                    mStatus.setText(getString(R.string.signed_in_fmt, name));
                } else {
                    Log.w(TAG, getString(R.string.error_null_person));
                    mStatus.setText(getString(R.string.signed_in_err));
                }
            }
            // Set button visibility
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            // Show signed-out message
            mStatus.setText(R.string.signed_out);

            // Set button visibility
            findViewById(R.id.sign_in_button).setEnabled(true);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    // [START on_start_on_stop]
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
    // [END on_start_on_stop]

    // [START on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putBoolean(KEY_SHOULD_RESOLVE, mShouldResolve);
    }
    // [END on_save_instance_state]

    // [START on_activity_result]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further errors.
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            mGoogleApiClient.connect();
        }
    }
    // [END on_activity_result]

    @Override
    public void onConnected(Bundle bundle) {
        // onConnected indicates that an account was selected on the device, that the selected
        // account has granted any requested permissions to our app and that we were able to
        // establish a service connection to Google Play services.
        Log.d(TAG, "onConnected:" + bundle);

        // Show the signed-in UI
        updateUI(true);


        if(isUser()) {
            Toast.makeText(this, "Conectado", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else noPermissionMessage();
    }

    public void noPermissionMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(GooglePlayServicesActivity.this);
        builder.setMessage("Para uso do ToAqui, é necessário ser usuário com permissões " +
                "dentro do sistema. Se você acha que isso é um erro e foi informado " +
                "que pode usá-lo, entre em contato com os administradores. " +
                "Email utilizado: "+mail)
                .setTitle("Sem Permissões");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean isUser(){

        int antiLoop = 0;
        while(!ableToContinue){
            //antiLoop = antiLoop + 1;

        }
        if(ableToContinue){
            for(Usuarios user:users){
                if(mail.equals(user.getEmail()))return true;

            }
            return false;
        }

        return false;

    }


    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost. The GoogleApiClient will automatically
        // attempt to re-connect. Any UI elements that depend on connection to Google APIs should
        // be hidden or disabled until onConnected is called again.
        Log.w(TAG, "onConnectionSuspended:" + i);
    }

    // [START on_connection_failed]
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                showErrorDialog(connectionResult);
            }
        } else {
            // Show the signed-out UI
            updateUI(false);
        }
    }
    // [END on_connection_failed]

    private void showErrorDialog(ConnectionResult connectionResult) {
        int errorCode = connectionResult.getErrorCode();

        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            // Show the default Google Play services error dialog which may still start an intent
            // on our behalf if the user can resolve the issue.
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, RC_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mShouldResolve = false;
                            updateUI(false);
                        }
                    }).show();
        } else {
            // No default Google Play Services error, display a message to the user.
            String errorString = getString(R.string.play_services_error_fmt, errorCode);
            Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();

            mShouldResolve = false;
            updateUI(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                // User clicked the sign-in button, so begin the sign-in process and automatically
                // attempt to resolve any errors that occur.
                mStatus.setText(R.string.signing_in);
                // [START sign_in_clicked]
                mShouldResolve = true;
                mGoogleApiClient.connect();
                // [END sign_in_clicked]
                break;
            case R.id.sign_out_button:
                // Clear the default account so that GoogleApiClient will not automatically
                // connect in the future.
                // [START sign_out_clicked]
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
                // [END sign_out_clicked]
                updateUI(false);
                break;
            case R.id.disconnect_button:
                // Revoke all granted permissions and clear the default account.  The user will have
                // to pass the consent screen to sign in again.
                // [START disconnect_clicked]
                if (mGoogleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                }
                // [END disconnect_clicked]
                updateUI(false);
                break;
        }
    }



    ////////////////carregar usuarios permitidos

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

                output = new FileOutputStream(context.getFilesDir()+"/Users.xml");


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

                output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/Users.xml");

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
                Toast.makeText(context,"Usuarios baixados - DELETE ESSE TOAST", Toast.LENGTH_SHORT).show();

                //informa q est? atualizado

                //statusUpdate = "Atualizado";
                //Toast.makeText(context, statusUpdate, Toast.LENGTH_LONG).show();
                statusUpdate = "Checar Atualizações";
                //ultimosupdates.contains(formattedDate);

            }
            useDownload();
        }

        public void useDownload(){

            File f = new File(context.getFilesDir()+ "/Users.xml");


            InputSource is = new InputSource(f.toURI().toString());
            if(ehArea) {

                users = RssParserHelper.parseUsuarios(is);

                if (users.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GooglePlayServicesActivity.this);
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
                    for (Usuarios u:users) {
                        savePrefs(u, datapadrao);
                    }



                    Toast.makeText(getApplicationContext(), "Users Carregadas - DELETE ESSE TOAST", Toast.LENGTH_LONG).show();
                    //}
                }
            }
            statusUpdate = "Checar Atualizações";
            ableToContinue = true;
        }

    }



    //
    public void loadAllPrefs(){
        // Restore preferences
        SharedPreferences versoes = getSharedPreferences(LOGIN_PREFS, 0);
        Map<String, ?> infos= versoes.getAll();

        for (Map.Entry<String, ?> entry : infos.entrySet())
        {
            if(users.indexOf(entry.getKey())==-1){
                Usuarios aux = new Usuarios();
                aux.setemail(entry.getKey());
                users.add(aux);

            }
        }
        for(int i=0;i<users.size();i++){
            String aux = versoes.getString(users.get(i).getEmail(),datapadrao);
            savePrefs(users.get(i),aux);
        }
        //ableToContinue = true;
    }
    /*Jo?o-10/09-Salva a escolha de mapa do usu?rio, deve ser chamado no bot?o de salvar as configura??es*/

    /*Jo?o-10/09-Salva mapas e datas de atualiza??o nas preferencias, caso seja um novo mapa ? passado o nome e uma datapadr?o=26-10-1989*/
    //N?O USADO AINDA
    public void savePrefs(Usuarios u, String valor){
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(LOGIN_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(u.getEmail(), valor);

        // Commit the edits!
        editor.commit();




    }

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

    public void verificaLogins(boolean teste)
    {
        if(!teste){
            teste = verificaConexao();
        }
        if(teste) {
            final DownloadTask2 dt = new DownloadTask2(GooglePlayServicesActivity.this);
            dt.execute(url + urlArea);
        }else Toast.makeText(this, "Você esta sem conexão no momento!", Toast.LENGTH_SHORT).show();
    }
}
