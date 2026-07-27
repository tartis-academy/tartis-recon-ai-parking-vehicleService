package com.tartis_recon_ai_parking.domain.vehicle.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class VehicleExceptionsTest {

    @Test
    void testInvalidVehicleException() {
        InvalidVehicleException ex1 = new InvalidVehicleException("color", "transparent", "Invalid color");
        assertEquals("color", ex1.getField());
        assertEquals("transparent", ex1.getInvalidValue());
        assertEquals("Invalid color", ex1.getMessage());

        InvalidVehicleException ex2 = new InvalidVehicleException("numDoors", -1);
        assertEquals("numDoors", ex2.getField());
        assertEquals(-1, ex2.getInvalidValue());
        assertEquals("Invalid vehicle data: numDoors can't be -1", ex2.getMessage());
    }

    @Test
    void testVehicleNotFoundException() {
        VehicleNotFoundException ex = new VehicleNotFoundException("plate", "9999XYZ");
        assertEquals("plate", ex.getSearchObjective());
        assertEquals("9999XYZ", ex.getId());
        assertEquals("Vehicle with 'plate = 9999XYZ' couldn't be found.", ex.getMessage());
    }

    @Test
    void testExistingVehicleException() {
        ExistingVehicleException ex = new ExistingVehicleException("1234ABC");
        assertEquals("1234ABC", ex.getPlate());
        assertEquals("There's already a vehicle with the specified plate : 1234ABC", ex.getMessage());
    }
}
