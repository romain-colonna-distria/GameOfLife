package fr.rom.gameoflife.utils.Validation;

import java.util.ArrayList;

public class InitFormValidator {

    public ArrayList<String> validateNbColumns(String nbColumsString){
        ArrayList<String> errors = new ArrayList<>();

        try {
            Double.parseDouble(nbColumsString);
        } catch (NumberFormatException e) {
            errors.add("Contient un caractère invalide.");
            return errors;
        }
        if(nbColumsString.length() < 1) errors.add("Ne peux pas être vide");

        double nbColumns = Double.parseDouble(nbColumsString);
        if(nbColumns < 1) errors.add("Doit être supérieur ou égale a 1.");
        if(nbColumns > 1000) errors.add("Doit être inférieur ou égale 1000.");

        return errors;
    }

    public ArrayList<String> validateNbRows(String nbRowsString){
        ArrayList<String> errors = new ArrayList<>();

        try {
            Double.parseDouble(nbRowsString);
        } catch (NumberFormatException e) {
            errors.add("Contient un caractère invalide.");
            return errors;
        }
        if(nbRowsString.length() < 1) errors.add("Ne peux pas être vide");

        double nbRows = Double.parseDouble(nbRowsString);
        if(nbRows < 1) errors.add("Doit être supérieur ou égale a 1.");
        if(nbRows > 1000) errors.add("Doit être inférieur ou égale 1000.");

        return errors;
    }

    public ArrayList<String> validateCellsHeight(String heightCellsString){
        ArrayList<String> errors = new ArrayList<>();

        try {
            Double.parseDouble(heightCellsString);
        } catch (NumberFormatException e) {
            errors.add("Contient un caractère invalide.");
            return errors;
        }
        if(heightCellsString.length() < 1) errors.add("Ne peux pas être vide");

        double heightCells = Double.parseDouble(heightCellsString);
        if(heightCells < 1) errors.add("Doit être supérieur ou égale a 1.");
        if(heightCells > 1000) errors.add("Doit être inférieur ou égale 1000.");

        return errors;
    }

    public ArrayList<String> validateCellsWidth(String widthCellsString){
        ArrayList<String> errors = new ArrayList<>();

        try {
            Double.parseDouble(widthCellsString);
        } catch (NumberFormatException e) {
            errors.add("Contient un caractère invalide.");
            return errors;
        }
        if(widthCellsString.length() < 1) errors.add("Ne peux pas être vide");

        double widthCells = Double.parseDouble(widthCellsString);
        if(widthCells < 1) errors.add("Doit être supérieur ou égale a 1.");
        if(widthCells > 1000) errors.add("Doit être inférieur ou égale 1000.");

        return errors;
    }

    public ArrayList<String> validateSVGPath(String svgPath){
        ArrayList<String> errors = new ArrayList<>();

        if(svgPath.length() < 1) errors.add("Ne peux pas être vide");
        if(!svgPath.matches("[MmZzLlHhVvCcSsQqTtAa0-9-,.\\s]")) errors.add("Contient un caractère invalide.");
        if(svgPath.matches("[0-9-,.]")) errors.add("Début invalides");
        if(svgPath.matches("[-,.]*")) errors.add("Fin invalide.");

        return errors;
    }
}
