/**
 * PrefixExp.java
 * ---------------------------------
 * Copyright (c) 2014
 * RESOLVE Software Research Group
 * School of Computing
 * Clemson University
 * All rights reserved.
 * ---------------------------------
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;

public class PrefixExp extends AbstractFunctionExp {

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The symbol member. */
    private PosSymbol symbol;

    /** The argument member. */
    private Exp argument;

    // ===========================================================
    // Constructors
    // ===========================================================

    public PrefixExp() {};

    public PrefixExp(Location location, PosSymbol symbol, Exp argument) {
        this.location = location;
        this.symbol = symbol;
        this.argument = argument;
    }

    // special constructor to use when we can determine the statement return 
    // type while building the symbol table in RBuilder.g
    public PrefixExp(Location location, PosSymbol symbol, Exp argument,
            Type bType) {
        this.location = location;
        this.symbol = symbol;
        this.argument = argument;
        super.bType = bType;
    }

    public Object clone() {
        PrefixExp clone = new PrefixExp();
        clone.setLocation(this.getLocation());
        clone.symbol = this.symbol.copy();
        if (this.argument != null) {
            clone.argument = (Exp) Exp.clone(this.argument);
        }

        clone.setType(this.bType);
        return clone;
    }

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        PrefixExp retval =
                new PrefixExp(location, symbol, substitute(argument,
                        substitutions));

        retval.setType(type);

        return retval;
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    /** Returns the value of the location variable. */
    @Override
    public Location getLocation() {
        return location;
    }

    /** Returns the value of the symbol variable. */
    public PosSymbol getSymbol() {
        return symbol;
    }

    /** Returns the value of the argument variable. */
    public Exp getArgument() {
        return argument;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the symbol variable to the specified value. */
    public void setSymbol(PosSymbol symbol) {
        this.symbol = symbol;
    }

    /** Sets the argument variable to the specified value. */
    public void setArgument(Exp argument) {
        this.argument = argument;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    @Override
    public int getQuantification() {
        return VarExp.NONE;
    }

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitPrefixExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getPrefixExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("PrefixExp\n");

        if (symbol != null) {
            sb.append(symbol.asString(indent + increment, increment));
        }

        if (argument != null) {
            sb.append(argument.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    public String toString(int indent) {
        StringBuffer sb = new StringBuffer();
        printSpace(indent, sb);
        if (symbol != null)
            sb.append(symbol.getName().toString());
        if (argument != null)
            sb.append("(" + argument.toString(0) + ")");
        return sb.toString();
    }

    public String toIsabelleString(int indent) {
        StringBuffer sb = new StringBuffer();
        printSpace(indent, sb);
        if (symbol != null)
            sb.append(symbol.getName().toString());
        if (argument != null)
            sb.append("(" + argument.toString(0) + ")");
        return sb.toString();
    }

    /** Returns true if the variable is found in any sub expression   
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        if (argument != null) {
            return argument.containsVar(varName, IsOldExp);
        }
        return false;
    }

    public List<Exp> getSubExpressions() {
        List<Exp> list = new List<Exp>();
        list.add(argument);
        return list;
    }

    public void setSubExpression(int index, Exp e) {
        argument = e;
    }

    public boolean shallowCompare(Exp e2) {
        if (!(e2 instanceof PrefixExp)) {
            return false;
        }
        if (!(symbol.equals(((PrefixExp) e2).getSymbol().getName()))) {
            return false;
        }
        return true;
    }

    public boolean equivalent(Exp e) {
        boolean retval = (e instanceof PrefixExp);

        if (retval) {
            PrefixExp eAsPrefixExp = (PrefixExp) e;
            retval =
                    Exp.posSymbolEquivalent(symbol, eAsPrefixExp.symbol)
                            && Exp.equivalent(argument, eAsPrefixExp.argument);
        }

        return retval;
    }

    public void prettyPrint() {
        System.out.print(symbol.getName() + "(");
        argument.prettyPrint();
        System.out.print(")");
    }

    public Exp copy() {
        PrefixExp retval;

        PosSymbol newSymbol = symbol.copy();
        Exp newArgument = Exp.copy(argument);

        retval = new PrefixExp(null, newSymbol, newArgument);
        retval.setType(type);
        return retval;
    }

    public Exp replace(Exp old, Exp replacement) {
        if (!(old instanceof PrefixExp)) {
            if (this.argument != null) {
                Exp newArgument = Exp.replace(argument, old, replacement);
                if (newArgument != null) {
                    this.setArgument(newArgument);
                }
            }
            if (this.symbol != null && old instanceof VarExp)
                if (symbol.toString().equals(
                        ((VarExp) old).getName().toString())) {
                    if (replacement instanceof VarExp)
                        symbol = ((VarExp) replacement).getName();
                }
        }
        else {}
        //
        return this;
    }

    public Exp remember() {
        if (argument instanceof OldExp)
            this.setArgument(((OldExp) (argument)).getExp());
        else {
            if (argument != null) {
                argument = argument.remember();
            }
        }
        return this;
    }

    public Exp simplify() {
        if (argument instanceof EqualsExp) {
            if (((EqualsExp) argument).getOperator() == EqualsExp.EQUAL)
                ((EqualsExp) argument).setOperator(EqualsExp.NOT_EQUAL);
            else
                ((EqualsExp) argument).setOperator(EqualsExp.EQUAL);
            return argument;
        }
        else
            return this;
    }

    @Override
    public String getOperatorAsString() {
        return this.symbol.getName();
    }

    @Override
    public PosSymbol getOperatorAsPosSymbol() {
        return this.symbol;
    }

    @Override
    public PosSymbol getQualifier() {
        return null;
    }
}
