package com.stockholm.model;

public enum BatteryCellState {
    SAINE,
    FAIBLE,
    HS,
    INCONNUE;

    @Override
    public String toString() {
        return switch (this) {
            case SAINE -> "Saine";
            case FAIBLE -> "Faible";
            case HS -> "Hors service";
            case INCONNUE -> "Inconnue";
        };
    }
}
