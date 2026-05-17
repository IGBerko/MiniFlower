package com.flower.ast;

public class BinaryExpr implements Expression {

    private final Expression left;
    private final Expression right;
    private final String operator;

    public BinaryExpr(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public String print() {
        return "(" + left.print() + " " + operator + " " + right.print() + ")";
    }
}
