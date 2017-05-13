/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.recorder;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializes the preference values.
 * @author Katsuhisa Maruyama
 */
public class ChangeOperationRecorderPreferenceInitializer extends AbstractPreferenceInitializer {
    
    /**
     * Stores initial preference values.
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getPlugin().getPreferenceStore();
        store.setDefault(ChangeOperationRecorderPreferencePage.DISPLAY_OPERATIONS, false);
        store.setDefault(ChangeOperationRecorderPreferencePage.START_WITHOUT_PROMPT, false);
    }
}
