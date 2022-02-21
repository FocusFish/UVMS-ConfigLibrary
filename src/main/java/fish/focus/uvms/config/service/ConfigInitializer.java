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

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fish.focus.uvms.config.exception.ConfigServiceException;
import javax.ejb.EJB;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
@Startup
public class ConfigInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigInitializer.class);

    @EJB
    private UVMSConfigService configService;

    @PostConstruct
    protected void startup() {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    configService.syncSettingsWithConfig();
                } catch (ConfigServiceException e) {
                    LOG.error("[ Error when synchronizing settings with Config at startup. ]");
                }
            }
        });
    }
}