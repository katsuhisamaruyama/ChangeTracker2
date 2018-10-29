/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.sample;

import org.jtool.changetracker.replayer.ui.CodeAnimatingView;
import org.jtool.changetracker.replayer.ui.CodeComparingView;
import org.jtool.changetracker.replayer.ui.HistoryView;
import org.jtool.changetracker.replayer.ui.OperationVisualizer;
import org.jtool.changetracker.repository.IRepositoryHandler;

/**
 * Replays operation changes.
 * @author Katsuhisa Maruyama
 */
public class OperationReplayerSample extends OperationVisualizer implements IRepositoryHandler {
    
    
    /**
     * Creates an object that replays change operations.
     */
    public OperationReplayerSample() {
    }
    
    /**
     * Invoked to initialize this handler before receiving repository change events.
     */
    public void initialize() {
        addView(HistoryView.ID);
        addView(CodeAnimatingViewSample.ID);
    }
    
    /**
     * Invoked to terminate this handler.
     */
    public void terminate() {
    }
}
