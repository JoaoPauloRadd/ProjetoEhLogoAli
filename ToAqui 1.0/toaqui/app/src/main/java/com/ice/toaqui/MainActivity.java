package com.ice.toaqui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;




import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.widget.ActionBarOverlayLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

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
    private static MarkerOptions markerOptions;

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

    /*Variaveis da classe -  duas estruturas entram na cria��o de menu de locais*/
    //List<Local> locais;
    List<String> nomesLocais;
    /* e uma para �reas*/
    //List<Area> areas;

    //Lista dos mapas vindos do servidor
    static List<String> mapas;
    //Dados dos �ltimos updates
    List<String> ultimosupdates;

    //string q armazena a url para fazer o update dos mapas
    String urlToUpdate;

    //URL REPRESENTA O CAMINHO DO SERVIDOR, � AQUI QUE O ip SER� ALTERADO QUANDO O SERVIDOR LOCAL ESTIVER FUNCIONANDO
    private static String url = "http://200.131.55.228:8080/elogoali/api/";
    private static String localHost = "http://200.131.55.228/elogoali/api/";
    private static String urlArea = "area/buscarTodos/";
    private static String urlLocal = "local/buscarTodos/";
    //ESTA � A DATA PADR�O QUE � ATRIBUIDA A UM MAPA CASO ELE AINDA N�O TENHA SIDO CHECADO NO SERVIDOR POR ATUALIZA��ES
    String datapadrao = "26-10-1989";
    private static String linkMarker = "http://www.ufjf.br/portal/";
    private static int sizeLoop = 0;
    private static LatLng latLng;
    private static LatLng lLDiferenca = new LatLng(0,0);
    private static double latDif =0;
    private static double lngDif =0;
    private static boolean first = true;



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
        ultimosupdates = new ArrayList<String>();
        statusUpdate = "Checar Atualizações";
        //locais = new ArrayList<Local>();
        nomesLocais = new ArrayList<String>();

        //Carrega e salva preferencias
        //loadAllPrefs();
        //loadMapaPref();
        boolean conexao = verificaConexao();
        boolean atualiza = true;
        if(!(conexao)){
            //gera��o do dialog para avisar a ausencia de conex�o
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Para o usu da aplicação, o uso de internet é necessário.\n Conecte a internet e reabra a aplicação")
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
            /**
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
             */
        }


        //aqui ele vai baixar as áreas caso seja o primeiro acesso
        /*
        if(atualiza)
        {
            verificaAreas(false);

            ehArea = true;
        }
        msgLocais = true;
*/

    }


    //tutorial
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



                image.setImageResource(R.drawable.gps);
                //image2.setImageResource(R.drawable.up);
                title = "Tutorial - Usando GPS";
                text.setText("Deixe seu GPS Ativo. Clicando no ícone abaixo, " +
                        "ele mostrará sua localização e em instantes, o marcador aparecerá sobre ele.");

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
                image2.setImageResource(R.drawable.tela);

