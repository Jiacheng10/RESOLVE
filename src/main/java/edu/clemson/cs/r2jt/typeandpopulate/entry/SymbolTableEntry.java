/**
 * SymbolTableEntry.java
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
package edu.clemson.cs.r2jt.typeandpopulate.entry;

import java.util.HashMap;
import java.util.Map;

import edu.clemson.cs.r2jt.absyn.ResolveConceptualElement;
import edu.clemson.cs.r2jt.absyn.VarExp;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.typeandpopulate.MTType;
import edu.clemson.cs.r2jt.typeandpopulate.ModuleIdentifier;
import edu.clemson.cs.r2jt.typeandpopulate.programtypes.PTType;
import edu.clemson.cs.r2jt.utilities.SourceErrorException;

/**
 * <p>Checklist for subclassing <code>SymbolTableEntry</code>:</p>
 * 
 * <ul>
 * 		<li>Create subclass.</li>
 * 		<li>Add "toXXX()" method in this parent class.</li>
 * 		<li>Override it in subclass.</li>
 *              <li>Consider if entry can be coerced to other kinds of entries,
 *                  and override those toXXXs as well. (See ProgramVariableEntry
 *                  as an example.</li>
 * </ul>
 */
public abstract class SymbolTableEntry {

    public enum Quantification {
        NONE {

            @Override
            public String toString() {
                return "None";
            }

            @Override
            public int toVarExpQuantificationCode() {
                return VarExp.NONE;
            }
        },
        UNIVERSAL {

            @Override
            public String toString() {
                return "Universal";
            }

            @Override
            public int toVarExpQuantificationCode() {
                return VarExp.FORALL;
            }
        },
        EXISTENTIAL {

            @Override
            public String toString() {
                return "Existential";
            }

            @Override
            public int toVarExpQuantificationCode() {
                return VarExp.EXISTS;
            }
        };

        public abstract int toVarExpQuantificationCode();
    }

    private final String myName;
    private final ResolveConceptualElement myDefiningElement;
    private final ModuleIdentifier mySourceModuleIdentifier;

    public SymbolTableEntry(String name,
            ResolveConceptualElement definingElement,
            ModuleIdentifier sourceModule) {

        myName = name;
        myDefiningElement = definingElement;
        mySourceModuleIdentifier = sourceModule;
    }

    public ModuleIdentifier getSourceModuleIdentifier() {
        return mySourceModuleIdentifier;
    }

    public String getName() {
        return myName;
    }

    public ResolveConceptualElement getDefiningElement() {
        return myDefiningElement;
    }

    public RepresentationTypeEntry toRepresentationTypeEntry(Location l) {
        throw new SourceErrorException("Expecting a program type "
                + "representation.  Found " + getEntryTypeDescription() + ".",
                l);
    }

    public MathSymbolEntry toMathSymbolEntry(Location l) {
        throw new SourceErrorException("Expecting a math symbol.  Found "
                + getEntryTypeDescription() + ".", l);
    }

    public ProgramTypeEntry toProgramTypeEntry(Location l) {
        throw new SourceErrorException("Expecting a program type.  Found "
                + getEntryTypeDescription() + ".", l);
    }

    public FacilityEntry toFacilityEntry(Location l) {
        throw new SourceErrorException("Expecting a facility.  Found "
                + getEntryTypeDescription(), l);
    }

    public ProgramParameterEntry toProgramParameterEntry(Location l) {
        throw new SourceErrorException("Expecting a program parameter.  "
                + "Found " + getEntryTypeDescription(), l);
    }

    public ProgramVariableEntry toProgramVariableEntry(Location l) {
        throw new SourceErrorException("Expecting a program variable.  "
                + "Found " + getEntryTypeDescription(), l);
    }

    public OperationEntry toOperationEntry(Location l) {
        throw new SourceErrorException("Expecting an operation.  Found "
                + getEntryTypeDescription(), l);
    }

    public OperationProfileEntry toOperationProfileEntry(Location l) {
        throw new SourceErrorException("Expecting a operation profile.  Found "
                + getEntryTypeDescription(), l);
    }

    public ProcedureEntry toProcedureEntry(Location l) {
        throw new SourceErrorException("Expecting a procedure.  Found "
                + getEntryTypeDescription(), l);
    }

    public ShortFacilityEntry toShortFacilityEntry(Location l) {
        throw new SourceErrorException("Expecting a short facility module.  "
                + "Found " + getEntryTypeDescription(), l);
    }

    public ProgramTypeDefinitionEntry toProgramTypeDefinitionEntry(Location l) {
        throw new SourceErrorException("Expecting a program type definition.  "
                + "Found " + getEntryTypeDescription(), l);
    }

    public TheoremEntry toTheoremEntry(Location l) {
        throw new SourceErrorException("Expecting a theorem.  " + "Found "
                + getEntryTypeDescription(), l);
    }

    public abstract String getEntryTypeDescription();

    public abstract SymbolTableEntry instantiateGenerics(
            Map<String, PTType> genericInstantiations,
            FacilityEntry instantiatingFacility);

    public static Map<String, MTType> buildMathTypeGenerics(
            Map<String, PTType> genericInstantiations) {

        Map<String, MTType> genericMathematicalInstantiations =
                new HashMap<String, MTType>();

        for (Map.Entry<String, PTType> instantiation : genericInstantiations
                .entrySet()) {

            genericMathematicalInstantiations.put(instantiation.getKey(),
                    instantiation.getValue().toMath());
        }

        return genericMathematicalInstantiations;
    }
}
