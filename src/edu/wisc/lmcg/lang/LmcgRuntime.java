/*
 * LmcgRunTime.java
 *
 * Created on July 22, 2005, 11:09 AM
 */
package edu.wisc.lmcg.lang;

import java.lang.Process;
import java.io.IOException;

/**
 * Interface to RunTime class
 *
 * @author churas
 */
public interface LmcgRuntime {

    public Process exec(String command) throws SecurityException, IOException, NullPointerException, IllegalArgumentException;

}
