/*
 *  Copyright 2018
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.convert;

import org.jtool.changetracker.core.CTDialog;
import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.repository.OperationCompactor;
import org.jtool.changetracker.repository.Repository;
import org.jtool.changetracker.xml.Xml2Operation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import java.util.List;
import java.io.File;

/**
 * Performs the action that checks change operations stored into a repository.
 * @author Katsuhisa Maruyama
 */
public class XmlChecker {
    
    /**
     * Creates an empty instance.
     */
    public XmlChecker() {
    }
    
    /**
     * Creates the action that checks change operations stored into a repository.
     * @param parent the parent control
     */
    public void createAction(Composite parent) {
        Button convert = new Button(parent, SWT.NONE);
        convert.setText("Check Operations in Repository...");
        convert.addSelectionListener(new SelectionListener() {
            
            /**
             * Receives a selection event when selection occurs in the control.
             * @param evt the event containing information about the selection
             */
            @Override
            public void widgetSelected(SelectionEvent evt) {
                DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
                String dirpath = dialog.open();
                if (dirpath == null) {
                    return;
                }
                
                boolean result = check(dirpath);
                if (result) {
                    CTDialog.informationDialog("Check Operations", "Success" + dirpath);
                }
            }
            
            /**
             * Receives a selection event when default selection occurs in the control.
             * @param evt the event containing information about the selection
             */
            @Override
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
        });
    }
    
    /**
     * Checks change operations that are stored in the operation history files.
     * @param dirpath the path of the directory that contains the converted files
     * @return <code>true</code> if all the change operations are consistent with the restored code, otherwise <code>false</code>
     */
    private boolean check(String dirpath) {
        Repository repository = new Repository(dirpath);
        List<File> files = Xml2Operation.getHistoryFiles(dirpath);
        for (File file : files) {
            String path = file.getAbsolutePath();
            
            List<IChangeOperation> ops = Xml2Operation.getOperations(path);
            if (ops.size() > 0) {
                ops = OperationCompactor.compact(ops);
            }
            
            repository.addOperationAll(ops);
        }
        
        return repository.checkOperationConsistency();
    }
}
