package com.rrpm.mzom.projectrrpm;

import android.content.Context;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Laget av Mathias Myklebust
 */

class RRReader extends Thread {

    private Context context;

    private ArrayList<ArrayList<RRPod>> masterlist;

    private final ArrayList<RRPod> allpods = new ArrayList<>();
    private final ArrayList<RRPod> archivepods = new ArrayList<>();

    RRReader(Context context, ArrayList<ArrayList<RRPod>> masterlist) {
        this.context = context;
        this.masterlist = masterlist;
    }

    public void run() {
        hovedPodkast();
        arkivPodkast();
    }

    private void hovedPodkast(){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        if (db == null) return;

        Document doc = null;

        try {
            doc = db.parse(new URL("http://podkast.nrk.no/program/radioresepsjonen.rss").openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (doc == null) return;

        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("item");

        if (nList == null) return;

        if(!(masterlist != null && masterlist.get(0).size() > 0 && nList.getLength() == masterlist.get(0).size())){
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = null;
                try {
                    nNode = nList.item(temp);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                if (nNode != null && nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String rawDate = eElement.getElementsByTagName("pubDate").item(0).getTextContent();
                    String url = eElement.getElementsByTagName("enclosure").item(0).getAttributes().getNamedItem("url").getNodeValue();

                    String duration = eElement.getElementsByTagName("itunes:duration").item(0).getTextContent();

                    String desc = eElement.getElementsByTagName("description").item(0).getTextContent();
                    if (desc.length() < 3) {
                        desc = "";
                    }

                    DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
                    Date dateObj = null;
                    try {
                        dateObj = format.parse(rawDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateObj);

                    String[] dager = new String[]{
                            "Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "Lørdag", "Søndag"
                    };

                    String[] måneder = context.getResources().getStringArray(R.array.months);

                    String date = String.valueOf(dager[cal.get(Calendar.DAY_OF_WEEK) - 2]) + " " + String.valueOf(cal.get(Calendar.DATE)) + ". " + String.valueOf(måneder[cal.get(Calendar.MONTH)]) + " " + String.valueOf(cal.get(Calendar.YEAR));

                    if (allpods.size() == 0 || !allpods.get(allpods.size() - 1).getTitle().equals(date)) {
                        RRPod newpod = new RRPod(date, dateObj, url, desc, duration);
                        allpods.add(newpod);
                    }
                }
            }
        }
    }

    private void arkivPodkast() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        if (db == null) return;

        Document doc = null;

        try {
            doc = db.parse(new URL("http://podkast.nrk.no/program/radioresepsjonens_arkivpodkast.rss").openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (doc == null) return;

        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("item");

        if (nList == null) return;

        if(!(masterlist != null && masterlist.get(1).size() > 0 && nList.getLength() == masterlist.get(1).size())){
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = null;
                try {
                    nNode = nList.item(temp);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                if (nNode != null) {
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;

                        String rawDate = eElement.getElementsByTagName("pubDate").item(0).getTextContent();
                        String url = eElement.getElementsByTagName("enclosure").item(0).getAttributes().getNamedItem("url").getNodeValue();

                        String duration = eElement.getElementsByTagName("itunes:duration").item(0).getTextContent();

                        String title = eElement.getElementsByTagName("title").item(0).getTextContent();

                        String desc = eElement.getElementsByTagName("description").item(0).getTextContent();
                        if (desc.length() < 3) {
                            desc = "";
                        }

                        DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
                        Date dateObj = null;
                        try {
                            dateObj = format.parse(rawDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateObj);

                        if (archivepods.size() == 0 || !archivepods.get(archivepods.size() - 1).getTitle().equals(title)) {
                            RRPod newpod = new RRPod(title, dateObj, url, desc, duration);
                            archivepods.add(newpod);
                        }

                    }
                }


            }
        }
    }

    ArrayList<ArrayList<RRPod>> retrievePods() {

        ArrayList<ArrayList<RRPod>> masterlist = new ArrayList<>();

        masterlist.add(allpods);
        masterlist.add(archivepods);

        return masterlist;

    }

}