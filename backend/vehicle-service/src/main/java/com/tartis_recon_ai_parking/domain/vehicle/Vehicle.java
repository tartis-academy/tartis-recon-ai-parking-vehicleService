package com.tartis_recon_ai_parking.domain.vehicle.Vehicle;

import VehicleType;

public enum VehicleType{
    CAR, CAR_PMR, MOTORBIKE
}

public class Vehicle{

    private VehicleType type;
    private String plate;
    private String brand;
    private String model;
    private String color;
    private int numDoors;
    private boolean hasSideCar;
    private boolean active;

    //Default constructor
    public Vehicle(){}

    //Vehicle constructor
    public Vehicle(VehicleType type, String plate, String brand, String model, String color,
        int numDoors, boolean hasSideCar, boolean active){

            //#region Data check (Separate if-statements for legibility)
                if(type < CAR || type > MOTORBIKE) throw new InvalidVehicleException("Invalid vehicle type.");
                if(type != MOTORBIKE && hasSideCar) throw new InvalidVehicleException("Invalid vehicle data.");
                if(type == null || brand == null || model == null || color == null || plate == null) throw new InvalidVehicleException("Null vehicle data");
            //#endregion

            //#region Attribute initialization
                this.type = type;
                this.plate = plate;
                this.brand = brand;
                this.model = model;
                this.color = color;
                this.numDoors = numDoors;
                this.hasSideCar = hasSideCar;
                this.active = active;
            //#endregion 
    }


    //#region Getters
        public VehicleType getType() {
            return type;
        }
    
        public String getPlate() {
            return plate;
        }
    
        public String getBrand() {
            return brand;
        }
    
        public String getModel() {
            return model;
        }
    
        public String getColor() {
            return color;
        }
    
        public int getNumDoors() {
            return numDoors;
        }
    
        public boolean isHasSideCar() {
            return hasSideCar;
        }
    
        public boolean isActive() {
            return active;
        }
    //#endregion

    //#region Setters
        public void setType(VehicleType type) {
            if (type == null) {
                throw new InvalidVehicleException("Null vehicle data");
            }
            this.type = type;
        }
    
        public void setPlate(String plate) {
            if (plate == null) {
                throw new InvalidVehicleException("Null vehicle data");
            }
            this.plate = plate;
        }
    
        public void setBrand(String brand) {
            if (brand == null) {
                throw new InvalidVehicleException("Null vehicle data");
            }
            this.brand = brand;
        }
    
        public void setModel(String model) {
            if (model == null) {
                throw new InvalidVehicleException("Null vehicle data");
            }
            this.model = model;
        }
    
        public void setColor(String color) {
            if (color == null) {
                throw new InvalidVehicleException("Null vehicle data");
            }
            this.color = color;
        }
    
        public void setNumDoors(int numDoors) {
            this.numDoors = numDoors;
        }
    
        public void setHasSideCar(boolean hasSideCar) {
            if (this.type != VehicleType.MOTORBIKE && hasSideCar) {
                throw new InvalidVehicleException("Invalid vehicle data.");
            }
            this.hasSideCar = hasSideCar;
        }
    
        public void setActive(boolean active) {
            this.active = active;
        }
    //#endregion

}