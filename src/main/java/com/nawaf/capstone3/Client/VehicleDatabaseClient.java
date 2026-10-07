package com.nawaf.capstone3.Client;

import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleFluidsResponse;
import com.nawaf.capstone3.DTO.VehicleDatabase.VehicleMaintenanceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class VehicleDatabaseClient {
    private final RestClient restClient;

    public VehicleDatabaseClient(
            RestClient.Builder builder,
            @Value("${vehicle-databases.base-url}") String baseUrl,
            @Value("${vehicle-databases.api-key}") String apiKey) {

        this.restClient = builder
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