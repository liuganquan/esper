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
package com.espertech.esper.common.internal.epl.expression.core;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypeNull;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenBlock;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRef;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionRelational;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.type.RelationalOpEnum;
import com.espertech.esper.common.internal.util.JavaClassHelper;
import com.espertech.esper.common.internal.util.SimpleNumberBigDecimalCoercer;
import com.espertech.esper.common.internal.util.SimpleNumberBigIntegerCoercer;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;
import static com.espertech.esper.common.internal.type.RelationalOpEnum.GT;
import static com.espertech.esper.common.internal.type.RelationalOpEnum.LT;

/**
 * Enumeration for the type of arithmatic to use.
 */
public enum MinMaxTypeEnum {
    /**
     * Max.
     */
    MAX("max"),

    /**
     * Min.
     */
    MIN("min");

    private String expressionText;

    private MinMaxTypeEnum(String expressionText) {
        this.expressionText = expressionText;
    }

    /**
     * Returns textual representation of enum.
     *
     * @return text for enum
     */
    public String getExpressionText() {
        return expressionText;
    }

    /**
     * Executes child expression nodes and compares results.
     */
    public interface Computer {
        /**
         * Executes child expression nodes and compares results, returning the min/max.
         *
         * @param eventsPerStream      events per stream
         * @param isNewData            true if new data
         * @param exprEvaluatorContext expression evaluation context
         * @return result
         */
        public Number execute(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext);
    }

    /**
     * Determines minimum using Number.doubleValue().
     */
    public static class MinComputerDoubleCoerce implements Computer {
        private final ExprEvaluator[] childNodes;

        /**
         * Ctor.
         *
         * @param childNodes array of expression nodes
         */
        public MinComputerDoubleCoerce(ExprEvaluator[] childNodes) {
            this.childNodes = childNodes;
        }

