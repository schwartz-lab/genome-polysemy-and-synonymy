/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wisc.lmcg.alignment;

/**
 * Represents an error during an alignment
 * @author dipaco
 */
public class AlignmentException extends Exception{
    
    /**
     * Holds the error message
     */
    private final String msg;
    
    /**
     * Exception constructor
     * @param msg Error message
     */
    public AlignmentException(String msg){
        super();
        this.msg = msg;
    }
    
    /**
     * Return the error message
     * @return error message
     */
    @Override
    public String getMessage(){
        return msg;
    }
}
