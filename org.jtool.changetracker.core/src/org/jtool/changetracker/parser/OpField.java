/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.parser;

import org.jtool.changetracker.repository.FileInfo;

/**
 * Stores information about a field.
 * @author Katsuhisa Maruyama
 */
public class OpField extends OpJavaElement {
    
    /**
     * Creates an instance that stores information about  field.
     * @param start the start point of the field on the source code
     * @param end the end point of the field on the source code
     * @param finfo the information about the file containing the field
     * @param name the name of the field
     */
    public OpField(int start, int end, FileInfo finfo, String name) {
        super(start, end, finfo, name);
    }
}
