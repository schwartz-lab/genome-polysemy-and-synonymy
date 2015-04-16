/*
 * PlacedAlignmentType.java
 *
 * Created on September 16, 2004, 6:16 PM
 */
package edu.wisc.lmcg.alignment.placed;

/**
 *
 * @author churas
 */
public final class PlacedAlignmentType {

    public final static PlacedAlignmentType METACHROMOSOME = new PlacedAlignmentType("MetaChromosome");
    public final static PlacedAlignmentType SOMAPILECONTIG = new PlacedAlignmentType("SomaPileContig");
    public final static PlacedAlignmentType SOMAPILE = new PlacedAlignmentType("SomaPile");

    private final String mp_Name;

    /**
     * Creates a new instance of PlacedAlignmentType
     */
    private PlacedAlignmentType(String name) {
        mp_Name = name;
    }

    /**
     * Gets the placed alignment type
     * @return String representing the placed alignment type
     */
    public String getPlacedAlignmentType() {
        return mp_Name;
    }
}
