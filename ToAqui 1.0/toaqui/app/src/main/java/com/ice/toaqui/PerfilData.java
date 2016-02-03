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

    public String mapaAtual;

    public String cidadeAtual;

    public boolean superUser;

    public PerfilData() {

    }

    public PerfilData(String email){
        this.email = email;
    }

    public PerfilData(String email, List<String> mapas){
        this.email = email;
        this.mapas.addAll(mapas);
    }

    public boolean isSuperUser(){
        return superUser;
    }

    public void setSuperUser(boolean superUser){
        this.superUser = superUser;
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String email){
        this.email=email;
    }

    public String getMapaAtual(){
        return this.mapaAtual;
    }

    public void setMapaAtual(String mapaAtual){
        this.mapaAtual=mapaAtual;
    }

    public String getCidadeAtual(){
        return this.cidadeAtual;
    }

    public void setCidadeAtual(String cidadeAtual){
        this.cidadeAtual=cidadeAtual;
    }

    public static List<String> getMapas(){
        return mapas;
    }

    public static void restartMapas(List<String> novosMapas){
        mapas.clear();
        mapas.addAll(novosMapas);
    }


}
