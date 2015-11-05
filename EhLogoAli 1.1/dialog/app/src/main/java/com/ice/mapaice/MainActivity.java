package com.ice.mapaice;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;




import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.ActionBarOverlayLayout;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;

import java.util.Map;

import com.ice.mapaice.pojo.*;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.InputSource;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    //o mapa em si
    private static GoogleMap map;
    //zoom m�nimo do mapa
    private final int MIN_ZOOM = 18;

    private static int TYPE_MAP = GoogleMap.MAP_TYPE_NORMAL;
    private static View infoWindow;
    private Marker chennai;

    ProgressDialog mProgressDialog;

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////

    static boolean ableToContinue = false;
    static boolean firstClick = false;
    static boolean mapasWasEmpty = false;
    static boolean subItemMenu = false;
    // true->carregar área false->carregar locais
    static boolean ehArea = true;
    static boolean ehUpdate = false;
    static boolean msgLocais = true;
    static boolean estaCarregandoLocais = false;
    String statusUpdate;

    //Strings de uso geral para se saber o mapa selecionados nos dois spinners
    public static String mapaescolhido;
    public static String mapaescolhido2;
    public static String code = "0";

    //public static String areaescolhida;


    //Atributos no uso dos dados de prefer�ncias (loadAllPrefs, mapPrefs, etc)
    public static final String VERSION_PREFS = "VersionsFile";
    public static final String MAPA_PREF = "MapaEscolhido";
    public static final String CITY_PREF = "CidadeEscolhida";
    public static final String ALL_CITY_PREFS = "CidadesExistentes";

    /*Variaveis da classe -  duas estruturas entram na cria��o de menu de locais*/
    List<Local> locais;
    List<String> nomesLocais;
    /* e uma para �reas*/
    List<Area> areas;
    List<Cidade> cidades;
    public static ArrayList<String> mapasDaCidade;
    //Lista dos mapas vindos do servidor
    static List<String> mapas;
    static List<String> cities;
    //Dados dos �ltimos updates
    List<String> ultimosupdates;

    //string q armazena a url para fazer o update dos mapas
    String urlToUpdate;

    //URL REPRESENTA O CAMINHO DO SERVIDOR, � AQUI QUE O ip SER� ALTERADO QUANDO O SERVIDOR LOCAL ESTIVER FUNCIONANDO
    private static String url = "http://ehlogoali.eduardobarrere.com/consulta/";
    private static String localHost = "http://200.131.55.228/elogoali/api/";
    private static String urlArea = "xml.php";
    private static String urlLocal = "xml3.php?go=";
    //ESTA � A DATA PADR�O QUE � ATRIBUIDA A UM MAPA CASO ELE AINDA N�O TENHA SIDO CHECADO NO SERVIDOR POR ATUALIZA��ES
    String datapadrao = "26-10-1989";
    private static String linkMarker = "http://www.ufjf.br/portal/";
    private static int sizeLoop = 0;

    private static String cidadeAtual;

    ////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //MAPA//
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //lists strings
        mapas = new ArrayList<String>();
        cities = new ArrayList<String>();
        ultimosupdates = new ArrayList<String>();
        statusUpdate = "Checar Atualizações";
        locais = new ArrayList<Local>();
        nomesLocais = new ArrayList<String>();
        mapasDaCidade = new ArrayList<String>();

        //Carrega e salva preferencias
        loadAllPrefs();
        loadMapaPref();
        loadCityPref();
        loadAllCityPrefs();
        boolean conexao = verificaConexao();
        boolean atualiza = true;
        if(!(conexao) && mapas.isEmpty()){
            //gera��o do dialog para avisar a ausencia de conex�o
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
//			carregamento dos dados e atualiza��o e exibi��o do tutorial
            if(mapas.isEmpty()){
                showTutorial(0);
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

        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("Carregando...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(true);
    }


    //tutorial a ser exibido somente na primeira execução
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



                image.setImageResource(R.drawable.tut1);
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



                //image.setImageResource(R.drawable.tut2);
                //image2.setImageResource(R.drawable.up);
                title = "Tutorial - Atualizar Mapas";
                text.setText("Ao selecionar \"Atualizar Mapas\" eles serão utilizados. " +
                        "Assim poderá selecioná-los na opção \"Selecionar Mapas\".");

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
                text.setText("Em \"Selecionar Mapas\" você escolhe um mapa para visualizar. Os locais serão caregados.");
                text2.setText("Depois no marker amarelo:");
                text3.setText("Você escolhe um local que aparecerá no mapa do App.\n (Imagens Ilustrativas)");
                title = "Tutorial - Selecionar Mapas";

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

                text.setText("Ao clicar no marker do mapa, você terá mais infomações sobre o local. " +
                        "Clicando na janela quando tiver <<Site>>, você irá para o site do local.");
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
                //geração do dialog para o sobre
                view = factory.inflate(R.layout.image, null);
                image = (ImageView) view.findViewById(R.id.image);
                text= (TextView) view.findViewById(R.id.textView);



                //image.setImageResource(R.drawable.tut2);
                //image2.setImageResource(R.drawable.up);
                title = "Tutorial - Relatar Erro";
                text.setText("Ao selecionar \"Relatar Erro\" será aberta uma lista de apps válidos para envio de email. " +
                        "Clicando em um app de email, alguns campos já estarão preenchidos, faltando somente a mensagem" +
                        " para ser escrita relatando o erro.");

                builder.setView(view)
                        //.setMessage(R.string.string_array_help_newmapa)
                        .setIcon(R.drawable.tut)
                        .setTitle(title);

                builder.setPositiveButton(proximo, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        showTutorial(7);
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
                        showTutorial(5);
                    }
                });

                break;
            case 7:
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
                        showTutorial(6);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //MenuItem menu3 = menu.findItem(R.id.menu3);
        //menu3.setTitleCondensed();


        MenuItem menu2 = menu.findItem(R.id.menu2);
        if(menu2 != null) {
            SubMenu subMenu2 = menu2.getSubMenu();

            File f;

            //pega arquivo dentro das pastas do app
            f = getFileINTERNAL();


            InputSource is = new InputSource(f.toURI().toString());
            //retira os valores dos arrays pra n�o repetir valores no menu j� adicionados antes
            locais = null;
            nomesLocais = null;

            if (subMenu2.size() > 1) {
                //restarta a cria��o do menu caso j� tivesse valores no submenu (evita repeti��o dos valores
                invalidateOptionsMenu();
            }
            locais = RssParserHelper.parseLocal(is);
            nomesLocais = new ArrayList<String>();
            //preenche a lista dos locais
            for (Local l : locais) {
                subMenu2.add(l.getNome());
                nomesLocais.add(l.getNome());
            }
            boolean internet = verificaConexao();
            if(!(locais.isEmpty()) && msgLocais)
            {
                Toast.makeText(this,"Locais carregados", Toast.LENGTH_SHORT).show();
                msgLocais = false;
                menu2.setEnabled(true);
                estaCarregandoLocais=false;
            }else if(locais.isEmpty()) menu2.setEnabled(false);
            if(estaCarregandoLocais) {
                if (sizeLoop < 10 && internet) {
                    sizeLoop = sizeLoop + 1;
                    onPrepareOptionsMenu(menu);
                } else {
                    Toast.makeText(this, "Verifique sua conexão\nNão foi possível carregar os locais", Toast.LENGTH_SHORT).show();
                    sizeLoop = 0;
                }
            }
        }
        return true;
    }

    //fun��o que pega arquivo dentro das pastas do app
    public File getFileINTERNAL()
    {

        this.setTitle(mapaescolhido);
        File f = new File(this.getFilesDir(), mapaescolhido+".xml");
        //Environment.getDataDirectory();
        return f;
    }
    //fun��o que pega arquivo dentro do cart�o (n�o est� sendo musada - 03/11/14
    public File getFileEXTERNAL()
    {

        File f = new File(Environment.getExternalStorageDirectory().getPath()+"/"+mapaescolhido2+".xml");
        return f;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Toast.makeText(this,item.getTitle(), Toast.LENGTH_SHORT).show();
        switch (id) {
            case R.id.menu1:
                //gera��o do dialog para o sobre
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("EHLOGOALI v1.000\n\nIdealizado por Eduardo Barrére e desenvolvido por João Paulo Radd, " +
                        "Claudio Augusto Lelis e Guilherme Barbosa. A aplicação tem por objetivo facilitar " +
                        "a localização de diversos locais correlacionados, a partir de um mapeamento realizado previamente por " +
                        "colaboradores.\n" +
                        "Pare enviar-nos um email, clique em contato.")
                        .setTitle("Sobre")
                        .setIcon(R.drawable.sobre);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button

                    }
                });
                builder.setNeutralButton("Contato", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        mail("Contato");
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.menu2:
                subItemMenu = true;
                if(locais.isEmpty()) {
                    Toast.makeText(this, "Espere ou escolha um mapa primeiro", Toast.LENGTH_SHORT).show();
                    msgLocais = true;
                    invalidateOptionsMenu();
                }


                return true;

            case R.id.menu3:
                if(TYPE_MAP == GoogleMap.MAP_TYPE_NORMAL){
                    TYPE_MAP = GoogleMap.MAP_TYPE_SATELLITE;
                }else{
                    TYPE_MAP = GoogleMap.MAP_TYPE_NORMAL;
                }
                map.setMapType(TYPE_MAP);

                return true;

            case R.id.submenu1:
                return true;

            case R.id.action_settings:
                escolhaMapa();

                return true;

            //case R.id.action_settings2:
            //    downloadMapa();
            //    return true;

            case R.id.action_settings3:
                verificaAreas(false);
                return true;

            case R.id.action_settings4:
                descobrirCidade();
                return true;

            case R.id.menu5:
                showTutorial(0);
                return true;

            case R.id.menu6:
                mail("Feedback de erro no EhLogoAli");
                return true;

            default:
                //situa��o onde uma das op��es de locais � selecionada
                if(subItemMenu){
                    int i = nomesLocais.indexOf(item.getTitle());
                    if(i>=0) {
                        Local local = locais.get(i);
                        plotaNoMap(local);
                    }
                    subItemMenu = false;
                }
                else{
                    String nomeMapa = (String) item.getTitle();
                    selfDestruct2(nomeMapa);
                    //selfDestruct2
                }


                return super.onOptionsItemSelected(item);
        }
    }

    private void mail(String s) {
        //Intent intent = new Intent(this, MailActivity.class);
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        String aEmailList[] = { "eduardo.barrere@ice.ufjf.br"};
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
        if(s.equals("Contato")){
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "App EhLogoAli: ");
        }else emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, s);

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "<Escreva aqui sua mensagem>");
        try {
            startActivity(Intent.createChooser(emailIntent, "Envie seu email pelo:"));
        }catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(this,"Não existe nenhum app para enviar emails\n" +
                    " no seu dispositivo.", Toast.LENGTH_LONG).show();
        }
    }

    public void escolhaMapa()
    {
        if(cidadeAtual==null) {
            descobrirCidade();
        }else {
            escolhaArea();
        }
    }
    public void escolhaArea(){

        if(mapasDaCidade.isEmpty()) {
            for (Area area : areas) {
                if (area.getCidade().equals(cidadeAtual))
                    mapasDaCidade.add(area.getNome());
            }
        }
        if(!mapasDaCidade.isEmpty()) {
            CharSequence[] opt = mapasDaCidade.toArray(new CharSequence[mapas.size()]);


            AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
            builder2
                    .setTitle("Mapas")
                    .setIcon(R.drawable.map)
                    .setItems(opt, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            //which eh o elemento selecionado
                            String s = mapasDaCidade.get(which).toString();
                            if (verificaConexao()) {
                                selfUpdate2(s, which);
                            } else
                                Toast.makeText(getApplicationContext(), "Você esta sem conexão no momento!\n Ative a internet para baixar / atualizar locais", Toast.LENGTH_SHORT).show();


                            msgLocais = true;
                            selfDestruct2(s);
                        }
                    });

            AlertDialog dialog2 = builder2.create();
            dialog2.show();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Essa cidade selecionada, não contém mapas. Clique em Atualizar mapas para mais opções ou selecione" +
                    " outra cidade")
                    .setTitle("Sem Mapas")
                    .setIcon(R.drawable.erro);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void descobrirCidade(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Cidade de interesse");
        alert.setMessage("Escolha sua cidade:");

        CharSequence[] opt = cities.toArray(new CharSequence[cities.size()]);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2
                .setTitle("Escolha sua cidade padrão")
                .setIcon(R.drawable.up)
                .setItems(opt, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        //which eh o elemento selecionado
                        cidadeAtual = cities.get(which).toString();
                        saveCityPref("CidadeEscolhida", cidadeAtual);
                        mapasDaCidade.clear();
                        for (Area area : areas) {
                            if (area.getCidade().equals(cidadeAtual))
                                mapasDaCidade.add(area.getNome());
                        }
                        escolhaArea();
                    }
                });

        AlertDialog dialog2 = builder2.create();
        dialog2.show();
    }

    public void downloadMapa(){
        CharSequence[] opt = mapas.toArray(new CharSequence[mapas.size()]);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2
                .setTitle("Atualizar Mapa")
                .setIcon(R.drawable.up)
                .setItems(opt, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        //which eh o elemento selecionado
                        String s = mapas.get(which).toString();
                        selfUpdate2(s,which);
                    }
                });

        AlertDialog dialog2 = builder2.create();
        dialog2.show();
    }


    public void helpDialog(){
        //gera��o do dialog para o help
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2
                .setTitle("Ajuda")
                .setIcon(R.drawable.help)
                .setItems(R.array.string_array_help, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialogResponse(which);
                    }
                });

        AlertDialog dialog2 = builder2.create();
        dialog2.show();
    }


    //para as respostas do help
    public void dialogResponse(int which){

        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.dialog1, null);

        ImageView image = (ImageView) view.findViewById(R.id.imageView);
        ImageView image2 = (ImageView) view.findViewById(R.id.imageView2);

        TextView text= (TextView) view.findViewById(R.id.textView);
        TextView text2= (TextView) view.findViewById(R.id.textView2);
        TextView text3= (TextView) view.findViewById(R.id.textView3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(which){
            case 0:
                //gera��o do dialog para o sobre

                image.setImageResource(R.drawable.opt);
                image2.setImageResource(R.drawable.up);

                text.setText("Na aba: ");
                text2.setText("Clique na lista com o ícone: ");
                text3.setText("Selecione um mapa. Logo em seguida iniciará um Download.");

                builder.setView(view)
                        //.setMessage(R.string.string_array_help_newmapa)
                        .setIcon(R.drawable.help)
                        .setTitle("Como obter mapas?");

                break;

            case 1:
                //gera��o do dialog para o sobre
                image.setImageResource(R.drawable.opt);
                image2.setImageResource(R.drawable.map);

                text.setText("Na aba: ");
                text2.setText("Clique na lista com o ícone: ");
                text3.setText("Selecione um mapa. Logo em seguida pode escolher o local.");

                builder.setView(view)
                        .setIcon(R.drawable.help)
                        .setTitle("Como escolho um mapa?");

                break;
            case 2:
                //gera��o do dialog para o sobre
                image.setImageResource(R.drawable.orangedot);
                //image2.setImageResource();

                text.setText("Na parte superior da aplicação, clique no ícone: ");
                text2.setText("Será carregado uma lista com os locais do mapa previamente escolhido");
                text3.setText("");

                builder.setView(view)
                        .setIcon(R.drawable.help)
                        .setTitle("Como vejo os locais?");

                break;
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

            }
        });
        builder.setNeutralButton("Voltar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                helpDialog();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(TYPE_MAP);
        //plotagem da UFJF - aqui ele posiciona a visualiza��o  no ponto
        LatLng latLong = new LatLng(-21.773459, -43.369259);
        CameraPosition position = new CameraPosition.Builder()
                .target(latLong)
                .bearing(0)
                .tilt(45)
                .zoom(MIN_ZOOM)
                .build();

        //plota o marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLong);
        markerOptions.title("UFJF");

        //markerOptions.snippet();

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.boneco));

        infoWindow=getLayoutInflater().inflate(R.layout.marker_adapter, null);


        chennai = map.addMarker(markerOptions);


        map.setInfoWindowAdapter(new CustomInfoAdapter());
        map.setOnInfoWindowClickListener(null);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                if(linkMarker != null) {
                    final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(linkMarker));
                    startActivity(intent);
                }

            }
        });

            /*
            *
            *
            * */
        //atualiza as configura��es no mapa
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.moveCamera(update);


    }
    ///////////////////
    //Plotagem do local no mapa

    public void plotaNoMap(Local local) {
        // Kabloey
        //plota, carrega dados do maker e focaliza onde � desejado

        map.clear();
        linkMarker = local.getLink();

        //plota, carrega dados do maker e focaliza onde � desejado
        LatLng latLong1 = new LatLng(Double.parseDouble(local.getLatitude()), Double.parseDouble(local.getLongitude()));
        MarkerOptions markerOptions1 = new MarkerOptions();
        markerOptions1.position(latLong1);
        markerOptions1.title(local.getNome());
        //markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        markerOptions1.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        //map.addMarker(markerOptions1);

        infoWindow=getLayoutInflater().inflate(R.layout.marker_adapter, null);


        chennai = map.addMarker(markerOptions1);

        map.setInfoWindowAdapter(new CustomInfoAdapter());
        map.setOnInfoWindowClickListener(null);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                if(linkMarker != null && linkMarker!="") {
                    final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(linkMarker));
                    startActivity(intent);
                }

            }
        });

        CameraUpdate update1 = CameraUpdateFactory.newLatLngZoom(latLong1, 18);
        map.moveCamera(update1);
    }

    public void selfDestruct2(String selecionado)
    {

        //var pref -> o nome mapaesc
        //valor ->nome do mapa referenciado ao mapa escolhido

        if(selecionado==null){
            //esse if foi contruido para evitar q o usu�rio escolha um mapa em quanto ta carregando (ou seja, n�o tem op��es)
            Toast.makeText(this, "Nenhuma opção selecionada!", Toast.LENGTH_SHORT).show();
        }
        else{
            //pega o nome do mapa escolhido, avisa o escolhido, salva a prefer�ncia, e recria o menu
            mapaescolhido = selecionado;
            // this.setTitle(selecionado);
            Toast.makeText(this, mapaescolhido + " Selecionado", Toast.LENGTH_SHORT).show();
            saveMapaPref("mapaescolhido", mapaescolhido);
            estaCarregandoLocais = true;
            invalidateOptionsMenu();
        }

    }

    public void selfUpdate2(String selecionado, int posicao)
    {

        //se foi clicado como checar... ele vai checar atualiza��es
        if(statusUpdate == "Checar Atualizações"){
            statusUpdate = "Checando...";
            //Toast.makeText(get, statusUpdate, Toast.LENGTH_SHORT).show();
            String nome = null;
            String ultimoupdate = null;
            if(!mapas.isEmpty()){/*CASO O mapas N�O ESTEJA VAZIO, PRECISAMOS PEGAR A ESCOLHA
			DO USU�RIO DO SPINNER E A DATA DO ULTIMOUPDATE PARA CONFERIR NO SERVIDOR A NECESSIDADE
			OU N�O DE ATUALIZAR O MAPA;*/

                if(selecionado==null){
                    //caso as op��es n�o tenham sido carregadas e n�o h� nada selecionado para atualizar
                    Toast.makeText(this, "Nenhuma opção selecionada!\nEspere a aplicação carregar.", Toast.LENGTH_SHORT).show();
                }
                else{
                    //registra e guarda o q est� sendo atualizado
                    nome = selecionado;
                    mapaescolhido2 = selecionado;
                    Log.v("nome do mapa", selecionado);
                    int aux = posicao;
                    ultimoupdate = ultimosupdates.get(aux);
                }
            }
			/*SE O mapas FOR VAZIO, � PORQUE O USUARIO EST� ABRINDO A APLICA��O PELA
			*PRIMEIRA VEZ E AINDA N�O FEZ NENHUMA REQUISI��O AO SERVIDOR;
			ASSIM ELE FAZ A REQUISI��O COM AS VARI�VEIS COM VALOR NULL POIS RETORNAR� A LISTA DE AREAS;
			*/
            code = "1";
            for(Area area:areas){
                ultimosupdates.add(datapadrao);
                savePrefs(area.getNome(), datapadrao);
                if(area.getNome().equals(mapaescolhido2))
                    code = ""+ area.getIdarea();
            }
            //carregamento dos dados e atualiza��o
            ehArea = true;
            //SendHttpRequestTask t = new SendHttpRequestTask();//ENVIA AO SERVIDOR UMA REQUISI��O ASYNCTASK, OU SEJA, EM PARALELO PARA N�O TRAVAR A APLICA��O;
            //String[] params = new String[]{url+urlArea/*+codigo da �rea*/, nome, ultimoupdate};
            //t.execute(params);
            //final DownloadTask2 dt = new DownloadTask2(MainActivity.this);
            //dt.execute(url+urlArea);
            completeUpdate();
        }


    }

    public void completeUpdate()
    {
        //caso seja clicado o bot�o de atualizar

        if(verificaConexao()) {
            final DownloadTask dt = new DownloadTask(MainActivity.this);
            dt.execute(url+urlLocal+code);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dt.cancel(true);
                }
            });
        }else {
            Toast.makeText(this, "Você esta sem conexão no momento!\n Ative a internet para baixar / atualizar locais", Toast.LENGTH_SHORT).show();
            statusUpdate = "Checar Atualizações";
        }
    }
    ///////Metodos relacionados as preferencias

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


    /*Jo�o-10/09-carregar no inicio da app a escolha de mapa do usu�rio*/
    public void loadCityPref(){
        // Restore preferences
        SharedPreferences cidade = getSharedPreferences(CITY_PREF, 0);

        String aux = cidade.getString("CidadeEscolhida", null);
        if(aux==null){

        }else{
            cidadeAtual=aux;
        }
    }
    /*Jo�o-10/09-carregar no inicio da app os mapas que tem registrado e qual a data de atualiza��o*/
    //
    public void loadAllCityPrefs(){
        // Restore preferences
        SharedPreferences versoes = getSharedPreferences(ALL_CITY_PREFS, 0);
        Map<String, ?> infos= versoes.getAll();
        for (Map.Entry<String, ?> entry : infos.entrySet())
        {
            if(cities.indexOf(entry.getKey())==-1){
                cities.add(entry.getKey());
                ultimosupdates.add((String)entry.getValue());
            }
        }
        for(int i=0;i<cities.size();i++){
            String aux = versoes.getString(cities.get(i), ultimosupdates.get(i));
            saveCityPrefs(cities.get(i), aux);

        }
    }
    /*Jo�o-10/09-Salva a escolha de mapa do usu�rio, deve ser chamado no bot�o de salvar as configura��es*/
    public void saveCityPref(String varPref, String valor){
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
    public void saveCityPrefs(String name, String valor){
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(ALL_CITY_PREFS, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, valor);

        // Commit the edits!
        editor.commit();
        int i = cities.indexOf(name);
    }




    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }


    public void actionButton(View view){
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.ufjf.br/portal"));
        this.startActivity(intent);
    }

    public void verificaAreas(boolean teste)
    {
        if(!teste){
            teste = verificaConexao();
        }
        if(teste) {
            final DownloadTask2 dt = new DownloadTask2(MainActivity.this);
            dt.execute(url + urlArea);
        }else Toast.makeText(this, "Você esta sem conexão no momento!", Toast.LENGTH_SHORT).show();
    }


    //-----------------------------------------------------------------------------------------------------------
    //-------------------------------------------------------INNER CLASSES---------------------------------------
    //-----------------------------------------------------------------------------------------------------------

    private class SendHttpRequestTask extends AsyncTask<String, Void, String> {

        //realiza a consulta com o BD
        @Override//PARA FAZER UMA A��O EM SEGUNDO PLANO (PARALELO)
        protected String doInBackground(String... params) {


            String nome = params[1];
            String ultimoupdate = params[2];
            StringBuffer buffer = new StringBuffer();
            try {
                System.out.println("URL ["+url+"] - Name ["+nome+"]");


                // Apache HTTP Request
                HttpClient client = new DefaultHttpClient();
                String urlToGet = params[0];

                HttpGet post = new HttpGet(urlToGet);
                List<NameValuePair> nvList = new ArrayList<NameValuePair>();
                BasicNameValuePair bnvp = new BasicNameValuePair("nome", nome);
                BasicNameValuePair bnvp2 = new BasicNameValuePair("ultimoupdate", ultimoupdate);

                // We can add more
                nvList.add(bnvp);
                nvList.add(bnvp2);

                //post.setEntity(new UrlEncodedFormEntity(nvList));

                HttpResponse resp = client.execute(post);
                // We read the response
                InputStream is  = resp.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder str = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null){
                    str.append(line + "\n");
                }
                is.close();
                buffer.append(str.toString());
                // Done!
            }
            catch(Throwable t) {
                t.printStackTrace();
            }

            return buffer.toString();
        }



        @Override//O QUE FAZER COM O RESULTADO DA EXECU��O EM SEGUNDO PLANO;
        //ISTO � DECIDIDO NESTE METODO ONPOSTEXECUTE;
        protected void onPostExecute(String result) {
            String[] results = result.split(":");
            switch (results[0]) {
                case "-1"://caso de erro
                    statusUpdate = "Checar Atualizações";
                    Toast.makeText(getApplicationContext(), "ERRO "+results[1], Toast.LENGTH_LONG).show();
                    break;
                case "0":	//caso j� esteja atualizado
                    statusUpdate = "Atualizado";
                    Toast.makeText(getApplicationContext(), statusUpdate, Toast.LENGTH_LONG).show();
                    break;
                case "1": // caso haja atualiza��o
                    statusUpdate = "Atualizar";
                    urlToUpdate= localHost + urlLocal + results[1];
                    completeUpdate();
                    break;
                default: // realiza a c�pia dos dados para o usu�rio possa manipular
//Jo�o-10/09-aqui vai ser usado futuro na resposta do servidor;inserir como teste a captura do arquivo de areas xml
                    Log.e("parse", result);
                    //mudar esta linha para pegar o arquivo xml e torn�-lo um inputsource
                    InputSource inputSource = new InputSource( new StringReader( result ) );
                    if(ehArea) {
                        areas = RssParserHelper.parseArea(inputSource);

                        //caso mapas esteja vazio ele é preenchido já q será utilizado para fornecer os dados
                        if (mapas.isEmpty()) {
                            for (Area area : areas) {
                                mapas.add(area.getNome());
                                ultimosupdates.add(datapadrao);
                                savePrefs(area.getNome(), datapadrao);
                            }
                        } else {//se n�o atualiza o q interessa
                            for (Area area : areas) {
                                int index = mapas.indexOf(area.getNome());
                                if (index == -1) {
                                    mapas.add(area.getNome());
                                    ultimosupdates.add(datapadrao);
                                    savePrefs(area.getNome(), datapadrao);
                                }
                                String codigo = (String)area.getIdarea().toString();
                                ehArea=false;
                                SendHttpRequestTask t = new SendHttpRequestTask();//ENVIA AO SERVIDOR UMA REQUISI��O ASYNCTASK, OU SEJA, EM PARALELO PARA N�O TRAVAR A APLICA��O;
                                String[] params = new String[]{url+urlLocal+codigo/*+codigo da �rea*/, null, null};
                                t.execute(params);

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
                        // se areas e mapas estiverem vazios significa q � primeira vez q a aplica��o � carregada
                        // e n�o h� comunica��o com o servidor
                        // logo deve se informar ao usu�rio q a aplica��o vai ter q ser executada mais tarde
                        if (areas.isEmpty() && mapas.isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

                            ///Faz o mesmo na configura��o em lista
                            if (mapasWasEmpty || firstClick) {
                                Toast.makeText(getApplicationContext(), "Opções de Mapas Carregadas", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    statusUpdate = "Checar Atualizações";

                    break;
            }
        }
    }
    //inner class que efetua o download e instala os dados na aplica��o
    public class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        //salva os dados dentro da aplica��o
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

                output = new FileOutputStream(context.getFilesDir()+"/"+mapaescolhido2+".xml");


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
        //metodo q grava no cart�o (n�o est� sendo usado
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
        //prepara��o para executa o salvamento dos arquivos na mem�ria do aparelho
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }
        //aqui mostra um progresso do download
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
            if (progress[0] == 100 || !verificaConexao() || !estaCarregandoLocais) {
                mProgressDialog.dismiss();
            }
        }

        //execu��o posterior ao download
        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            if(mProgressDialog.isShowing()) mProgressDialog.dismiss();
//	      mProgressDialog.dismiss();
            if (result != null){ //aviso caso tenha ocorrido um erro

                if(verificaConexao()){
                    Toast.makeText(context, "Sem conexão para o download", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(context, "Erro de download", Toast.LENGTH_LONG).show();
                    statusUpdate = "Checar Atualizações";
                }
            }
            else{//aviso q ocorreu tudo certo

                Toast.makeText(context,"Arquivo Baixado", Toast.LENGTH_SHORT).show();
                Calendar today = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");//gera a data da autualiza��o
                String formattedDate = df.format(today.getTime());
                savePrefs(mapas.get(0), formattedDate);
                //informa q est� atualizado

                statusUpdate = "Atualizado";
                //Toast.makeText(context, statusUpdate, Toast.LENGTH_LONG).show();
                statusUpdate = "Checar Atualizações";

            }
        }

    }
    ///////////////////////////////////////////////////////////////////////////////////////////
    //inner class que efetua o download e instala os dados na aplica��o
    public class DownloadTask2 extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask2(Context context) {
            this.context = context;
        }

        //salva os dados dentro da aplica��o
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
        //metodo q grava no cart�o (n�o est� sendo usado
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
        //prepara��o para executa o salvamento dos arquivos na mem�ria do aparelho
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

        //execu��o posterior ao download
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

                //informa q est� atualizado

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

                ////TREXO ADAPTADO PARA TESTE

                //cidades = new ArrayList<Cidade>();


                for (Area a:areas){
                    if(!cities.contains(a.getCidade())){
                        cities.add(a.getCidade());
                        saveCityPrefs(a.getCidade(), datapadrao);
                    }
                }



/*
                Cidade cidade = new Cidade();
                cidade.setAreas(areas);
                cidade.setIdCidade(0);
                cidade.setResponsavel("Barrere");
                cidade.setNome("Juiz de Fora/MG");
                cidades.add(cidade);
                cidade = new Cidade();
                Area a = new Area();
                List<Area> ar = new ArrayList<Area>();
                cidade.setAreas(ar);
                cidade.setIdCidade(1);
                cidade.setResponsavel("Barrere");
                cidade.setNome("Belo Horizonte/MG");
                cidades.add(cidade);

                for(Cidade cid: cidades){
                    if(!cities.contains(cid.getNome())){
                        cities.add(cid.getNome());
                        saveCityPrefs(cid.getNome(), datapadrao);
                    }
                }*/


                ////FIM TREXO ADAPTADO PARA TESTE

                //caso mapas esteja vazio ele é preenchido já q será utilizado para fornecer os dados
                if (mapas.isEmpty()) {
                    for (Area area : areas) {
                        mapas.add(area.getNome());
                        ultimosupdates.add(datapadrao);
                        savePrefs(area.getNome(), datapadrao);
                    }
                } else {//se n�o atualiza o q interessa
                    for (Area area : areas) {
                        int index = mapas.indexOf(area.getNome());
                        if (index == -1) {
                            mapas.add(area.getNome());
                            ultimosupdates.add(datapadrao);
                            savePrefs(area.getNome(), datapadrao);
                        }
                        String codigo = (String)area.getIdarea().toString();
                        //ehArea=false;
                        //SendHttpRequestTask t = new SendHttpRequestTask();//ENVIA AO SERVIDOR UMA REQUISI��O ASYNCTASK, OU SEJA, EM PARALELO PARA N�O TRAVAR A APLICA��O;
                        //String[] params = new String[]{url+urlLocal+codigo/*+codigo da �rea*/, null, null};
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
                // se areas e mapas estiverem vazios significa q � primeira vez q a aplica��o � carregada
                // e n�o h� comunica��o com o servidor
                // logo deve se informar ao usu�rio q a aplica��o vai ter q ser executada mais tarde
                if (areas.isEmpty() && mapas.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

                    ///Faz o mesmo na configura��o em lista
                    //if (mapasWasEmpty || firstClick) {
                    Toast.makeText(getApplicationContext(), "Opções de Mapas Carregadas", Toast.LENGTH_LONG).show();
                    //}
                }
            }
            statusUpdate = "Checar Atualizações";


        }


    }

    class CustomInfoAdapter implements GoogleMap.InfoWindowAdapter {


        @Override
        public View getInfoContents(Marker arg0) {
            displayView(arg0);

            return infoWindow;
        }

        @Override
        public View getInfoWindow(Marker arg0) {

            return null;
        }




    }
    //static ImageView image;

    public void displayView(Marker arg0) {

        //((ImageView) infoWindow.findViewById(R.id.lblImageHeader)).setImageResource(R.drawable.logo_ufjf);
        ((TextView)infoWindow.findViewById(R.id.lblListHeader)).setText(arg0.getTitle());
        if(linkMarker != null && linkMarker !="") {
            ((TextView) infoWindow.findViewById(R.id.lblButtonHeader)).setText(R.string.maisInfo);
        }else ((TextView) infoWindow.findViewById(R.id.lblButtonHeader)).setText("");


    }



}
