package com.tartis_recon_ai_parking.infrastructure.vehicle.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.vehicle.VehicleType;
import jakarta.persistence.*;

// Representa la tabla "vehicles" en el sistema de base de datos relacional.
@Entity
@Table(name = "vehicles")
public class VehicleEntity {

    // clave primaria (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uniqueId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Column(nullable = false, unique = true)
    private String plate;

    private String brand;
    private String model;
    private String color;
    private Integer numDoors;
    private Boolean hasSideCar;
    
    @Column(nullable = false)
    private Boolean active;


    public VehicleEntity() {}

    public Long getUniqueId() { return uniqueId; }
    public void setUniqueId(Long uniqueId) { this.uniqueId = uniqueId; }

    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getNumDoors() { return numDoors; }
    public void setNumDoors(Integer numDoors) { this.numDoors = numDoors; }

    public Boolean getHasSideCar() { return hasSideCar; }
    public void setHasSideCar(Boolean hasSideCar) { this.hasSideCar = hasSideCar; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
