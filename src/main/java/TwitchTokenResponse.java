import com.fasterxml.jackson.annotation.JsonProperty;

public class TwitchTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    public String getAccessToken() {
        return accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

}
