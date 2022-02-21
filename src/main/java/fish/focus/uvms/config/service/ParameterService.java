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
import fish.focus.schema.config.types.v1.SettingType;
import fish.focus.uvms.config.exception.ConfigServiceException;

@Local
public interface ParameterService {

    /**
     * Returns the parameter value as a string.
     * 
     * @param key a parameter key
     * @return a string representation of the parameter value
     * @throws ConfigServiceException if unsuccessful
     */
    String getStringValue(String key) throws ConfigServiceException;

    /**
     * Remove the parameter by key
     * @param key a parameter key
     * @return true if able to remove, false otherwise
     * @throws ConfigServiceException
     */
    boolean removeParameter(String key) throws ConfigServiceException;
    
    /**
     * @param keys a list of parameter keys
     * @return a list of all settings matching one of the keys
     * @throws ConfigServiceException if unsuccessful
     */
    List<SettingType> getSettings(List<String> keys) throws ConfigServiceException;

    /**
     * Get all settings in parameter table
     * @return
     * @throws ConfigServiceException
     */
    List<SettingType> getAllSettings() throws ConfigServiceException;
    
    /**
     * Sets a value for the specified key.
     *
     * @param key a parameter key
     * @param value a value
     * @param description a description of the parameter
     * @throws ConfigServiceException if unsuccessful
     */
    boolean setStringValue(String key, String value, String description) throws ConfigServiceException;

    /**
     * Returns the parameter value as a boolean.
     * Persisted value must be case insensitively "true" or "false". 
     * 
     * @param key a parameter key
     * @return a boolean representation of the parameter value
     * @throws ConfigServiceException if parameter is neither "true" nor "false"
     */
    Boolean getBooleanValue(String key) throws ConfigServiceException;

    /**
     * Removes any parameter with the specified key.
     * 
     * @param key a parameter key 
     * @throws ConfigServiceException if unsuccessful
     */
    void reset(String key) throws ConfigServiceException;

    /**
     * Removes all parameters.
     * 
     * @throws ConfigServiceException if unsuccessful
     */
    void clearAll() throws ConfigServiceException;
}