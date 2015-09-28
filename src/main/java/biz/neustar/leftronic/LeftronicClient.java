package biz.neustar.leftronic;

import biz.neustar.leftronic.util.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeftronicClient {
    private String accessKey;
    private LeftronicHttpClient client;
    private ObjectMapper mapper;

    /**
     * Creates a new Leftronic client with the supplied API access key and desired max number of concurrent HTTP threads.
     *
     * @param accessKey your Leftronic API key
     * @param maxThreads the max number of HTTP requests you want to allow to Leftronic's servers
     */
    @Inject
    public LeftronicClient(@Named("leftronic.accessKey") String accessKey, @Named("leftronic.maxThreads") int maxThreads) {
        this.accessKey = accessKey;
        this.client = new LeftronicHttpClient(maxThreads);
        this.mapper = new ObjectMapper();
    }

    /**
     * Sends a "Custom Number" data point to the supplied Leftronic stream.
     *
     * @param streamName target widget/stream
     * @param value numeric value to display/plot
     * @throws IOException when there is an IO problem such as making a network request
     * @throws LeftronicException when a non-200 HTTP response code is returned
     */
    public void sendNumber(String streamName, int value) throws IOException, LeftronicException {
        sendPoint(streamName, value);
    }

    /**
     * Sends a "Custom Geo" data point to the supplied Leftronic stream.
     *
     * @param streamName target widget/stream
     * @param lat the lattitude of the geo point
     * @param lon the longitude of the geo point
     * @throws IOException when there is an IO problem such as making a network request
     * @throws LeftronicException when a non-200 HTTP response code is returned
     */
    public void sendGeoPoint(String streamName, double lat, double lon) throws IOException, LeftronicException {
        sendPoint(streamName, new LatLong(lat, lon));
    }

    /**
     * Sends a "Custom Text" data point to the supplied Leftronic stream.
     *
     * @param streamName target widget/stream
     * @param title the title of the text to be displayed
     * @param message the actual message of the text to be displayed
     * @throws IOException when there is an IO problem such as making a network request
     * @throws LeftronicException when a non-200 HTTP response code is returned
     */
    public void sendText(String streamName, String title, String message) throws LeftronicException, IOException {
        sendPoint(streamName, new Text(title, message));
    }

    /**
     * Sends a "Custom Text" data point to the supplied Leftronic stream.
     *
     * @param streamName target widget/stream
     * @param title the title of the text to be displayed
     * @param message the actual message of the text to be displayed
     * @param imgUrl the image url
     * @throws IOException when there is an IO problem such as making a network request
     * @throws LeftronicException when a non-200 HTTP response code is returned
     */
    public void sendText(String streamName, String title, String message, String imgUrl) throws LeftronicException, IOException {
        Text text = new Text(title, message);
        text.setImgUrl(imgUrl);
        sendPoint(streamName, text);
    }

    /**
     * Sends a "Custom Leaderboard" data point to the supplied Leftronic stream.
     *
     * @param streamName target widget/stream
     * @param entries the list of leaderboard entries, which is a name/value pair (String, int)
     * @throws IOException when there is an IO problem such as making a network request
     * @throws LeftronicException when a non-200 HTTP response code is returned
     */
    public void sendLeaderboard(String streamName, LeaderboardEntry... entries) throws LeftronicException, IOException {
        ArrayList<LeaderboardEntry> list = new ArrayList<LeaderboardEntry>();
        Collections.addAll(list, entries);
        sendPoint(streamName, new Leaderboard(list));
    }

    /**
     * Alternative form of sendLeaderboard that takes a List rather than var-args/array.
     *
     * @param streamName target widget/stream
     * @param entries the list of leaderboard entries, which is a name/value pair (String, int)
     * @throws IOException when there is an IO problem such as making a network request
     * @throws LeftronicException when a non-200 HTTP response code is returned
     */
    public void sendLeaderboard(String streamName, List<LeaderboardEntry> entries) throws LeftronicException, IOException {
        sendPoint(streamName, new Leaderboard(entries));
    }

    /**
     * Sends a "Custom List" data point to the supplied Leftronic stream.
     *
     * @param streamName target widget/stream
     * @param entries the list of text elements to display
     * @throws IOException when there is an IO problem such as making a network request
     * @throws LeftronicException when a non-200 HTTP response code is returned
     */
    public void sendList(String streamName, String... entries) throws LeftronicException, IOException {
        List<LeftronicListEntry> list = new ArrayList<LeftronicListEntry>(entries.length);
        for (String entry : entries) {
            list.add(new LeftronicListEntry(entry));
        }
        sendPoint(streamName, new LeftronicList(list));
    }

    /**
     * Alternative form of sendList that takes a List rather than var-args/array.
     *
     * @param streamName target widget/stream
     * @param entries the list of text elements to display
     * @throws IOException when there is an IO problem such as making a network request
     * @throws LeftronicException when a non-200 HTTP response code is returned
     */
    public void sendList(String streamName, List<String> entries) throws LeftronicException, IOException {
        List<LeftronicListEntry> list = new ArrayList<LeftronicListEntry>(entries.size());
        for (String entry : entries) {
            list.add(new LeftronicListEntry(entry));
        }
        sendPoint(streamName, new LeftronicList(list));
    }

    /**
     * Sends a data point to the supplied Leftronic stream.
     *
     * @param streamName target widget/stream
     * @param value the data to send. this can be any object which serializes to a valid leftronic point format
     * @throws IOException when there is an IO problem such as making a network request
     * @throws LeftronicException when a non-200 HTTP response code is returned
     */
    public void sendPoint(String streamName, Object value) throws IOException, LeftronicException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, new Data(accessKey, streamName, value));
        post(baos);
    }

    /**
     * Sends a command to the supplied Leftronic stream.
     *
     * @param streamName target widget/stream
     * @param command the command to send
     * @throws IOException when there is an IO problem such as making a network request
     * @throws LeftronicException when a non-200 HTTP response code is returned
     */
    public void sendCommand(String streamName, String command) throws IOException, LeftronicException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, new Command(accessKey, streamName, command));
        post(baos);
    }

    private void post(ByteArrayOutputStream data) throws IOException, LeftronicException {
        HttpPost post = new HttpPost("https://www.leftronic.com/customSend/");
        post.setEntity(new ByteArrayEntity(data.toByteArray()));
        HttpResponse response = null;
        try {
            response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new LeftronicException(response);
            }
        } finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }
}
