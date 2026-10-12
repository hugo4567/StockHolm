package com.stockholm.model;

public enum ProductStatus {
    EN_ATTENTE,
    EN_COURS,
    REPARE,
    REBUT;

    @Override
    public String toString() {
        return switch (this) {
            case EN_ATTENTE -> "En attente";
            case EN_COURS -> "En cours";
            case REPARE -> "Réparé";
            case REBUT -> "Rebut";
        };
    }
}
