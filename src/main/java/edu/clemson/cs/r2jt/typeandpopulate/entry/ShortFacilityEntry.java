package edu.clemson.cs.r2jt.typeandpopulate.entry;

import edu.clemson.cs.r2jt.absyn.ResolveConceptualElement;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.typeandpopulate.PTType;
import java.util.Map;

public class ShortFacilityEntry extends ModuleEntry {

    private final FacilityEntry myEnclosedFacility;

    public ShortFacilityEntry(String name,
            ResolveConceptualElement definingElement,
            FacilityEntry enclosedFacility) {
        super(name, definingElement);

        myEnclosedFacility = enclosedFacility;
    }

    @Override
    public ShortFacilityEntry toShortFacilityEntry(Location l) {
        return this;
    }

    /**
     * <p>A short facility module contains exactly one facility declaration.
     * This method returns the entry corresponding to that declaration.</p>
     * 
     * @return The entry corresponding to the single facility enclosed in the
     *         short facility module.
     */
    public FacilityEntry getEnclosedFacility() {
        return myEnclosedFacility;
    }

    @Override
    public String getEntryTypeDescription() {
        return "a short facility module";
    }

    @Override
    public SymbolTableEntry instantiateGenerics(
            Map<String, PTType> genericInstantiations,
            FacilityEntry instantiatingFacility) {
        return new ShortFacilityEntry(getName(), getDefiningElement(),
                myEnclosedFacility.instantiateGenerics(genericInstantiations,
                        instantiatingFacility));
    }
}