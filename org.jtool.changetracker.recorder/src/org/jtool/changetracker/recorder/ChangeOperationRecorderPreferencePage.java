/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.jtool.changetracker.recorder.Activator;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

/**
 * Manages the preference page.
 * @author Katsuhisa Maruyama
 */
public class ChangeOperationRecorderPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    /**
     * Displays change operations on the console for debugging.
     */
    static final String DISPLAY_OPERATIONS = "display.opetrations";
    
    /**
     * Starts recording change operations without prompt when the recorder is activated.
     */
    static final String START_WITHOUT_PROMPT = "start.recording.without.prompt";
    
    /**
     * Creates an object for a preference page.
     */
    public ChangeOperationRecorderPreferencePage() {
        super(GRID);
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        setPreferenceStore(store);
    }
    
    /**
     * Creates the field editors for preference settings.
     */
    @Override
    public void createFieldEditors() {
        addField(new BooleanFieldEditor(START_WITHOUT_PROMPT,
                "Always starts recording change operations without prompt", getFieldEditorParent()) {
            
            /**
             * Stores the preference value from this field editor into the preference store.
             */
            @Override
            protected void doStore() {
                super.doStore();
                ChangeOperationRecorder changeRecorder = ChangeOperationRecorder.getInstance();
                changeRecorder.displayOperationsOnConsole(getBooleanValue());
            }
        });
        
        addField(new BooleanFieldEditor(DISPLAY_OPERATIONS,
                "Displays recorded change operations on the console", getFieldEditorParent()) {
            
            /**
             * Stores the preference value from this field editor into the preference store.
             */
            @Override
            protected void doStore() {
                super.doStore();
                ChangeOperationRecorder changeRecorder = ChangeOperationRecorder.getInstance();
                changeRecorder.displayOperationsOnConsole(getBooleanValue());
            }
        });
    }
    
    /**
     * Initializes a preference page for a given workbench.
     */
    @Override
    public void init(IWorkbench workbench) {
    }
    
    /**
     * Tests if the recorder starts recording change operations without prompt.
     * @return <code>true</code> if the recording is started without prompt, otherwise <code>false</code>
     */
    static boolean startWithoutPrompt() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        return store.getBoolean(START_WITHOUT_PROMPT);
    }
    
    /**
     * Sets if the recorder starts recording change operations without prompt.
     * @param bool <code>true</code> if the recording is started without prompt, otherwise <code>false</code>
     */
    static void startWithoutPrompt(boolean bool) {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        store.setValue(START_WITHOUT_PROMPT, bool);
    }
    
    /**
     * Tests if recorded change operations will be displayed.
     * @return <code>true</code> if the displaying is required, otherwise <code>false</code>
     */
    static boolean displayOperations() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        return store.getBoolean(DISPLAY_OPERATIONS);
    }
}
