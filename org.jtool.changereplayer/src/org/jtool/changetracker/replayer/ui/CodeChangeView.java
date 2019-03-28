/*
 *  Copyright 2017-2019
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer.ui;

import org.jtool.changetracker.repository.CTFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import java.time.ZonedDateTime;

/**
 * A view that shows changes of code.
 * @author Katsuhisa Maruyama
 */
public abstract class CodeChangeView extends ViewPart implements ViewStateChangedListener {
    
    /**
     * The instance that visualizes change operations.
     */
    protected OperationVisualizer operationVisualizer;
    
    /**
     * The control for the code viewer.
     */
    protected Control codeViewerControl;
    
    /**
     * The control for the time-line bar.
     */
    protected TimelineControl timelineControl;
    
    /**
     * The actions for buttons.
     */
    protected ToolBarAction toolbarAction;
    
    /**
     * Creates a code change view.
     */
    protected CodeChangeView() {
        ViewManager.getInstance().addEventListener(this);
        if (ViewManager.getInstance().getChangeExplorerView() != null) {
            operationVisualizer = ViewManager.getInstance().getOperationVisualizer();
        } else {
            ViewManager.getInstance().hideView(this);
        }
    }
    
    /**
     * Creates the part of this code change view.
     * @param parent the parent control
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.BORDER);
        FormLayout layout = new FormLayout();
        composite.setLayout(layout);
        
        if (toolbarAction != null) {
            toolbarAction.createActions();
        }
        if (timelineControl != null) {
            timelineControl.createTimeline(composite);
        }
        codeViewerControl = createCodeView(composite);
        
        FormData cvdata = new FormData();
        if (timelineControl != null) {
            cvdata.top = new FormAttachment(timelineControl.getControl(), 2);
        } else {
            cvdata.top = new FormAttachment(0, 0);
        }
        cvdata.bottom = new FormAttachment(100, 0);
        cvdata.left = new FormAttachment(0, 0);
        cvdata.right = new FormAttachment(100, 0);
        codeViewerControl.setLayoutData(cvdata);
    }
    
    /**
     * Creates a code viewer.
     * @return the control for the created code viewer
     */
    protected abstract Control createCodeView(Composite parent);
    
    /**
     * Selects the code viewer.
     */
    protected abstract void selectCodeViewer();
    
    /**
     * Updates the code viewer.
     */
    protected abstract void updateCodeViewer();
    
    /**
     * Resets the code viewer.
     */
    protected abstract void resetCodeViewer();
    
    /**
     * Sets the focus to this view.
     */
    @Override
    public void setFocus() {
        codeViewerControl.setFocus();
    }
    
    /**
     * Disposes this view.
     */
    @Override
    public void dispose() {
        if (codeViewerControl != null) {
            codeViewerControl.dispose();
        }
        ViewManager.getInstance().removeEventListener(this);
        super.dispose();
    }
    
