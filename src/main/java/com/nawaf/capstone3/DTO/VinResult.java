package com.nawaf.capstone3.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VinResult {

    @JsonProperty("Make")
    private String make;

    @JsonProperty("Model")
    private String model;

    @JsonProperty("ModelYear")
    private String modelYear;

    @JsonProperty("DisplacementL")
    private String displacementL;

    @JsonProperty("EngineCylinders")
    private String engineCylinders;

    @JsonProperty("FuelTypePrimary")
    private String fuelType;

    @JsonProperty("VIN")
    private String vin;

    @JsonProperty("ErrorCode")
    private String errorCode;

    @JsonProperty("ErrorText")
    private String errorText;
}