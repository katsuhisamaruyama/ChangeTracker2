/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * A confirmation dialog.
 * @author Katsuhisa Maruyama
 */
public class ComfirmationDialog extends MessageDialog {
    
    /**
     * A flag that indicates if the confirmation succeeds.
     */
    private boolean ok;
    
    /**
     * A flag that indicates if the recorder can start recording change operations without prompt.
     */
    private boolean prompt;
    
    /**
     * Creates a confirmation dialog.
     * @param shell the shell of this dialog
     */
    public ComfirmationDialog(Shell shell) {
        super(shell, "ChangeOperationRecorder Confirmation",
                null, "Could you permit the ChangeOperationRecorder plug-in to record change operations?",
                MessageDialog.CONFIRM, new String[] { "Yes", "No" }, 0);
    }
    
    /**
     * Creates the custom area of this dialog.
     * @param parent the parent composite to contain the dialog area
     * @return the control of the custom area
     */
    @Override
    protected Control createCustomArea(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setLayout(new GridLayout());
        Button message = new Button(area, SWT.CHECK);
        message.setText("Always starts recording change operations without prompt");
        message.addSelectionListener(new SelectionListener() {
            
            /**
             * Receives a selection event when selection occurs in the control.
             * @param evt the event containing information about the selection
             */
            @Override
            public void widgetSelected(SelectionEvent evt) {
                prompt = message.getSelection();
            }
            
            /**
             * Receives a selection event when default selection occurs in the control.
             * @param evt the event containing information about the selection
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
        });
        message.setSelection(false);
        return area;
    }
    
    /**
     * Receives the button id when the button was pressed.
     * @param bid the id of the button that was pressed
     */
    @Override
    protected void buttonPressed(int bid) {
        if(bid == OK) {
            ok = true;
        } else {
            ok = false;
        }
        super.buttonPressed(bid);
    }
    
    /**
     * Tests if the confirmation succeeds.
     * @return <code>true</code> if the confirmation succeeds, otherwise <code>false</code>
     */
    boolean isOk() {
        return ok;
    }
    
    /**
     * Tests if the recorder can start recording change operations without prompt.
     */
    boolean withoutPrompt() {
        return prompt;
    }
}
