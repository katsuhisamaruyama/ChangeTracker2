/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.jtool.changetracker.repository.RepositoryManager;
import org.jtool.macrorecorder.recorder.MacroRecorder;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * @author Katsuhisa Maruyama
 */
public class Activator extends AbstractUIPlugin implements IStartup {
    
    /**
     * The plug-in ID.
     */
    public static final String PLUGIN_ID = "org.jtool.changetracker.recorder";
    
    /**
     * The plug-in instance.
     */
    private static Activator plugin;
    
    /**
     * Creates a plug-in instance.
     */
    public Activator() {
    }
    
    /**
     * Performs actions in a separate thread after the workbench initializes.
     */
    @Override
    public void earlyStartup() {
        ChangeOperationRecorder recorder = ChangeOperationRecorder.getInstance();
        recorder.displayOperationsOnConsole(ChangeOperationRecorderPreferencePage.displayOperations());
    }
    
    /**
     * Performs actions when the plug-in is activated.
     * @param context the bundle context for this plug-in
     * @throws Exception if this plug-in did not start up properly
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            
            /**
             * Runs the thread.
             */
            public void run() {
                if (ChangeOperationRecorderPreferencePage.startWithoutPrompt()) {
                    startRecording();
                } else {
                    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                    ComfirmationDialog dialog = new ComfirmationDialog(shell);
                    dialog.open();
                    ChangeOperationRecorderPreferencePage.startWithoutPrompt(dialog.withoutPrompt());
                    if (dialog.isOk()) {
                        startRecording();
                    }
                }
            }
        });
    }
    
    /**
     * Starts recording of change operations.
     */
    private void startRecording() {
        RepositoryManager manager = RepositoryManager.getInstance();
        manager.getMainRepository().addEventListener(ChangeOperationRecorder.getInstance());
        MacroRecorder.getInstance().start();
        ChangeOperationRecorder.getInstance().start();
    }
    
    /**
     * Performs actions when when the plug-in is shut down.
     * @param context the bundle context for this plug-in
     * @throws Exception if this this plug-in fails to stop
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        if (ChangeOperationRecorder.getInstance().isRunning()) {
            MacroRecorder.getInstance().stop();
            ChangeOperationRecorder.getInstance().stop();
        }
        
        super.stop(context);
        plugin = null;
    }
    
    /**
     * Returns the default plug-in instance.
     * @return the default plug-in instance
     */
    public static Activator getPlugin() {
        return plugin;
    }
}
