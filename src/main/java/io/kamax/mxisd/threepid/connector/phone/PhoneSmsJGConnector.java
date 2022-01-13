package io.kamax.mxisd.threepid.connector.phone;

import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.JsonObject;
import io.kamax.matrix.json.GsonUtil;
import io.kamax.mxisd.config.threepid.connector.PhoneJGConfig;
import io.kamax.mxisd.exception.InternalServerError;
import io.kamax.mxisd.exception.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PhoneSmsJGConnector implements PhoneConnector {

    public static final String ID = "jiguang";

    private transient final Logger log = LoggerFactory.getLogger(PhoneSmsJGConnector.class);

    private final PhoneJGConfig cfg;

    private final String auth;

    private final CloseableHttpClient client = HttpClients.createDefault();

    public PhoneSmsJGConnector(PhoneJGConfig cfg) {
        this.cfg = cfg.build();
        auth = Base64.getEncoder().encodeToString((cfg.getAppkey() + ":" + cfg.getSecret()).getBytes(StandardCharsets.UTF_8));

        log.info("Jiguang API has been initiated");
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void send(String recipient, String content) {
        if (StringUtils.isBlank(cfg.getAppkey()) || StringUtils.isBlank(cfg.getSecret())) {
            log.error("Jiguang connector in not fully configured and is missing mandatory configuration values.");
            throw new NotImplementedException("Phone numbers cannot be validated at this time. Contact your administrator.");
        }

        recipient = recipient.substring(2); // remove '86' country code
        content = content.substring(content.length() - 6); // only code

        final String pushURL = "https://api.sms.jpush.cn/v1/messages";
        HttpPost request = new HttpPost(pushURL);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        CloseableHttpResponse response = null;
        try {
            String httpContent = GsonUtil.makeObj(
                    new Message(recipient, cfg.getSignID(), cfg.getTempID(),
                            GsonUtil.makeObj("code", content))
            ).toString();
            log.info("Sending SMS notification to {} with code {}.", recipient, content);
            StringEntity entity = new StringEntity(httpContent);
            request.setEntity(entity);
            response = client.execute(request);
            String resp = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatusCodes.STATUS_CODE_OK) {
                log.info("Send SMS notification success, Jiguang push response: {}.", resp);
            } else {
                log.error("Failed to send SMS notification, Jiguang push response: {}.", resp);
                JsonObject errResp = GsonUtil.get().fromJson(resp, JsonObject.class);
                throw new InternalServerError(errResp.getAsJsonObject("error").get("message").toString());
            }
        } catch (InternalServerError e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to send http request, exception: {}.", e.getMessage());
            throw new InternalServerError(e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    log.error("Failed to close response, exception: {}.", e.getMessage());
                }
            }
        }
    }

    public static class Message {

        private final String mobile;
        private final Integer sign_id;
        private final Integer temp_id;
        private final JsonObject temp_para;

        public String getMobile() {
            return mobile;
        }

        public Integer getSignID() {
            return sign_id;
        }

        public Integer getTempID() {
            return temp_id;
        }

        public JsonObject getTempPara() {
            return temp_para;
        }

        public Message(String mobile, Integer signID, Integer tempID, JsonObject tempPara) {
            this.mobile = mobile;
            this.sign_id = signID;
            this.temp_id = tempID;
            this.temp_para = tempPara;
        }

    }

}