        public Number execute(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
            Number valueChildOne = (Number) childNodes[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            Number valueChildTwo = (Number) childNodes[1].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

            if ((valueChildOne == null) || (valueChildTwo == null)) {
                return null;
            }

            Number result;
            if (valueChildOne.doubleValue() > valueChildTwo.doubleValue()) {
                result = valueChildTwo;
            } else {
                result = valueChildOne;
            }
            for (int i = 2; i < childNodes.length; i++) {
                Number valueChild = (Number) childNodes[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                if (valueChild == null) {
                    return null;
                }
                if (valueChild.doubleValue() < result.doubleValue()) {
                    result = valueChild;
                }
            }
            return result;
        }

        public static CodegenExpression codegen(CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope, ExprNode[] nodes, EPTypeClass returnType) {
            return MinMaxTypeEnum.codegenMinMax(true, codegenMethodScope, exprSymbol, codegenClassScope, nodes, returnType);
        }
    }

    /**
     * Determines maximum using Number.doubleValue().
     */
    public static class MaxComputerDoubleCoerce implements Computer {
        private final ExprEvaluator[] childNodes;

        /**
         * Ctor.
         *
         * @param childNodes array of expression nodes
         */
        public MaxComputerDoubleCoerce(ExprEvaluator[] childNodes) {
            this.childNodes = childNodes;
        }

        public Number execute(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
            Number valueChildOne = (Number) childNodes[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            Number valueChildTwo = (Number) childNodes[1].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

            if ((valueChildOne == null) || (valueChildTwo == null)) {
                return null;
            }

            Number result;
            if (valueChildOne.doubleValue() > valueChildTwo.doubleValue()) {
                result = valueChildOne;
            } else {
                result = valueChildTwo;
            }
            for (int i = 2; i < childNodes.length; i++) {
                Number valueChild = (Number) childNodes[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                if (valueChild == null) {
                    return null;
                }
                if (valueChild.doubleValue() > result.doubleValue()) {
                    result = valueChild;
                }
            }
            return result;
        }

        public static CodegenExpression codegen(CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope, ExprNode[] nodes, EPTypeClass returnType) {
            return MinMaxTypeEnum.codegenMinMax(false, codegenMethodScope, exprSymbol, codegenClassScope, nodes, returnType);
        }
    }

    /**
     * Determines minimum/maximum using BigInteger.compareTo.
     */
    public static class ComputerBigIntCoerce implements Computer {
        private final ExprEvaluator[] childNodes;
        private final SimpleNumberBigIntegerCoercer[] convertors;
        private final boolean isMax;

        /**
         * Ctor.
         *
         * @param childNodes expressions
         * @param convertors convertors to BigInteger
         * @param isMax      true if max, false if min
         */
        public ComputerBigIntCoerce(ExprEvaluator[] childNodes, SimpleNumberBigIntegerCoercer[] convertors, boolean isMax) {
            this.childNodes = childNodes;
            this.convertors = convertors;
            this.isMax = isMax;
        }

        public Number execute(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
            Number valueChildOne = (Number) childNodes[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            Number valueChildTwo = (Number) childNodes[1].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

            if ((valueChildOne == null) || (valueChildTwo == null)) {
                return null;
            }

            BigInteger bigIntOne = convertors[0].coerceBoxedBigInt(valueChildOne);
            BigInteger bigIntTwo = convertors[1].coerceBoxedBigInt(valueChildTwo);

            BigInteger result;
            if ((isMax && (bigIntOne.compareTo(bigIntTwo) > 0)) ||
                    (!isMax && (bigIntOne.compareTo(bigIntTwo) < 0))) {
                result = bigIntOne;
            } else {
                result = bigIntTwo;
            }
            for (int i = 2; i < childNodes.length; i++) {
                Number valueChild = (Number) childNodes[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                if (valueChild == null) {
                    return null;
                }
                BigInteger bigInt = convertors[i].coerceBoxedBigInt(valueChild);
                if ((isMax && (result.compareTo(bigInt) < 0)) ||
                        (!isMax && (result.compareTo(bigInt) > 0))) {
                    result = bigInt;
                }
            }
            return result;
        }

        public static CodegenExpression codegen(boolean max, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope, ExprNode[] nodes, SimpleNumberBigIntegerCoercer[] convertors) {
            EPType r0Type = nodes[0].getForge().getEvaluationType();
            EPType r1Type = nodes[1].getForge().getEvaluationType();
            if (r0Type == null || r1Type == null || r0Type == EPTypeNull.INSTANCE || r1Type == EPTypeNull.INSTANCE) {
                return constantNull();
            }

            CodegenMethod methodNode = codegenMethodScope.makeChild(EPTypePremade.BIGINTEGER.getEPType(), ComputerBigIntCoerce.class, codegenClassScope);
            CodegenBlock block = methodNode.getBlock();

            EPTypeClass r0TypeClass = (EPTypeClass) r0Type;
            EPTypeClass r1TypeClass = (EPTypeClass) r1Type;

            block.declareVar(r0TypeClass, "r0", nodes[0].getForge().evaluateCodegen(r0TypeClass, methodNode, exprSymbol, codegenClassScope));
            if (!r0TypeClass.getType().isPrimitive()) {
                block.ifRefNullReturnNull("r0");
            }
            block.declareVar(r1TypeClass, "r1", nodes[1].getForge().evaluateCodegen(r1TypeClass, methodNode, exprSymbol, codegenClassScope));
            if (!r1TypeClass.getType().isPrimitive()) {
                block.ifRefNullReturnNull("r1");
            }
            block.declareVar(EPTypePremade.BIGINTEGER.getEPType(), "bi0", convertors[0].coerceBoxedBigIntCodegen(ref("r0"), r0TypeClass));
            block.declareVar(EPTypePremade.BIGINTEGER.getEPType(), "bi1", convertors[1].coerceBoxedBigIntCodegen(ref("r1"), r1TypeClass));

            block.declareVarNoInit(EPTypePremade.BIGINTEGER.getEPType(), "result");
            block.ifCondition(codegenCompareCompareTo(ref("bi0"), ref("bi1"), max))
                    .assignRef("result", ref("bi0"))
                    .ifElse()
                    .assignRef("result", ref("bi1"))
                    .blockEnd();

            for (int i = 2; i < nodes.length; i++) {
                EPTypeClass nodeType = (EPTypeClass) nodes[i].getForge().getEvaluationType();
                String refnameNumber = "r" + i;
                block.declareVar(nodeType, refnameNumber, nodes[i].getForge().evaluateCodegen(nodeType, methodNode, exprSymbol, codegenClassScope));
                if (!nodeType.getType().isPrimitive()) {
                    block.ifRefNullReturnNull(refnameNumber);
                }
                String refnameBigint = "bi" + i;
                block.declareVar(EPTypePremade.BIGINTEGER.getEPType(), refnameBigint, convertors[i].coerceBoxedBigIntCodegen(ref(refnameNumber), nodeType));
                block.ifCondition(not(codegenCompareCompareTo(ref("result"), ref(refnameBigint), max)))
                        .assignRef("result", ref(refnameBigint))
                        .blockEnd();
            }
            block.methodReturn(ref("result"));
            return localMethod(methodNode);
        }
    }

    /**
     * Determines minimum/maximum using BigDecimal.compareTo.
     */
    public static class ComputerBigDecCoerce implements Computer {
        private final ExprEvaluator[] childNodes;
        private final SimpleNumberBigDecimalCoercer[] convertors;
        private final boolean isMax;

        /**
         * Ctor.
         *
         * @param childNodes expressions
         * @param convertors convertors to BigDecimal
         * @param isMax      true if max, false if min
         */
        public ComputerBigDecCoerce(ExprEvaluator[] childNodes, SimpleNumberBigDecimalCoercer[] convertors, boolean isMax) {
            this.childNodes = childNodes;
            this.convertors = convertors;
            this.isMax = isMax;
        }

        public Number execute(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
            Number valueChildOne = (Number) childNodes[0].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            Number valueChildTwo = (Number) childNodes[1].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);

            if ((valueChildOne == null) || (valueChildTwo == null)) {
                return null;
            }

            BigDecimal bigDecOne = convertors[0].coerceBoxedBigDec(valueChildOne);
            BigDecimal bigDecTwo = convertors[1].coerceBoxedBigDec(valueChildTwo);

            BigDecimal result;
            if ((isMax && (bigDecOne.compareTo(bigDecTwo) > 0)) ||
                    (!isMax && (bigDecOne.compareTo(bigDecTwo) < 0))) {
                result = bigDecOne;
            } else {
                result = bigDecTwo;
            }
            for (int i = 2; i < childNodes.length; i++) {
                Number valueChild = (Number) childNodes[i].evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
                if (valueChild == null) {
                    return null;
                }
                BigDecimal bigDec = convertors[i].coerceBoxedBigDec(valueChild);
                if ((isMax && (result.compareTo(bigDec) < 0)) ||
                        (!isMax && (result.compareTo(bigDec) > 0))) {
                    result = bigDec;
                }
            }
            return result;
        }

        public static CodegenExpression codegen(boolean max, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope, ExprNode[] nodes, SimpleNumberBigDecimalCoercer[] convertors) {
            EPType r0Type = nodes[0].getForge().getEvaluationType();
            EPType r1Type = nodes[1].getForge().getEvaluationType();
            if (r0Type == null || r1Type == null || r0Type == EPTypeNull.INSTANCE || r1Type == EPTypeNull.INSTANCE) {
                return constantNull();
            }
            CodegenMethod methodNode = codegenMethodScope.makeChild(EPTypePremade.BIGDECIMAL.getEPType(), ComputerBigDecCoerce.class, codegenClassScope);
            CodegenBlock block = methodNode.getBlock();

            EPTypeClass r0TypeClass = (EPTypeClass) r0Type;
            EPTypeClass r1TypeClass = (EPTypeClass) r1Type;

            block.declareVar(r0TypeClass, "r0", nodes[0].getForge().evaluateCodegen(r0TypeClass, methodNode, exprSymbol, codegenClassScope));
            if (!r0TypeClass.getType().isPrimitive()) {
                block.ifRefNullReturnNull("r0");
            }
            block.declareVar(r1TypeClass, "r1", nodes[1].getForge().evaluateCodegen(r1TypeClass, methodNode, exprSymbol, codegenClassScope));
            if (!r1TypeClass.getType().isPrimitive()) {
                block.ifRefNullReturnNull("r1");
            }
            block.declareVar(EPTypePremade.BIGDECIMAL.getEPType(), "bi0", convertors[0].coerceBoxedBigDecCodegen(ref("r0"), r0TypeClass));
            block.declareVar(EPTypePremade.BIGDECIMAL.getEPType(), "bi1", convertors[1].coerceBoxedBigDecCodegen(ref("r1"), r1TypeClass));

            block.declareVarNoInit(EPTypePremade.BIGDECIMAL.getEPType(), "result");
            block.ifCondition(codegenCompareCompareTo(ref("bi0"), ref("bi1"), max))
                    .assignRef("result", ref("bi0"))
                    .ifElse()
                    .assignRef("result", ref("bi1"))
                    .blockEnd();

            for (int i = 2; i < nodes.length; i++) {
                EPTypeClass nodeType = (EPTypeClass) nodes[i].getForge().getEvaluationType();
                String refnameNumber = "r" + i;
                block.declareVar(nodeType, refnameNumber, nodes[i].getForge().evaluateCodegen(nodeType, methodNode, exprSymbol, codegenClassScope));
                if (!nodeType.getType().isPrimitive()) {
                    block.ifRefNullReturnNull(refnameNumber);
                }
                String refnameBigint = "bi" + i;
                block.declareVar(EPTypePremade.BIGDECIMAL.getEPType(), refnameBigint, convertors[i].coerceBoxedBigDecCodegen(ref(refnameNumber), nodeType));
                block.ifCondition(not(codegenCompareCompareTo(ref("result"), ref(refnameBigint), max)))
                        .assignRef("result", ref(refnameBigint))
                        .blockEnd();
            }
            block.methodReturn(ref("result"));
            return localMethod(methodNode);
        }
    }

    private static CodegenExpression codegenMinMax(boolean min, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope, ExprNode[] nodes, EPTypeClass returnType) {
        EPType r0Type = nodes[0].getForge().getEvaluationType();
        EPType r1Type = nodes[1].getForge().getEvaluationType();
        if (r0Type == null || r1Type == null || r0Type == EPTypeNull.INSTANCE || r1Type == EPTypeNull.INSTANCE) {
            return constantNull();
        }
        CodegenMethod methodNode = codegenMethodScope.makeChild(returnType, MaxComputerDoubleCoerce.class, codegenClassScope);
        CodegenBlock block = methodNode.getBlock();

        EPTypeClass r0TypeClass = (EPTypeClass) r0Type;
        EPTypeClass r1TypeClass = (EPTypeClass) r1Type;

        block.declareVar(r0TypeClass, "r0", nodes[0].getForge().evaluateCodegen(r0TypeClass, methodNode, exprSymbol, codegenClassScope));
        if (!r0TypeClass.getType().isPrimitive()) {
            block.ifRefNullReturnNull("r0");
        }
        block.declareVar(r1TypeClass, "r1", nodes[1].getForge().evaluateCodegen(r1TypeClass, methodNode, exprSymbol, codegenClassScope));
        if (!r1TypeClass.getType().isPrimitive()) {
            block.ifRefNullReturnNull("r1");
        }

        block.declareVarNoInit(returnType, "result");
        block.ifCondition(codegenCompareRelop(returnType, min ? LT : GT, ref("r0"), r0TypeClass, ref("r1"), r1TypeClass))
                .assignRef("result", JavaClassHelper.coerceNumberToBoxedCodegen(ref("r0"), r0TypeClass, returnType))
                .ifElse()
                .assignRef("result", JavaClassHelper.coerceNumberToBoxedCodegen(ref("r1"), r1TypeClass, returnType))
                .blockEnd();

        for (int i = 2; i < nodes.length; i++) {
            EPTypeClass nodeType = (EPTypeClass) nodes[i].getForge().getEvaluationType();
            String refname = "r" + i;
            block.declareVar(nodeType, refname, nodes[i].getForge().evaluateCodegen(nodeType, methodNode, exprSymbol, codegenClassScope));
            if (!nodeType.getType().isPrimitive()) {
                block.ifRefNullReturnNull(refname);
            }
            block.ifCondition(not(codegenCompareRelop(returnType, min ? LT : GT, ref("result"), returnType, ref(refname), r1TypeClass)))
                    .assignRef("result", JavaClassHelper.coerceNumberToBoxedCodegen(ref(refname), nodeType, returnType))
                    .blockEnd();
        }
        block.methodReturn(ref("result"));
        return localMethod(methodNode);
    }

    private static CodegenExpression codegenCompareRelop(EPTypeClass resultType, RelationalOpEnum op, CodegenExpressionRef lhs, EPTypeClass lhsType, CodegenExpression rhs, EPTypeClass rhsType) {
        return op(lhs, op.getExpressionText(), rhs);
    }

    private static CodegenExpression codegenCompareCompareTo(CodegenExpression lhs, CodegenExpression rhs, boolean max) {
        return relational(exprDotMethod(lhs, "compareTo", rhs), max ? CodegenExpressionRelational.CodegenRelational.GT : CodegenExpressionRelational.CodegenRelational.LT, constant(0));
    }
}
