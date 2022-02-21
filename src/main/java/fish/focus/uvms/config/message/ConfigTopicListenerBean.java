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
package fish.focus.uvms.config.message;

import fish.focus.schema.config.module.v1.ConfigTopicBaseRequest;
import fish.focus.schema.config.module.v1.PushModuleSettingMessage;
import fish.focus.schema.config.types.v1.SettingType;
import fish.focus.uvms.config.model.mapper.JAXBMarshaller;
import fish.focus.uvms.config.constants.ConfigConstants;
import fish.focus.uvms.config.constants.ConfigHelper;
import fish.focus.uvms.config.exception.ConfigServiceException;
import fish.focus.uvms.config.service.UVMSConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

//@formatter:off
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = ConfigConstants.DESTINATION_TYPE_TOPIC),
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = ConfigConstants.CONFIG_STATUS_TOPIC)
})
//@formatter:on
public class ConfigTopicListenerBean implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigTopicListenerBean.class);

    @EJB
    private UVMSConfigService configService;

    @EJB
    private ConfigHelper configHelper;

	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage) message;
        try {
            ConfigTopicBaseRequest baseRequest = JAXBMarshaller.unmarshallTextMessage(textMessage, ConfigTopicBaseRequest.class);
            switch (baseRequest.getStatus()) {
            case DEPLOYED:
                configService.syncSettingsWithConfig();
                break;
            case SETTING_CHANGED:
            	PushModuleSettingMessage pushMessage = JAXBMarshaller.unmarshallTextMessage(textMessage, PushModuleSettingMessage.class);
                SettingType setting = pushMessage.getSetting();
            	if (!ignoreModule(setting.getModule())) {
            		configService.updateSetting(setting, pushMessage.getAction());
            	}

            	break;
            }
        } catch (ConfigServiceException e) {
            LOG.error("[ Error when synchronizing settings with Config. ] {}", e.getMessage());
        }
    }

	private boolean ignoreModule(String moduleName) {
		return moduleName != null && !moduleName.equals(configHelper.getModuleName());
	}

}