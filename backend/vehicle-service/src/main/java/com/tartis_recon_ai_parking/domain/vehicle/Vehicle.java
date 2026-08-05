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
    private Long version;


    // Vehicle es una entidad de dominio anemica por diseno: agrupar estos campos en un
    // parameter object no aporta invariantes nuevas y solo trasladaria el problema a otro
    // tipo. Se acepta el exceso de parametros (java:S107).
    @SuppressWarnings("java:S107")
    public static Vehicle create(VehicleType type, String plate, String brand, String model, String color,
        int numDoors, boolean hasSidecar, boolean active){
            return new Vehicle(UUID.randomUUID(), null, type, plate, brand, model, color, numDoors, hasSidecar, active);
    }

    @SuppressWarnings("java:S107")
    public static Vehicle reconstruct(UUID id, Long version, VehicleType type, String plate, String brand, String model, String color,
        int numDoors, boolean hasSidecar, boolean active){
            return withFields(id, version, type, plate, brand, model, color, numDoors, hasSidecar, active);
    }

    // Asigna campos sin pasar por validateData(): usado por reconstruct() y por activate()/deactivate(),
    // que no deben revalidar una entidad ya persistida solo por cambiar el flag active.
    @SuppressWarnings("java:S107")
    private static Vehicle withFields(UUID id, Long version, VehicleType type, String plate, String brand, String model, String color,
        int numDoors, boolean hasSidecar, boolean active){
            Vehicle v = new Vehicle();
            v.uniqueId = id;
            v.version = version;
            v.type = type;
            v.plate = plate;
            v.brand = brand;
            v.model = model;
            v.color = color;
            v.numDoors = numDoors;
            v.hasSidecar = hasSidecar;
            v.active = active;
            return v;
    }

    //Constructor privado sin argumentos, solo para uso interno de reconstruct
    private Vehicle(){}

    //Vehicle constructor
    @SuppressWarnings("java:S107")
    private Vehicle(UUID id, Long version, VehicleType type, String plate, String brand, String model, String color,
        int numDoors, boolean hasSidecar, boolean active){

            validateData(type, plate, brand, model, color, numDoors, hasSidecar);

            this.uniqueId = id;
            this.version = version;
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

        if(type == null) throw new InvalidVehicleException("VehicleType", null);
        if(type != VehicleType.MOTORBIKE && hasSidecar) throw new InvalidVehicleException("hasSideCar",true,"Cars do not have sidecar");

        //Cars can only have 2 or 4 doors.
        //Bikes cannot have doors. 
        if(numDoors < 0) throw new InvalidVehicleException("Number of doors", numDoors);
        if(type != VehicleType.MOTORBIKE && !(numDoors == 2 || numDoors == 4 || numDoors == 5)) 
            throw new InvalidVehicleException("Number of doors", numDoors,"Incorrect number of doors for a car");
        if(type == VehicleType.MOTORBIKE && numDoors > 0) throw new InvalidVehicleException("Number of doors", numDoors, "Incorrect number of doors for a motorbike");

        if(brand == null) throw new InvalidVehicleException("Brand", null);
        if(model == null) throw new InvalidVehicleException("Model", null);
        if(color == null) throw new InvalidVehicleException("Color", null);

        validPlate(plate);

        if(brand.isBlank()) throw new InvalidVehicleException("Brand", "empty ( )");
        if(model.isBlank()) throw new InvalidVehicleException("Model", "empty ( )");
        if(color.isBlank()) throw new InvalidVehicleException("Color", "empty ( )");
   
    }

    public static void validPlate(String plate) {

        if(plate == null) throw new InvalidVehicleException("Plate", null);
        if(plate.isBlank()) throw new InvalidVehicleException("Plate", "empty ( )");

        // Formato moderno (2000-actualidad): 4 digitos seguidos de 3 consonantes
        // Las letras validas son consonantes excluyendo Ñ y Q
        String modernPattern = "^\\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$";

        // Formato antiguo (1971-2000): codigo de provincia real, guion, 4-6 digitos, guion, 2 consonantes
        String oldPattern = "^(A|AB|AL|AV|B|BA|BI|BU|C|CA|CC|CE|CO|CR|CS|CU|GC|GI|GE|GR|GU|H|HU|J|L|LE|LO|LU|M|MA|ML|MU|NA|O|OR|OU|P|PM|PO|S|SA|SE|SG|SO|SS|T|TE|TF|TO|V|VA|VI|Z|ZA)-\\d{4,6}-[BCDFGHJKLMNPRSTVWXYZ]{2}$";

        //Lanza excepción si no cumple ninguno de los dos formatos.
        if (!plate.matches(modernPattern) && !plate.matches(oldPattern)) 
            throw new InvalidVehicleException("Plate", plate, "Invalid plate pattern: " + plate);

    }

    @SuppressWarnings("java:S107")
    public Vehicle update(VehicleType type, String plate, String brand, String model,
        String color, int numDoors, boolean hasSidecar, boolean active){
            return new Vehicle(this.uniqueId, this.version, type, plate, brand, model, color, numDoors, hasSidecar, active);
    }

    public Vehicle activate(){
        return withFields(this.uniqueId, this.version, this.type, this.plate, this.brand,
            this.model, this.color, this.numDoors, this.hasSidecar, true);
    }

    public Vehicle deactivate(){
        return withFields(this.uniqueId, this.version, this.type, this.plate, this.brand,
            this.model, this.color, this.numDoors, this.hasSidecar, false);
    }

    

    //=============== Getters ===============
    public UUID getUniqueId() {
        return uniqueId;
    }

    public Long getVersion() {
        return version;
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
