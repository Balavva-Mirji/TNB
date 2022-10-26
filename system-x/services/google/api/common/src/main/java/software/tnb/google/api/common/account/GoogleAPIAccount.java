package software.tnb.google.api.common.account;

import software.tnb.common.account.Account;
import software.tnb.common.account.WithId;

public class GoogleAPIAccount implements Account, WithId {
    private String api_client_id;
    private String api_client_secret;
    private String api_refresh_token;
    private String email;

    @Override
    public String credentialsId() {
        return "google";
    }

    public String clientId() {
        return api_client_id;
    }

    public void setApi_client_id(String apiClientId) {
        this.api_client_id = apiClientId;
    }

    public String clientSecret() {
        return api_client_secret;
    }

    public void setApi_client_secret(String apiClientSecret) {
        this.api_client_secret = apiClientSecret;
    }

    public String refreshToken() {
        return api_refresh_token;
    }

    public void setApi_refresh_token(String api_refresh_token) {
        this.api_refresh_token = api_refresh_token;
    }

    public String email() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
