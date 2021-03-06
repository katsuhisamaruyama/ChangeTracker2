/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer;

import java.util.Set;

import org.jtool.changetracker.replayer.ui.CodeAnimatingView;
import org.jtool.changetracker.replayer.ui.HistoryView;
import org.jtool.changetracker.replayer.ui.OperationVisualizer;
import org.jtool.changetracker.repository.IRepositoryHandler;
import org.jtool.changetracker.repository.RepositoryManager;

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
        Set<IRepositoryHandler> handlers = RepositoryManager.getInstance().getRepositoryHandlers();
        if (handlers.size() == 1) {
            addView(HistoryView.ID);
            addView(CodeAnimatingView.ID);
        }
    }
    
    /**
     * Invoked to terminate this handler.
     */
    public void terminate() {
    }
}
