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
package com.espertech.esper.common.internal.view.prior;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.annotation.AppliesTo;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.context.aifactory.core.SAIFFInitializeSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;
import com.espertech.esper.common.client.util.StateMgmtSetting;
import com.espertech.esper.common.internal.view.core.ViewFactoryForgeBase;
import com.espertech.esper.common.internal.view.core.ViewForgeEnv;
import com.espertech.esper.common.internal.view.core.ViewParameterException;

import java.util.List;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.constant;

public class PriorEventViewForge extends ViewFactoryForgeBase {
    private final boolean unbound;

    public PriorEventViewForge(boolean unbound, EventType eventType, StateMgmtSetting stateMgmtSettings) {
        this.unbound = unbound;
        this.eventType = eventType;
        this.stateMgmtSettings = stateMgmtSettings;
    }

    public void setViewParameters(List<ExprNode> parameters, ViewForgeEnv viewForgeEnv, int streamNumber) throws ViewParameterException {
    }

    protected void attachValidate(EventType parentEventType, int streamNumber, ViewForgeEnv viewForgeEnv, boolean grouped) throws ViewParameterException {
        throw new IllegalStateException("Should not be called for 'prior'");
    }

    protected EPTypeClass typeOfFactory() {
        return PriorEventViewFactory.EPTYPE;
    }

    protected String factoryMethod() {
        return "prior";
    }

    protected void assign(CodegenMethod method, CodegenExpressionRef factory, SAIFFInitializeSymbol symbols, CodegenClassScope classScope) {
        method.getBlock()
            .exprDotMethod(factory, "setUnbound", constant(unbound));
    }

    public String getViewName() {
        return "prior";
    }

    protected AppliesTo appliesTo() {
        return AppliesTo.WINDOW_PRIOR;
    }
}
