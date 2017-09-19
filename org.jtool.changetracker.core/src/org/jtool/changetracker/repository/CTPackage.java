/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;

/**
 * Stores information about a package.
 * @author Katsuhisa Maruyama
 */
public class CTPackage extends CTResource {
    
    /**
     * The project containing this package.
     */
    private CTProject projectInfo;
    
    /**
     * The collection of all files within this package.
     */
    private List<CTFile> files = new ArrayList<CTFile>();
    
    /**
     * Creates an instance that stores information on this package.
     * @param pathinfo information about path of this package
     * @param pinfo the information about a project containing this package
     */
    CTPackage(CTPath pathinfo, CTProject pinfo) {
        super(pathinfo.getPackageName(), pathinfo.getQualifiedPackageName());
        projectInfo = pinfo;
    }
    
    /**
     * Clears the information about this package.
     */
    public void clear() {
        files.clear();
    }
    
    /**
     * Returns information about the project containing this package.
     * @return the project information
     */
    public CTProject getProject() {
        return projectInfo;
    }
    
    /**
     * Stores information about a file within this package.
     * @param finfo the file information to be stored
     */
    public void addFile(CTFile finfo) {
        if (!files.contains(finfo)) {
            files.add(finfo);
            CTResource.sort(files);
        }
    }
    
    /**
     * Returns information about files within this package.
     * @return information about the files
     */
    public List<CTFile> getFiles() {
        return files;
    }
    
    /**
     * Collects change operations related to this package.
     * @return the collection of the change operations
     */
    public List<IChangeOperation> getOperations() {
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        for (CTFile finfo : getFiles()) {
            ops.addAll(finfo.getOperations());
        }
        return ops;
    }
    
    /**
     * Finds a change operation that was performed at the specified time.
     * @param time the specified time
     * @return the found operation, or <code>null</code> if none
     */
    public IChangeOperation getOperationAt(ZonedDateTime time) {
        for (CTFile finfo : getFiles()) {
            IChangeOperation op = finfo.getOperationAt(time);
            if (op != null) {
                return op;
            }
        }
        return null;
    }
}
