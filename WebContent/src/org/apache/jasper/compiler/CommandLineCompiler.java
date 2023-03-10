/*
 * $Header: /home/cvspublic/jakarta-tomcat/src/share/org/apache/jasper/compiler/CommandLineCompiler.java,v 1.5 2000/02/25 20:10:46 mandar Exp $
 * $Revision: 1.5 $
 * $Date: 2000/02/25 20:10:46 $
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.jasper.compiler;

import java.io.File;
import java.io.IOException;

import org.apache.jasper.Constants;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.CommandLineContext;

/**
 * Overrides some methods so that we get the desired effects.
 *@author Danno Ferrin
 */
public class CommandLineCompiler extends Compiler implements Mangler {

    String javaFileName;
    String classFileName;
    String pkgName;
    String className;
    File jsp;
    String outputDir;

    public CommandLineCompiler(CommandLineContext ctxt) {
        super(ctxt);

        jsp = new File(ctxt.getJspFile());
        outputDir =  ctxt.getOptions().getScratchDir().getAbsolutePath();

        setMangler(this);

        computePackageName();
        ctxt.setServletPackageName(pkgName);
        className = getBaseClassName();
        // yes this is kind of messed up ... but it works
        if (ctxt.isOutputInDirs()) {
            String pkgName = ctxt.getServletPackageName();
            if (pkgName == null) {
                pkgName = "";
            }
            String tmpDir = outputDir
                   + File.separatorChar
                   + pkgName.replace('.', File.separatorChar);
            File f = new File(tmpDir);
            if (!f.exists()) {
                if (f.mkdirs()) {
                    outputDir = tmpDir;
                }
            } else {
                outputDir = tmpDir;
            }
        }
        computeClassFileName();
        computeJavaFileName();
    };


    /**
     * Always outDated.  (Of course we are, this is an explicit invocation
     *@returns true
     */
    public boolean isOutDated() {
        return true;
    };


    public final void computeJavaFileName() {
	javaFileName = ctxt.getServletClassName() + ".java";
	if ("null.java".equals(javaFileName)) {
    	    javaFileName = getBaseClassName() + ".java";
    	};
	if (outputDir != null && !outputDir.equals(""))
	    javaFileName = outputDir + File.separatorChar + javaFileName;
    }

    void computeClassFileName() {
        String prefix = getPrefix(jsp.getPath());
        classFileName = prefix + getBaseClassName() + ".class";
	if (outputDir != null && !outputDir.equals(""))
	    classFileName = outputDir + File.separatorChar + classFileName;
    }

    public static String [] keywords = {
        "abstract", "boolean", "break", "byte",
        "case", "catch", "char", "class",
        "const", "continue", "default", "do",
        "double", "else", "extends", "final",
        "finally", "float", "for", "goto",
        "if", "implements", "import",
        "instanceof", "int", "interface",
        "long", "native", "new", "package",
        "private", "protected", "public",
        "return", "short", "static", "super",
        "switch", "synchronized", "this",
        "throw", "throws", "transient",
        "try", "void", "volatile", "while"
    };

    void computePackageName() {
	String pathName = jsp.getPath();
	StringBuffer modifiedpkgName = new StringBuffer ();
        int indexOfSepChar = pathName.lastIndexOf(File.separatorChar);

        if (("".equals(ctxt.getServletPackageName())) ||
	    (indexOfSepChar == -1) || (indexOfSepChar == 0)) {
	    pkgName = null;
	} else {
	    for (int i = 0; i < keywords.length; i++) {
		char fs = File.separatorChar;
		int index;
		if (pathName.startsWith(keywords[i] + fs)) {
		    index = 0;
		} else {
		    index = pathName.indexOf(fs + keywords[i] + fs);
		}
		while (index != -1) {
		    String tmpathName = pathName.substring (0,index+1) + '%';
		    pathName = tmpathName + pathName.substring (index+2);
		    index = pathName.indexOf(fs + keywords[i] + fs);
		}
	    }
	
	    // XXX fix for paths containing '.'.
	    // Need to be more elegant here.
            pathName = pathName.replace('.','_');
	
	    pkgName = pathName.substring(0, pathName.lastIndexOf(
	    		File.separatorChar)).replace(File.separatorChar, '.');
	    if (ctxt.getServletPackageName() != null) {
	        pkgName = ctxt.getServletPackageName();
	    }
	    for (int i=0; i<pkgName.length(); i++)
		if (Character.isLetter(pkgName.charAt(i)) == true ||
		    pkgName.charAt(i) == '.') {
		    modifiedpkgName.append(pkgName.substring(i,i+1));
		}
		else
		    modifiedpkgName.append(mangleChar(pkgName.charAt(i)));

	    if (modifiedpkgName.charAt(0) == '.') {
                String modifiedpkgNameString = modifiedpkgName.toString();
                pkgName = modifiedpkgNameString.substring(1,
                                                         modifiedpkgName.length ());
            }
	    else
	        pkgName = modifiedpkgName.toString();
	}

    }

					
    private final String getInitialClassName() {
        String prefix = getPrefix(jsp.getPath());

        return prefix + getBaseClassName() + Constants.JSP_TOKEN + "0";
    }

    private final String getBaseClassName() {
	String className = ctxt.getServletClassName();

	if (className == null) {
            if (jsp.getName().endsWith(".jsp"))
                className = jsp.getName().substring(0, jsp.getName().length() - 4);
            else
                className = jsp.getName();
        }
	
	// since we don't mangle extensions like the servlet does,
	// we need to check for keywords as class names
	for (int i = 0; i < keywords.length; i++) {
	    if (className.equals(keywords[i])) {
		className += "%";
	    };
	};
	
	// Fix for invalid characters. If you think of more add to the list.
	StringBuffer modifiedClassName = new StringBuffer();
	for (int i = 0; i < className.length(); i++) {
	    if (Character.isLetterOrDigit(className.charAt(i)) == true)
		modifiedClassName.append(className.substring(i,i+1));
	    else
		modifiedClassName.append(mangleChar(className.charAt(i)));
	}
	
	return modifiedClassName.toString();
    }

    private final String getPrefix(String pathName) {
	if (pathName != null) {
	    StringBuffer modifiedName = new StringBuffer();
	    for (int i = 0; i < pathName.length(); i++) {
		if (Character.isLetter(pathName.charAt(i)) == true)
		    modifiedName.append(pathName.substring(i,i+1));
		else
		    modifiedName.append(mangleChar(pathName.charAt(i)));
 	    }
	    return modifiedName.toString();
	}
	else
            return "";
    }

    private static final String mangleChar(char ch) {
	
        if(ch == File.separatorChar) {
	    ch = '/';
	}
	String s = Integer.toHexString(ch);
	int nzeros = 5 - s.length();
	char[] result = new char[6];
	result[0] = '_';
	for (int i = 1; i <= nzeros; i++)
	    result[i] = '0';
	for (int i = nzeros+1, j = 0; i < 6; i++, j++)
	    result[i] = s.charAt(j);
	return new String(result);
    }


    public final String getPackageName() {
        return pkgName;
    }

    public final String getClassName() {
        return className;
    }

    public final String getJavaFileName() {
        return javaFileName;
    }

    public final String getClassFileName() {
        return classFileName;
    }


}
