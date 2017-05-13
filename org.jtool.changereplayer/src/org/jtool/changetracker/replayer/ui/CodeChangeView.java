/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer.ui;

import org.jtool.changetracker.repository.ChangeTrackerFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import java.time.ZonedDateTime;

/**
 * A view that shows the results of comparing between two source codes.
 * @author Katsuhisa Maruyama
 */
public class CodeChangeView extends ViewPart implements ReplayStateChangedListener {
    
    /**
     * The identification string that is used to register this view.
     */
    public static final String ID = "org.jtool.changetracker.replayer.ui.CodeChangeView";
    
    /**
     * The manager that manages the history of change operations.
     */
    private ReplayManager replayManager;
    
    /**
     * The control for the source code viewer.
     */
    protected SourceCodeViewer sourceCodeViewer;
    
    /**
     * The control for the time-line bar.
     */
    protected TimelineControl timelineControl;
    
    /**
     * The actions for buttons.
     */
    protected ToolBarAction toolbarAction;
    
    /**
     * Creates a code compare view.
     */
    public CodeChangeView() {
        replayManager = ReplayManager.getInstance();
        replayManager.addEventListener(this);
    }
    
    /**
     * Creates this code compare view.
     * @param parent the parent control
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.BORDER);
        FormLayout layout = new FormLayout();
        composite.setLayout(layout);
        
        timelineControl = new TimelineControl(this);
        Composite timeline = timelineControl.createTimeline(composite);
        sourceCodeViewer = new SourceCodeViewer(this);
        sourceCodeViewer.createSourceCodeViewer(composite, timeline);
        toolbarAction = new ToolBarAction(this);
        toolbarAction.createActions();
    }
    
    /**
     * Sets the focus to this view.
     */
    @Override
    public void setFocus() {
        sourceCodeViewer.setFocus();
    }
    
    /**
     * Disposes this view.
     */
    @Override
    public void dispose() {
        replayManager.close();
        sourceCodeViewer.dispose();
        timelineControl.dispose();
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
                sourceCodeViewer.update();
                timelineControl.select();
                toolbarAction.update();
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
                toolbarAction.update();
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
    }
    
    /**
     * Updates this view.
     */
    private void update() {
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
                sourceCodeViewer.update();
                timelineControl.update();
                toolbarAction.update();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Resets this view.
     */
    private void reset() {
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
                sourceCodeViewer.reset();
                timelineControl.reset();
                toolbarAction.reset();
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
     * Returns the time when the present change operation was performed.
     * @return the time of the change operation, or <code>null</code> if the present time is invalid.
     */
    protected ZonedDateTime getPresentTime() {
        return replayManager.getPresentTime();
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
     * Returns the present contents of the source code.
     * @return the present source code.
     */
    protected String getPresentCode() {
        return replayManager.getPresentCode();
    }
    
    /**
     * Returns the precedent contents of the source code.
     * @return the precedent source code, or the empty string if there is no precedent source code found.
     */
    protected String getPrecedentCode() {
        return replayManager.getPrecedentCode();
    }
    
    /**
     * Returns the successive contents of the source code.
     * @return the successive source code, or the empty string if there is no successive source code found.
     */
    protected String getSucessiveCode() {
        return replayManager.getSucessiveCode();
    }
    
    /**
     * Returns the scale for the time range.
     * @return the percentage of the scale
     */
    protected int getTimeScale() {
        return replayManager.getTimeScale();
    }
    
    /**
     * Sets the scale for the time range.
     * @param scale the percentage of the scale
     */
    protected void setTimeScale(int scale) {
        replayManager.setTimeScale(scale);
    }
    
    /**
     * Zooms in the time range.
     */
    protected void zoominTimeScale() {
        replayManager.zoominTimeScale() ;
    }
    
    /**
     * Zooms out the time range.
     */
    protected void zoomoutTimeScale() {
        replayManager.zoomoutTimeScale();
    }
}
