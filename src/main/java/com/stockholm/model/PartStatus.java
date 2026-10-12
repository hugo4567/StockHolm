package com.stockholm.model;

public enum PartStatus {
    EN_ATTENTE,
    EN_COURS,
    INSTALLEE,
    REBUT;

    @Override
    public String toString() {
        return switch (this) {
            case EN_ATTENTE -> "En attente";
            case EN_COURS -> "En cours";
            case INSTALLEE -> "Installée";
            case REBUT -> "Rebut";
        };
    }
}
