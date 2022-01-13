package io.kamax.mxisd.threepid.connector.phone;

import com.google.gson.JsonObject;
import io.kamax.matrix.json.GsonUtil;
import io.kamax.mxisd.Mxisd;
import io.kamax.mxisd.config.threepid.connector.PhoneJGConfig;
import io.kamax.mxisd.config.threepid.medium.PhoneConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class JGPhoneConnectorSupplier implements PhoneConnectorSupplier {

    @Override
    public Optional<PhoneConnector> apply(PhoneConfig cfg, Mxisd mxisd) {
        if (StringUtils.equals(PhoneSmsJGConnector.ID, cfg.getConnector())) {
            PhoneJGConfig cCfg = GsonUtil.get().fromJson(cfg.getConnectors().getOrDefault(PhoneSmsJGConnector.ID, new JsonObject()), PhoneJGConfig.class);
            return Optional.of(new PhoneSmsJGConnector(cCfg));
        }
        return Optional.empty();
    }

}