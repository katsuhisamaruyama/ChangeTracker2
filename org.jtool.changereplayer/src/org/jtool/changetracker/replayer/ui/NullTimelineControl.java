/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer.ui;

import org.eclipse.swt.widgets.Composite;

/**
 * A time-line bar for change operations.
 * @author Katsuhisa Maruyama
 */
public class NullTimelineControl extends TimelineControl {
    
    /**
     * Creates a time-time bar.
     * @param view the code change view that contains the table control
     */
    public NullTimelineControl(CodeChangeView view) {
        super(view);
    }
    
    /**
     * Creates a control of a time-time bar.
     * @param parent the parent control
     */
    @Override
    public void createTimeline(Composite parent) {
    }
    
    /**
     * Sets the focus to the control of the time-line bar.
     */
    @Override
    public void setFocus() {
        canvas.setFocus();
    }
    
    /**
     * Disposes the control of the time-line bar.
     */
    @Override
    public void dispose() {
    }
    
    /**
     * Selects a change operation in the time-line bar.
     */
    @Override
    public void select() {
    }
    
    /**
     * Updates this view.
     */
    @Override
    public void update() {
    }
    
    /**
     * Resets this view.
     */
    @Override
    public void reset() {
    }
    
    /**
     * Redraws this view.
     */
    @Override
    public void redraw() {
    }
}
