/**
 * ConjunctionOfNormalizedAtomicExpressions.java
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
package edu.clemson.cs.r2jt.congruenceclassprover;

import edu.clemson.cs.r2jt.proving.absyn.PExp;
import edu.clemson.cs.r2jt.proving.absyn.PExpSubexpressionIterator;
import edu.clemson.cs.r2jt.proving.absyn.PSymbol;
import edu.clemson.cs.r2jt.typeandpopulate.MTType;

import java.util.*;

/**
 * Created by mike on 4/3/2014.
 */
public class ConjunctionOfNormalizedAtomicExpressions {

    private final Registry m_registry;
    private final List<NormalizedAtomicExpressionMapImpl> m_exprList;
    protected long m_timeToEnd = -1;
    private final List<NormalizedAtomicExpressionMapImpl> m_removedExprList;

    /**
     * @param registry the Registry symbols contained in the conjunction will
     * reference. This class will add entries to the registry if needed.
     */
    public ConjunctionOfNormalizedAtomicExpressions(Registry registry) {
        m_registry = registry;
        m_exprList = new LinkedList<NormalizedAtomicExpressionMapImpl>();
        m_removedExprList = new LinkedList<NormalizedAtomicExpressionMapImpl>();
    }

    protected int size() {
        return m_exprList.size();
    }

    protected void findNAE(SearchBox box) {
        NormalizedAtomicExpressionMapImpl translQuery = box.m_translated;
        int lowerBound = Collections.binarySearch(m_exprList, translQuery);
        // if it exists in the list...
        if (lowerBound >= 0 && lowerBound < m_exprList.size()) {
            box.lowerBound = lowerBound;
            box.upperBound = lowerBound;
            box.currentIndex = lowerBound;
            box.directMatch = true;
            return;
        }
        lowerBound = -lowerBound - 1;
        NormalizedAtomicExpressionMapImpl ubExpr =
                translQuery.incrementLastKnown();

        box.upperBound = m_exprList.size() - 1;
        box.lowerBound = lowerBound;
        box.currentIndex = lowerBound;
        box.directMatch = false;
    }

    protected NormalizedAtomicExpressionMapImpl getExprAtPosition(int position) {
        return m_exprList.get(position);
    }

    protected void addExpression(PExp expression, long timeToEnd) {
        m_timeToEnd = timeToEnd;
        addExpression(expression);
    }

    // Top level
    protected void addExpression(PExp expression) {
        if (m_timeToEnd > 0 && System.currentTimeMillis() > m_timeToEnd) {
            return;
        }
        String name = expression.getTopLevelOperation();

        if (expression.isEquality()) {
            int lhs = addFormula(expression.getSubExpressions().get(0));
            int rhs = addFormula(expression.getSubExpressions().get(1));
            mergeOperators(lhs, rhs);
        }
        else if (name.equals("and")) {
            addExpression(expression.getSubExpressions().get(0));
            addExpression(expression.getSubExpressions().get(1));
        }
        else {
            MTType type = expression.getType();
            PSymbol asPsymbol = (PSymbol) expression;
            int intRepOfOp = addPsymbol(asPsymbol);
            int root = addFormula(expression);
            if (type.isBoolean()) {
                mergeOperators(m_registry.getIndexForSymbol("true"), root);
            }
        }
    }

    // adds a particular sumbol to the registry
    protected int addPsymbol(PSymbol ps) {
        String name = ps.getTopLevelOperation();
        MTType type = ps.getType();
        Registry.Usage usage = Registry.Usage.SINGULAR_VARIABLE;
        if (ps.isLiteral()) {
            usage = Registry.Usage.LITERAL;
        }
        else if (ps.isFunction()) {
            if (ps.quantification.equals(PSymbol.Quantification.FOR_ALL)) {
                usage = Registry.Usage.HASARGS_FORALL;
            }
            else {
                usage = Registry.Usage.HASARGS_SINGULAR;
            }
        }
        else if (ps.quantification.equals(PSymbol.Quantification.FOR_ALL)) {
            usage = Registry.Usage.FORALL;
        }
        return m_registry.addSymbol(name, type, usage);
    }

