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

import fish.focus.schema.config.module.v1.PullSettingsResponse;
import fish.focus.schema.config.module.v1.PushSettingsResponse;
import fish.focus.schema.config.module.v1.SettingEventType;
import fish.focus.schema.config.types.v1.PullSettingsStatus;
import fish.focus.schema.config.types.v1.SettingType;
import fish.focus.uvms.config.model.mapper.JAXBMarshaller;
import fish.focus.uvms.config.model.mapper.ModuleRequestMapper;
import fish.focus.uvms.config.model.mapper.ModuleResponseMapper;
import fish.focus.uvms.config.constants.ConfigHelper;
import fish.focus.uvms.config.event.ConfigSettingEvent;
import fish.focus.uvms.config.event.ConfigSettingEventType;
import fish.focus.uvms.config.event.ConfigSettingUpdatedEvent;
import fish.focus.uvms.config.exception.ConfigMessageException;
import fish.focus.uvms.config.exception.ConfigServiceException;
import fish.focus.uvms.config.message.ConfigMessageConsumer;
import fish.focus.uvms.config.message.ConfigMessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;

@Stateless
public class UVMSConfigServiceBean implements UVMSConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(UVMSConfigServiceBean.class);

    @EJB
    private ParameterService parameterService;
    
    @EJB
    private ConfigHelper configHelper;
    
    @EJB
    private ConfigMessageProducer producer;
    
    @EJB
    private ConfigMessageConsumer consumer;

    @Inject
    @ConfigSettingUpdatedEvent
    private Event<ConfigSettingEvent> settingUpdated;

    @Override
    public void syncSettingsWithConfig() throws ConfigServiceException {

        try {
            boolean pullSuccess = pullSettingsFromConfig();
            if (!pullSuccess) {
                boolean pushSuccess = pushSettingsToConfig();
                if (!pushSuccess) {
                    throw new ConfigMessageException("Failed to push missing settings to Config.");
                }
            }
        } catch (ConfigMessageException e) {
            LOG.error("[ Error when synchronizing settings with Config module. ] {}", e.getMessage());
            throw new ConfigServiceException(e.getMessage());
        }
    }

	@Override
	public void updateSetting(SettingType setting, SettingEventType eventType) throws ConfigServiceException {
		ConfigSettingEventType configSettingEventType = ConfigSettingEventType.UPDATE;
		try {
	    	if (eventType == SettingEventType.SET) {
				parameterService.setStringValue(setting.getKey(), setting.getValue(), setting.getDescription());
            } else if (eventType == SettingEventType.RESET) {
				configSettingEventType = ConfigSettingEventType.DELETE;
				parameterService.reset(setting.getKey());
			} else {
				throw new ConfigServiceException("SettingEventType " + eventType + " not implemented");
			}
        } catch (Exception e) {
			LOG.error("[ Error when updating setting. ]", e.getMessage());
			throw new ConfigServiceException(e.getMessage());
		}
		settingUpdated.fire(new ConfigSettingEvent(configSettingEventType, setting.getKey()));
	}

    /**
     * @return true if settings were pulled successful, or false if they are
     * missing in the Config module
     */
    private boolean pullSettingsFromConfig() throws ConfigMessageException, ConfigServiceException {
        String request = ModuleRequestMapper.toPullSettingsRequest(configHelper.getModuleName());
        TextMessage response = sendSyncronousMsgWithResponseToConfig(request);
        PullSettingsResponse pullResponse = JAXBMarshaller.unmarshallTextMessage(response, PullSettingsResponse.class);
        if (pullResponse.getStatus() == PullSettingsStatus.MISSING) {
            return false;
        }
        storeSettings(pullResponse.getSettings());
        return true;
    }

    @Override
	public boolean pushSettingToConfig(SettingType setting, boolean remove) {
    	try {
    		String request;
    		if(!remove) {
    			request = ModuleRequestMapper.toSetSettingRequest(configHelper.getModuleName(), setting, "UVMS");
    		} else {
    			setting.setModule(configHelper.getModuleName());
    			request = ModuleRequestMapper.toResetSettingRequest(setting);
    		}
    		sendSyncronousMsgWithResponseToConfig(request);
    		return true;
        } catch (ConfigMessageException e) {
        	return false;
        }
	}

    @Override
    public List<SettingType> getSettings(String keyPrefix) throws ConfigServiceException {
        try {
            String request = ModuleRequestMapper.toListSettingsRequest(configHelper.getModuleName());
            TextMessage response = sendSyncronousMsgWithResponseToConfig(request);
            List<SettingType> settings = ModuleResponseMapper.getSettingsFromSettingsListResponse(response);
            if (keyPrefix != null) {
                settings = getSettingsWithKeyPrefix(settings, keyPrefix);
            }
            return settings;
        } catch (ConfigMessageException e) {
            LOG.error("[ Error when getting settings with key prefix. ] {}", e.getMessage());
            throw new ConfigServiceException("[ Error when getting settings with key prefix. ]");
        }
    }

    private TextMessage sendSyncronousMsgWithResponseToConfig(String request) throws ConfigMessageException {
        String messageId = producer.sendConfigMessage(request);
        return consumer.getConfigMessage(messageId, TextMessage.class);
    }

    /**
     * @return true if settings were pushed successfully
     * @throws ConfigMessageException
     */
    private boolean pushSettingsToConfig() throws ConfigServiceException, ConfigMessageException {
        String moduleName = configHelper.getModuleName();
        List<SettingType> moduleSettings = parameterService.getSettings(configHelper.getAllParameterKeys());
        String request = ModuleRequestMapper.toPushSettingsRequest(moduleName, moduleSettings, "UVMS");
        TextMessage response = sendSyncronousMsgWithResponseToConfig(request);
        PushSettingsResponse pushResponse = JAXBMarshaller.unmarshallTextMessage(response, PushSettingsResponse.class);
        if (pushResponse.getStatus() != PullSettingsStatus.OK) {
            return false;
        }
        storeSettings(pushResponse.getSettings());
        return true;
    }

    private void storeSettings(List<SettingType> settings) throws ConfigServiceException {
        parameterService.clearAll();
        for (SettingType setting: settings) {
            try {
                parameterService.setStringValue(setting.getKey(), setting.getValue(), setting.getDescription());
            } catch (Exception e) {
                LOG.error("[ Error when storing setting. ]", e);
            }
        }
        settingUpdated.fire(new ConfigSettingEvent(ConfigSettingEventType.STORE));
    }

    @Override
    public void sendPing() throws ConfigServiceException {
        try {
            producer.sendConfigMessage(ModuleRequestMapper.toPingRequest(configHelper.getModuleName()));
        } catch (ConfigMessageException e) {
            LOG.error("[ Error when sending ping to config. ] {}", e.getMessage());
            throw new ConfigServiceException(e.getMessage());
        }
    }

    private List<SettingType> getSettingsWithKeyPrefix(List<SettingType> settings, String keyPrefix) {
        List<SettingType> filteredSettings = new ArrayList<>();
        for (SettingType setting : settings) {
            if (setting.getKey().startsWith(keyPrefix)) {
                filteredSettings.add(setting);
            }
        }
        return filteredSettings;
    }

}
