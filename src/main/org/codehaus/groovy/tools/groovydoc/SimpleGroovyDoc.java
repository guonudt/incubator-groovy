/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.tools.groovydoc;

import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.groovydoc.*;
import org.codehaus.groovy.ant.Groovydoc;

public class SimpleGroovyDoc implements GroovyDoc {
    private static final Pattern TAG_REGEX = Pattern.compile("(?m)@([a-z]*)\\s*(.*$[^@]*)");
    private static final Pattern LINK_REGEX = Pattern.compile("(?m)[{]@(link)\\s*([^}]*)}");
    private static final Pattern CODE_REGEX = Pattern.compile("(?m)[{]@(code)\\s*([^}]*)}");
	private String name;
	private String commentText;
	private String rawCommentText;
	private String firstSentenceCommentText;
    private List<Groovydoc.LinkArgument> links;
    private int definitionType;

    private final int CLASS = 0;
    private final int INTERFACE = 1;
    private final int ANNOTATION = 2;

    public SimpleGroovyDoc(String name, List<Groovydoc.LinkArgument> links) {
        this.name = name;
        this.links = links;
        this.setRawCommentText("");  // default to no comments (good for default constructors which will not have a reason to call this)
        definitionType = CLASS; // default this instance to a class, unless told otherwise
    }

    public SimpleGroovyDoc(String name) {
        this(name, new ArrayList<Groovydoc.LinkArgument>());
    }

	public String name() {
		return name;
	}

	public String toString() {
		return "" + getClass() + "(" + name + ")";
	}
	public String commentText() {
		return commentText; // derived from rawCommentText
	}
	public String getRawCommentText() {
		return rawCommentText;
	}
	public String firstSentenceCommentText() {
		return firstSentenceCommentText; // derived from rawCommentText
	}

	public void setRawCommentText(String rawCommentText) {
		this.rawCommentText = rawCommentText;
		
		// remove all the * from beginning of lines
		this.commentText = rawCommentText.replaceAll("(?m)^\\s*\\*", ""); // todo precompile regex Patterns

		// Comment Summary using first sentence (Locale sensitive)
		BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.getDefault()); // todo - allow locale to be passed in
        boundary.setText(commentText);
        int start = boundary.first();
        int end = boundary.next();
        if (start > -1 && end > -1) {
        	// need to abbreviate this comment for the summary
        	this.firstSentenceCommentText = commentText.substring(start,end);
        } else {
        	this.firstSentenceCommentText = commentText;
        }

        // TODO {@link processing hack}
        this.commentText = replaceAllTags(this.commentText, "", "", LINK_REGEX);

        // TODO {@code processing hack}
        this.commentText = replaceAllTags(this.commentText, "<TT>", "</TT>", CODE_REGEX);

		// hack to reformat groovydoc tags into html (todo: tags)
		this.commentText = replaceAllTags(this.commentText, "<DL><DT><B>$1:</B></DT><DD>", "</DD></DL>", TAG_REGEX);

        this.commentText = commentText.replaceAll("&at;", "@").replaceAll("&dollar;", "\\$");

		// hack to hide groovydoc tags in summaries
		this.firstSentenceCommentText = this.firstSentenceCommentText.replaceAll("(?m)@([a-z]*\\s*.*)$",""); // remove @return etc from summaries
        
	}

    // TODO: this should go away once we have proper tags
    public String replaceAllTags(String self, String s1, String s2, Pattern regex) {
        Matcher matcher = regex.matcher(self);
        if (matcher.find()) {
            matcher.reset();
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String tagname = matcher.group(1);
                if (tagname.equals("see") || tagname.equals("link")) {
                    // TODO: escape $ signs?
                    matcher.appendReplacement(sb, s1 + getDocUrl(matcher.group(2)).replaceAll("[$]", "&dollar;") + s2);
                } else {
                    matcher.appendReplacement(sb, s1 + matcher.group(2).replaceAll("@", "&at;").replaceAll("[$]", "&dollar;") + s2);
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
    }

    public String getDocUrl(String type) {
        if (type == null)
            return type;
        type = type.trim();
        if (type.startsWith("#"))
            return "<a href='" + type + "'>" + type + "</a>";
        if (type.indexOf('.') == -1)
            return type;

        final String[] target = type.split("#");
        String shortClassName = target[0].replaceAll(".*\\.", "");
//        String packageName = type.substring(0, type.length()-shortClassName.length()-2);
        shortClassName += (target.length > 1 ? "#" + target[1].split("\\(")[0] : "");
        for (Groovydoc.LinkArgument link : links) {
            final StringTokenizer tokenizer = new StringTokenizer(link.getPackages(), ", ");
            while (tokenizer.hasMoreTokens()) {
                final String token = tokenizer.nextToken();
                if (type.startsWith(token)) {
                    String apiBaseUrl = link.getHref();
                    if (!apiBaseUrl.endsWith("/")) {
                        apiBaseUrl += "/";
                    }
                    String url = apiBaseUrl + target[0].replaceAll("\\.", "/") + ".html" + (target.length > 1 ? "#" + target[1] : "");
                    return "<a href='" + url + "' title='" + shortClassName + "'>" + shortClassName + "</a>";
                }
            }
        }
        return type;
    }

    public boolean isClass() {
        return definitionType == CLASS;
    }

    public boolean isInterface() {
        return definitionType == INTERFACE;
    }

    public boolean isAnnotationType() {
        return definitionType == ANNOTATION;
    }

    public void setAsInterfaceDefinition() {
        definitionType = INTERFACE;
    }

    public void setAsAnnotationDefinition() {
        definitionType = ANNOTATION;
    }

    // Methods from Comparable
	public int compareTo(Object that) {
		if (that instanceof SimpleGroovyDoc) {
			return name.compareTo(((SimpleGroovyDoc) that).name);
		} else {
			throw new ClassCastException();
		}
	}

    // Methods from GroovyDoc
//	public GroovyTag[] firstSentenceTags() {/*todo*/return null;}
//	public GroovyTag[] inlineTags() {/*todo*/return null;}
	public boolean isAnnotationTypeElement() {/*todo*/return false;}
	public boolean isConstructor() {/*todo*/return false;}
	public boolean isEnum() {/*todo*/return false;}
	public boolean isEnumConstant() {/*todo*/return false;}
	public boolean isError() {/*todo*/return false;}
	public boolean isException() {/*todo*/return false;}
	public boolean isField() {/*todo*/return false;}
	public boolean isIncluded() {/*todo*/return false;}
	public boolean isMethod() {/*todo*/return false;}
	public boolean isOrdinaryClass() {/*todo*/return false;}
//	public GroovySourcePosition position() {/*todo*/return null;}
//	public GroovySeeTag[] seeTags() {/*todo*/return null;}
//	public GroovyTag[] tags() {/*todo*/return null;}
//	public GroovyTag[] tags(String arg0) {/*todo*/return null;}

}
