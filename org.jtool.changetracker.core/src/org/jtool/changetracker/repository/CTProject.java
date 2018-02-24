/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import org.jtool.changetracker.operation.ChangeOperation;
import java.util.List;
import java.util.ArrayList;
import java.time.ZonedDateTime;

/**
 * Stores information about a project.
 * @author Katsuhisa Maruyama
 */
public class CTProject extends CTResource {
    
    /**
     * The branch name of this project.
     */
    private String branch;
    
    /**
     * The collection of all packages within this project.
     */
    private List<CTPackage> packages = new ArrayList<CTPackage>();
    
    /**
     * Creates an instance that stores information about this project.
     * @param pathinfo information about path of this project
     */
    public CTProject(CTPath pathinfo) {
        super(pathinfo.getProjectName(), pathinfo.getQualifiedProjectName());
        this.branch = pathinfo.getBranch();
    }
    
    /**
     * Clears the information about this project.
     */
    public void clear() {
        packages.clear();
    }
    
    /**
     * Stores information about a package within this project.
     * @param pinfo the package information to be stored
     */
    void addPackage(CTPackage pinfo) {
        if (!packages.contains(pinfo)) {
            packages.add(pinfo);
            CTResource.sort(packages);
        }
    }
    
    /**
     * Returns the branch name of this project.
     * @return the branch name
     */
    public String getBranch() {
        return branch;
    }
    
    /**
     * Returns information about packages within this project.
     * @return information of the packages
     */
    public List<CTPackage> getPackages() {
        return packages;
    }
    
    /**
     * Returns the files within this project.
     * @return the collection of the file information
     */
    public List<CTFile> getFiles() {
        List<CTFile> files = new ArrayList<CTFile>();
        for (CTPackage pinfo : getPackages()) {
            files.addAll(pinfo.getFiles());
        }
        return files;
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
    
    /**
     * Returns change operations related to this project.
     * @return the collection of the change operations
     */
    public List<IChangeOperation> getOperations() {
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        for (CTFile finfo : getFiles()) {
            ops.addAll(finfo.getOperations());
        }
        ChangeOperation.sort(ops);
        return ops;
    }
}
