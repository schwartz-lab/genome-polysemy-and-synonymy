/*
 * LmcgRunTimeImpl.java
 *
 * Created on July 22, 2005, 3:20 PM
 */
package edu.wisc.lmcg.lang;

import java.lang.Runtime;

/**
 *
 * @author churas
 */
public class LmcgRuntimeImpl implements LmcgRuntime {

    private Runtime mp_RunTime;

    /**
     * Creates a new instance of LmcgRunTimeImpl
     */
    public LmcgRuntimeImpl() {
        mp_RunTime = Runtime.getRuntime();
    }

    /**
     * Calls java.lang.Runtime.exec(String command) method and returns its
     * value.
     */
    public Process exec(String command) throws SecurityException, java.io.IOException, NullPointerException, IllegalArgumentException {
        return mp_RunTime.exec(command);
    }

}
