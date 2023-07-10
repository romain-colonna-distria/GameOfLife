package fr.rom.gameoflife.controller.validator;

import fr.rom.gameoflife.property.GameProps;
import fr.rom.gameoflife.utils.Message;

import java.util.ArrayList;
import java.util.List;

public class InitFormValidator {

    final ArrayList<String> errors = new ArrayList<>();

    public List<String> validateNbColumns(String nbColumnsString){
        errors.clear();
        final double nbColumns = checkNumeric(nbColumnsString, errors);
        final int minNbColumns = GameProps.get().getMinNbColumns();
        if(nbColumns < minNbColumns) errors.add(Message.get("message.greaterEqual", minNbColumns));
        final int maxNbColumns = GameProps.get().getMaxNbColumns();
        if(nbColumns > maxNbColumns) errors.add(Message.get("message.lessEqual", maxNbColumns));

        return errors;
    }

    public List<String> validateNbRows(String nbRowsString){
        errors.clear();
        double nbRows = checkNumeric(nbRowsString, errors);
        if(nbRows < 1) errors.add(Message.get("message.greaterEqual", 1));
        if(nbRows > 1000) errors.add(Message.get("message.lessEqual", 1000));

        return errors;
    }

    public List<String> validateCellsHeight(String heightCellsString){
        errors.clear();
        double heightCells = checkNumeric(heightCellsString, errors);
        if(heightCells < 1) errors.add(Message.get("message.greaterEqual", 1));
        if(heightCells > 1000) errors.add(Message.get("message.lessEqual", 1000));

        return errors;
    }

    public List<String> validateCellsWidth(String widthCellsString){
        errors.clear();
        double widthCells = checkNumeric(widthCellsString, errors);
        if(widthCells < 1) errors.add(Message.get("message.greaterEqual", 1));
        if(widthCells > 1000) errors.add(Message.get("message.lessEqual", 1000));

        return errors;
    }

    public List<String> validateSVGPath(String svgPath){
        errors.clear();
        if(svgPath.length() < 1) errors.add(Message.get("message.cantBeEmpty"));
        if(!svgPath.matches("[MmZzLlHhVvCcSsQqTtAa0-9-,.\\s]*")) errors.add(Message.get("message.invalidCharacter"));
        if(svgPath.matches("^[0-9-,.]")) errors.add(Message.get("message.invalidStart"));
        if(svgPath.matches("[-,.]*")) errors.add(Message.get("message.invalidEnd"));

        return errors;
    }

    private double checkNumeric(final String str, List<String> errors) {
        if(!isNumeric(str)) errors.add(Message.get("message.invalidCharacter"));
        if(str.length() < 1) errors.add(Message.get("message.cantBeEmpty"));
        return Double.parseDouble(str);
    }

    private boolean isNumeric(final String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
