package com.nawaf.capstone3.DTO;

import lombok.Data;

import java.util.List;

@Data
public class VinResponse {

    @com.fasterxml.jackson.annotation.JsonProperty("Results")
    private List<VinResult> results;
}
