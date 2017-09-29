/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.replayer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * Buttons that moves the selection of change operations.
 * @author Katsuhisa Maruyama
 */
public class ButtonControl {
    
    /**
     * A history view that contains this button control.
     */
    protected HistoryView historyView;
    
    /**
     * The composite for the buttons.
     */
    protected Composite buttons;
    
    /**
     * The button for going backward to the precedent change operation.
     */
    protected Button goPrecButton;
    
    /**
     * The button for going forward to the successive change operation.
     */
    protected Button goSuccButton;
    
    /**
     * The button for going backward over the previous change operations.
     */
    protected Button fastRewindButton;
    
    /**
     * The button for going forward over the next change operations.
     */
    protected Button fastForwardButton;
    
    /**
     * The button for jumping to the first change operation.
     */
    protected Button goFirstButton;
    
    /**
     * The button for jumping to the last change operation.
     */
    protected Button goLastButton;
    
    /**
     * The listener that receives an event related to the button selection.
     */
    protected ButtonSelectionListener buttonSelectionListener;
    
    /**
     * Creates replay buttons.
     * @param view the history view that contains the button control
     */
    protected ButtonControl(HistoryView view) {
        historyView = view;
    }
    
    /**
     * Creates a control of the buttons.
     * @param parent the parent control
     */
    public void createButtons(Composite parent) {
        buttons = new Composite(parent, SWT.NONE);
        buttonSelectionListener = new ButtonSelectionListener();
        
        goFirstButton = new Button(buttons, SWT.FLAT);
        goFirstButton.setToolTipText("Go to the first change operation");
        goFirstButton.setImage(ToolBarAction.goFirstIcon.createImage());
        goFirstButton.setEnabled(true);
        goFirstButton.addSelectionListener(buttonSelectionListener);
        goFirstButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        fastRewindButton = new Button(buttons, SWT.FLAT);
        fastRewindButton.setToolTipText("Go to a previous change operation with a mark");
        fastRewindButton.setImage(ToolBarAction.fastRewindIcon.createImage());
        fastRewindButton.setEnabled(true);
        fastRewindButton.addSelectionListener(buttonSelectionListener);
        fastRewindButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        goPrecButton = new Button(buttons, SWT.FLAT);
        goPrecButton.setToolTipText("Go to the precedent change operation");
        goPrecButton.setImage(ToolBarAction.goPrecIcon.createImage());
        goPrecButton.setEnabled(true);
        goPrecButton.addSelectionListener(buttonSelectionListener);
        goPrecButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        goSuccButton = new Button(buttons, SWT.FLAT);
        goSuccButton.setToolTipText("Go to the successive change operation");
        goSuccButton.setImage(ToolBarAction.goSuccIcon.createImage());
        goSuccButton.setEnabled(true);
        goSuccButton.addSelectionListener(buttonSelectionListener);
        goSuccButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        fastForwardButton = new Button(buttons, SWT.FLAT);
        fastForwardButton.setToolTipText("Go to the next change operation with a mark");
        fastForwardButton.setImage(ToolBarAction.fastForwardIcon.createImage());
        fastForwardButton.setEnabled(true);
        fastForwardButton.addSelectionListener(buttonSelectionListener);
        fastForwardButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        goLastButton = new Button(buttons, SWT.FLAT);
        goLastButton.setToolTipText("Go to the last change operation");
        goLastButton.setImage(ToolBarAction.goLastIcon.createImage());
        goLastButton.setEnabled(true);
        goLastButton.addSelectionListener(buttonSelectionListener);
        goLastButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
        
        final int MARGIN = 2;
        GridLayout btlayout = new GridLayout();
        btlayout.numColumns = 6;
        btlayout.makeColumnsEqualWidth = true;
        btlayout.marginWidth = 0;
        btlayout.marginHeight = 0;
        btlayout.horizontalSpacing = MARGIN;
        btlayout.marginHeight = MARGIN;
        btlayout.marginWidth = MARGIN;
        btlayout.marginLeft = MARGIN;
        btlayout.marginRight = MARGIN;
        btlayout.marginTop = MARGIN;
        btlayout.marginBottom = MARGIN;
        buttons.setLayout(btlayout);
        
        FormData btdata = new FormData();
        btdata.bottom = new FormAttachment(100, 0);
        btdata.left = new FormAttachment(0, 0);
        btdata.right = new FormAttachment(100, 0);
        buttons.setLayoutData(btdata);
    }
    
