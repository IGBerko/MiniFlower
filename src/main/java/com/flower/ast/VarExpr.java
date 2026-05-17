package com.flower.ast;

public class VarExpr implements Expression {

    private final String name;

    public VarExpr(String name) {
        this.name = name;
    }

    @Override
    public String print() {
        return name;
    }
}
