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
package com.espertech.esper.common.internal.epl.expression.time.node;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.util.TimePeriod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.epl.expression.core.ExprNode;
import com.espertech.esper.common.internal.epl.expression.time.eval.TimePeriodComputeForge;
import com.espertech.esper.common.internal.epl.expression.time.eval.TimePeriodEval;

/**
 * Expression representing a time period.
 * <p>
 * Child nodes to this expression carry the actual parts and must return a numeric value.
 */
public interface ExprTimePeriod extends ExprNode {
    public boolean hasVariable();

    public TimePeriodComputeForge getTimePeriodComputeForge();

    public TimePeriodEval getTimePeriodEval();

    public double evaluateAsSeconds(EventBean[] eventsPerStream, boolean newData, ExprEvaluatorContext context);

    public TimePeriod evaluateGetTimePeriod(EventBean[] eventsPerStream, boolean newData, ExprEvaluatorContext context);

    /**
     * Indicator whether the time period has a day part child expression.
     *
     * @return true for part present, false for not present
     */
    public boolean isHasDay();

    /**
     * Indicator whether the time period has a hour part child expression.
     *
     * @return true for part present, false for not present
     */
    public boolean isHasHour();

    /**
     * Indicator whether the time period has a minute part child expression.
     *
     * @return true for part present, false for not present
     */
    public boolean isHasMinute();

    /**
     * Indicator whether the time period has a second part child expression.
     *
     * @return true for part present, false for not present
     */
    public boolean isHasSecond();

    /**
     * Indicator whether the time period has a millisecond part child expression.
     *
     * @return true for part present, false for not present
     */
    public boolean isHasMillisecond();

    /**
     * Indicator whether the time period has a microsecond part child expression.
     *
     * @return true for part present, false for not present
     */
    public boolean isHasMicrosecond();

    /**
     * Indicator whether the time period has a year part child expression.
     *
     * @return true for part present, false for not present
     */
    public boolean isHasYear();

    /**
     * Indicator whether the time period has a month part child expression.
     *
     * @return true for part present, false for not present
     */
    public boolean isHasMonth();

    /**
     * Indicator whether the time period has a week part child expression.
     *
     * @return true for part present, false for not present
     */
    public boolean isHasWeek();

    public boolean isConstantResult();

    CodegenExpression evaluateGetTimePeriodCodegen(CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope);

    CodegenExpression evaluateAsSecondsCodegen(CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope);

    CodegenExpression makeTimePeriodAnonymous(CodegenMethod method, CodegenClassScope classScope);
}
