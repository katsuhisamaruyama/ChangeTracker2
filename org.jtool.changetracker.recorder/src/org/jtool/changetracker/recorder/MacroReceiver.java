/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.jtool.macrorecorder.recorder.IMacroHandler;
import org.jtool.macrorecorder.recorder.IMacroRecorder;
import org.jtool.macrorecorder.recorder.MacroEvent;
import org.jtool.macrorecorder.recorder.MacroRecorder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * Records change operations that were performed on the Eclipse' editor.
 * @author Katsuhisa Maruyama
 */
public class MacroReceiver implements IMacroHandler {
    
    /**
     * The single instance that records change operations.
     */
    private OperationRecorder operationRecorder;
    
    /**
     * A flag that indicates if the recording is allowed or not.
     */
    private boolean recordingOk;
    
    /**
     * A flag that indicates if the recording is on.
     */
    private boolean onRecording = true;
    
    /**
     * Creates an object that records change operations.
     */
    public MacroReceiver() {
        operationRecorder = new OperationRecorder(this);
    }
    
    /**
     * Confirms the recording.
     * @return <code>true</code> if the recording is allowed, otherwise <code>false</code>
     */
    public boolean recordingAllowed() {
        recordingOk = false;
        UIJob job = new UIJob("Confirm") {
            
            /**
             * Run the job in the UI thread.
             * @param monitor the progress monitor to use to display progress
             */
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (OperationRecorderPreferencePage.startWithoutPrompt()) {
                    recordingOk = true;
                } else {
                    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                    ComfirmationDialog dialog = new ComfirmationDialog(shell);
                    dialog.open();
                    OperationRecorderPreferencePage.startWithoutPrompt(dialog.withoutPrompt());
                    if (dialog.isOk()) {
                        recordingOk = true;
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
        
        try {
            job.join();
        } catch (InterruptedException e) {
            recordingOk = false;
        }
        return recordingOk;
    }
    
    /**
     * Initializes this handler immediately before starting the macro recording.
     */
    @Override
    public void initialize() {
        operationRecorder.initialize();
    }
    
    /**
     * Terminate this handler immediately after stopping the macro recording.
     */
    @Override
    public void terminate() {
        operationRecorder.terminate();
    }
    
    /**
     * Receives a macro event when a new macro is added.
     * @param evt the macro event
     */
    @Override
    public void macroAdded(MacroEvent evt) {
        operationRecorder.recordMacro(evt.getMacro());
    }
    
    /**
     * Receives a macro event when a new raw macro is added.
     * @param evt the raw macro event
     */
    @Override
    public void rawMacroAdded(MacroEvent evt) {
    }
    
    /**
     * Starts the recording.
     */
    void start() {
        if (!onRecording) {
            IMacroRecorder macroRecorder = MacroRecorder.getInstance();
            macroRecorder.addMacroListener(this);
            macroRecorder.start();
        }
    }
    
    /**
     * Stops the recording.
     */
    void stop() {
        if (onRecording) {
            IMacroRecorder macroRecorder = MacroRecorder.getInstance();
            macroRecorder.removeMacroListener(this);
            macroRecorder.stop();
        }
    }
}
