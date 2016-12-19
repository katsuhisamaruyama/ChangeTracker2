/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.parser;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Stores information about a compilation unit.
 * @author Katsuhisa Maruyama
 */
public class OpCompilationUnit {
    
    /**
     * The Eclipse's compilation unit object corresponding to this compilation unit.
     */
    private ICompilationUnit compilationUnit;
    
    /**
     * The file path of this compilation unit.
     */
    private String path;
    
    /**
     * The collections of Java elements within this compilation unit.
     */
    private List<OpJavaElement> elements;
    
    /**
     * The time when this compilation unit was generated.
     */
    private ZonedDateTime time;
    
    /**
     * Creates an instance that stores information about a compilation unit.
     * @param cu the compilation unit
     * @param path a file path of the compilation unit
     * @param time the time when the compilation unit was generated
     * @param elements the collection of the class members within the compilation unit
     */
    public OpCompilationUnit(CompilationUnit cu, String path, ZonedDateTime time, List<OpJavaElement> elements) {
        compilationUnit = (ICompilationUnit)cu.getJavaElement();
        this.path = path;
        this.time = time;
        this.elements = elements;
    }
    
    /**
     * Returns the compilation unit stored in this compilation unit.
     * @return the Eclipse' compilation unit
     */
    public ICompilationUnit getCompilationUnit() {
        return compilationUnit;
    }
    
    /**
     * Returns the path name of the source file.
     * @return the path name.
     */
    public String getFilePath() {
        return path;
    }
    
    /**
     * Returns the time when this compilation unit was generated.
     * @return the time of this compilation unit
     */
    public ZonedDateTime getTime() {
        return time;
    }
    
    /**
     * Returns the Java elements within this compilation unit.
     * @return the collection of the Java elements
     */
    public List<OpJavaElement> getJavaElements() {
        return elements;
    }
    
    /**
     * Restores the contents of the working copy of this compilation unit to the current contents.
     * @return <code>true</code> if the restoration succeeds, otherwise <code>false</code>
     */
    protected boolean restore() {
        try {
            compilationUnit.restore();
        } catch (JavaModelException e) {
            return false;
        }
        return true;
    }
    
    /**
     * Reconciles the contents of the working copy of this compilation unit.
     * @return the compilation unit if the reconciliation succeeds, otherwise <code>null</code>
     */
    protected OpCompilationUnit reconcile() {
        try {
            CompilationUnit cu = compilationUnit.reconcile(AST.JLS8, true, null, null);
            return new OpCompilationUnit(cu, path, time, elements);
        } catch (JavaModelException e) {
            return null;
        }
    }
    
    /**
     * Returns the file resource for this compilation unit.
     * @return the Eclipse's file resource, or <code>null</code> the file resource was not found
     */
    public IFile getIFile() {
        try {
            IResource resource = compilationUnit.getCorrespondingResource();
            if (resource instanceof IFile) {
                return (IFile)resource;
            }
        } catch (CoreException e) {
        }
        return null;
    }
    
    /**
     * Obtains the source code of this compilation unit.
     * @return the contents of the source code, or <code>null</code> if the source code was not obtained
     */
    public String getCode() {
        try {
            return compilationUnit.getSource();
        } catch (JavaModelException e) {
        }
        return null;
    }
}
