package edu.wisc.lmcg.alignment.contigrun;

/**
 * This interface defines a factory class that creates ContigRun objects.
 */
public interface ContigRunFactory {

    /**
     * Gets the next ContigRun available
     * @return A contig run
     * @throws NullPointerException
     * @throws Exception 
     */
    public ContigRun getNextContigRun() throws NullPointerException, Exception;

    /**
     * Restarts the factory
     * @throws Exception 
     */
    public void reset() throws Exception;

    /**
     * Indicates if the reset functionality is available
     * @return 
     */
    public boolean resetSupported();
}
