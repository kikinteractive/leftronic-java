package biz.neustar.leftronic.util;

public class GraphPoint {

    private Long timestamp;
    private Double number;

    public GraphPoint(Long timestamp, Double number) {
        this.timestamp = timestamp;
        this.number = number;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Double getNumber() {
        return number;
    }
}
