package com.nawaf.capstone3.Client;

import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleFluidsResponse;
import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleMaintenanceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.time.Duration;

@Component
public class VehicleDatabaseClient {
    private final RestClient restClient;

    public VehicleDatabaseClient(
            RestClient.Builder builder,
            @Value("${vehicle-databases.base-url}") String baseUrl,
            @Value("${vehicle-databases.api-key}") String apiKey) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        // The shared builder is mutable: never leak this API's authentication to VIN decoding.
        this.restClient = builder.clone()
                .requestFactory(requestFactory)
                .baseUrl(baseUrl)
                .defaultHeader("x-authkey", apiKey)
                .build();
    }

    public VehicleMaintenanceResponse getMaintenance(String vin) {
        return restClient.get()
                .uri("/vehicle-maintenance/v4/{vin}", vin)
                .retrieve()
                .body(VehicleMaintenanceResponse.class);
    }

    public VehicleFluidsResponse getFluids(String vin) {
        return restClient.get()
                .uri("/fluid-specs/{vin}", vin)
                .retrieve()
                .body(VehicleFluidsResponse.class);
    }
}
