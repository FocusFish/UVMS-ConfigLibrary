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

import java.util.List;

import javax.ejb.Local;

import fish.focus.schema.config.module.v1.SettingEventType;
import fish.focus.schema.config.types.v1.SettingType;
import fish.focus.uvms.config.exception.ConfigServiceException;

@Local
public interface UVMSConfigService {

    /**
     * Requests parameters from Config module and stores in the database. If
     * module is not registered, pushes current parameters to Config.
     *  
     * @throws
     * fish.focus.uvms.config.exception.ConfigServiceException
     */
    void syncSettingsWithConfig() throws ConfigServiceException;

    /**
     * Updates a setting by setting a value or removing it.
     * 
     * @param setting a setting
     * @param eventType an event type
     * @throws ConfigServiceException if unsuccessful
     */
    void updateSetting(SettingType setting, SettingEventType eventType) throws ConfigServiceException;

    /**
     * Push setting to config
     *
     * @param setting
     * @param remove
     * @return
     * @throws ConfigServiceException
     */
    boolean pushSettingToConfig(SettingType setting, boolean remove) throws ConfigServiceException;

    /**
     * @param keyPrefix a prefix
     * @return list of settings for this module, filtered by key prefix
     * @throws ConfigServiceException if unsuccessful
     */
    List<SettingType> getSettings(String keyPrefix) throws ConfigServiceException;

    /**
     * Sends a ping message to the Config module.
     * 
     * @throws ConfigServiceException if unsuccessful 
     */
    void sendPing() throws ConfigServiceException;

}
