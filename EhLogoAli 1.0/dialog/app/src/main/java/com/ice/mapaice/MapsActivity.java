package com.ice.mapaice;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;



public class MapsActivity extends Activity implements OnMapReadyCallback {

    private static MapView mMapView;
    //o mapa em si
    private static GoogleMap map;
    //zoom m�nimo do mapa
    private final int MIN_ZOOM = 18;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // View rootView = inflater.inflate(R.layout.home_fragment, container, false);
        //mMapView = (MapView) rootView.findViewById(R.id.mapview);

        // inflat and return the layout
       // mMapView.onCreate(savedInstanceState);
       // mMapView.onResume();// needed to get the map to display immediately
        //inicaia a activity do mapa e faz seu get para manipul�-la
       // MapsInitializer.initialize(getActivity());


        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //return rootView;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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



    /*
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            //((MainActivity) activity).onSectionAttached(
            //        getArguments().getInt(ARG_SECTION_NUMBER));
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }


         * Using a mapview in a fragment requires you to 'route'
         * the lifecycle events of the fragment to the mapview
         */
    @Override
    public void onResume() {
        super.onResume();
        if (null != mMapView)
            mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mMapView)
            mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mMapView)
            mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mMapView)
            mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (null != mMapView)
            mMapView.onLowMemory();
    }


    //get e set do map para manipul�-lo no Main
    public static GoogleMap getMyMap(){
        return map;
    }



    public void receveNewMap(GoogleMap newMap){
        map = newMap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //plotagem do ICE - aqui ele posiciona a visualiza��o  no ponto
        LatLng latLong = new LatLng(-21.7768679, -43.3716235);
        CameraPosition position = new CameraPosition.Builder()
                .target(latLong)
                .bearing(0)
                .tilt(45)
                .zoom(MIN_ZOOM)
                .build();

        //plota o marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLong);
        markerOptions.title("ICE");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.boneco));
        map.addMarker(markerOptions);
        //atualiza as configura��es no mapa
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
        map.moveCamera(update);


    }
}