/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer;

import org.jtool.changetracker.repository.IRepositoryHandler;
import org.jtool.changetracker.ui.OperationVisualizer;
import org.jtool.changetracker.ui.HistoryView;
import org.jtool.changetracker.ui.CodeComparingView;
import org.jtool.changetracker.ui.CodeAnimatingView;

/**
 * Replays operation changes.
 * @author Katsuhisa Maruyama
 */
public class OperationReplayer extends OperationVisualizer implements IRepositoryHandler {
    
    
    /**
     * Creates an object that replays change operations.
     */
    public OperationReplayer() {
    }
    
    /**
     * Invoked to initialize this handler before receiving repository change events.
     */
    public void initialize() {
        addView(HistoryView.ID);
        addView(CodeComparingView.ID);
        addView(CodeAnimatingView.ID);
    }
    
    /**
     * Invoked to terminate this handler.
     */
    public void terminate() {
    }
}
