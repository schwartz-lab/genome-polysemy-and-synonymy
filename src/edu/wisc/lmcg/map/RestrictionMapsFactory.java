package edu.wisc.lmcg.map;

/**
 * Interface for RestrictionMapsFactory
 */
public interface RestrictionMapsFactory {

    public RestrictionMap getNextRestrictionMap() throws Exception;

    public void reset() throws java.io.IOException, NullPointerException;

    public boolean resetSupported();

}
