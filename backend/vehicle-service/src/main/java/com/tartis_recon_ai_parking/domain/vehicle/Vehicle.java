package com.tartis_recon_ai_parking.domain.vehicle;

import com.tartis_recon_ai_parking.domain.vehicle.exception.InvalidVehicleException;
import java.util.UUID;

public class Vehicle{

    private UUID uniqueId;

    private VehicleType type;
    private String plate;
    private String brand;
    private String model;
    private String color;
    private int numDoors;
    private boolean hasSidecar;
    private boolean active;

    public static Vehicle create(VehicleType type, String plate, String brand, String model, String color,
        int numDoors, boolean hasSidecar, boolean active){
            return new Vehicle(UUID.randomUUID(), type, plate, brand, model, color, numDoors, hasSidecar, active);
    }

    public static Vehicle reconstruct(UUID id, VehicleType type, String plate, String brand, String model, String color,
        int numDoors, boolean hasSidecar, boolean active){
            return new Vehicle(id, type, plate, brand, model, color, numDoors, hasSidecar, active);
    }

    //Vehicle constructor
    private Vehicle(UUID id, VehicleType type, String plate, String brand, String model, String color,
        int numDoors, boolean hasSidecar, boolean active){

            validateData(type, plate, brand, model, color, numDoors, hasSidecar);

            this.uniqueId = id;
            this.type = type;
            this.plate = plate;
            this.brand = brand;
            this.model = model;
            this.color = color;
            this.numDoors = numDoors;
            this.hasSidecar = hasSidecar;
            this.active = active;

    }

    private void validateData(VehicleType type, String plate, String brand, String model, String color, int numDoors, boolean hasSidecar){

        if(type == null) throw new InvalidVehicleException("Vehicle type is null");
        if(type != VehicleType.MOTORBIKE && hasSidecar) throw new InvalidVehicleException("Cars do not have sidecar");

        //Cars can only have 2 or 4 doors.
        //Bikes cannot have doors. 
        if(numDoors < 0) throw new InvalidVehicleException("Number of doors cannot be a negative number");
        if(type != VehicleType.MOTORBIKE && !(numDoors == 2 || numDoors == 4 || numDoors == 5)) throw new InvalidVehicleException("Incorrect number of doors for a car");
        if(type == VehicleType.MOTORBIKE && numDoors > 0) throw new InvalidVehicleException("Incorrect number of doors for a motorbike");

        if(brand == null) throw new InvalidVehicleException("Vehicle brand is null");
        if(model == null) throw new InvalidVehicleException("Vehicle model is null");
        if(color == null) throw new InvalidVehicleException("Vehicle color is null");
        if(plate == null) throw new InvalidVehicleException("Vehicle plate is null");
   
    }

    public Vehicle update(VehicleType type, String plate, String brand, String model, 
        String color, int numDoors, boolean hasSidecar, boolean active){
            return new Vehicle(this.uniqueId, type, plate, brand, model, color, numDoors, hasSidecar, active);
    }

    public Vehicle activate(){
        return new Vehicle(this.uniqueId, this.type, this.plate, this.brand, this.model, 
            this.color, this.numDoors, this.hasSidecar, true);
    }

    public Vehicle deactivate(){
        return new Vehicle(this.uniqueId, this.type, this.plate, this.brand, this.model, 
            this.color, this.numDoors, this.hasSidecar, false);
    }

    //=============== Getters ===============
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
    
    public boolean getHasSidecar() {
        return hasSidecar;
    }
    
    public boolean isActive() {
        return active;
    }

}