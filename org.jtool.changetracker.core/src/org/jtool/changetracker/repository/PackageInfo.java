/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores information on a package.
 * @author Katsuhisa Maruyama
 */
public class PackageInfo extends ElementInfo {
    
    /**
     * The project containing this package.
     */
    private ProjectInfo projectInfo;
    
    /**
     * The collection of all files within this package.
     */
    private List<FileInfo> files = new ArrayList<FileInfo>();
    
    /**
     * Creates an instance that stores information on this package.
     * @param name the name of the package
     * @param pinfo the data on the project containing the package
     */
    PackageInfo(String name, ProjectInfo pinfo) {
        super(name, pinfo.getName() + "#" + name);
        projectInfo = pinfo;
    }
    
    /**
     * Clears the information on this package.
     */
    public void clear() {
        files.clear();
    }
    
    /**
     * Returns the information on the project containing this package.
     * @return the project information
     */
    public ProjectInfo getProject() {
        return projectInfo;
    }
    
    /**
     * Stores the file data.
     * @param data the file data to be stored
     */
    public void addFile(FileInfo finfo) {
        if (!files.contains(finfo)) {
            files.add(finfo);
        }
    }
    
    /**
     * Returns all the data on the packages within this project.
     * @return the collection of the package data
     */
    public List<FileInfo> getAllFiles() {
        ElementInfo.sort(files);
        return files;
    }
    
    /**
     * Collects all the code change operations related to this package.
     * @return the collection of the code change operations related to the package
     */
    public List<IChangeOperation> getOperations() {
        List<IChangeOperation> ops = new ArrayList<IChangeOperation>();
        for (FileInfo data : files) {
            ops.addAll(data.getOperations());
        }
        return ops;
    }
}
