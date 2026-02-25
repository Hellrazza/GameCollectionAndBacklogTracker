import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Game(
        long id,
        String name,
        @JsonProperty("game_type") String gameType,
        double rating,
        @JsonProperty("rating_count") int totalRatings,
        @JsonProperty("first_release_date") long releaseDate,
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