/**
 * ConsequentWeakeningStep.java
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
package edu.clemson.cs.r2jt.proving;

import java.util.Iterator;

public class ConsequentWeakeningStep implements VCTransformer {

    private final ConsequentConjunctWeakeningTransformer myConsequentTransformer;

    private final boolean myIntroducesQuantifiedVariablesFlag;

    public ConsequentWeakeningStep(Antecedent theoremAntecedent,
            Consequent theoremConsequent) {

        myIntroducesQuantifiedVariablesFlag =
                theoremConsequent
                        .containsQuantifiedVariableNotIn(theoremAntecedent);

        myConsequentTransformer =
                new ConsequentConjunctWeakeningTransformer(theoremAntecedent,
                        theoremConsequent);
    }

    @Override
    public Iterator<VC> transform(VC original) {
        return new StaticAntecedentIterator(original.getSourceName(), original
                .getAntecedent(), myConsequentTransformer.transform(original
                .getConsequent()));
    }

    @Override
    public String toString() {
        return myConsequentTransformer.toString();
    }

    @Override
    public Antecedent getPattern() {
        return myConsequentTransformer.getTheoremAntecedent();
    }

    @Override
    public Consequent getReplacementTemplate() {
        return myConsequentTransformer.getTheoremConsequent();
    }

    @Override
    public boolean introducesQuantifiedVariables() {
        return myIntroducesQuantifiedVariablesFlag;
    }
}
