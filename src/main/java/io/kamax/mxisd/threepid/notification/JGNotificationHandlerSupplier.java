package io.kamax.mxisd.threepid.notification;

import com.google.gson.JsonSyntaxException;
import io.kamax.matrix.ThreePidMedium;
import io.kamax.matrix.json.GsonUtil;
import io.kamax.mxisd.Mxisd;
import io.kamax.mxisd.config.threepid.medium.JGConfig;
import io.kamax.mxisd.config.threepid.medium.PhoneConfig;
import io.kamax.mxisd.exception.ConfigurationException;
import io.kamax.mxisd.notification.NotificationHandlerSupplier;
import io.kamax.mxisd.notification.NotificationHandlers;
import io.kamax.mxisd.threepid.connector.phone.PhoneConnector;
import io.kamax.mxisd.threepid.connector.phone.PhoneConnectorSupplier;
import io.kamax.mxisd.threepid.generator.phone.PhoneGenerator;
import io.kamax.mxisd.threepid.generator.phone.PhoneGeneratorSupplier;
import io.kamax.mxisd.threepid.notification.phone.PhoneNotificationHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JGNotificationHandlerSupplier implements NotificationHandlerSupplier {

    @Override
    public void accept(Mxisd mxisd) {
        String phoneHandler = mxisd.getConfig().getNotification().getHandler().get(ThreePidMedium.PhoneNumber.getId());
        acceptPhone(phoneHandler, mxisd);
    }

    private void acceptPhone(String handler, Mxisd mxisd) {
        if (StringUtils.equals(PhoneNotificationHandler.ID, handler)) {
            Object o = mxisd.getConfig().getThreepid().getMedium().get(ThreePidMedium.PhoneNumber.getId());
            if (Objects.nonNull(o)) {
                PhoneConfig cfg;
                try {
                    cfg = GsonUtil.get().fromJson(GsonUtil.makeObj(o), JGConfig.class);
                } catch (JsonSyntaxException e) {
                    throw new ConfigurationException("Invalid configuration for threepid msisdn notification");
                }

                List<PhoneGenerator> generators = StreamSupport
                        .stream(ServiceLoader.load(PhoneGeneratorSupplier.class).spliterator(), false)
                        .map(s -> s.apply(cfg, mxisd))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                List<PhoneConnector> connectors = StreamSupport
                        .stream(ServiceLoader.load(PhoneConnectorSupplier.class).spliterator(), false)
                        .map(s -> s.apply(cfg, mxisd))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                NotificationHandlers.register(() -> new PhoneNotificationHandler(cfg, generators, connectors));
            }
        }
    }

}