    /**
     * Returns the control for the buttons.
     * @return the buttons control
     */
    public Control getControl() {
        return buttons;
    }
    
    /**
     * Disposes the control of the replay buttons.
     */
    public void dispose() {
        if (!goFirstButton.isDisposed()) {
            goFirstButton.removeSelectionListener(buttonSelectionListener);
        }
        if (!fastRewindButton.isDisposed()) {
            fastRewindButton.removeSelectionListener(buttonSelectionListener);
        }
        if (!goPrecButton.isDisposed()) {
            goPrecButton.removeSelectionListener(buttonSelectionListener);
        }
        if (!goSuccButton.isDisposed()) {
            goSuccButton.removeSelectionListener(buttonSelectionListener);
        }
        if (!fastForwardButton.isDisposed()) {
            fastForwardButton.removeSelectionListener(buttonSelectionListener);
        }
        if (!goLastButton.isDisposed()) {
            goLastButton.removeSelectionListener(buttonSelectionListener);
        }
        goFirstButton.dispose();
        fastRewindButton.dispose();
        goPrecButton.dispose();
        goSuccButton.dispose();
        fastForwardButton.dispose();
        goLastButton.dispose();
    }
    
    /**
     * Updates the replay buttons.
     */
    public void update() {
        int index = historyView.getFirstOperationIndex();
        if (historyView.getPresentIndex() != index) {
            goFirstButton.setEnabled(true);
        } else {
            goFirstButton.setEnabled(false);
        }
        index = historyView.getPreviousMarkedOperationIndex();
        if (index != -1) {
            fastRewindButton.setEnabled(true);
        } else {
            fastRewindButton.setEnabled(false);
        }
        index = historyView.getPrecedentOperationIndex();
        if (index != -1) {
            goPrecButton.setEnabled(true);
        } else {
            goPrecButton.setEnabled(false);
        }
        index = historyView.getSuccessiveOperationIndex();
        if (index != -1) {
            goSuccButton.setEnabled(true);
        } else {
            goSuccButton.setEnabled(false);
        }
        index = historyView.getNextMarkedOperationIndex();
        if (index != -1) {
            fastForwardButton.setEnabled(true);
        } else {
            fastForwardButton.setEnabled(false);
        }
        index = historyView.getLastOperationIndex();
        if (historyView.getPresentIndex() != index) {
            goLastButton.setEnabled(true);
        } else {
            goLastButton.setEnabled(false);
        }
    }
    
    /**
     * Resets the replay buttons.
     */
    public void reset() {
        if (!goFirstButton.isDisposed()) {
            goFirstButton.setEnabled(false);
        }
        if (!fastRewindButton.isDisposed()) {
            fastRewindButton.setEnabled(false);
        }
        if (!goPrecButton.isDisposed()) {
            goPrecButton.setEnabled(false);
        }
        if (!goSuccButton.isDisposed()) {
            goSuccButton.setEnabled(false);
        }
        if (!fastForwardButton.isDisposed()) {
            fastForwardButton.setEnabled(false);
        }
        if (!goLastButton.isDisposed()) {
            goLastButton.setEnabled(false);
        }
    }
    
    /**
     * Deals with the button events.
     */
    protected class ButtonSelectionListener implements SelectionListener {
        
        /**
         * Creates a listener that receives button selection events.
         */
        ButtonSelectionListener() {
        }
        
        /**
         * Receives the button event when the default button selection occurs in the control.
         * @param evt the event containing information about the default selection
         */
        @Override
        public void widgetDefaultSelected(SelectionEvent evt) {
        }
        
        /**
         * Receives the selection event when button selection occurs in the control.
         * @param evt the event containing information about the selection
         */
        @Override
        public void widgetSelected(SelectionEvent evt) {
            if (!historyView.readyToVisualize()) {
                return;
            }
            
            int index = -1;
            Object source = evt.getSource();
            if (source == goFirstButton) {
                index = historyView.getFirstOperationIndex();
            } else if (source == fastRewindButton) {
                index = historyView.getPreviousMarkedOperationIndex();
            } else if (source == goPrecButton) {
                index = historyView.getPrecedentOperationIndex();
            } else if (source == goSuccButton) {
                index = historyView.getSuccessiveOperationIndex();
            } else if (source == fastForwardButton) {
                index = historyView.getNextMarkedOperationIndex();
            } else if (source == goLastButton) {
                index = historyView.getLastOperationIndex();
            }
            if (index != -1) {
                historyView.goTo(index);
            }
        }
    }
}
