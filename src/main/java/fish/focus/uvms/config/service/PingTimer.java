/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package fish.focus.uvms.config.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
@Startup
public class PingTimer {

    private static final Logger LOG = LoggerFactory.getLogger(PingTimer.class);

    @EJB
    private UVMSConfigService configService;

    @PostConstruct
    public void sendPing() {
        LOG.info("PingTimer init");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        PingTask checkCommunicationTask = new PingTask(configService);
        executor.scheduleWithFixedDelay(checkCommunicationTask, 5, 5, TimeUnit.MINUTES);
    }

}