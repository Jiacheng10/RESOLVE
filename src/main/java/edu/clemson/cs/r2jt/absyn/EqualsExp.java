/**
 * EqualsExp.java
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
import edu.clemson.cs.r2jt.type.BooleanType;
import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;

public class EqualsExp extends AbstractFunctionExp {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final int EQUAL = 1;
    public static final int NOT_EQUAL = 2;

    // ===========================================================
    // Variables
    // ===========================================================

    /** The location member. */
    private Location location;

    /** The left member. */
    private Exp left;

    /** The operator member. */
    private int operator;

    /** The right member. */
    private Exp right;

    // ===========================================================
    // Constructors
    // ===========================================================

    public EqualsExp() {};

    public EqualsExp(Location location, Exp left, int operator, Exp right) {
        this.location = location;
        this.left = left;
        this.operator = operator;
        this.right = right;
        super.bType = BooleanType.INSTANCE;
    }

    public boolean equivalent(Exp e) {
        boolean retval = e instanceof EqualsExp;

        if (retval) {
            EqualsExp eAsEquals = (EqualsExp) e;
            retval =
                    (operator == eAsEquals.operator)
                            && (left.equivalent(eAsEquals.left))
                            && (right.equivalent(eAsEquals.right));
        }

        return retval;
    }

    // ===========================================================
    // Accessor Methods
    // ===========================================================

    // -----------------------------------------------------------
    // Get Methods
    // -----------------------------------------------------------

    @Override
    public int getQuantification() {
        return VarExp.NONE;
    }

    /** Returns the value of the location variable. */
    @Override
    public Location getLocation() {
        return location;
    }

    /** Returns the value of the left variable. */
    public Exp getLeft() {
        return left;
    }

    /** Returns the value of the operator variable. */
    public int getOperator() {
        return operator;
    }

    /** Returns the value of the right variable. */
    public Exp getRight() {
        return right;
    }

    // -----------------------------------------------------------
    // Set Methods
    // -----------------------------------------------------------

    /** Sets the location variable to the specified value. */
    public void setLocation(Location location) {
        this.location = location;
    }

    /** Sets the left variable to the specified value. */
    public void setLeft(Exp left) {
        this.left = left;
    }

    /** Sets the operator variable to the specified value. */
    public void setOperator(int operator) {
        this.operator = operator;
    }

    /** Sets the right variable to the specified value. */
    public void setRight(Exp right) {
        this.right = right;
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    public String getOperatorAsString() {
        String retval;

        switch (operator) {
        case EQUAL:
            retval = "=";
            break;
        case NOT_EQUAL:
            retval = "/=";
            break;
        default:
            throw new RuntimeException("Invalid operator code.");
        }

        return retval;
    }

    @Override
    public PosSymbol getOperatorAsPosSymbol() {
        return new PosSymbol(location, Symbol.symbol(getOperatorAsString()));
    }

    public Exp substituteChildren(java.util.Map<Exp, Exp> substitutions) {
        Exp retval =
                new EqualsExp(location, substitute(left, substitutions),
                        operator, substitute(right, substitutions));

        retval.setType(type);

        return retval;
    }

    /** Accepts a ResolveConceptualVisitor. */
    public void accept(ResolveConceptualVisitor v) {
        v.visitEqualsExp(this);
    }

    /** Accepts a TypeResolutionVisitor. */
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getEqualsExpType(this);
    }

    /** Returns a formatted text string of this class. */
    public String asString(int indent, int increment) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);
        sb.append("EqualsExp\n");

        if (left != null) {
            sb.append(left.asString(indent + increment, increment));
        }

        printSpace(indent + increment, sb);
        sb.append(printConstant(operator) + "\n");

        if (right != null) {
            sb.append(right.asString(indent + increment, increment));
        }

        return sb.toString();
    }

    /** Returns a formatted text string of this class. */
    public String toString(int indent) {
        //Environment   env	= Environment.getInstance();
        //if(env.isabelle()){return toIsabelleString(indent);};    	

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);

        if (left != null) {
            sb.append(left.toString(0));
        }

        if (operator == 1)
            sb.append(" = ");
        else {
            sb.append(" /= ");
        }

        if (right != null) {
            sb.append(right.toString(0));
        }

        return sb.toString();
    }

    /** Returns a formatted text string of this class. */
    public String toIsabelleString(int indent) {

        StringBuffer sb = new StringBuffer();

        printSpace(indent, sb);

        if (left != null) {
            sb.append(left.toString(0));
        }

        if (operator == 1)
            sb.append(" = ");
        else
            sb.append(" ~= ");

        if (right != null) {
            sb.append(right.toString(0));
        }

        return sb.toString();
    }

    public Exp replace(Exp old, Exp replacement) {
        if (!(old instanceof EqualsExp)) {
            EqualsExp newExp = new EqualsExp();
            newExp.setLeft((Exp) Exp.clone(left));
            newExp.setRight((Exp) Exp.clone(right));
            newExp.setOperator(this.operator);
            newExp.setType(type);
            newExp.setLocation(this.location);
            Exp lft = Exp.replace(left, old, replacement);
            Exp rgt = Exp.replace(right, old, replacement);
            if (lft != null)
                newExp.setLeft(lft);
            if (rgt != null)
                newExp.setRight(rgt);
            return newExp;
        }
        else {}
        //
        return this;
    }

    /** Returns true if the variable is found in any sub expression   
        of this one. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        Boolean found = false;
        if (left != null) {
            found = left.containsVar(varName, IsOldExp);
        }
        if (!found && right != null) {
            found = right.containsVar(varName, IsOldExp);
        }
        return found;
    }

    private String printConstant(int k) {
        StringBuffer sb = new StringBuffer();
        switch (k) {
        case 1:
            sb.append("EQUAL");
            break;
        case 2:
            sb.append("NOT_EQUAL");
            break;
        default:
            sb.append(k);
        }
        return sb.toString();
    }

    public Object clone() {
        EqualsExp clone = new EqualsExp();
        clone.setLeft((Exp) Exp.copy(this.getLeft()));
        clone.setRight((Exp) Exp.copy(this.getRight()));
        if (this.location != null)
            clone.setLocation((Location) this.getLocation().clone());
        clone.setOperator(this.getOperator());
        clone.setType(type);
        return clone;
    }

    public List<Exp> getSubExpressions() {
        List<Exp> list = new List<Exp>();
        list.add(left);
        list.add(right);
        return list;
    }

    public void setSubExpression(int index, Exp e) {
        switch (index) {
        case 0:
            left = e;
            break;
        case 1:
            right = e;
            break;
        }
    }

    public boolean shallowCompare(Exp e2) {
        if (!(e2 instanceof EqualsExp)) {
            return false;
        }
        if (operator != ((EqualsExp) e2).getOperator()) {
            return false;
        }
        return true;
    }

    public Exp remember() {
        if (left instanceof OldExp)
            this.setLeft(((OldExp) (left)).getExp());
        if (right instanceof OldExp)
            this.setRight(((OldExp) (right)).getExp());
        if (left != null)
            left = left.remember();
        if (left != null)
            right = right.remember();

        return this;
    }

    public void prettyPrint() {
        left.prettyPrint();
        if (operator == EQUAL)
            System.out.print(" = ");
        else
            System.out.print(" /= ");
        right.prettyPrint();
    }

    public Exp copy() {
        Exp retval;

        Exp newLeft = Exp.copy(left);
        Exp newRight = Exp.copy(right);
        int newOperator = operator;
        retval = new EqualsExp(null, newLeft, newOperator, newRight);
        retval.setType(type);

        return retval;
    }

    public Exp simplify() {
        Exp simplified = this;
        if (left.equals(right)) {
            simplified = getTrueVarExp(this.myMathType.getTypeGraph());
        }
        return simplified;
    }

    /* Commented out because it is not used locally.
     * - YS
    private PosSymbol createPosSymbol(String name){
    	PosSymbol posSym = new PosSymbol();
    	posSym.setSymbol(Symbol.symbol(name));
    	return posSym; 	
    }
     */

    public boolean equals(Exp exp) {
        if (exp instanceof EqualsExp)
            if (this.right.equals(((EqualsExp) exp).getRight())
                    && this.left.equals(((EqualsExp) exp).getLeft())
                    && this.operator == ((EqualsExp) exp).getOperator()) {
                return true;
            }
        return false;
    }

    @Override
    public PosSymbol getQualifier() {
        return null;
    }

}
