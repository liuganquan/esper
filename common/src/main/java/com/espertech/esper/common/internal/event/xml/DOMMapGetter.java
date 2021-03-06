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
package com.espertech.esper.common.internal.event.xml;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.PropertyAccessException;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionField;
import com.espertech.esper.common.internal.event.core.EventPropertyGetterSPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

/**
 * DOM getter for Map-property.
 */
public class DOMMapGetter implements EventPropertyGetterSPI, DOMPropertyGetter {
    private final String propertyMap;
    private final String mapKey;
    private final FragmentFactorySPI fragmentFactory;

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param node        node
     * @param propertyMap property
     * @param mapKey      key
     * @return value
     */
    public static Node getNodeValue(Node node, String propertyMap, String mapKey) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            if (childNode == null) {
                continue;
            }
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (!(childNode.getNodeName().equals(propertyMap))) {
                continue;
            }

            Node attribute = childNode.getAttributes().getNamedItem("id");
            if (attribute == null) {
                continue;
            }
            if (!(attribute.getTextContent().equals(mapKey))) {
                continue;
            }

            return childNode;
        }
        return null;
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param node        node
     * @param propertyMap property
     * @param mapKey      key
     * @return exists flag
     */
    public static boolean getNodeValueExists(Node node, String propertyMap, String mapKey) {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            if (childNode == null) {
                continue;
            }
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            if (!(childNode.getNodeName().equals(propertyMap))) {
                continue;
            }

            Node attribute = childNode.getAttributes().getNamedItem("id");
            if (attribute == null) {
                continue;
            }
            if (!(attribute.getTextContent().equals(mapKey))) {
                continue;
            }

            return true;
        }
        return false;
    }

    /**
     * Ctor.
     *
     * @param propertyName    property name
     * @param mapKey          key in map
     * @param fragmentFactory for creating fragments
     */
    public DOMMapGetter(String propertyName, String mapKey, FragmentFactorySPI fragmentFactory) {
        this.propertyMap = propertyName;
        this.mapKey = mapKey;
        this.fragmentFactory = fragmentFactory;
    }

    public Node[] getValueAsNodeArray(Node node) {
        return null;
    }

    public Object getValueAsFragment(Node node) {
        if (fragmentFactory == null) {
            return null;
        }

        Node result = getValueAsNode(node);
        if (result == null) {
            return null;
        }
        return fragmentFactory.getEvent(result);
    }

    private CodegenMethod getValueAsFragmentCodegen(CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        CodegenExpressionField member = codegenClassScope.addFieldUnshared(true, FragmentFactory.EPTYPE, fragmentFactory.make(codegenClassScope.getPackageScope().getInitMethod(), codegenClassScope));
        return codegenMethodScope.makeChild(EPTypePremade.OBJECT.getEPType(), this.getClass(), codegenClassScope).addParam(EPTypePremade.NODE.getEPType(), "node").getBlock()
                .declareVar(EPTypePremade.NODE.getEPType(), "result", getValueAsNodeCodegen(ref("node"), codegenMethodScope, codegenClassScope))
                .ifRefNullReturnNull("result")
                .methodReturn(exprDotMethod(member, "getEvent", ref("result")));
    }

    public Node getValueAsNode(Node node) {
        return getNodeValue(node, propertyMap, mapKey);
    }

    public Object get(EventBean eventBean) throws PropertyAccessException {
        Object result = eventBean.getUnderlying();
        if (!(result instanceof Node)) {
            return null;
        }
        Node node = (Node) result;
        return getValueAsNode(node);
    }

    public boolean isExistsProperty(EventBean eventBean) {
        Object result = eventBean.getUnderlying();
        if (!(result instanceof Node)) {
            return false;
        }
        Node node = (Node) result;
        return getNodeValueExists(node, propertyMap, mapKey);
    }

    public Object getFragment(EventBean eventBean) {
        return null;
    }

    public CodegenExpression eventBeanGetCodegen(CodegenExpression beanExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return underlyingGetCodegen(castUnderlying(EPTypePremade.NODE.getEPType(), beanExpression), codegenMethodScope, codegenClassScope);
    }

    public CodegenExpression eventBeanExistsCodegen(CodegenExpression beanExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return underlyingExistsCodegen(castUnderlying(EPTypePremade.NODE.getEPType(), beanExpression), codegenMethodScope, codegenClassScope);
    }

    public CodegenExpression eventBeanFragmentCodegen(CodegenExpression beanExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return constantNull();
    }

    public CodegenExpression underlyingGetCodegen(CodegenExpression underlyingExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return staticMethod(this.getClass(), "getNodeValue", underlyingExpression, constant(propertyMap), constant(mapKey));
    }

    public CodegenExpression underlyingExistsCodegen(CodegenExpression underlyingExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return staticMethod(this.getClass(), "getNodeValueExists", underlyingExpression, constant(propertyMap), constant(mapKey));
    }

    public CodegenExpression underlyingFragmentCodegen(CodegenExpression underlyingExpression, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return constantNull();
    }

    public CodegenExpression getValueAsNodeCodegen(CodegenExpression value, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return underlyingGetCodegen(value, codegenMethodScope, codegenClassScope);
    }

    public CodegenExpression getValueAsNodeArrayCodegen(CodegenExpression value, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return constantNull();
    }

    public CodegenExpression getValueAsFragmentCodegen(CodegenExpression value, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return localMethod(getValueAsFragmentCodegen(codegenMethodScope, codegenClassScope), value);
    }
}
