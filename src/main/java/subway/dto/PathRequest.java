package subway.dto;

public final class PathRequest {
    private Long upStationId;
    private Long downStationID;
    private Integer distance;

    public PathRequest() {
    }

    public PathRequest(final Long upStationId, final Long downStationID, final Integer distance) {
        this.upStationId = upStationId;
        this.downStationID = downStationID;
        this.distance = distance;
    }
}
