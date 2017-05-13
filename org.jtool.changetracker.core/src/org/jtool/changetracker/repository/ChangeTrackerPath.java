/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.repository;

import org.jtool.changetracker.operation.IChangeOperation;

/**
 * Information about the path of a resource on which a change operation was performed.
 * @author Katsuhisa Maruyama
 */
public class ChangeTrackerPath {
    
    /**
     * The name of a project that contains the resource.
     */
    private String projectName;
    
    /**
     * The name of a package that contains the resource.
     */
    private String packageName;
    
    /**
     * The name of a file that contains the resource.
     */
    private String fileName;
    
    /**
     * The path of the resource.
     */
    private String path;
    
    /**
     * The branch of the resource.
     */
    private String branch;
    
    /**
     * Creates information about the path of a resource on which a change operation was performed.
     * @param projectName the name of a project that contains the resource
     * @param packageName the name of a package that contains the resource
     * @param fileName the name of a file that contains the resource
     */
    public ChangeTrackerPath(String projectName, String packageName, String fileName, String path, String branch) {
        this.projectName = projectName;
        this.packageName = packageName;
        this.fileName = fileName;
        this.path = path;
        this.branch = branch;
    }
    
    /**
     * Creates information about the path of a resource on which a change operation was performed.
     * @param op the change operation
     */
    public ChangeTrackerPath(IChangeOperation op) {
        this(op.getProjectName(), op.getPackageName(), op.getFileName(), op.getPath(), op.getBranch());
    }
    
    /**
     * Returns the name of a project that contains the resource.
     * @return the project name
     */
    public String getProjectName() {
        return projectName;
    }
    
    /**
     * Returns the name of a package that contains the resource.
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }
    
    /**
     * Returns the name of a file that contains the resource.
     * @return the filename
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Returns the path of the resource.
     * @return the path name
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Returns the branch of the resource.
     * @return the branch name
     */
    public String getBranch() {
        return branch;
    }
    
    /**
     * Returns the qualified name of a project.
     * @return the qualified name of the project
     */
    public String getQualifiedProjectName() {
        return projectName;
    }
    
    /**
     * Returns the qualified name of a package.
     * @return the qualified name of the package
     */
    public String getQualifiedPackageName() {
        return projectName + "#" + packageName;
    }
    
    /**
     * Returns the qualified name of a file.
     * @return the qualified name of the file
     */
    public String getQualifiedName() {
        return projectName + "#" + packageName + "$" + fileName;
    }
    
    /**
     * Returns the key for retrieving project information.
     * @return the key string for the project
     */
    public String getProjectKey() {
        return getQualifiedProjectName() + "%" + branch;
    }
    
    /**
     * Returns the key for retrieving package information.
     * @param prjname the project name
     * @param pkgname the package name
     * @param bname the branch name
     * @return the key string for the package
     */
    public String getPackageKey() {
        return getQualifiedPackageName() + "%" + branch;
    }
    
    /**
     * Returns the key for retrieving file information.
     * @return the key string for the file
     */
    public String getFileKey() {
        return getQualifiedName() + "%" + branch;
    }
}
