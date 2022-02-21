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

import fish.focus.schema.config.types.v1.SettingType;
import fish.focus.uvms.config.constants.ConfigHelper;
import fish.focus.uvms.config.exception.ConfigServiceException;
import fish.focus.uvms.config.service.entity.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class ParameterServiceBean implements ParameterService {

    private static final Logger LOG = LoggerFactory.getLogger(ParameterServiceBean.class);

    @EJB
    private ConfigHelper configHelper;

    @Override
    public String getStringValue(String key) throws ConfigServiceException {
        try {
            TypedQuery<Parameter> query = configHelper.getEntityManager().createNamedQuery(Parameter.FIND_BY_ID, Parameter.class);
            query.setParameter("id", key);
            return query.getSingleResult().getParamValue();
        }
        catch (RuntimeException e) {
            LOG.error("[ Error when getting String value ] {}", e);
            throw new ConfigServiceException("[ Error when getting String value. ]");
        }
    }

    @Override
	public boolean removeParameter(String key) throws ConfigServiceException {
    	try {
    		TypedQuery<Parameter> query = configHelper.getEntityManager().createNamedQuery(Parameter.FIND_BY_ID, Parameter.class);
    		query.setParameter("id", key);
    		Parameter parameter = query.getSingleResult();
    		configHelper.getEntityManager().remove(parameter);
    		configHelper.getEntityManager().flush();
    		return true;
    	} catch (RuntimeException e) {
    		LOG.error("[ Error when remove parameter " + key + " ]", e);
    		throw new ConfigServiceException("[ Error when remove parameter " + key + " ]");
    	}
	}
    
    public List<SettingType> getSettings(List<String> keys) throws ConfigServiceException {
        try {
            TypedQuery<Parameter> query = configHelper.getEntityManager().createNamedQuery(Parameter.LIST_ALL_BY_IDS, Parameter.class);
            query.setParameter("ids", keys);

            List<SettingType> settings = new ArrayList<>();
            for (Parameter parameter : query.getResultList()) {
                SettingType setting = new SettingType();
                setting.setKey(parameter.getParamId());
                setting.setValue(parameter.getParamValue());
                setting.setDescription(parameter.getParamDescription());
                settings.add(setting);
            }

            return settings;
        }
        catch (RuntimeException e) {
            LOG.error("[ Error when getting settings by IDs. ] {}", e);
            throw new ConfigServiceException("[ Error when getting settings by IDs. ]");
        }
    }

    @Override
	public List<SettingType> getAllSettings() throws ConfigServiceException {
        try {
            TypedQuery<Parameter> query = configHelper.getEntityManager().createNamedQuery(Parameter.LIST_ALL, Parameter.class);
            List<SettingType> settings = new ArrayList<>();
            for (Parameter parameter : query.getResultList()) {
                SettingType setting = new SettingType();
                setting.setKey(parameter.getParamId());
                setting.setValue(parameter.getParamValue());
                setting.setDescription(parameter.getParamDescription());
                settings.add(setting);
            }
            return settings;
        }
        catch (RuntimeException e) {
            LOG.error("[ Error when getting all settings. ] {}", e);
            throw new ConfigServiceException("[ Error when getting all settings. ]");
        }
	}
    
    @Override
    public boolean setStringValue(String key, String value, String description) throws ConfigServiceException {
        try {
            TypedQuery<Parameter> query = configHelper.getEntityManager().createNamedQuery(Parameter.FIND_BY_ID, Parameter.class);
            query.setParameter("id", key);
            List<Parameter> parameters = query.getResultList();

            if (parameters.size() == 1) {
                // Update existing parameter
                parameters.get(0).setParamValue(value);
                configHelper.getEntityManager().flush();
            }
            else {
                if (!parameters.isEmpty()) {
                    // Remove all parameters occurring more than once
                    for (Parameter parameter : parameters) {
                        configHelper.getEntityManager().remove(parameter);
                    }
                }
                // Create new parameter
                Parameter parameter = new Parameter();
                parameter.setParamId(key);
                parameter.setParamDescription(description != null ? description : "-");
				parameter.setParamValue(value);
                configHelper.getEntityManager().persist(parameter);
            }
            return true;
        }
        catch (RuntimeException e) {
            LOG.error("[ Error when setting String value. ] {}={}, {}", key, value, description, e);
            throw new ConfigServiceException("[ Error when setting String value. ]");
        }
    }

    @Override
    public Boolean getBooleanValue(String key) throws ConfigServiceException {
        try {
            return parseBooleanValue(getStringValue(key));
        }
        catch (Exception e) {
            LOG.error("[ Error when getting Boolean value. ]", e);
            throw new ConfigServiceException("[ Error when getting Boolean value. ]");
        }
    }

    @Override
    public void reset(String key) throws ConfigServiceException {
    	List<Parameter> parameters;
    	try {
        	TypedQuery<Parameter> query = configHelper.getEntityManager().createNamedQuery(Parameter.FIND_BY_ID, Parameter.class);
            query.setParameter("id", key);
            parameters = query.getResultList();
    	}
    	catch (Exception e) {
    		LOG.error("[ Error when removing parameters. ]", e);
    		throw new ConfigServiceException(e.getMessage());
    	}
    	//TODO: No, fix this!
		for (Parameter parameter : parameters) {
	        try {
	        	configHelper.getEntityManager().remove(parameter);
	        }
        	catch (Exception e) {
        		LOG.error("[ Error when removing parameter. ]", e);
        	}
    	}
    }

    @Override
    public void clearAll() throws ConfigServiceException {
    	List<Parameter> parameters;
    	try {
            TypedQuery<Parameter> query = configHelper.getEntityManager().createNamedQuery(Parameter.LIST_ALL, Parameter.class);
            parameters = query.getResultList();
        }
        catch (Exception e) {
            LOG.error("[ Error when clearing all settings. ] ", e);
            throw new ConfigServiceException("[ Error when clearing all settings. ]");
        }
    	
        for (Parameter parameter : parameters) {
        	try {
                configHelper.getEntityManager().remove(parameter);
        	}
        	catch (Exception e) {
        		LOG.error("[ Error when removing parameter. ]", e);
        	}
        }
    }

    private Boolean parseBooleanValue(String value) {
        if (value.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        else if (value.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        else {
            LOG.error("[ Error when parsing Boolean value from String, The String provided dows not equal 'TRUE' or 'FALSE'. The value is {} ]", value);
            throw new RuntimeException("The String value provided does not equal boolean value, value provided = " + value);
        }
    }
}