/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.ui;

import org.eclipse.swt.widgets.Composite;

/**
 * Buttons that are not shown in the history view.
 * @author Katsuhisa Maruyama
 */
public class NullButtonControl extends ButtonControl {
    
    /**
     * Creates replay buttons.
     * @param view the history view that contains the button control
     */
    public NullButtonControl(HistoryView view) {
        super(view);
    }
    
    /**
     * Creates a control of the buttons.
     * @param parent the parent control
     */
    @Override
    public void createButtons(Composite parent) {
    }
    
    /**
     * Disposes the control of the replay buttons.
     */
    @Override
    public void dispose() {
    }
    
    /**
     * Updates the replay buttons.
     */
    @Override
    public void update() {
    }
    
    /**
     * Resets the replay buttons.
     */
    @Override
    public void reset() {
    }
}
