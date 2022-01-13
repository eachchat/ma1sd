package io.kamax.mxisd.config.threepid.medium;

import io.kamax.mxisd.threepid.connector.phone.PhoneSmsJGConnector;
import io.kamax.mxisd.threepid.generator.phone.SmsNotificationGenerator;

public class JGConfig extends PhoneConfig {

    public JGConfig() {
        setConnector(PhoneSmsJGConnector.ID);
        setGenerator(SmsNotificationGenerator.ID);
    }

}