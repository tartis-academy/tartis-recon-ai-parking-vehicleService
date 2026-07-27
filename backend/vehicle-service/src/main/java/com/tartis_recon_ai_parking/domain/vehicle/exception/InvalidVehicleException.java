package com.tartis_recon_ai_parking.domain.vehicle.exception;

//NOTA: Debido al gran número de parámetros, se ha optado por
//simplemente concretar mediante un string el campo erróneo y
//el valor que ha lanzado la excepción. Así resulta más fácil manejar
//la excepción en código.
public class InvalidVehicleException extends RuntimeException {

    private final String field;
    private final Object invalidValue;

    public InvalidVehicleException(String field, Object invalidValue, String msj) {
        super(msj);
        this.field = field;
        this.invalidValue = invalidValue;
    }

    public InvalidVehicleException(String field, Object invalidValue) {
        super("Invalid vehicle data: " + field + " can't be " + String.valueOf(invalidValue));
        this.field = field;
        this.invalidValue = invalidValue;
    }

    public String getField() {
        return field;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}