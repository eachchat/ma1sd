package io.kamax.mxisd.config.threepid.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhoneJGConfig {

    private transient final Logger log = LoggerFactory.getLogger(PhoneJGConfig.class);

    private String appkey = "";
    private String master_secret = "";
    private Integer temp_id = 1;
    private Integer sign_id = 1;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getSecret() {
        return master_secret;
    }

    public void setSecret(String secret) {
        this.master_secret = secret;
    }

    public Integer getTempID() {
        return temp_id;
    }

    public void setTempID(Integer tempID) {
        this.temp_id = tempID;
    }

    public Integer getSignID() {
        return sign_id;
    }

    public void setSignID(Integer signID) {
        this.sign_id = signID;
    }

    public PhoneJGConfig build() {
        log.info("--- Phone SMS Jiguang connector config ---");
        log.info("Appkey: {}", appkey);
        log.info("Master Secret: {}", master_secret);

        return this;
    }

}