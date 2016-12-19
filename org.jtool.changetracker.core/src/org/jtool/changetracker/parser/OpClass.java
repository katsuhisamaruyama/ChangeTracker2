/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.parser;

import org.jtool.changetracker.repository.FileInfo;

/**
 * Stores information about a class.
 * @author Katsuhisa Maruyama
 */
public class OpClass extends OpJavaElement {
    
    /**
     * Creates an instance that stores information about a class.
     * @param start the start point of the class on the source code
     * @param end the end point of the class on the source code
     * @param finfo the information about the file containing the class
     * @param name the name of the class
     */
    public OpClass(int start, int end, FileInfo finfo, String name) {
        super(start, end, finfo, name);
    }
}
