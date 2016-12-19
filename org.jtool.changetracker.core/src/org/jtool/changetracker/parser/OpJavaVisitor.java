/*
 *  Copyright 2016
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.parser;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.jtool.changetracker.repository.FileInfo;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Collects Java elements within the source code to be parsed.
 * @author Katsuhisa Maruyama
 */
public class OpJavaVisitor extends ASTVisitor {
    
    /**
     * The file information about the AST to be visited.
     */
    private FileInfo fileInfo;
    
    /**
     * The collection of Java elements appearing in the AST.
     */
    private List<OpJavaElement> elements = new ArrayList<OpJavaElement>();
        
    /**
     * The stack that stores classes in the AST.
     */
    private Stack<OpClass> classes = new Stack<OpClass>();
    
    /**
     * The stack that stores parent Java elements in the AST.
     */
    private Stack<OpJavaElement> parents = new Stack<OpJavaElement>();
    
    /**
     * Creates a visitor visiting the AST.
     * @param the file information about AST to be visited
     */
    public OpJavaVisitor(FileInfo finfo) {
        this.fileInfo = finfo;
    }
    
    /**
     * Returns all the Java elements appearing in the AST.
     * @return the collection of the Java elements
     */
    public List<OpJavaElement> getJavaElements() {
        return elements;
    }
    
    /**
     * Stores information about a class.
     * @param node the class node to be visited
     * @return always <code>true</code> to visit its child nodes
     */
    @Override
    public boolean visit(TypeDeclaration node) {
        int start = node.getStartPosition();
        int end = start + node.getLength() - 1;
        String name = node.getName().getFullyQualifiedName();
        OpClass clazz = new OpClass(start, end, fileInfo, name);
        
        if (parents.size() > 0) {
            elements.add(clazz);
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(clazz);
        }
        
        parents.push(clazz);
        classes.push(clazz);
        
        return true;
    }
    
    /**
     * Discards the visited class.
     * @param node the visited class node
     */
    @Override
    public void endVisit(TypeDeclaration node) {
        classes.pop();
        parents.pop();
    }
    
    /**
     * Stores information about an enum class.
     * @param node the enum class node to be visited
     * @return always <code>true</code> to visit its child nodes
     */
    @Override
    public boolean visit(EnumDeclaration node) {
        int start = node.getStartPosition();
        int end = start + node.getLength() - 1;
        String name = node.getName().getFullyQualifiedName();
        OpClass clazz = new OpClass(start, end, fileInfo, name);
        
        if (parents.size() > 0) {
            elements.add(clazz);
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(clazz);
        }
        
        parents.push(clazz);
        classes.push(clazz);
        
        return true;
    }
    
    /**
     * Discards the visited enum class.
     * @param node the visited enum class node
     */
    @Override
    public void endVisit(EnumDeclaration node) {
        classes.pop();
        parents.pop();
    }
    
    /**
     * Stores information about an anonymous class.
     * @param node the anonymous class node to be visited
     * @return always <code>true</code> to visit its child nodes
     */
    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        int start = node.getStartPosition();
        int end = start + node.getLength() - 1;
        String name = "$";
        OpClass clazz = new OpClass(start, end, fileInfo, name);
        
        if (parents.size() > 0) {
            elements.add(clazz);
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(clazz);
        }
        parents.push(clazz);
        classes.push(clazz);
        
        return true;
    }
    
    /**
     * Discards the visited anonymous class.
     * @param node the visited anonymous class node
     */
    @Override
    public void endVisit(AnonymousClassDeclaration node) {
        classes.pop();
        parents.pop();
    }
    
    /**
     * Returns the name of a class that is currently visited.
     * @return the class name.
     */
    private String getClassName() {
        StringBuffer buf = new StringBuffer();
        for (OpClass c: classes) {
            buf.append("%");
            buf.append(c.getName());
        }
        return buf.substring(1);
    }
    
    /**
     * Stores information about a method
     * @param node the method node to be visited
     * @return always <code>true</code> to visit its child nodes
     */
    @Override
    public boolean visit(MethodDeclaration node) {
        int start = node.getStartPosition();
        int end = start + node.getLength() - 1;
        @SuppressWarnings("unchecked")
        String name = getClassName() + "#" + node.getName().getIdentifier() + getFormalParameters(node.parameters());
        
        OpMethod method = new OpMethod(start, end, fileInfo, name);
        elements.add(method);
        
        if (parents.size() > 0) {
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(method);
        }
        parents.push(method);
        
        return true;
    }
    
    /**
     * Discards the visited method.
     * @param node the visited method node
     */
    @Override
    public void endVisit(MethodDeclaration node) {
        parents.pop();
    }
    
    /**
     * Stores information about in initializer method
     * @param node the initializer method node to be visited
     * @return always <code>true</code> to visit its child nodes
     */
    @Override
    public boolean visit(Initializer node) {
        int start = node.getStartPosition();
        int end = start + node.getLength() - 1;
        String name = getClassName() + "#" + "$Init()";
        
        OpMethod method = new OpMethod(start, end, fileInfo, name);
        elements.add(method);
        
        if (parents.size() > 0) {
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(method);
        }
        parents.push(method);
        
        return true;
    }
    
    /**
     * Discards the visited initializer method.
     * @param node the visited initializer method node
     */
    @Override
    public void endVisit(Initializer node) {
        parents.pop();
    }
    
    /**
     * Returns the string representing of formal parameters of the method.
     * @param params the parameters of the method
     * @return the string representing the formal parameters
     */
    private String getFormalParameters(List<SingleVariableDeclaration> params) {
        StringBuffer buf = new StringBuffer();
        for (SingleVariableDeclaration param : params) {
            buf.append(" ");
            buf.append(param.toString());
        }
        if (buf.length() != 0) {
            return "(" + buf.substring(1) + ")";
        }
        return "()";
    }
    
    /**
     * Stores information about a field.
     * @param node the field node to be visited
     * @return always <code>true</code> to visit its child nodes
     */
    @Override
    public boolean visit(FieldDeclaration node) {
        int start = node.getStartPosition();
        int end = node.getStartPosition() + node.getLength() - 1;
        
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fields = (List<VariableDeclarationFragment>)node.fragments();
        Map<OpField, CodeRange> franges = new HashMap<OpField, CodeRange>();
        for (VariableDeclarationFragment f : fields) {
            String name = getClassName() + "#" + f.getName().getIdentifier();
            OpField field = new OpField(start, end, fileInfo, name);
            elements.add(field);
            
            int fstart = f.getStartPosition();
            int fend = fstart + f.getLength() - 1;
            franges.put(field, new CodeRange(fstart, fend)); 
        }
        
        for (OpField f: franges.keySet()) {
            for (CodeRange r : franges.values()) {
                if (franges.get(f) != r) {
                    f.addExcludedCodeRange(r.getStart(), r.getEnd());
                }
            }
        }
        
        OpField wfield = new OpField(start, end, fileInfo, "");
        if (parents.size() > 0) {
            OpJavaElement parent = (OpJavaElement)parents.peek();
            parent.addJavaElement(wfield);
        }
        parents.push(wfield);
        
        return true;
    }
    
    /**
     * Discards the visited field.
     * @param node the visited field node
     */
    @Override
    public void endVisit(FieldDeclaration node) {
        parents.pop();
    }
}
