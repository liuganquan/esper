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
package com.espertech.esper.common.internal.filterspec;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.core.ExprFilterSpecLookupable;

import java.io.StringWriter;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.newInstance;
import static com.espertech.esper.common.internal.filterspec.FilterSpecParam.REF_FILTEROPERATOR;
import static com.espertech.esper.common.internal.filterspec.FilterSpecParam.REF_LOOKUPABLE;

/**
 * Filter parameter value defining the event property to filter, the filter operator, and the filter value.
 */
public class FilterValueSetParamImpl implements FilterValueSetParam {
    public final static EPTypeClass EPTYPE = new EPTypeClass(FilterValueSetParamImpl.class);

    private final ExprFilterSpecLookupable lookupable;
    private final FilterOperator filterOperator;
    private final Object filterValue;

    /**
     * Ctor.
     *
     * @param lookupable     - stuff to use to interrogate
     * @param filterOperator - operator to apply
     * @param filterValue    - value to look for
     */
    public FilterValueSetParamImpl(ExprFilterSpecLookupable lookupable, FilterOperator filterOperator, Object filterValue) {
        this.lookupable = lookupable;
        this.filterOperator = filterOperator;
        this.filterValue = filterValue;
    }

    public static CodegenExpression codegenNew(CodegenExpression filterForValue) {
        return newInstance(FilterValueSetParamImpl.EPTYPE, REF_LOOKUPABLE, REF_FILTEROPERATOR, filterForValue);
    }

    public ExprFilterSpecLookupable getLookupable() {
        return lookupable;
    }

    public FilterOperator getFilterOperator() {
        return filterOperator;
    }

    public Object getFilterForValue() {
        return filterValue;
    }

    public String toString() {
        return "FilterValueSetParamImpl{" +
                "lookupable='" + lookupable + '\'' +
                ", filterOperator=" + filterOperator +
                ", filterValue=" + filterValue +
                '}';
    }

    public void appendTo(StringWriter writer) {
        lookupable.appendTo(writer);
        writer.append(filterOperator.getTextualOp());
        writer.append(filterValue == null ? "null" : filterValue.toString());
    }
}
