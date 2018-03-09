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
import org.jtool.changetracker.xml.XmlFileManager;
import org.jtool.changetracker.xml.Operation2Xml;
import org.jtool.changetracker.xml.Xml2Operation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.time.ZonedDateTime;

/**
 * Performs the action that converts from operation history in the old format into one in the new format.
 * @author Katsuhisa Maruyama
 */
public class XmlConverter {
    
    /**
     * Creates an empty instance.
     */
    public XmlConverter() {
    }
    
    /**
     * Creates the action that converts the old format of change operations into the new format.
     * @param parent the parent control
     */
    public void createAction(Composite parent) {
        Button convert = new Button(parent, SWT.NONE);
        convert.setText("Convert Format of Repository...");
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
                
                if (Xml2Operation.isChangeTrackerVersion2(dirpath)) {
                    CTDialog.errorDialog("Convert Format",
                            "No need to convert since the XML format is compatible with ChangeTracker v2");
                    return;
                }
                
                String timeString = String.valueOf(ZonedDateTime.now().toInstant().toEpochMilli());
                String convertedPath = dirpath + File.separatorChar + "_converted-" + timeString;
                XmlFileManager.makeDir(new File(convertedPath));
                
                convert(dirpath, convertedPath);
                CTDialog.informationDialog("Convert Format", "The converted files are stored in " + dirpath);
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
     * Converts the format of change operations stored in a source directory and stores the converted ones into a target directory.
     * @param dirpath the source directory
     * @param convertedPath the target directory
     */
    public void convert(String dirpath, String convertedPath) {
        Map<String, List<IChangeOperation> > fileMap = new HashMap<>();
        
        Repository repository = new Repository(dirpath);
        List<File> files = Xml2Operation.getHistoryFiles(dirpath, "_");
        for (File file : files) {
            String path = file.getAbsolutePath();
            
            List<IChangeOperation> ops = Xml2Operation.getOperations(path);
            if (ops.size() > 0) {
                ops = OperationCompactor.compact(ops);
            }
            
            repository.addOperationAll(ops);
            fileMap.put(file.getAbsolutePath(), ops);
        }
        
        repository.checkOperationConsistency();
        
        for (File file : files) {
            List<IChangeOperation> ops = fileMap.get(file.getAbsolutePath());
            String filename = convertedPath + File.separatorChar + file.getName();
            Operation2Xml.storeOperations(ops, filename);
        }
    }
}
