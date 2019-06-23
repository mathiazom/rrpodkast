package com.rrpm.mzom.projectrrpm.podfeed;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.rrpm.mzom.projectrrpm.debugging.Assertions;
import com.rrpm.mzom.projectrrpm.pod.PodId;
import com.rrpm.mzom.projectrrpm.pod.PodType;
import com.rrpm.mzom.projectrrpm.pod.RRPod;
import com.rrpm.mzom.projectrrpm.pod.RRPodBuilder;
import com.rrpm.mzom.projectrrpm.podstorage.DateUtils;
import com.rrpm.mzom.projectrrpm.podstorage.MillisFormatter;
import com.rrpm.mzom.projectrrpm.podstorage.PodStorageHandle;
import com.rrpm.mzom.projectrrpm.podstorage.PodUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PodsFeedReader extends Thread {

    private static final String TAG = "RRP-PodsFeedReader";


    @NonNull
    public ArrayList<RRPod> readPodsFeed(@NonNull PodType podType) throws InvalidFeedException {

        final URL feedUrl = podType.getFeedUrl();

        if(feedUrl == null){

            throw new InvalidFeedException(PodsFeedError.INVALID_FEED_URL);

        }

        switch (podType) {

            case MAIN_PODS:

                return retrievePods(feedUrl, podType, podBuilder -> {

                            Assertions._assert(podBuilder.getDate() != null, "Pod builder date was null");

                            podBuilder.setTitle(DateUtils.getDateAsString(podBuilder.getDate()));

                        }
                );

            case ARCHIVE_PODS:

                return retrievePods(feedUrl, podType);

            default:

                Assertions._assert(false, "Invalid pod type");

        }

        throw new InvalidFeedException(PodsFeedError.INVALID_FEED_TYPE);

    }

    private interface PodBuilderCallback {

        /***
         *
         * Called before a new RRPod object is created with {@link RRPodBuilder#build()}.
         * Enables raw RSS values to be modified before pod creation.
         *
         * @param builder: Pod builder with values required for pod creation.
         *
         */

        void preBuild(@NonNull final RRPodBuilder builder);

    }

    @NonNull
    private ArrayList<RRPod> retrievePods(@NonNull final URL rssUrl, @NonNull final PodType podType) throws InvalidFeedException {

        return retrievePods(rssUrl, podType, null);

    }

    @NonNull
    private ArrayList<RRPod> retrievePods(@NonNull final URL feedUrl, @NonNull final PodType podType, @Nullable final PodBuilderCallback podBuilderCallback) throws InvalidFeedException {

        final NodeList nodeList = retrieveFeedNodeList(feedUrl);

        final ArrayList<RRPod> retrievedPods = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {

            final RRPod pod;
            try{

                pod = buildPodFromFeedNode(nodeList.item(i),podType,podBuilderCallback);

            }catch (InvalidFeedDataException e){

                Log.e(TAG,"Invalid feed item data (index: " + String.valueOf(i) + ")");

                continue;
            }

            retrievedPods.add(pod);

        }

        return retrievedPods;
    }

    private RRPod buildPodFromFeedNode(@Nullable Node node, @NonNull PodType podType, @Nullable final PodBuilderCallback podBuilderCallback) throws InvalidFeedDataException {

        if (node == null) {

            throw new InvalidFeedDataException("Node was null");

        }

        if (node.getNodeType() != Node.ELEMENT_NODE) {

            throw new InvalidFeedDataException("Node was not a valid element");

        }

        final Element element = (Element) node;

        final RRPodBuilder builder = RRPod.createBuilder();

        builder.setPodType(podType);

        final String guid = element.getElementsByTagName(PodsFeedConstants.ELEMENT_ID_TAG_NAME).item(0).getTextContent();
        validateFeedData(guid != null, PodsFeedConstants.ELEMENT_ID_TAG_NAME);
        builder.setId(new PodId(guid));

        final String rawDate = element.getElementsByTagName(PodsFeedConstants.RAW_DATE_TAG_NAME).item(0).getTextContent();
        validateFeedData(rawDate != null, PodsFeedConstants.RAW_DATE_TAG_NAME);
        final DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
        final Date date;
        try {
            date = format.parse(rawDate);
        } catch (ParseException e) {
            throw new RRReaderException("Item date (" + rawDate + ") could not be parsed: " + e);
        }
        builder.setDate(date);

        final String url = element.getElementsByTagName("enclosure").item(0).getAttributes().getNamedItem(PodsFeedConstants.URL_ITEM_NAME).getNodeValue();
        validateFeedData(url != null, PodsFeedConstants.URL_ITEM_NAME);
        builder.setUrl(url);

        final String durationString = element.getElementsByTagName(PodsFeedConstants.DURATION_TAG_NAME).item(0).getTextContent();
        validateFeedData(durationString != null, PodsFeedConstants.DURATION_TAG_NAME);
        int duration = MillisFormatter.fromFormat(durationString, MillisFormatter.MillisFormat.HH_MM_SS);
        builder.setDuration(duration);

        final String title = element.getElementsByTagName(PodsFeedConstants.TITLE_TAG_NAME).item(0).getTextContent();
        validateFeedData(title != null, PodsFeedConstants.TITLE_TAG_NAME);
        builder.setTitle(title);

        String description = element.getElementsByTagName(PodsFeedConstants.DESCRIPTION_TAG_NAME).item(0).getTextContent();
        validateFeedData(description != null, PodsFeedConstants.DESCRIPTION_TAG_NAME);
        description = description.trim();
        builder.setDescription(description);

        if (podBuilderCallback != null) {

            podBuilderCallback.preBuild(builder);

        }

        return builder.build();


    }


    @NonNull
    private NodeList retrieveFeedNodeList(@NonNull final URL url) throws InvalidFeedException {

        final DocumentBuilder db;
        try {

            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        } catch (ParserConfigurationException e) {

            throw new InvalidFeedException(PodsFeedError.FAILED_DOCUMENT_BUILD);

        }

        final InputStream inputStream;
        try {

            inputStream = url.openStream();

        } catch (IOException e) {

            throw new InvalidFeedException(PodsFeedError.INVALID_FEED_URL);

        }

        final Document doc;
        try {

            doc = db.parse(inputStream);

        } catch (IOException | SAXException e) {

            throw new InvalidFeedException(PodsFeedError.FAILED_FEED_PARSE);

        }

        doc.getDocumentElement().normalize();

        final NodeList nodeList = doc.getElementsByTagName(PodsFeedConstants.NODE_TAG_NAME);

        if(nodeList == null){

            throw new InvalidFeedException(PodsFeedError.NULL_FEED_LIST);

        }else if(nodeList.getLength() <= 0){

            throw new InvalidFeedException(PodsFeedError.EMPTY_FEED_LIST);

        }

        return nodeList;
    }


    private void validateFeedData(boolean valid, String tagName) throws InvalidFeedDataException {

        if (!valid) {

            throw new InvalidFeedDataException(tagName + "value was not valid");

        }

    }

    public class InvalidFeedDataException extends Exception {

        InvalidFeedDataException(String msg) {

            super(msg);

        }

    }

    public class InvalidFeedException extends Exception {

        @NonNull final PodsFeedError error;

        InvalidFeedException(@NonNull PodsFeedError error) {

            super(error.getMessage());

            this.error = error;

        }

        @NonNull
        public PodsFeedError getError() {
            return error;
        }

    }

    // TODO: Better exception handling

    public class RRReaderException extends RuntimeException {

        RRReaderException(String msg) {

            super(msg);

        }

    }


}