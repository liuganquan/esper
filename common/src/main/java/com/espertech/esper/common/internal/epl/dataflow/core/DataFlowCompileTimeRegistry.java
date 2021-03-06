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
package com.espertech.esper.common.internal.epl.dataflow.core;

import com.espertech.esper.common.internal.epl.expression.core.ExprValidationException;

import java.util.HashSet;
import java.util.Set;

public class DataFlowCompileTimeRegistry {
    private Set<String> dataFlows;

    public void newDataFlow(String dataFlowName) throws ExprValidationException {
        if (dataFlows == null) {
            dataFlows = new HashSet<>();
        }
        if (dataFlows.contains(dataFlowName)) {
            throw new ExprValidationException("A dataflow by name '" + dataFlowName + "' has already been declared");
        }
        dataFlows.add(dataFlowName);
    }
}
