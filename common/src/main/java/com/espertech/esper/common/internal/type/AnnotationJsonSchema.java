/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.type;

import com.espertech.esper.common.client.annotation.JsonSchema;

import java.lang.annotation.Annotation;

public class AnnotationJsonSchema implements JsonSchema {
    private final boolean dynamic;

    public AnnotationJsonSchema(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public boolean dynamic() {
        return dynamic;
    }

    public Class<? extends Annotation> annotationType() {
        return JsonSchema.class;
    }
}
