/*
 *  Copyright 2017
 *  Software Science and Technology Lab.
 *  Department of Computer Science, Ritsumeikan University
 */

package org.jtool.changetracker.dependencyanalyzer;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Collects Java constructs within the source code to be parsed.
 * @author Katsuhisa Maruyama
 */
class CTVisitor extends ASTVisitor {
    
    /**
     * A flag that indicates if this visitor sets the range of
     * the whole of a method declaration or its method body only.
     */
    private boolean wholeMethod;
    
    /**
     * The collection of Java constructs appearing in the AST.
     */
    private List<JavaConstruct> constructs = new ArrayList<JavaConstruct>();
        
    /**
     * The stack that stores classes in the AST.
     */
    private Stack<JavaConstruct> classes = new Stack<JavaConstruct>();
    
    /**
     * The stack that stores parent Java constructs in the AST.
     */
    private Stack<JavaConstruct> parents = new Stack<JavaConstruct>();
    
    /**
     * Creates a visitor visiting an AST.
     * @param body <code>true</code> if this visitor uses the whole of a method declaration
     */
    CTVisitor(boolean whole) {
        wholeMethod = whole;
    }
    
    /**
     * Creates a visitor visiting an AST.
     */
    CTVisitor() {
        this(true);
    }
    
    /**
     * Returns all the Java constructs appearing in the AST.
     * @return the collection of the Java constructs
     */
    public List<JavaConstruct> getJavaConstructs() {
        return constructs;
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
        JavaConstruct clazz = new JavaConstruct(JavaConstruct.Type.CLASS, start, end, name);
        constructs.add(clazz);
        
        if (parents.size() > 0) {
            JavaConstruct parent = (JavaConstruct)parents.peek();
            parent.addJavaConstruct(clazz);
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
        JavaConstruct clazz;
        if (parents.size() == 0) {
            clazz = new JavaConstruct(JavaConstruct.Type.CLASS, start, end, name);
            constructs.add(clazz);
        } else {
            clazz = new JavaConstruct(JavaConstruct.Type.INNER_CLASS, start, end, name);
            constructs.add(clazz);
            JavaConstruct parent = (JavaConstruct)parents.peek();
            parent.addJavaConstruct(clazz);
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
        String name = "!";
        JavaConstruct clazz = new JavaConstruct(JavaConstruct.Type.INNER_CLASS, start, end, name);
        constructs.add(clazz);
        if (parents.size() > 0) {
            JavaConstruct parent = (JavaConstruct)parents.peek();
            parent.addJavaConstruct(clazz);
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
        for (JavaConstruct c : classes) {
            buf.append("$");
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
        Block body = node.getBody();
        int start;
        int end;
        if (!wholeMethod && body != null) {
            start = body.getStartPosition();
            end = start + body.getLength() - 1;
        } else {
            start = node.getStartPosition();
            end = start + node.getLength() - 1;
            
        }
        
        @SuppressWarnings("unchecked")
        String name = getClassName() + "#" + node.getName().getIdentifier() + getFormalParameters(node.parameters());
        JavaConstruct method = new JavaConstruct(JavaConstruct.Type.METHOD, start, end, name);
        constructs.add(method);
        
        if (parents.size() > 0) {
            JavaConstruct parent = (JavaConstruct)parents.peek();
            parent.addJavaConstruct(method);
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
        Block body = node.getBody();
        int start;
        int end;
        if (!wholeMethod && body != null) {
            start = body.getStartPosition();
            end = start + body.getLength() - 1;
        } else {
            start = node.getStartPosition();
            end = start + node.getLength() - 1;
            
        }
        
        String name = getClassName() + "$" + "$Init()";
        JavaConstruct method = new JavaConstruct(JavaConstruct.Type.METHOD, start, end, name);
        constructs.add(method);
        
        if (parents.size() > 0) {
            JavaConstruct parent = (JavaConstruct)parents.peek();
            parent.addJavaConstruct(method);
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
        int end = start + node.getLength() - 1;
        
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fields = (List<VariableDeclarationFragment>)node.fragments();
        Map<JavaConstruct, CodeRange> franges = new HashMap<JavaConstruct, CodeRange>();
        for (VariableDeclarationFragment f : fields) {
            String name = getClassName() + "#" + f.getName().getIdentifier();
            JavaConstruct field = new JavaConstruct(JavaConstruct.Type.FIELD, start, end, name);
            constructs.add(field);
            
            int fstart = f.getStartPosition();
            int fend = fstart + f.getLength() - 1;
            franges.put(field, new CodeRange(fstart, fend));
        }
        
        for (JavaConstruct f : franges.keySet()) {
            for (CodeRange r : franges.values()) {
                if (franges.get(f) != r) {
                    f.addExcludedCodeRange(r.getStart(), r.getEnd());
                }
            }
        }
        
        JavaConstruct wfield = new JavaConstruct(JavaConstruct.Type.FIELD, start, end, "");
        if (parents.size() > 0) {
            JavaConstruct parent = (JavaConstruct)parents.peek();
            parent.addJavaConstruct(wfield);
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
