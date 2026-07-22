package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.input.rest.dto.response;

import java.util.UUID;

public class VehicleResponse {

    private UUID uniqueId;
    private String type;
    private String plate;
    private String brand;
    private String model;
    private String color;
    private Integer numDoors;
    private Boolean hasSidecar;
    private Boolean active;

    public VehicleResponse() {
    }

    public VehicleResponse(UUID uniqueId, String type, String plate, String brand, String model, String color,
            Integer numDoors, Boolean hasSidecar, Boolean active) {
        this.uniqueId = uniqueId;
        this.type = type;
        this.plate = plate;
        this.brand = brand;
        this.model = model;
        this.color = color;
        this.numDoors = numDoors;
        this.hasSidecar = hasSidecar;
        this.active = active;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getNumDoors() {
        return numDoors;
    }

    public void setNumDoors(Integer numDoors) {
        this.numDoors = numDoors;
    }

    public Boolean getHasSidecar() {
        return hasSidecar;
    }

    public void setHasSidecar(Boolean hasSidecar) {
        this.hasSidecar = hasSidecar;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
