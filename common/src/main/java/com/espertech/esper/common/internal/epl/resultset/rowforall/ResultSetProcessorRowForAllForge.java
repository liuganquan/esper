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
package com.espertech.esper.common.internal.epl.resultset.rowforall;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenCtor;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenInstanceAux;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenTypedParam;
import com.espertech.esper.common.internal.compile.stage1.spec.OutputLimitSpec;
import com.espertech.esper.common.internal.epl.agg.core.AggregationService;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.epl.expression.core.ExprForge;
import com.espertech.esper.common.internal.epl.resultset.core.ResultSetProcessorFactoryForge;
import com.espertech.esper.common.internal.epl.resultset.core.ResultSetProcessorUtil;
import com.espertech.esper.common.client.util.StateMgmtSetting;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.constant;
import static com.espertech.esper.common.internal.epl.resultset.codegen.ResultSetProcessorCodegenNames.MEMBER_EXPREVALCONTEXT;
import static com.espertech.esper.common.internal.epl.resultset.codegen.ResultSetProcessorCodegenNames.MEMBER_AGGREGATIONSVC;

/**
 * Result set processor prototype for the case: aggregation functions used in the select clause, and no group-by,
 * and all properties in the select clause are under an aggregation function.
 */
public class ResultSetProcessorRowForAllForge implements ResultSetProcessorFactoryForge {
    private final EventType resultEventType;
    private final boolean isSelectRStream;
    private final boolean isUnidirectional;
    private final boolean isHistoricalOnly;
    private final ExprForge optionalHavingNode;
    private final OutputLimitSpec outputLimitSpec;
    private final boolean hasOrderBy;
    private final Supplier<StateMgmtSetting> outputAllHelperSettings;

    public ResultSetProcessorRowForAllForge(EventType resultEventType,
                                            ExprForge optionalHavingNode,
                                            boolean isSelectRStream,
                                            boolean isUnidirectional,
                                            boolean isHistoricalOnly,
                                            OutputLimitSpec outputLimitSpec,
                                            boolean hasOrderBy,
                                            Supplier<StateMgmtSetting> outputAllHelperSettings) {
        this.resultEventType = resultEventType;
        this.optionalHavingNode = optionalHavingNode;
        this.isSelectRStream = isSelectRStream;
        this.isUnidirectional = isUnidirectional;
        this.isHistoricalOnly = isHistoricalOnly;
        this.outputLimitSpec = outputLimitSpec;
        this.hasOrderBy = hasOrderBy;
        this.outputAllHelperSettings = outputAllHelperSettings;
    }

    public EventType getResultEventType() {
        return resultEventType;
    }

    public boolean isSelectRStream() {
        return isSelectRStream;
    }

    public boolean isUnidirectional() {
        return isUnidirectional;
    }

    public ExprForge getOptionalHavingNode() {
        return optionalHavingNode;
    }

    public boolean isHistoricalOnly() {
        return isHistoricalOnly;
    }

    public boolean isSorting() {
        return hasOrderBy;
    }

    public OutputLimitSpec getOutputLimitSpec() {
        return outputLimitSpec;
    }

    public EPTypeClass getInterfaceClass() {
        return ResultSetProcessorRowForAll.EPTYPE;
    }

    public void instanceCodegen(CodegenInstanceAux instance, CodegenClassScope classScope, CodegenCtor factoryCtor, List<CodegenTypedParam> factoryMembers) {
        instance.getMethods().addMethod(AggregationService.EPTYPE, "getAggregationService", Collections.emptyList(), ResultSetProcessorRowForAll.class, classScope, methodNode -> methodNode.getBlock().methodReturn(MEMBER_AGGREGATIONSVC));
        instance.getMethods().addMethod(ExprEvaluatorContext.EPTYPE, "getExprEvaluatorContext", Collections.emptyList(), ResultSetProcessorRowForAll.class, classScope, methodNode -> methodNode.getBlock().methodReturn(MEMBER_EXPREVALCONTEXT));
        instance.getMethods().addMethod(EPTypePremade.BOOLEANPRIMITIVE.getEPType(), "isSelectRStream", Collections.emptyList(), ResultSetProcessorRowForAll.class, classScope, methodNode -> methodNode.getBlock().methodReturn(constant(isSelectRStream())));
        ResultSetProcessorUtil.evaluateHavingClauseCodegen(optionalHavingNode, classScope, instance);
        ResultSetProcessorRowForAllImpl.getSelectListEventsAsArrayCodegen(this, classScope, instance);
    }

    public void processViewResultCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.processViewResultCodegen(this, classScope, method, instance);
    }

    public void processJoinResultCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.processJoinResultCodegen(this, classScope, method, instance);
    }

    public void getIteratorViewCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.getIteratorViewCodegen(this, classScope, method, instance);
    }

    public void getIteratorJoinCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.getIteratorJoinCodegen(this, classScope, method, instance);
    }

    public void processOutputLimitedViewCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.processOutputLimitedViewCodegen(this, classScope, method, instance);
    }

    public void processOutputLimitedJoinCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.processOutputLimitedJoinCodegen(this, classScope, method, instance);
    }

    public void applyViewResultCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.applyViewResultCodegen(method);
    }

    public void applyJoinResultCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.applyJoinResultCodegen(method);
    }

    public void processOutputLimitedLastAllNonBufferedViewCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.processOutputLimitedLastAllNonBufferedViewCodegen(this, classScope, method, instance);
    }

    public void processOutputLimitedLastAllNonBufferedJoinCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.processOutputLimitedLastAllNonBufferedJoinCodegen(this, classScope, method, instance);
    }

    public void continueOutputLimitedLastAllNonBufferedViewCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.continueOutputLimitedLastAllNonBufferedViewCodegen(this, method);
    }

    public void continueOutputLimitedLastAllNonBufferedJoinCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.continueOutputLimitedLastAllNonBufferedJoinCodegen(this, method);
    }

    public void acceptHelperVisitorCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.acceptHelperVisitorCodegen(method, instance);
    }

    public void stopMethodCodegen(CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowForAllImpl.stopCodegen(method, instance);
    }

    public void clearMethodCodegen(CodegenClassScope classScope, CodegenMethod method) {
        ResultSetProcessorRowForAllImpl.clearCodegen(method);
    }

    public String getInstrumentedQName() {
        return "ResultSetProcessUngroupedFullyAgg";
    }

    public Supplier<StateMgmtSetting> getOutputAllHelperSettings() {
        return outputAllHelperSettings;
    }
}
