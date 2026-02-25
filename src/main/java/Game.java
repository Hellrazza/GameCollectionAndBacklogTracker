import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Game(
        long id,
        String name,
        @JsonProperty("game_type") String gameType,
        double rating,
        @JsonProperty("rating_count") int totalRatings,
        @JsonProperty("first_release_date") long releaseDate,
        @JsonProperty("platforms") List<Platform> platforms,
        @JsonProperty("cover") CoverData cover
) {
    public String coverUrl() {
        if (cover != null && cover.url() != null) {
            return "https:" + cover.url();
        }
        return null;
    }

    public String formattedReleaseDate() {
        if (releaseDate == 0) {return "Unknown";}

        return Instant.ofEpochSecond(releaseDate).
                atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String platformNames() {
        if(platforms == null || platforms.isEmpty()) {
            return "Unknown";
        }

        return platforms.stream()
                .map(Platform::name)
                .collect(Collectors.joining(", "));
    }

    public LocalDate releaseLocalDate() {
        if (releaseDate == 0) {
            return null;
        }
        return Instant.ofEpochSecond(releaseDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record  CoverData(String url){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Platform(
            long id,
            String name
    ) {}

}