/**
 * ProgramExp.java
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

import edu.clemson.cs.r2jt.type.Type;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.typeandpopulate.programtypes.PTType;

public abstract class ProgramExp extends Exp {

    private PTType myProgramType;

    public abstract void accept(ResolveConceptualVisitor v);

    public abstract Type accept(TypeResolutionVisitor v)
            throws TypeResolutionException;

    public abstract String asString(int indent, int increment);

    public abstract String toString(int indent);

    /** Should be overridden by classes extending ProgramExp. **/
    public boolean containsVar(String varName, boolean IsOldExp) {
        return false;
    }

    public void setProgramType(PTType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Attempt to set program type to null.");
        }

        myProgramType = type;
    }

    public PTType getProgramType() {
        return myProgramType;
    }
}
