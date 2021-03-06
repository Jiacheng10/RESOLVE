/**
 * AssertiveCode.java
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
package edu.clemson.cs.r2jt.vcgeneration;

/*
 * Libraries
 */
import edu.clemson.cs.r2jt.absyn.*;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.init.CompileEnvironment;

import java.util.*;

/**
 * TODO: Write a description of this module
 */
public class AssertiveCode {

    // ===========================================================
    // Global Variables 
    // ===========================================================

    /**
     * <p>Our current final confirm statement.</p>
     */
    private Exp myConfirm;

    /**
     * <p>The list of free variables.</p>
     */
    private List<Exp> myFreeVars;

    /**
     * <p>List of verification statements that we
     * need to apply proof rules to./p>
     */
    private List<VerificationStatement> myVerificationStmtList;

    // ===========================================================
    // Constructors
    // ===========================================================

    public AssertiveCode(CompileEnvironment env) {
        myConfirm = Exp.getTrueVarExp(env.getTypeGraph());
        myFreeVars = new ArrayList<Exp>();
        myVerificationStmtList = new ArrayList<VerificationStatement>();
    }

    // ===========================================================
    // Public Methods
    // ===========================================================

    /**
     * <p>Add the <code>Exp</code> containing a new assumes
     * clause.</p>
     *
     * @param e The corresponding assume <code>Exp</code>.
     */
    public void addAssume(Exp e) {
        // Creates a new AssumeStmt
        AssumeStmt assume = new AssumeStmt();
        assume.setAssertion(e);

        // Adds the assume to our list of verification statements
        addCode(assume);
    }

    /**
     * <p>Add a new <code>VerificationStatement</code> to the list</p>
     *
     * @param stmt The corresponding <code>Statement</code>.
     */
    public void addCode(Statement stmt) {
        myVerificationStmtList.add(new VerificationStatement(
                VerificationStatement.CODE, stmt));
    }

    /**
     * <p>Add the <code>Exp</code> containing a new confirm
     * clause.</p>
     *
     * @param e The corresponding confirm <code>Exp</code>.
     */
    public void addConfirm(Exp e) {
        // Creates a new ConfirmStmt
        ConfirmStmt confirm = new ConfirmStmt();
        confirm.setAssertion(e);

        // Adds the confirm to our list of verification statements
        addCode(confirm);
    }

    /**
     * <p>Add the <code>Exp</code> containing the name of the
     * variable.</p>
     *
     * @param var The name of the variable.
     */
    public void addFreeVar(Exp var) {
        // Check our list looking for var
        boolean inFreeVar = false;
        for (Exp freeVar : myFreeVars) {
            if (freeVar.equals(var)) {
                inFreeVar = true;
                break;
            }
        }

        // Adds the variable into our free variable list
        // if it isn't in our list already.
        if (!inFreeVar) {
            myFreeVars.add(var);
        }
    }

    /**
     * <p>Add a Remember statement to the list</p>
     */
    public void addRemember() {
        myVerificationStmtList.add(new VerificationStatement(
                VerificationStatement.REMEMBER, null));
    }

    /**
     * <p>Loop through the list of <code>Statement</code> to the
     * list of verification statements</p>
     *
     * @param statementList The corresponding <code>Statement</code>.
     */
    public void addStatements(List<Statement> statementList) {
        // Loop
        Iterator<Statement> it = statementList.iterator();
        while (it.hasNext()) {
            addCode(it.next());
        }
    }

    /**
     * <p>Loop through the list of <code>VarDec</code> and add
     * them to the list of verification statements</p>
     *
     * @param variableList List of the all variables as
     *                     <code>VarDec</code>.
     */
    public void addVariableDecs(List<VarDec> variableList) {
        Iterator<VarDec> i = variableList.iterator();
        while (i.hasNext()) {
            VarDec dec = i.next();
            myVerificationStmtList.add(new VerificationStatement(
                    VerificationStatement.VARIABLE, dec.clone()));
        }
    }

