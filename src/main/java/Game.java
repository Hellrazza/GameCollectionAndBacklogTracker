import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Game(
        long id,
        String name,
        @JsonProperty("cover") CoverData cover
) {
    public String coverUrl() {
        if (cover != null && cover.url() != null) {
            return "https:" + cover.url();
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record  CoverData(String url){}
}