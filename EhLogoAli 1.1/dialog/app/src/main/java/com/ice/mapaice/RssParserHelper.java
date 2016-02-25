package com.ice.mapaice;
/**
 *
 * @author claudio e joão paulo
 */
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ice.mapaice.pojo.Area;
import com.ice.mapaice.pojo.Local;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

/**
 * Parse rss feeds and returns HashMap
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class RssParserHelper {

    private static final String TAG = "RssParserHelper";

    /**
     * Parse xml input
     * @param input
     * @return string
     */
    public static List<Local> parseMapa(InputSource input) {
        List<Local> locais = new ArrayList<Local>();
        String EXPR_LIST = "//mapa/local";

        String EXPR_IDLOCAL = "idlocal";
        String EXPR_NOME = "nome";
        String EXPR_LATITUDE = "latitude";
        String EXPR_LONGITUDE = "longitude";
        String EXPR_IDAREA = "idarea";
        String EXPR_LINK = "link";

        try {

            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(EXPR_LIST, input, XPathConstants.NODESET);

            int index = 0;
            while (index < nodes.getLength()) {
                Node item = nodes.item(index);

                Node idlocalNode = (Node) xpath.evaluate(EXPR_IDLOCAL, item, XPathConstants.NODE);
                Node nomeNode    = (Node) xpath.evaluate(EXPR_NOME, item, XPathConstants.NODE);
                Node latitudeNode    = (Node) xpath.evaluate(EXPR_LATITUDE, item, XPathConstants.NODE);
                Node longitudeNode    = (Node) xpath.evaluate(EXPR_LONGITUDE, item, XPathConstants.NODE);
                Node idareaNode = (Node) xpath.evaluate(EXPR_IDAREA, item, XPathConstants.NODE);
                Node linkNode    = (Node) xpath.evaluate(EXPR_LINK, item, XPathConstants.NODE);

                Local local = new Local(Integer.getInteger(idlocalNode.getTextContent()), nomeNode.getTextContent(), latitudeNode.getTextContent(), longitudeNode.getTextContent(), Integer.getInteger(idareaNode.getTextContent()), linkNode.getTextContent());

                locais.add(local);
                index++;
            }

        } catch (XPathExpressionException e) {
            Log.e(TAG, e.toString());
        }

        return locais;
    }

    //realiza o parse de areas
    public static List<Area> parseArea(InputSource input) {
        List<Area> areas = new ArrayList<Area>();
        String EXPR_LIST = "//areas/area";

        String EXPR_IDAREA = "idarea";
        String EXPR_NOME = "nome";
        String EXPR_RESPONSAVEL = "responsavel";
        String EXPR_LAT = "latitudeare";
        String EXPR_LONG = "longitudearea";
        String EXPR_CIDADE = "cidade";
        String EXPR_LATCID = "latitudecidade";
        String EXPR_LONGCID = "longitudecidade";

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(EXPR_LIST, input, XPathConstants.NODESET);

            int index = 0;
            while (index < nodes.getLength()) {
                Node item = nodes.item(index);

                Node idareaNode    = (Node) xpath.evaluate(EXPR_IDAREA, item, XPathConstants.NODE);
                Node nomeNode    = (Node) xpath.evaluate(EXPR_NOME, item, XPathConstants.NODE);
                Node responsavelNode    = (Node) xpath.evaluate(EXPR_RESPONSAVEL, item, XPathConstants.NODE);
                Node latNode    = (Node) xpath.evaluate(EXPR_LAT, item, XPathConstants.NODE);
                Node longNode    = (Node) xpath.evaluate(EXPR_LONG, item, XPathConstants.NODE);
                Node cidadeNode    = (Node) xpath.evaluate(EXPR_CIDADE, item, XPathConstants.NODE);
                Node latCidadeNode    = (Node) xpath.evaluate(EXPR_LATCID, item, XPathConstants.NODE);
                Node longCidadeNode    = (Node) xpath.evaluate(EXPR_LONGCID, item, XPathConstants.NODE);

                Area area = new Area(Integer.valueOf(idareaNode.getTextContent()), nomeNode.getTextContent(),
                        responsavelNode.getTextContent(), cidadeNode.getTextContent(), latNode.getTextContent(),
                        longNode.getTextContent(), latCidadeNode.getTextContent(), longCidadeNode.getTextContent());
                areas.add(area);
                index++;
            }

        } catch (XPathExpressionException e) {
            Log.e(TAG, e.toString());
        }

        return areas;
    }
    //realiza o parse dos locais
    public static List<Local> parseLocal(InputSource input) {
        List<Local> locais = new ArrayList<Local>();
        String EXPR_LIST = "//locals/local";

        String EXPR_IDLOCAL = "idlocal";
        String EXPR_NOME = "nome";
        String EXPR_LATITUDE = "latitude";
        String EXPR_LONGITUDE = "longitude";
        String EXPR_IDAREA = "idarea";
        String EXPR_LINK = "link";

        try {

            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(EXPR_LIST, input, XPathConstants.NODESET);

            int index = 0;
            while (index < nodes.getLength()) {
                Node item = nodes.item(index);

                Node idlocalNode = (Node) xpath.evaluate(EXPR_IDLOCAL, item, XPathConstants.NODE);
                Node nomeNode    = (Node) xpath.evaluate(EXPR_NOME, item, XPathConstants.NODE);
                Node latitudeNode    = (Node) xpath.evaluate(EXPR_LATITUDE, item, XPathConstants.NODE);
                Node longitudeNode    = (Node) xpath.evaluate(EXPR_LONGITUDE, item, XPathConstants.NODE);
                Node idareaNode = (Node) xpath.evaluate(EXPR_IDAREA, item, XPathConstants.NODE);
                Node linkNode    = (Node) xpath.evaluate(EXPR_LINK, item, XPathConstants.NODE);

                Local local;

                if(linkNode!=null) {
                     local = new Local(Integer.getInteger(idlocalNode.getTextContent()), nomeNode.getTextContent(), latitudeNode.getTextContent(), longitudeNode.getTextContent(), Integer.getInteger(idareaNode.getTextContent()), linkNode.getTextContent());
                }
                else  local = new Local(Integer.getInteger(idlocalNode.getTextContent()), nomeNode.getTextContent(), latitudeNode.getTextContent(), longitudeNode.getTextContent(), Integer.getInteger(idareaNode.getTextContent()), null);

                locais.add(local);
                index++;
            }

        } catch (XPathExpressionException e) {
            Log.e(TAG, e.toString());
        }

        return locais;
    }


}
