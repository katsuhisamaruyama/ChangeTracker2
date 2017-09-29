/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.sample;

import org.eclipse.jface.action.Action;

/**
 * An action for presenting a label.
 * @author Katsuhisa Maruyama
 */
public class LabelAction extends Action {
    
    /**
     * Creates an instance for performing the backward slicing.
     * @param text the text displayed as a menu item
     */
    public LabelAction(String text) {
        super();
        
        setText(text);
        setEnabled(false);
    }
    
    /**
     * Performs the menu action.
     */
    @Override
    public void run() {
    }
}
