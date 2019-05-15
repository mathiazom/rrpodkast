package com.rrpm.mzom.projectrrpm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class RRReader extends Thread {

    private static final String TAG = "RRP-RRReader";

    enum PodType{
        MAIN_PODS,
        ARCHIVE_PODS
    }

    private PodType podType;

    private ArrayList<RRPod> retrievedPods = new ArrayList<>();


    void readPodsFeed(final PodType podType){

        this.podType = podType;

        start();

    }


    @Override
    public void run() {

        switch (podType){

            case MAIN_PODS:

                try {

                    retrievedPods = getRetrievedPods(

                            new URL(PodFeedConstants.MAIN_PODS_URL),

                            podType,

                            podBuilder -> {
                                podBuilder.setTitle(getMainPodTitle(podBuilder.getDate()));
                                return podBuilder.build();
                            }
                    );
                }

                // TODO: Handle exceptions
                catch (ParserConfigurationException | SAXException | IOException | ParseException e) {
                    e.printStackTrace();
                }

                break;

            case ARCHIVE_PODS:

                try {

                    retrievedPods = getRetrievedPods(

                            new URL(PodFeedConstants.ARCHIVE_PODS_URL),

                            podType,

                            RRPod.Builder::build
                    );
                }

                // TODO: Handle exceptions
                catch (ParserConfigurationException | SAXException | IOException | ParseException e) {
                    e.printStackTrace();
                }

                break;

        }

    }

    private interface PodBuilderCallback {
        @NonNull RRPod buildPod(@NonNull final RRPod.Builder podBuilder);
    }

    @NonNull
    private ArrayList<RRPod> getRetrievedPods(@NonNull final URL rssUrl, @NonNull final PodType podType, @NonNull final PodBuilderCallback podBuilderCallback) throws IOException, SAXException, ParserConfigurationException, ParseException {

        final NodeList nList = getNodeListFromRSS(rssUrl);

        if (nList == null){

            throw new RRReaderException("Feed list was null");
        }

        if (nList.getLength() == 0){

            throw new RRReaderException("Feed list was empty");
        }

        final ArrayList<RRPod> pods = new ArrayList<>();

        for (int i = 0; i < nList.getLength(); i++) {

            final Node nNode = nList.item(i);

            if (nNode == null || nNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            final Element eElement = (Element) nNode;

            final String guid = eElement.getElementsByTagName("guid").item(0).getTextContent();

            final String rawDate = eElement.getElementsByTagName("pubDate").item(0).getTextContent();
            final DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
            final Date date = format.parse(rawDate);

            final String url = eElement.getElementsByTagName("enclosure").item(0).getAttributes().getNamedItem("url").getNodeValue();

            final String durationString = eElement.getElementsByTagName("itunes:duration").item(0).getTextContent();
            int duration = MillisFormatter.fromFormat(durationString, MillisFormatter.MillisFormat.HH_MM_SS);

            final String title = eElement.getElementsByTagName("title").item(0).getTextContent();

            String description = eElement.getElementsByTagName("description").item(0).getTextContent();
            if (description.length() < 3) {
                description = "";
            }
            description = description.trim();


            final RRPod.Builder podBuilder = new RRPod.Builder()
                    .setId(new PodId(guid))
                    .setPodType(podType)
                    .setDate(date)
                    .setUrl(url)
                    .setDuration(duration)
                    .setTitle(title)
                    .setDescription(description);

            final RRPod pod = podBuilderCallback.buildPod(podBuilder);


            pods.add(pod);

        }

        return pods;

    }


    private String getMainPodTitle(@NonNull final Date date) {

        final String format = "EEEE d. MMMM yyyy";

        final Locale locale = new Locale("no","NO");

        final SimpleDateFormat formatter = new SimpleDateFormat(format,locale);

        final String rawDate = formatter.format(date);

        return rawDate.substring(0,1).toUpperCase() + rawDate.substring(1);

    }

    private NodeList getNodeListFromRSS(@NonNull final URL url) throws ParserConfigurationException, IOException, SAXException {

        final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        final Document doc = db.parse(url.openStream());

        doc.getDocumentElement().normalize();

        return doc.getElementsByTagName("item");

    }

    @Nullable
    ArrayList<RRPod> getRetrievedPods() {

        return retrievedPods;

    }


    class RRReaderException extends RuntimeException{

        RRReaderException(String msg){

            super(msg);

        }

    }


}