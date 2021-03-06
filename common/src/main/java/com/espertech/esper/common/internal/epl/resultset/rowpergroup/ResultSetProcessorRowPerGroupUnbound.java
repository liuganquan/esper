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
package com.espertech.esper.common.internal.epl.resultset.rowpergroup;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenBlock;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenInstanceAux;
import com.espertech.esper.common.internal.bytecodemodel.core.CodegenNamedParam;
import com.espertech.esper.common.internal.collection.UniformPair;
import com.espertech.esper.common.internal.epl.resultset.core.ResultSetProcessorUtil;

import java.util.function.Consumer;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenNames.NAME_EPS;
import static com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenNames.REF_EPS;
import static com.espertech.esper.common.internal.epl.resultset.codegen.ResultSetProcessorCodegenNames.*;
import static com.espertech.esper.common.internal.epl.resultset.core.ResultSetProcessorUtil.METHOD_TOPAIRNULLIFALLNULL;
import static com.espertech.esper.common.internal.epl.resultset.rowpergroup.ResultSetProcessorRowPerGroupImpl.*;

public class ResultSetProcessorRowPerGroupUnbound {

    public static void applyViewResultCodegen(ResultSetProcessorRowPerGroupForge forge, CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        method.getBlock().declareVar(EventBean.EPTYPEARRAY, NAME_EPS, newArrayByLength(EventBean.EPTYPE, constant(1)));

        {
            CodegenBlock ifNew = method.getBlock().ifCondition(notEqualsNull(REF_NEWDATA));
            {
                CodegenBlock newLoop = ifNew.forEach(EventBean.EPTYPE, "aNewData", REF_NEWDATA);
                newLoop.assignArrayElement(NAME_EPS, constant(0), ref("aNewData"))
                        .declareVar(EPTypePremade.OBJECT.getEPType(), "mk", localMethod(forge.getGenerateGroupKeySingle(), REF_EPS, constantTrue()))
                        .exprDotMethod(ref("groupReps"), "put", ref("mk"), ref("aNewData"))
                        .exprDotMethod(MEMBER_AGGREGATIONSVC, "applyEnter", REF_EPS, ref("mk"), MEMBER_EXPREVALCONTEXT);
            }
        }

        {
            CodegenBlock ifOld = method.getBlock().ifCondition(notEqualsNull(REF_OLDDATA));
            {
                CodegenBlock oldLoop = ifOld.forEach(EventBean.EPTYPE, "anOldData", REF_OLDDATA);
                oldLoop.assignArrayElement(NAME_EPS, constant(0), ref("anOldData"))
                        .declareVar(EPTypePremade.OBJECT.getEPType(), "mk", localMethod(forge.getGenerateGroupKeySingle(), REF_EPS, constantFalse()))
                        .exprDotMethod(MEMBER_AGGREGATIONSVC, "applyLeave", REF_EPS, ref("mk"), MEMBER_EXPREVALCONTEXT);
            }
        }
    }

    static void processViewResultUnboundCodegen(ResultSetProcessorRowPerGroupForge forge, CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        CodegenMethod generateGroupKeysKeepEvent = generateGroupKeysKeepEventCodegen(forge, classScope, instance);
        CodegenMethod generateOutputEventsView = generateOutputEventsViewCodegen(forge, classScope, instance);
        CodegenMethod processViewResultNewDepthOneUnbound = processViewResultNewDepthOneUnboundCodegen(forge, classScope, instance);

        CodegenBlock ifShortcut = method.getBlock().ifCondition(and(notEqualsNull(REF_NEWDATA), equalsIdentity(arrayLength(REF_NEWDATA), constant(1))));
        ifShortcut.ifCondition(or(equalsNull(REF_OLDDATA), equalsIdentity(arrayLength(REF_OLDDATA), constant(0))))
                .blockReturn(localMethod(processViewResultNewDepthOneUnbound, REF_NEWDATA, REF_ISSYNTHESIZE));

        method.getBlock().declareVar(EPTypePremade.MAP.getEPType(), "keysAndEvents", newInstance(EPTypePremade.HASHMAP.getEPType()))
                .declareVar(EventBean.EPTYPEARRAY, NAME_EPS, newArrayByLength(EventBean.EPTYPE, constant(1)))
                .declareVar(EPTypePremade.OBJECTARRAY.getEPType(), "newDataMultiKey", localMethod(generateGroupKeysKeepEvent, REF_NEWDATA, ref("keysAndEvents"), constantTrue(), REF_EPS))
                .declareVar(EPTypePremade.OBJECTARRAY.getEPType(), "oldDataMultiKey", localMethod(generateGroupKeysKeepEvent, REF_OLDDATA, ref("keysAndEvents"), constantFalse(), REF_EPS))
                .declareVar(EventBean.EPTYPEARRAY, "selectOldEvents", forge.isSelectRStream() ? localMethod(generateOutputEventsView, ref("keysAndEvents"), constantFalse(), REF_ISSYNTHESIZE, REF_EPS) : constantNull());

        {
            CodegenBlock ifNew = method.getBlock().ifCondition(notEqualsNull(REF_NEWDATA));
            {
                CodegenBlock newLoop = ifNew.forLoopIntSimple("i", arrayLength(REF_NEWDATA));
                newLoop.assignArrayElement(NAME_EPS, constant(0), arrayAtIndex(REF_NEWDATA, ref("i")))
                        .exprDotMethod(ref("groupReps"), "put", arrayAtIndex(ref("newDataMultiKey"), ref("i")), arrayAtIndex(REF_EPS, constant(0)))
                        .exprDotMethod(MEMBER_AGGREGATIONSVC, "applyEnter", REF_EPS, arrayAtIndex(ref("newDataMultiKey"), ref("i")), MEMBER_EXPREVALCONTEXT);
            }
        }

        {
            CodegenBlock ifOld = method.getBlock().ifCondition(notEqualsNull(REF_OLDDATA));
            {
                CodegenBlock newLoop = ifOld.forLoopIntSimple("i", arrayLength(REF_OLDDATA));
                newLoop.assignArrayElement(NAME_EPS, constant(0), arrayAtIndex(REF_OLDDATA, ref("i")))
                        .exprDotMethod(MEMBER_AGGREGATIONSVC, "applyLeave", REF_EPS, arrayAtIndex(ref("oldDataMultiKey"), ref("i")), MEMBER_EXPREVALCONTEXT);
            }
        }

        method.getBlock().declareVar(EventBean.EPTYPEARRAY, "selectNewEvents", localMethod(generateOutputEventsView, ref("keysAndEvents"), constantTrue(), REF_ISSYNTHESIZE, REF_EPS))
                .methodReturn(staticMethod(ResultSetProcessorUtil.class, METHOD_TOPAIRNULLIFALLNULL, ref("selectNewEvents"), ref("selectOldEvents")));
    }

