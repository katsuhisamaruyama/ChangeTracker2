/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jtool.changetracker.core.CTPreferencePage;

/**
 * Initializes the preference values.
 * @author Katsuhisa Maruyama
 */
public class OperationRecorderPreferenceInitializer extends AbstractPreferenceInitializer {
    
    /**
     * Stores initial preference values.
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        String defaultPath = CTPreferencePage.getDefaultPath();
        store.setDefault(OperationRecorderPreferencePage.REPOSITORY_LOCATION_FOR_RECORDING, defaultPath);
        store.setDefault(OperationRecorderPreferencePage.DISPLAY_OPERATIONS, false);
        store.setDefault(OperationRecorderPreferencePage.START_WITHOUT_PROMPT, false);
    }
}
