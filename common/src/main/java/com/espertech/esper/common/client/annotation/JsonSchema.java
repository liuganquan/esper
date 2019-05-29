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
package com.espertech.esper.common.client.annotation;

/**
 * Annotation for use with JSON schemas.
 */
public @interface JsonSchema {
    /**
     * Flag indicating whether to discard unrecognized property names (the default, false, i.e. non-dynamic)
     * or whether to retain all JSON object properties (true, dynamic)
     * @return dynamic flag
     */
    boolean dynamic() default false;
}
