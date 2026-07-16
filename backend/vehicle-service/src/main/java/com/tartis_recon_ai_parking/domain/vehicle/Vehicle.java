package com.tartis_recon_ai_parking.domain.vehicle;

import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import java.util.UUID;

public class Vehicle{

    private UUID uniqueId = UUID.randomUUID();

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

            if(type == null) throw new InvalidVehicleException("Vehicle type is null.");
            if(type != VehicleType.MOTORBIKE && hasSideCar) throw new InvalidVehicleException("Cars do not have sidecar.");

            //Cars can only have 2 or 4 doors.
            //Bikes cannot have doors. 
            if(numDoors < 0) throw new InvalidVehicleException("Number of doors cannot be a negative number.");
            if(type != VehicleType.MOTORBIKE && !(numDoors == 2 || numDoors == 4 || numDoors == 5)) throw new InvalidVehicleException("Incorrect number of doors for a car.");
            if(type == VehicleType.MOTORBIKE && numDoors > 0) throw new InvalidVehicleException("Incorrect number of doors for a motorbike.");

            if(brand == null) throw new InvalidVehicleException("Vehicle brand is null.");
            if(model == null) throw new InvalidVehicleException("Vehicle model is null.");
            if(color == null) throw new InvalidVehicleException("Vehicle color is null.");
            if(plate == null) throw new InvalidVehicleException("Vehicle plate is null.");

            this.type = type;
            this.plate = plate;
            this.brand = brand;
            this.model = model;
            this.color = color;
            this.numDoors = numDoors;
            this.hasSideCar = hasSideCar;
            this.active = active;
 
    }


    //Getters
    public UUID getUniqueId() {
        return uniqueId;
    }

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
    
    public boolean getSideCar() {
        return hasSideCar;
    }
    
    public boolean isActive() {
        return active;
    }

    //Setters
    public void setUniqueId(UUID uniqueId){
        this.uniqueId = uniqueId;
    }

    public void setType(VehicleType type) throws InvalidVehicleException {
        if (type == null) {
            throw new InvalidVehicleException("Null vehicle type.");
        }
        this.type = type;
    }
    
    public void setPlate(String plate) throws InvalidVehicleException {
        if (plate == null) {
            throw new InvalidVehicleException("Null vehicle plate.");
        }
        this.plate = plate;
    }
    
    public void setBrand(String brand) throws InvalidVehicleException {
        if (brand == null) {
            throw new InvalidVehicleException("Null vehicle brand.");
        }
        this.brand = brand;
    }
    
    public void setModel(String model) throws InvalidVehicleException {
        if (model == null) {
            throw new InvalidVehicleException("Null vehicle model.");
        }
        this.model = model;
    }
    
    public void setColor(String color) throws InvalidVehicleException {
        if (color == null) {
            throw new InvalidVehicleException("Null vehicle color.");
        }
        this.color = color;
    }
    
    public void setNumDoors(int numDoors) {
        this.numDoors = numDoors;
    }
    
    public void setSideCar(boolean hasSideCar) throws InvalidVehicleException {
        if (this.type != VehicleType.MOTORBIKE && hasSideCar) {
            throw new InvalidVehicleException("Cars cannot have sidecar.");
        }
        this.hasSideCar = hasSideCar;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }

}