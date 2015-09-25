package biz.neustar.leftronic.util;

public class Command
{
    private String accessKey;
    private String streamName;
    private String command;

    public Command(String accessKey, String streamName, String command)
    {
        this.accessKey = accessKey;
        this.streamName = streamName;
        this.command = command;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public String getStreamName()
    {
        return streamName;
    }

    public String getCommand()
    {
        return command;
    }
}
