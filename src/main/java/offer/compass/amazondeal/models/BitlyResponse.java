package offer.compass.amazondeal.models;

import lombok.Data;

import java.util.Date;

@Data
public class BitlyResponse {
    private Date createdAt;
    private String id;
    private String link;
    private String long_url;
    private boolean archived;
}
