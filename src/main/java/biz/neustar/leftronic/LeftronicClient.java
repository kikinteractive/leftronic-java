package biz.neustar.leftronic;

import biz.neustar.leftronic.util.Command;
import biz.neustar.leftronic.util.Data;
import biz.neustar.leftronic.util.LatLong;
import biz.neustar.leftronic.util.Leaderboard;
import biz.neustar.leftronic.util.LeaderboardEntry;
import biz.neustar.leftronic.util.LeftronicHttpClient;
import biz.neustar.leftronic.util.LeftronicList;
import biz.neustar.leftronic.util.LeftronicListEntry;
import biz.neustar.leftronic.util.Text;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

public class LeftronicClient
{
    private String accessKey;
    private LeftronicHttpClient client;
    private ObjectMapper mapper;

    @Inject
    public LeftronicClient(@Named("leftronic.accessKey") String accessKey, @Named("leftronic.maxThreads") int maxThreads)
    {
        this.accessKey = accessKey;
        this.client = new LeftronicHttpClient(maxThreads);
        this.mapper = new ObjectMapper();
    }

    public void sendNumber(String streamName, int value) throws IOException, LeftronicException
    {
        postPoint(streamName, value);
    }

    public void sendGeoPoint(String streamName, double lat, double lon) throws IOException, LeftronicException
    {
        postPoint(streamName, new LatLong(lat, lon));
    }

    public void sendText(String streamName, String title, String message) throws LeftronicException, IOException
    {
        postPoint(streamName, new Text(title, message));
    }

    public void sendText(String streamName, String title, String message, String imgUrl) throws LeftronicException, IOException
    {
        Text text = new Text(title, message);
        text.setImgUrl(imgUrl);
        postPoint(streamName, text);
    }

    public void sendLeaderboard(String streamName, LeaderboardEntry... entries) throws LeftronicException, IOException
    {
        ArrayList<LeaderboardEntry> list = new ArrayList<LeaderboardEntry>();
        Collections.addAll(list, entries);
        postPoint(streamName, new Leaderboard(list));
    }

    public void sendLeaderboard(String streamName, List<LeaderboardEntry> entries) throws LeftronicException, IOException
    {
        postPoint(streamName, new Leaderboard(entries));
    }

    public void sendList(String streamName, String... entries) throws LeftronicException, IOException
    {
        List<LeftronicListEntry> list = new ArrayList<LeftronicListEntry>(entries.length);
        for (String entry : entries) {
            list.add(new LeftronicListEntry(entry));
        }
        postPoint(streamName, new LeftronicList(list));
    }

    public void sendList(String streamName, List<String> entries) throws LeftronicException, IOException
    {
        List<LeftronicListEntry> list = new ArrayList<LeftronicListEntry>(entries.size());
        for (String entry : entries) {
            list.add(new LeftronicListEntry(entry));
        }
        postPoint(streamName, new LeftronicList(list));
    }

    public void postPoint(String streamName, Object value) throws IOException, LeftronicException
    {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, new Data(accessKey, streamName, value));
        post(baos);
    }

    public void postCommand(String streamName, String command) throws IOException, LeftronicException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, new Command(accessKey, streamName, command));
        post(baos);
    }

    private void post(ByteArrayOutputStream data) throws IOException, LeftronicException
    {
        HttpPost post = new HttpPost("https://www.leftronic.com/customSend/");
        post.setEntity(new ByteArrayEntity(data.toByteArray()));
        HttpResponse response = null;
        try {
            response = client.execute(post);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new LeftronicException(response);
            }
        }
        finally {
            if (response != null) {
                EntityUtils.consume(response.getEntity());
            }
        }
    }
}
