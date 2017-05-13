/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer.ui;

import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.jtool.changetracker.repository.ChangeTrackerFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * A view that displays the history of change operations.
 * @author Katsuhisa Maruyama
 */
public class HistoryView extends ViewPart implements ReplayStateChangedListener {
    
    /**
     * The identification string that is used to register this view.
     */
    public static final String ID = "org.jtool.changetracker.replayer.ui.HistoryView";
    
    /**
     * The manager that manages the history of change operations.
     */
    private ReplayManager replayManager;
    
    /**
     * The control for the replay table.
     */
    protected OperationTableViewer tableControl;
    
    /**
     * The control for the replay buttons.
     */
    protected ButtonControl buttonControl;
    
    /**
     * Creates an instance of this operation history view.
     * @param manager the replay manager
     */
    public HistoryView() {
        replayManager = ReplayManager.getInstance();
        replayManager.addEventListener(this);
    }
    
    /**
     * Creates this operation history view.
     * @param parent the parent control
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.BORDER);
        FormLayout layout = new FormLayout();
        composite.setLayout(layout);
        
        buttonControl = new NullButtonControl(this);
        Composite buttons = buttonControl.createButtons(composite);
        tableControl = new OperationTableViewer(this);
        tableControl.createTable(composite, buttons);
    }
    
    /**
     * Sets the focus to this view.
     */
    @Override
    public void setFocus() {
        tableControl.setFocus();
    }
    
    /**
     * Disposes this this view.
     */
    @Override
    public void dispose() {
        replayManager.close();
        tableControl.dispose();
        buttonControl.dispose();
        replayManager.removeEventListener(this);
        super.dispose();
    }
    
    /**
     * Receives an event that represents the change of replay state.
     * @param evt the event
     */
    @Override
    public void notify(ReplayStateChangedEvent evt) {
        ReplayStateChangedEvent.Type type = evt.getType();
        if (type.equals(ReplayStateChangedEvent.Type.INDEX_CHANGED)) {
            select();
        } if (type.equals(ReplayStateChangedEvent.Type.MARK_CHANGED)) {
            mark();
        } else if (type.equals(ReplayStateChangedEvent.Type.FILE_OPENED)) {
            open();
        } else if (type.equals(ReplayStateChangedEvent.Type.UPDATE)) {
            update();
        } else if (type.equals(ReplayStateChangedEvent.Type.RESET)) {
            reset();
        }
    }
    
    /**
     * Selects a change operation in this operation history view.
     */
    protected void select() {
        UIJob job = new UIJob("Select") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!replayManager.readyToReplay()) {
                    return Status.CANCEL_STATUS;
                }
                tableControl.select();
                buttonControl.update();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Changes the mark states of a change operations in this operation history view.
     */
    protected void mark() {
        UIJob job = new UIJob("Mark") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!replayManager.readyToReplay()) {
                    return Status.CANCEL_STATUS;
                }
                tableControl.mark();
                buttonControl.update();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Opens this view.
     */
    protected void open() {
        update();
        setFocus();
    }
    /**
     * Updates this view.
     */
    protected void update() {
        UIJob job = new UIJob("Update") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!replayManager.readyToReplay()) {
                    return Status.CANCEL_STATUS;
                }
                tableControl.update();
                buttonControl.update();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Resets this view.
     */
    protected void reset() {
        UIJob job = new UIJob("Reset") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!replayManager.readyToReplay()) {
                    return Status.CANCEL_STATUS;
                }
                tableControl.reset();
                buttonControl.reset();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Returns information about a file related to the operation history.
     * @return the file information
     */
    protected ChangeTrackerFile getFile() {
        return replayManager.getFile();
    }
    
    /**
     * Tests if change operations are ready to be replayed.
     * @return <code>true</code> if change operations are ready to be replayed, otherwise <code>false</code>
     */
    protected boolean readyToReplay() {
        return replayManager.readyToReplay();
    }
    
    /**
     * Goes to a specified change operation.
     * @param index the index number of the change operation
     */
    protected void goTo(int index) {
        replayManager.goTo(index);
    }
    
    /**
     * Returns the index number of the present change operation in the operation history.
     * @return the index number of the change operation
     */
    protected int getPresentIndex() {
        return replayManager.getPresentIndex();
    }
    
    /**
     * Returns the index number of the precedent change operation.
     * @return the index number of the precedent change operation, <code>-1</code> if node
     */
    protected int getPrecedentOperationIndex() {
        return replayManager.getPrecedentOperationIndex();
    }
    
    /**
     * Returns the index number of the successive change operation.
     * @return the index number of the successive change operation, <code>-1</code> if node
     */
    protected int getSuccessiveOperationIndex() {
        return replayManager.getSuccessiveOperationIndex();
    }
    
    /**
     * Returns the first change operation.
     * @return the first change operation, <code>-1</code> if node
     */
    protected int getFirstOperationIndex() {
        return replayManager.getFirstOperationIndex();
    }
    
    /**
     * Returns the last change operation.
     * @return the last change operation, <code>-1</code> if node
     */
    protected int getLastOperationIndex() {
        return replayManager.getLastOperationIndex();
    }
    
    /**
     * Finds the index number of a previous change operation with a mark.
     * @return the index number of the previous change operation, or <code>-1</code> if none
     */
    protected int getPreviousMarkedOperationIndex() {
        return replayManager.getPreviousMarkedOperationIndex();
    }
    
    /**
     * Finds the index number of a next change operation with a mark.
     * @return the index number of the next change operation, or <code>-1</code> if none
     */
    protected int getNextMarkedOperationIndex() {
        return replayManager.getNextMarkedOperationIndex();
    }
    
    /**
     * Marks or unmarks all the change operations.
     * @param mark <code>true</code> if the change operations will be marked, otherwise <code>false</code>
     */
    void setAllMarks(boolean mark) {
        replayManager.setAllMarks(mark);
    }
    
    /**
     * Return information about marked and unmarked change operations.
     * @return the array that memorizes which change operations are marked
     */
    protected boolean[] getPresentMarks() {
        return replayManager.getPresentMarks();
    }
    
    /**
     * Marks a change operation.
     * @param index the index number of the change operation to be marked
     */
    protected void markOperation(int index) {
        replayManager.markOperation(index);
    }
    
    /**
     * Unmarks a change operation.
     * @param index the index number of the change operation to be unmarked
     */
    protected void unmarkOperation(int index) {
        replayManager.unmarkOperation(index);
    }
}
