package offer.compass.amazondeal.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BitlyRequest {
    private String domain = "bit.ly";
    @JsonProperty(value = "long_url")
    private String longUrl;
}
