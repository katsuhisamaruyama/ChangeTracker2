/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.parser;

import org.jtool.changetracker.repository.FileInfo;

/**
 * Stores information about a method.
 * @author Katsuhisa Maruyama
 */
public class OpMethod extends OpJavaElement {
    
    /**
     * Creates an instance that stores information about a method.
     * @param start the start point of the method on the source code
     * @param end the end point of the method on the source code
     * @param finfo the information about the file containing the method
     * @param name the name of the method
     */
    public OpMethod(int start, int end, FileInfo finfo, String name) {
        super(start, end, finfo, name);
    }
}
