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
package com.espertech.esper.runtime.internal.kernel.thread;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.internal.context.util.EPStatementHandleCallbackFilter;
import com.espertech.esper.runtime.internal.kernel.stage.EPStageEventServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Route unit for single match.
 */
public class RouteUnitSingleStaged implements RouteUnitRunnable {
    private static final Logger log = LoggerFactory.getLogger(RouteUnitSingleStaged.class);

    private final EPStageEventServiceImpl epRuntime;
    private EPStatementHandleCallbackFilter handleCallback;
    private final EventBean theEvent;
    private final long filterVersion;

    /**
     * Ctor.
     *
     * @param epRuntime      runtime to process
     * @param handleCallback callback
     * @param theEvent       event
     * @param filterVersion  version of filter
     */
    public RouteUnitSingleStaged(EPStageEventServiceImpl epRuntime, EPStatementHandleCallbackFilter handleCallback, EventBean theEvent, long filterVersion) {
        this.epRuntime = epRuntime;
        this.theEvent = theEvent;
        this.handleCallback = handleCallback;
        this.filterVersion = filterVersion;
    }

    public void run() {
        try {
            epRuntime.processStatementFilterSingle(handleCallback.getAgentInstanceHandle(), handleCallback, theEvent, filterVersion, 0);

            epRuntime.dispatch();

            epRuntime.processThreadWorkQueue();
        } catch (RuntimeException e) {
            log.error("Unexpected error processing route execution: " + e.getMessage(), e);
        }
    }

}
