package com.ice.toaqui;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 *
 * @author claudio
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

import com.ice.toaqui.pojo.Area;
import com.ice.toaqui.pojo.Usuarios;


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

//realiza o parse de areas
    public static List<Area> parseArea(InputSource input) {
        List<Area> areas = new ArrayList<Area>();
        String EXPR_LIST = "//areas/area";

        String EXPR_IDAREA = "idarea";
        String EXPR_NOME = "nome";
        String EXPR_RESPONSAVEL = "responsavel";
        String EXPR_CIDADE = "cidade";

        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(EXPR_LIST, input, XPathConstants.NODESET);

            int index = 0;
            while (index < nodes.getLength()) {
                Node item = nodes.item(index);

                Node idareaNode    = (Node) xpath.evaluate(EXPR_IDAREA, item, XPathConstants.NODE);
                Node nomeNode    = (Node) xpath.evaluate(EXPR_NOME, item, XPathConstants.NODE);
                Node responsavelNode    = (Node) xpath.evaluate(EXPR_RESPONSAVEL, item, XPathConstants.NODE);
                Node cidadeNode    = (Node) xpath.evaluate(EXPR_CIDADE, item, XPathConstants.NODE);

                Area area = new Area(Integer.valueOf(idareaNode.getTextContent()), nomeNode.getTextContent(), responsavelNode.getTextContent(), cidadeNode.getTextContent());
                areas.add(area);
                index++;
            }

        } catch (XPathExpressionException e) {
            Log.e(TAG, e.toString());
        }

        return areas;
    }

    public static List<Usuarios> parseUsuarios(InputSource input) {
        List<Usuarios> users = new ArrayList<Usuarios>();
        String EXPR_LIST = "//logins/login";

        String EXPR_EMAIL = "google_email";


        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(EXPR_LIST, input, XPathConstants.NODESET);

            int index = 0;
            while (index < nodes.getLength()) {
                Node item = nodes.item(index);

                Node emailNode    = (Node) xpath.evaluate(EXPR_EMAIL, item, XPathConstants.NODE);

                Usuarios user = new Usuarios(emailNode.getTextContent());
                users.add(user);
                index++;
            }

        } catch (XPathExpressionException e) {
            Log.e(TAG, e.toString());
        }

        return users;
    }


}
