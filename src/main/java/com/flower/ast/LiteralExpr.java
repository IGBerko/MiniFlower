package com.flower.ast;

public class LiteralExpr implements Expression {

    private final Object value;

    public LiteralExpr(Object value) {
        this.value = value;
    }

    @Override
    public String print() {
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return String.valueOf(value);
    }
}