    /* experimentally handling =
     i.e.: (|?S| = 0) = (?S = Empty_String))
     is broken down by addExpression so (|?S| = 0) is an argument
     should return int for true if known to be equal, otherwise return root representative. 
     */
    protected int addFormula(PExp formula) {
        if (formula.isEquality()) {
            int lhs = addFormula(formula.getSubExpressions().get(0));
            PExp r = formula.getSubExpressions().get(1);
            int rhs = addFormula(r);
            lhs = m_registry.findAndCompress(lhs);
            rhs = m_registry.findAndCompress(rhs);
            if (lhs == rhs) {
                return m_registry.getIndexForSymbol("true");
            }
            else {
                // insert =(lhs,rhs) = someNewRoot
                int questEq = m_registry.getIndexForSymbol("=");
                NormalizedAtomicExpressionMapImpl pred =
                        new NormalizedAtomicExpressionMapImpl();
                pred.writeOnto(questEq, 0);
                pred.writeOnto(lhs, 1);
                pred.writeOnto(rhs, 2);
                return addAtomicFormula(pred);
            }
        }
        PSymbol asPsymbol = (PSymbol) formula;
        int intRepOfOp = addPsymbol(asPsymbol);
        // base case
        if (formula.isVariable()) {
            return intRepOfOp;
        }

        NormalizedAtomicExpressionMapImpl newExpr =
                new NormalizedAtomicExpressionMapImpl();
        newExpr.writeOnto(intRepOfOp, 0);
        int pos = 0;
        PExpSubexpressionIterator it = formula.getSubExpressionIterator();
        while (it.hasNext()) {
            PExp p = it.next();
            pos++;
            int root = addFormula(p);
            assert newExpr != null;
            newExpr.writeOnto(root, pos);
        }
        return addAtomicFormula(newExpr);
    }

    /**
     * @param atomicFormula one sided expression. (= new root) is appended and
     * expression is inserted if no match of the side is found. Otherwise
     * current root is returned.
     * @return current integer value of root symbol that represents the input.
     */
    private int addAtomicFormula(NormalizedAtomicExpressionMapImpl atomicFormula) {
        int posIfFound = Collections.binarySearch(m_exprList, atomicFormula);
        if (posIfFound >= 0) {
            return m_exprList.get(posIfFound).readRoot();
        }
        // no such formula exists
        int indexToInsert = -(posIfFound + 1);
        MTType typeOfFormula =
                m_registry.getTypeByIndex(atomicFormula.readPosition(0));
        String symName =
                m_registry.getSymbolForIndex(atomicFormula.readPosition(0));
        assert typeOfFormula != null : symName + " has null type";
        int rhs = m_registry.makeSymbol(typeOfFormula);
        atomicFormula.writeToRoot(rhs);
        m_exprList.add(indexToInsert, atomicFormula);
        return rhs;
    }

    protected void mergeOperators(int a, int b) {
        if (m_timeToEnd > 0 && System.currentTimeMillis() > m_timeToEnd) {
            return;
        }
        a = m_registry.findAndCompress(a);
        b = m_registry.findAndCompress(b);
        Stack<Integer> holdingTank = mergeOnlyArgumentOperators(a, b);
        while (holdingTank != null && !holdingTank.empty()) {
            if (m_timeToEnd > 0 && System.currentTimeMillis() > m_timeToEnd) {
                return;
            }
            int opA = m_registry.findAndCompress(holdingTank.pop());
            int opB = m_registry.findAndCompress(holdingTank.pop());
            if (opA != opB) {
                holdingTank.addAll(mergeOnlyArgumentOperators(opA, opB));
            }
        }
        mergeArgsOfEqualityPredicateIfRootIsTrue();
    }

    // look for =(x,y)=true in list.  If found call merge(x,y).
    //  = will always be at top of list.
    // These expression will not be removed by this function,
    // but can be removed by merge().
    protected void mergeArgsOfEqualityPredicateIfRootIsTrue() {
        if (m_timeToEnd > 0 && System.currentTimeMillis() > m_timeToEnd) {
            return;
        }
        // loop until end, function op is not =, or =(x,y)=true
        // when found do merge, start again.
        int eqQ = m_registry.getIndexForSymbol("=");
        for (int i = 0; i < m_exprList.size(); ++i) {
            NormalizedAtomicExpressionMapImpl cur = m_exprList.get(i);
            int f = cur.readPosition(0);
            if (f != eqQ) {
                return;
            }
            int t = m_registry.getIndexForSymbol("true");
            int root = cur.readRoot();
            int op1 = cur.readPosition(1);
            int op2 = cur.readPosition(2);
            if (root == t && op1 != op2) {
                mergeOperators(cur.readPosition(1), cur.readPosition(2));
                // mergeOperators will do any other merges that arise.
                i = 0;
            }
        }
    }

