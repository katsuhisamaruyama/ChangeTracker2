/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.jtool.changetracker.recorder.Activator;
import org.jtool.changetracker.core.CTDirectoryFieldEditor;
import org.jtool.changetracker.core.CTPreferencePage;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import java.io.File;

/**
 * Manages the preference page.
 * @author Katsuhisa Maruyama
 */
public class OperationRecorderPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    /**
     * The location of a directory that stores the history files of recorded change operations.
     */
    static final String REPOSITORY_LOCATION_FOR_RECORDING = "repository.location.recording";
    
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
    public OperationRecorderPreferencePage() {
        super(GRID);
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        setPreferenceStore(store);
    }
    
    /**
     * Creates the field editors for preference settings.
     */
    @Override
    public void createFieldEditors() {
        CTDirectoryFieldEditor fieldEditor = new CTDirectoryFieldEditor(REPOSITORY_LOCATION_FOR_RECORDING,
                "Repository: ", getFieldEditorParent()) {
            
            /**
             * Stores the preference value from this field editor into the preference store.
             */
            @Override
            protected void doStore() {
                if (!getLocation().equals(getStringValue())) {
                    String location = CTPreferencePage.getLocation(getStringValue());
                    if (location != null) {
                        super.doStore();
                        OperationRecorder.getInstance().changeRepository(location);
                    }
                }
            }
        };
        fieldEditor.setFilterPath(new File(getLocation()));
        fieldEditor.setEmptyStringAllowed(false);
        addField(fieldEditor);
        
        addField(new BooleanFieldEditor(START_WITHOUT_PROMPT,
                "Always starts recording change operations without prompt", getFieldEditorParent()));
        
        addField(new BooleanFieldEditor(DISPLAY_OPERATIONS,
                "Displays recorded change operations on the console", getFieldEditorParent()) {
            
            /**
             * Stores the preference value from this field editor into the preference store.
             */
            @Override
            protected void doStore() {
                super.doStore();
                OperationRecorder.getInstance().displayOperations(getBooleanValue());
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
     * Returns the location of a directory that contains operation history files.
     * @return the location of the directory
     */
    public static String getLocation() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        return CTPreferencePage.getLocation(store.getString(REPOSITORY_LOCATION_FOR_RECORDING));
    }
    
    /**
     * Sets the location of a directory that contains operation history files.
     * @param location the location of the directory
     */
    public static void setLocation(String location) {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        store.setValue(REPOSITORY_LOCATION_FOR_RECORDING, CTPreferencePage.getPath(location));
    }
    
    /**
     * Tests if the recorder starts recording change operations without prompt.
     * @return <code>true</code> if the recording is started without prompt, otherwise <code>false</code>
     */
    public static boolean startWithoutPrompt() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        return store.getBoolean(START_WITHOUT_PROMPT);
    }
    
    /**
     * Sets if the recorder starts recording change operations without prompt.
     * @param bool <code>true</code> if the recording is started without prompt, otherwise <code>false</code>
     */
    public static void startWithoutPrompt(boolean bool) {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        store.setValue(START_WITHOUT_PROMPT, bool);
    }
    
    /**
     * Tests if recorded change operations will be displayed.
     * @return <code>true</code> if the displaying is required, otherwise <code>false</code>
     */
    public static boolean displayOperations() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        return store.getBoolean(DISPLAY_OPERATIONS);
    }
}
