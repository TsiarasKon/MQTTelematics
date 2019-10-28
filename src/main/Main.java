package main;

import sumo_data_handler.SumoXml2csv;

public class Main {

    public static void main(String[] args) {

        SumoXml2csv sumo = new SumoXml2csv();
        sumo.xml2csvConvert("res/all_vehicles.xml", "out/");

    }
}