    // Return list of modified predicates by their position. Only these can cause new merges.
    protected Stack<Integer> mergeOnlyArgumentOperators(int a, int b) {
        if (m_timeToEnd > 0 && System.currentTimeMillis() > m_timeToEnd) {
            return null;
        }
        if (a == b) {
            return null;
        }
        /*if (a > b) {
         int temp = a;
         a = b;
         b = temp;
         }*/// this is the original way, merge toward lower valued indices.
        String aString = m_registry.getSymbolForIndex(a);
        String bString = m_registry.getSymbolForIndex(b);
        if (aString.compareTo(bString) > 0) {
            int temp = a;
            a = b;
            b = temp;
        }
        // Favor retaining literals
        // THIS IS CURRENTLY ABSOLUTELY NECCESSARY.  Finding could be redone to avoid this.
        // Otherwise, proofs can fail to prove if literals are redefined.
        bString = m_registry.getSymbolForIndex(b);
        Registry.Usage uB = m_registry.getUsage(bString);
        if (uB.equals(Registry.Usage.LITERAL)) {
            int temp = a;
            a = b;
            b = temp;
        }
        Iterator<NormalizedAtomicExpressionMapImpl> it = m_exprList.iterator();
        Stack<NormalizedAtomicExpressionMapImpl> modifiedEntries =
                new Stack<NormalizedAtomicExpressionMapImpl>();
        Stack<Integer> coincidentalMergeHoldingTank = new Stack<Integer>();
        while (it.hasNext()) {
            NormalizedAtomicExpressionMapImpl curr = it.next();
            if (curr.replaceOperator(b, a)) {
                modifiedEntries.push(curr);
                it.remove();
            }
        }
        while (!modifiedEntries.empty()) {
            int indexToInsert =
                    Collections
                            .binarySearch(m_exprList, modifiedEntries.peek());
            // If the modified one is already there, don't put it back
            if (indexToInsert < 0) {
                indexToInsert = -(indexToInsert + 1);
                m_exprList.add(indexToInsert, modifiedEntries.pop());
            }
            else {
                // the expr is in the list, but are the roots different?
                int rootA = modifiedEntries.pop().readRoot();
                int rootB = m_exprList.get(indexToInsert).readRoot();
                if (rootA != rootB) {
                    coincidentalMergeHoldingTank.push(rootA);
                    coincidentalMergeHoldingTank.push(rootB);
                }
            }
        }
        m_registry.substitute(a, b);
        return coincidentalMergeHoldingTank;
    }

    protected Map<String, Integer> getSymbolProximity(Set<String> symbols) {
        boolean done = false;
        Map<Integer, Integer> relatedKeys = new HashMap<Integer, Integer>();
        for (String s : symbols) {
            relatedKeys.put(m_registry.getIndexForSymbol(s), 0);
        }
        int closeness = 0;
        Set<Integer> relatedSet = new HashSet<Integer>();

        while (!done) {
            closeness++;
            int startSize = relatedKeys.size();
            HashMap<Integer, Integer> relatedKeys2 =
                    new HashMap<Integer, Integer>();
            for (int i = 0; i < m_exprList.size(); ++i) {
                if (relatedSet.contains(i))
                    continue;
                Set<Integer> intersection =
                        new HashSet<Integer>(m_exprList.get(i).getKeys());
                intersection.retainAll(relatedKeys.keySet());
                if (!intersection.isEmpty()) {
                    relatedSet.add(i);
                    for (Integer k : m_exprList.get(i).getKeys()) {
                        if (!relatedKeys.containsKey(k)) {
                            relatedKeys2.put(k, closeness);
                        }
                    }
                }
            }
            relatedKeys.putAll(relatedKeys2);
            if (startSize == relatedKeys.size()) {
                done = true;
            }
        }

        HashSet<Integer> toRemove = new HashSet<Integer>();
        for (int i = 0; i < m_exprList.size(); ++i) {
            toRemove.add(i);
        }
        toRemove.removeAll(relatedSet);

        for (Integer i : toRemove) {
            m_removedExprList.add(m_exprList.get(i));
        }

        for (NormalizedAtomicExpressionMapImpl r : m_removedExprList) {
            m_exprList.remove(r);
        }

        Map<String, Integer> rMap =
                new HashMap<String, Integer>(relatedKeys.size());
        for (Integer i : relatedKeys.keySet()) {
            rMap.put(m_registry.getSymbolForIndex(i), relatedKeys.get(i));
        }
        return rMap;
    }

    @Override
    public String toString() {
        String r = "";
        for (NormalizedAtomicExpressionMapImpl cur : m_exprList) {
            r += cur.toHumanReadableString(m_registry) + "\n";
        }
        return r;
    }

}