    /**
     * Receives an event that represents the change of replay state.
     * @param evt the event
     */
    @Override
    public void notify(ViewStateChangedEvent evt) {
        ViewStateChangedEvent.Type type = evt.getType();
        if (type.equals(ViewStateChangedEvent.Type.INDEX_CHANGED)) {
            select();
        } if (type.equals(ViewStateChangedEvent.Type.MARK_CHANGED)) {
            mark();
        } else if (type.equals(ViewStateChangedEvent.Type.FILE_OPENED)) {
            open();
        } else if (type.equals(ViewStateChangedEvent.Type.UPDATE)) {
            update();
        } else if (type.equals(ViewStateChangedEvent.Type.RESET)) {
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
                if (!readyToVisualize()) {
                    return Status.CANCEL_STATUS;
                }
                selectCodeViewer();
                if (timelineControl != null) {
                    timelineControl.select();
                }
                if (toolbarAction != null) {
                    toolbarAction.select();
                }
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
                if (!readyToVisualize()) {
                    return Status.CANCEL_STATUS;
                }
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
    protected void update() {
        UIJob job = new UIJob("Update") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!readyToVisualize()) {
                    return Status.CANCEL_STATUS;
                }
                updateCodeViewer();
                if (timelineControl != null) {
                    timelineControl.update();
                }
                if (toolbarAction != null) {
                    toolbarAction.update();
                }
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
                if (!readyToVisualize()) {
                    return Status.CANCEL_STATUS;
                }
                resetCodeViewer();
                if (timelineControl != null) {
                    timelineControl.reset();
                }
                if (toolbarAction != null) {
                    toolbarAction.reset();
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
    
    /**
     * Returns information about a file related to the operation history.
     * @return the file information
     */
    protected CTFile getFile() {
        return operationVisualizer.getFile();
    }
    
    /**
     * Tests if change operations are ready to be visualized.
     * @return <code>true</code> if change operations are ready to be visualized, otherwise <code>false</code>
     */
    protected boolean readyToVisualize() {
        return operationVisualizer != null && operationVisualizer.readyToVisualize();
    }
    
    /**
     * Returns the time when the present change operation was performed.
     * @return the time of the change operation, or <code>null</code> if the present time is invalid.
     */
    protected ZonedDateTime getPresentTime() {
        return operationVisualizer.getPresentTime();
    }
    
    /**
     * Goes to a specified change operation.
     * @param index the index number of the change operation
     */
    protected void goTo(int index) {
        operationVisualizer.goTo(index);
    }
    
    /**
     * Returns the index number of the present change operation in the operation history.
     * @return the index number of the change operation
     */
    protected int getPresentIndex() {
        return operationVisualizer.getPresentIndex();
    }
    
    /**
     * Returns the index number of the precedent change operation.
     * @return the index number of the precedent change operation, <code>-1</code> if node
     */
    protected int getPrecedentOperationIndex() {
        return operationVisualizer.getPrecedentOperationIndex();
    }
    
    /**
     * Returns the index number of the successive change operation.
     * @return the index number of the successive change operation, <code>-1</code> if node
     */
    protected int getSuccessiveOperationIndex() {
        return operationVisualizer.getSuccessiveOperationIndex();
    }
    /**
     * Returns the first change operation.
     * @return the first change operation, <code>-1</code> if node
     */
    protected int getFirstOperationIndex() {
        return operationVisualizer.getFirstOperationIndex();
    }
    
    /**
     * Returns the last change operation.
     * @return the last change operation, <code>-1</code> if node
     */
    protected int getLastOperationIndex() {
        return operationVisualizer.getLastOperationIndex();
    }
    
    /**
     * Finds the index number of a previous change operation with a mark.
     * @return the index number of the previous change operation, or <code>-1</code> if none
     */
    protected int getPreviousMarkedOperationIndex() {
        return operationVisualizer.getPreviousMarkedOperationIndex();
    }
    
    /**
     * Finds the index number of a next change operation with a mark.
     * @return the index number of the next change operation, or <code>-1</code> if none
     */
    protected int getNextMarkedOperationIndex() {
        return operationVisualizer.getNextMarkedOperationIndex();
    }
    
    /**
     * Returns the present contents of the source code.
     * @return the present source code.
     */
    protected String getPresentCode() {
        return operationVisualizer.getPresentCode();
    }
    
    /**
     * Returns the precedent contents of the source code.
     * @return the precedent source code, or the empty string if there is no precedent source code found.
     */
    protected String getPrecedentCode() {
        return operationVisualizer.getPrecedentCode();
    }
    
    /**
     * Returns the successive contents of the source code.
     * @return the successive source code, or the empty string if there is no successive source code found.
     */
    protected String getSucessiveCode() {
        return operationVisualizer.getSucessiveCode();
    }
    
    /**
     * Returns the scale for the time range.
     * @return the percentage of the scale
     */
    protected int getTimeScale() {
        return operationVisualizer.getTimeScale();
    }
    
    /**
     * Sets the scale for the time range.
     * @param scale the percentage of the scale
     */
    protected void setTimeScale(int scale) {
        operationVisualizer.setTimeScale(scale);
    }
    
    /**
     * Zooms in the time range.
     */
    protected void zoominTimeScale() {
        operationVisualizer.zoominTimeScale() ;
    }
    
    /**
     * Zooms out the time range.
     */
    protected void zoomoutTimeScale() {
        operationVisualizer.zoomoutTimeScale();
    }
}
