/**
 * TypeNode.java
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
package edu.clemson.cs.r2jt.typereasoning;

import edu.clemson.cs.r2jt.typeandpopulate.TypeMismatchException;
import edu.clemson.cs.r2jt.typeandpopulate.NoSolutionException;
import edu.clemson.cs.r2jt.typeandpopulate.MTType;
import java.util.*;

import edu.clemson.cs.r2jt.absyn.Exp;

public class TypeNode {

    private static final ExpValuePathStrategy EXP_VALUE_PATH =
            new ExpValuePathStrategy();

    private static final MTTypeValuePathStrategy MTTYPE_VALUE_PATH =
            new MTTypeValuePathStrategy();

    private MTType myType;
    private Map<MTType, Set<TypeRelationship>> myRelationships;
    private final TypeGraph myTypeGraph;

    public TypeNode(TypeGraph g, MTType type) {
        myType = type;
        myRelationships = new HashMap<MTType, Set<TypeRelationship>>();
        myTypeGraph = g;
    }

    public int getOutgoingRelationshipCount() {
        return myRelationships.size();
    }

    public Exp getValidTypeConditionsTo(Exp value, MTType dst,
            Map<String, MTType> bindings) throws TypeMismatchException {

        return getValidTypeConditionsTo(value, dst, bindings, EXP_VALUE_PATH);
    }

    public Exp getValidTypeConditionsTo(MTType value, MTType dst,
            Map<String, MTType> bindings) throws TypeMismatchException {

        return getValidTypeConditionsTo(value, dst, bindings, MTTYPE_VALUE_PATH);
    }

    private <V> Exp getValidTypeConditionsTo(V value, MTType dst,
            Map<String, MTType> bindings,
            RelationshipPathStrategy<V> pathStrategy)
            throws TypeMismatchException {

        if (!myRelationships.containsKey(dst)) {
            throw TypeMismatchException.INSTANCE;
        }

        Exp finalConditions = myTypeGraph.getFalseVarExp();
        Set<TypeRelationship> relationships = myRelationships.get(dst);
        boolean foundTrivialPath = false;
        Iterator<TypeRelationship> relationshipIter = relationships.iterator();
        TypeRelationship relationship;
        Exp relationshipConditions;
        while (!foundTrivialPath && relationshipIter.hasNext()) {
            relationship = relationshipIter.next();

            try {
                relationshipConditions =
                        pathStrategy.getValidTypeConditionsAlong(relationship,
                                value, bindings);

                foundTrivialPath = (relationshipConditions.isLiteralTrue());

                finalConditions =
                        myTypeGraph.formDisjunct(relationshipConditions,
                                finalConditions);
            }
            catch (NoSolutionException nse) {}
        }

        if (foundTrivialPath) {
            finalConditions = myTypeGraph.getTrueVarExp();
        }

        return finalConditions;
    }

    public static interface RelationshipPathStrategy<V> {

        public Exp getValidTypeConditionsAlong(TypeRelationship relationship,
                V value, Map<String, MTType> bindings)
                throws NoSolutionException;
    }

    public static class ExpValuePathStrategy
            implements
                RelationshipPathStrategy<Exp> {

        @Override
        public Exp getValidTypeConditionsAlong(TypeRelationship relationship,
                Exp value, Map<String, MTType> bindings)
                throws NoSolutionException {

            return relationship.getValidTypeConditionsTo(value, bindings);
        }
    }

    public static class MTTypeValuePathStrategy
            implements
                RelationshipPathStrategy<MTType> {

        @Override
        public Exp getValidTypeConditionsAlong(TypeRelationship relationship,
                MTType value, Map<String, MTType> bindings)
                throws NoSolutionException {

            return relationship.getValidTypeConditionsTo(value, bindings);
        }
    }

    public boolean hasPathTo(TypeNode node, Exp bindingExpr) {
        /*Iterator<TypeRelationship> iter = myRelationships.iterator();
        while(iter.hasNext()) {
        	TypeRelationship rel = iter.next();
        	if (rel.getDestinationNode() == node && rel.exprBinds(bindingExpr)) {
        		bindingExpr.getMathType().equals(rel.getSourceType());
        		System.out.println("Coercion! Expression [ " + rel.getBindingExpressionString() + " ] binds to [ " + bindingExpr.toString() + " ]");
        		if (!rel.hasTrivialBindingCondition()) {
        			System.out.println("Raised proof obligation(s):\n" + rel.getBindingConditionString());
        		}
        		return true;
        	}
        }
        return false;*/
        //throw new UnsupportedOperationException();
        return true;
    }

    //XXX : Can we do this so that analyzer isn't setting up TypeRelationship objects?
    void addRelationship(TypeRelationship relationship) {
        Set<TypeRelationship> bucket =
                myRelationships.get(relationship.getDestinationType());
        if (bucket == null) {
            bucket = new HashSet<TypeRelationship>();
            myRelationships.put(relationship.getDestinationType(), bucket);
        }

        bucket.add(relationship);
    }

    public MTType getType() {
        return myType;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();

        for (Set<TypeRelationship> target : myRelationships.values()) {
            for (TypeRelationship rel : target) {
                str.append(rel);
            }
        }

        return str.toString();
    }
}
