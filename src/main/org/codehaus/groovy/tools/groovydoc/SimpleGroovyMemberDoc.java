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

import org.codehaus.groovy.groovydoc.*;
import org.codehaus.groovy.ant.Groovydoc;

import java.util.List;
import java.util.ArrayList;

public class SimpleGroovyMemberDoc extends SimpleGroovyProgramElementDoc implements GroovyMemberDoc {
    public SimpleGroovyMemberDoc(String name) {
        this(name, new ArrayList<Groovydoc.LinkArgument>());
    }

    public SimpleGroovyMemberDoc(String name, List<Groovydoc.LinkArgument> links) {
        super(name, links);
    }

    public boolean isSynthetic() {/*todo*/
        return false;
    }
}