    public static void getIteratorViewUnboundedCodegen(ResultSetProcessorRowPerGroupForge forge, CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        if (!forge.isSorting()) {
            method.getBlock().declareVar(EPTypePremade.ITERATOR.getEPType(), "it", exprDotMethod(ref("groupReps"), "valueIterator"))
                    .methodReturn(newInstance(ResultSetProcessorRowPerGroupIterator.EPTYPE, ref("it"), ref("this"), MEMBER_AGGREGATIONSVC, MEMBER_EXPREVALCONTEXT));
        } else {
            CodegenMethod getIteratorSorted = getIteratorSortedCodegen(forge, classScope, instance);
            method.getBlock().methodReturn(localMethod(getIteratorSorted, exprDotMethod(ref("groupReps"), "valueIterator")));
        }
    }

    static CodegenMethod processViewResultNewDepthOneUnboundCodegen(ResultSetProcessorRowPerGroupForge forge, CodegenClassScope classScope, CodegenInstanceAux instance) {
        CodegenMethod shortcutEvalGivenKey = ResultSetProcessorRowPerGroupImpl.shortcutEvalGivenKeyCodegen(forge.getOptionalHavingNode(), classScope, instance);

        Consumer<CodegenMethod> code = methodNode -> {
            methodNode.getBlock().declareVar(EPTypePremade.OBJECT.getEPType(), "groupKey", localMethod(forge.getGenerateGroupKeySingle(), REF_NEWDATA, constantTrue()));
            if (forge.isSelectRStream()) {
                methodNode.getBlock().declareVar(EventBean.EPTYPE, "rstream", localMethod(shortcutEvalGivenKey, REF_NEWDATA, ref("groupKey"), constantFalse(), REF_ISSYNTHESIZE));
            }
            methodNode.getBlock().exprDotMethod(MEMBER_AGGREGATIONSVC, "applyEnter", REF_NEWDATA, ref("groupKey"), MEMBER_EXPREVALCONTEXT)
                    .exprDotMethod(ref("groupReps"), "put", ref("groupKey"), arrayAtIndex(REF_NEWDATA, constant(0)))
                    .declareVar(EventBean.EPTYPE, "istream", localMethod(shortcutEvalGivenKey, REF_NEWDATA, ref("groupKey"), constantTrue(), REF_ISSYNTHESIZE));
            if (forge.isSelectRStream()) {
                methodNode.getBlock().methodReturn(staticMethod(ResultSetProcessorUtil.class, "toPairNullIfAllNullSingle", ref("istream"), ref("rstream")));
            } else {
                methodNode.getBlock().methodReturn(staticMethod(ResultSetProcessorUtil.class, "toPairNullIfNullIStream", ref("istream")));
            }
        };

        return instance.getMethods().addMethod(UniformPair.EPTYPE, "processViewResultNewDepthOneUnboundCodegen", CodegenNamedParam.from(EventBean.EPTYPEARRAY, NAME_NEWDATA, EPTypePremade.BOOLEANPRIMITIVE.getEPType(), NAME_ISSYNTHESIZE), ResultSetProcessorRowPerGroupImpl.class, classScope, code);
    }

    public static void stopMethodCodegenUnbound(ResultSetProcessorRowPerGroupForge forge, CodegenClassScope classScope, CodegenMethod method, CodegenInstanceAux instance) {
        ResultSetProcessorRowPerGroupImpl.stopMethodCodegenBound(method, instance);
        method.getBlock().exprDotMethod(ref("groupReps"), "destroy");
    }
}
