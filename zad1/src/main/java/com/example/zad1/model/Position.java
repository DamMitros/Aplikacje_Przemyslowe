package com.example.zad1.model;

public enum Position {
    PREZES(25000,1),
    WICEPREZES(18000,2),
    MANAGER(12000,3),
    PROGRAMISTA(8000,4),
    STAZYSTA(3000,5);

    private final int baseSalary;
    private final int baseHierarchy;
    
    Position(int baseSalary,int baseHierarchy) {
        this.baseSalary = baseSalary;
        this.baseHierarchy = baseHierarchy;
    }

    public int getSalary(){
        return baseSalary;
    }

    public int getHierarchy(){
        return baseHierarchy;
    }
}