    /**
     * <p>Returns a string containing the assertion formatted for
     * output.</p>
     *
     * @return Readable <code>String</code> form of the assertion.
     */
    public String assertionToString() {
        // Create the return string
        String retStr = new String();
        retStr = retStr.concat("\n");

        // Free variables
        retStr = retStr.concat("Free Variables: \n");
        Iterator<Exp> freeVarIt = myFreeVars.iterator();
        while (freeVarIt.hasNext()) {
            Exp current = freeVarIt.next();
            retStr =
                    retStr.concat(current.toString(0) + " : "
                            + current.getMathType().toString());

            if (freeVarIt.hasNext())
                retStr = retStr.concat(", ");
            else
                retStr = retStr.concat("\n");
        }
        retStr = retStr.concat("\n");

        // Verification Statements
        Iterator<VerificationStatement> vsIt =
                myVerificationStmtList.iterator();
        while (vsIt.hasNext()) {
            VerificationStatement current = vsIt.next();

            // All possible types of verification statements
            switch (current.getType()) {
            // Assume Verification Statements
            case VerificationStatement.ASSUME:
                retStr =
                        retStr.concat("Assume "
                                + ((Exp) current.getAssertion()).toString(0));
                break;
            // Change Verification Statements
            case VerificationStatement.CHANGE:
                // TODO:  Add when we have change rule implemented.
                break;
            // Code Verification Statements
            case VerificationStatement.CODE:
                retStr =
                        retStr.concat(((Statement) current.getAssertion())
                                .toString(6));
                break;
            // Confirm Verification Statements
            case VerificationStatement.CONFIRM:
                retStr =
                        retStr.concat("Confirm "
                                + ((Exp) current.getAssertion()).toString(0));
                break;
            // Remember Verification Statements
            case VerificationStatement.REMEMBER:
                retStr = retStr.concat("      Remember");
                break;
            // Variable Verification Statements
            case VerificationStatement.VARIABLE:
                VarDec varDec = (VarDec) current.getAssertion();
                retStr =
                        retStr
                                .concat("      Var "
                                        + varDec.getName().getName()
                                        + " : "
                                        + ((NameTy) varDec.getTy()).getName()
                                                .getName());
                break;
            }
            retStr = retStr.concat(";\n");
        }

        retStr = retStr.concat("      Confirm " + myConfirm.toString(0) + ";");

        return retStr;
    }

    /**
     * <p>Returns a deep copy of the final confirm
     * statement.</p>
     *
     * @return <code>Exp</code> confirm clause.
     */
    public Exp getFinalConfirm() {
        return Exp.copy(myConfirm);
    }

    /**
     * <p>Returns the free variable with the
     * specified name.</p>
     *
     * @param name Name of the variable.
     * @param isGlobal Check all global free variables.
     *
     * @return The free variable in <code>VarExp</code> form.
     */
    public Exp getFreeVar(PosSymbol name, boolean isGlobal) {
        Exp exp = null;
        for (Exp v : myFreeVars) {
            // Global free variables
            if (isGlobal && v instanceof DotExp) {
                DotExp dotExp = (DotExp) v;
                Exp lastExp =
                        dotExp.getSegments().get(
                                dotExp.getSegments().size() - 1);
                if (lastExp.containsVar(name.getName(), false)) {
                    exp = v;
                    break;
                }
            }
            // Local free variables
            else if (v instanceof VarExp
                    && ((VarExp) v).getName().equals(name.getName())) {
                exp = v;
                break;
            }
        }

        return exp;
    }

    /**
     * <p>Returns the last <code>VerificationStatement</code> from the list</p>
     *
     * @return <code>VerificationStatement</code> from the list
     */
    public VerificationStatement getLastAssertion() {
        if (!myVerificationStmtList.isEmpty())
            return myVerificationStmtList
                    .remove(myVerificationStmtList.size() - 1);
        else
            return new VerificationStatement();
    }

    /**
     * <p>Checks if the assertion list is empty or not</p>
     *
     * @return Boolean
     */
    public boolean hasAnotherAssertion() {
        return (!myVerificationStmtList.isEmpty());
    }

    /**
     * <p>Set the final confirm clause</p>
     *
     * @param confirm <code>Exp</code> containing the final confirm clause.
     */
    public void setFinalConfirm(Exp confirm) {
        // By default it is already TrueVarExp
        if (confirm != null) {
            myConfirm = confirm;
        }
    }

    // ===========================================================
    // Private Methods
    // ===========================================================
}
