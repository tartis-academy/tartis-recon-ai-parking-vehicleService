package com.tartis_recon_ai_parking.domain.vehicle;

import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;

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
        int numDoors, boolean hasSideCar, boolean active) throws InvalidVehicleException{

            //#region Data check (Separate if-statements for legibility)
                if(type != VehicleType.CAR && type != VehicleType.MOTORBIKE && type != VehicleType.CAR_PMR) throw new InvalidVehicleException("Invalid vehicle type.");
                if(type != VehicleType.MOTORBIKE && hasSideCar) throw new InvalidVehicleException("Invalid vehicle data.");
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
        public void setType(VehicleType type) throws InvalidVehicleException {
            if (type == null) {
                throw new InvalidVehicleException("Null vehicle data");
            }
            this.type = type;
        }
    
        public void setPlate(String plate) throws InvalidVehicleException {
            if (plate == null) {
                throw new InvalidVehicleException("Null vehicle data");
            }
            this.plate = plate;
        }
    
        public void setBrand(String brand) throws InvalidVehicleException {
            if (brand == null) {
                throw new InvalidVehicleException("Null vehicle data");
            }
            this.brand = brand;
        }
    
        public void setModel(String model) throws InvalidVehicleException {
            if (model == null) {
                throw new InvalidVehicleException("Null vehicle data");
            }
            this.model = model;
        }
    
        public void setColor(String color) throws InvalidVehicleException {
            if (color == null) {
                throw new InvalidVehicleException("Null vehicle data");
            }
            this.color = color;
        }
    
        public void setNumDoors(int numDoors) {
            this.numDoors = numDoors;
        }
    
        public void setHasSideCar(boolean hasSideCar) throws InvalidVehicleException {
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