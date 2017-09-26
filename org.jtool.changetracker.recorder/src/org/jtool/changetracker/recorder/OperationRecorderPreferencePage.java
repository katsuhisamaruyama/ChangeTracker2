/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.jtool.changetracker.recorder.Activator;
import org.jtool.changetracker.repository.RepositoryManager;
import org.jtool.changetracker.core.CTPreferencePage;
import org.jtool.changetracker.core.CTDirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import java.io.File;

/**
 * Manages the preference page.
 * @author Katsuhisa Maruyama
 */
public class OperationRecorderPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    /**
     * The location of a directory that stores the history files of recorded operations.
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
     * The field editor that specifies location of a repository. 
     */
    private CTDirectoryFieldEditor fieldEditor;
    
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
        fieldEditor = new CTDirectoryFieldEditor(REPOSITORY_LOCATION_FOR_RECORDING,
                "Repository: ", getFieldEditorParent()) {
            
            /**
             * Stores the preference value from this field editor into the preference store.
             */
            @Override
            protected void doStore() {
                String location = CTPreferencePage.getLocation(fieldEditor.getStringValue());
                if (location != null) {
                    super.doStore();
                    RepositoryManager.getInstance().changeMainRepository(location);
                }
            }
        };
        fieldEditor.setFilterPath(new File(getLocation()));
        fieldEditor.setEmptyStringAllowed(false);
        addField(fieldEditor);
        
        addField(new BooleanFieldEditor(START_WITHOUT_PROMPT,
                "Always starts recording change operations without prompt", getFieldEditorParent()) {
            
            /**
             * Stores the preference value from this field editor into the preference store.
             */
            @Override
            protected void doStore() {
                super.doStore();
                OperationRecorder changeRecorder = OperationRecorder.getInstance();
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
                OperationRecorder changeRecorder = OperationRecorder.getInstance();
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
     * Returns the location of a directory that contains operation history files.
     * @return the location of the directory
     */
    static String getLocation() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        return store.getString(REPOSITORY_LOCATION_FOR_RECORDING);
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
