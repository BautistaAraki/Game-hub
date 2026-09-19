package com.gamehub.gamehub.integration.igdb;

import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

final class IgdbHttpClients {
    private IgdbHttpClients() { }

    static RestClient.Builder builder() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().requestFactory(factory);
    }
}
