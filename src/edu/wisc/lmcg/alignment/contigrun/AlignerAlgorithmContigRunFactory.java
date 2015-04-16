/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wisc.lmcg.alignment.contigrun;

import edu.wisc.lmcg.alignment.AlignmentException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 *
 * @author dipaco
 */
public interface AlignerAlgorithmContigRunFactory extends ContigRunFactory {

    /**
     * Creates a new contig run and make it the current one.
     */
    void startNewContingRun();

    /**
     * Perform one alignment and adds the results to the current contig run.
     * @param parameters Parameter list for the alignment algorithm
     * @throws AlignmentException 
     */
    void performAlignment(Map<String, Object> parameters) throws AlignmentException;

    /**
     * Returns the parameter list for the algorithm
     * @return parameter list for the algorithm
     */
    Map<String, Type> getParamterList();
}
