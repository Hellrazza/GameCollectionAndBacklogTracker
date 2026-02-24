import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cover {

    private String url;

    public String getUrl() {
        return url;
    }
}
