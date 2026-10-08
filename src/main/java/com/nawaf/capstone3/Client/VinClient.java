package com.nawaf.capstone3.Client;

import com.nawaf.capstone3.DTO.VinResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class VinClient {

    private final RestClient restClient;

    public VinClient(RestClient.Builder builder) {
        this.restClient = builder.clone()
                .baseUrl("https://vpic.nhtsa.dot.gov/api")
                .build();
    }

    public VinResponse decodeVin(String vin) {

        return restClient.get()
                .uri("/vehicles/DecodeVinValues/{vin}?format=json", vin)
                .retrieve()
                .body(VinResponse.class);
    }
}
