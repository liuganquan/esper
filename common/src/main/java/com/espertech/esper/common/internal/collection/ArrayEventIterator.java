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
package com.espertech.esper.common.internal.collection;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator for an array of events.
 */
public class ArrayEventIterator implements Iterator<EventBean> {
    public final static EPTypeClass EPTYPE = new EPTypeClass(ArrayEventIterator.class);

    private final EventBean[] events;
    private int position;

    /**
     * Ctor.
     *
     * @param events is an array of events to iterator over, or null to indicate no results
     */
    public ArrayEventIterator(EventBean[] events) {
        this.events = events;
    }

    public boolean hasNext() {
        if ((events == null) || (position >= events.length)) {
            return false;
        }
        return true;
    }

    public EventBean next() {
        if ((events == null) || (position >= events.length)) {
            throw new NoSuchElementException();
        }
        return events[position++];
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