//				text.setText("");
                text.setText("Precionando o marcador por um instante poderá reposicioná-lo para a posição desejada.");
                text2.setText("Uma aba sobre ele será aberta. Ao clicá-lo, você poderá registar esse lugar. Veja na imagem:");
                text3.setText("Preencha todos os dados e no fim clique na carta.\nSe não tiver link, não insira nada.");
                title = "Tutorial - Usando o Marcador";

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
                        showTutorial(4);
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
/*

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

            if(!(locais.isEmpty()) && msgLocais)
            {
                Toast.makeText(this,"Locais carregados", Toast.LENGTH_SHORT).show();
                msgLocais = false;
                menu2.setEnabled(true);
                estaCarregandoLocais=false;
            }else if(locais.isEmpty()) menu2.setEnabled(false);
            if(estaCarregandoLocais) {
                if (sizeLoop > 10 && verificaConexao()) {
                    sizeLoop = sizeLoop + 1;
                    onPrepareOptionsMenu(menu);
                } else {
                    Toast.makeText(this, "Verifique sua conexão\nNão foi possível carregar os locais", Toast.LENGTH_SHORT).show();
                    sizeLoop = 0;
                }
            }
        }*/
        super.onPrepareOptionsMenu(menu);
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
                builder.setMessage("Idealizado por Eduardo Barrére e desenvolvido por João Paulo Radd, " +
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


            case R.id.menu3:
                if(TYPE_MAP == GoogleMap.MAP_TYPE_NORMAL){
                    TYPE_MAP = GoogleMap.MAP_TYPE_SATELLITE;
                }else{
                    TYPE_MAP = GoogleMap.MAP_TYPE_NORMAL;
                }
                map.setMapType(TYPE_MAP);

                return true;





            //case R.id.action_settings2:
            //    downloadMapa();
            //    return true;



            case R.id.menu5:
                showTutorial(0);
                return true;

            case R.id.menu6:
                mail("Feedback de erro no EhLogoAli");
                return true;

            default:



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



    public static LatLng getLatLng(){
        return latLng;
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(TYPE_MAP);
        //plotagem da UFJF - aqui ele posiciona a visualiza��o  no ponto
        latLng = new LatLng(-21.773459, -43.369259);
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng)
                .bearing(0)
                .tilt(45)
                .zoom(MIN_ZOOM)
                .build();

        //plota o marker

        markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("UFJF");


        //markerOptions.snippet();

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.boneco));

        infoWindow=getLayoutInflater().inflate(R.layout.marker_adapter, null);

        markerOptions.draggable(true);
        chennai = map.addMarker(markerOptions);


        map.setInfoWindowAdapter(new CustomInfoAdapter());
        map.setOnInfoWindowClickListener(null);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                final Intent intent = new Intent(MainActivity.this, SendLocalActivity.class);
                startActivity(intent);


            }
        });


        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.setPosition(latLng);
                marker.showInfoWindow();
                first=true;

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                if(first) {
                    lLDiferenca = marker.getPosition();

                        latDif = lLDiferenca.latitude - (latLng.latitude);
                        if(latDif<0) latDif=(-1)*latDif;


                        lngDif = lLDiferenca.longitude - (latLng.longitude);
                        if(lngDif<0) lngDif=(-1)*lngDif;

                    first=false;
                }
                latLng = marker.getPosition();
                lLDiferenca = new LatLng(latLng.latitude-latDif,latLng.longitude-lngDif);
                marker.setPosition(lLDiferenca);
                latLng = marker.getPosition();

                marker.showInfoWindow();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                marker.setPosition(latLng);
                marker.showInfoWindow();
            }

        });


            /*
            *
            *
            * */
        //atualiza as configura��es no mapa
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.moveCamera(update);

        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){

            @Override
            public boolean onMyLocationButtonClick() {
                final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
                if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                    buildAlertMessageNoGps();
                }else {
                    firstClick = true;
                    Toast.makeText(getApplicationContext(), "Realocando o Marcador...", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if(firstClick) {
                    firstClick=false;
                    plotaNoMap(location);
                }
            }
        });


    }
    ///////////////////
    //Plotagem do local no mapa

     public void plotaNoMap(Location location) {
     // Kabloey
     //plota, carrega dados do maker e focaliza onde � desejado

        map.clear();

         map.setMapType(TYPE_MAP);
         //plotagem da UFJF - aqui ele posiciona a visualiza��o  no ponto
         latLng = new LatLng(location.getLatitude(), location.getLongitude());
         CameraPosition position = new CameraPosition.Builder()
                 .target(latLng)
                 .bearing(0)
                 .tilt(45)
                 .zoom(MIN_ZOOM)
                 .build();

         //plota o marker

         markerOptions = new MarkerOptions();
         markerOptions.position(latLng);



         //markerOptions.snippet();

         markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
         //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.boneco));

         infoWindow=getLayoutInflater().inflate(R.layout.marker_adapter, null);

         markerOptions.draggable(true);
         chennai = map.addMarker(markerOptions);


         map.setInfoWindowAdapter(new CustomInfoAdapter());
         map.setOnInfoWindowClickListener(null);
         map.setOnMarkerClickListener(this);
         map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

             @Override
             public void onInfoWindowClick(Marker marker) {

                 final Intent intent = new Intent(MainActivity.this, SendLocalActivity.class);
                 startActivity(intent);


             }
         });


         map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

             @Override
             public void onMarkerDragStart(Marker marker) {
                 marker.setPosition(latLng);
                 marker.showInfoWindow();
                 first=true;

             }

             @Override
             public void onMarkerDrag(Marker marker) {
                 if(first) {
                     lLDiferenca = marker.getPosition();

                     latDif = lLDiferenca.latitude - (latLng.latitude);
                     if(latDif<0) latDif=(-1)*latDif;


                     lngDif = lLDiferenca.longitude - (latLng.longitude);
                     if(lngDif<0) lngDif=(-1)*lngDif;

                     first=false;
                 }
                 latLng = marker.getPosition();
                 lLDiferenca = new LatLng(latLng.latitude-latDif,latLng.longitude-lngDif);
                 marker.setPosition(lLDiferenca);
                 latLng = marker.getPosition();

                 marker.showInfoWindow();
             }

             @Override
             public void onMarkerDragEnd(Marker marker) {
                 marker.setPosition(latLng);
                 marker.showInfoWindow();
             }

         });


            /*
            *
            *
            * */
         //atualiza as configura��es no mapa
         CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
         map.moveCamera(update);

         map.setMyLocationEnabled(true);
         map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

             @Override
             public boolean onMyLocationButtonClick() {
                 firstClick = true;

                 return false;
             }
         });
         map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
             @Override
             public void onMyLocationChange(Location location) {

                 if (firstClick) {
                     firstClick = false;
                     plotaNoMap(location);
                 }
             }
         });
     }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Seu GPS está desligado, deseja ativá-lo?")
                .setCancelable(false)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }


    public void actionButton(View view){
        final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.ufjf.br/portal"));
        this.startActivity(intent);
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
        ((TextView) infoWindow.findViewById(R.id.lblListHeader)).setText("Cadastrar esse lugar?");


        ((TextView) infoWindow.findViewById(R.id.lblLat)).setText("Lat: "+String.valueOf(latLng.latitude));
        ((TextView) infoWindow.findViewById(R.id.lblLong)).setText("Long: "+String.valueOf(latLng.longitude));


    }
///http://blog.kerul.net/2014/04/insert-new-record-on-android-online.html

}
