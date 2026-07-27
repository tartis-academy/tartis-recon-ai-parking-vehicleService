package com.tartis_recon_ai_parking.domain.vehicle.exception;


//Con la siguiente implementación, no es necesario separar la búsqueda por ID de la búsqueda por matrícula.
public class VehicleNotFoundException extends Exception {

    private String searchObjective; //ID o Matrícula (abierto a más filtros de búsqueda)
    private Object valueToSearch;   //Valor a buscar

    public VehicleNotFoundException(String searchObjective, Object valueToSearch) {
        super("Vehicle with '" + searchObjective + " = " + valueToSearch.toString() + "' couldn't be found.");
        this.searchObjective = searchObjective;
        this.valueToSearch = valueToSearch;
    }

    public String getsearchObjective(){
        return this.searchObjective;
    }

    public Object getId(){
        return this.valueToSearch;
    }
}
