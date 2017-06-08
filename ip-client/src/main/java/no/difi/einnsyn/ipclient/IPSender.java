package no.difi.einnsyn.ipclient;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class IPSender {



    @Autowired
    private IPClient ipClient;

    private static final String FILE = "file";

    Logger errorLogger = LoggerFactory.getLogger(IPSender.class);
    Logger transactionLogger  = LoggerFactory.getLogger("ipclient_transactionlogger");

    public String send(Map<String, String> payload, String transactionId,String baseUrl,String messageType,Map<String,String> customProperties, String receiverId, boolean devMode){
        String correlationId = initTransactionId(transactionId);
        JsonObject message = createMessageMetadata(correlationId, messageType,customProperties,receiverId);

        transactionLogger.debug("Post parameters " + message.toString());
        transactionLogger.info("ConversationId " + message.get(IPClientProp.CONVERSATION_ID));

        String conversationId = ipClient.createResource(baseUrl, message);
        transactionLogger.info("Post url " + baseUrl);

        HttpEntity<MultiValueMap<String, Object>> entity = packHttpEntity(payload);
        ipClient.uploadFiles(entity,baseUrl,conversationId,devMode);


        return conversationId;
    }

    public String initTransactionId(String transactionId) {
        //crate message
        String correlationId = UUID.randomUUID().toString();

        if(transactionId!=null){
            correlationId += "_" +transactionId;
        }
        return correlationId;
    }

    public String sendJournaldata(String content,String transactionId, String url, String receiverId,String senderId, boolean devMode){
        Map<String, String> payload = new HashMap<>();
        payload.put(IPClientProp.PAYLOAD,content);

        Map<String,String> customProperties = new HashMap<>();
        customProperties.put(IPClientProp.ORGNUMBER,senderId);
        customProperties.put(IPClientProp.DATA_TYPE,IPClientProp.JOURNALDATA);

        return send(payload,transactionId,url, IPClientProp.DPE_DATA, customProperties,receiverId,devMode);
    }

    public String sendInnsynskrav(String content,String transactionId, String url, String receiverId,String senderId,String email, boolean devMode){
        Map<String, String> payload = new HashMap<>();
        payload.put(IPClientProp.PAYLOAD,content);

        Map<String,String> customProperties = new HashMap<>();
        customProperties.put(IPClientProp.ORGNUMBER,senderId);
        customProperties.put(IPClientProp.DATA_TYPE,IPClientProp.INNSYNSKRAV);
        customProperties.put(IPClientProp.EMAIL,email);

        return send(payload,transactionId,url, IPClientProp.DPE_INNSYN, customProperties,receiverId,devMode);
    }


    public HttpEntity<MultiValueMap<String, Object>> packHttpEntity(Map<String, String> payload) {
        LinkedMultiValueMap fileMap = new LinkedMultiValueMap<>();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        payload.forEach((key,value)->{
            try {
                if(value!=null){
                    fileMap.add(key,new MultipartFileResource(value.getBytes("UTF-8"),key));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });

        return new HttpEntity<>(fileMap,headers);

    }

    public JsonObject createMessageMetadata(String transactionId, String messageType, Map<String,String> customPropertyMap,String receiverId){
        JsonObject messageObject = new JsonObject();
        messageObject.addProperty(IPClientProp.RECEIVER_ID,receiverId);
        messageObject.addProperty(IPClientProp.SERVICE_IDENTIFIER,messageType);
        messageObject.addProperty(IPClientProp.CONVERSATION_ID,transactionId);
        JsonObject customProperties = new JsonObject();
        customPropertyMap.forEach((k,v) -> customProperties.addProperty(k,v));
        messageObject.add(IPClientProp.CUSTOM_PROPERTIES,customProperties);
        return messageObject;
    }


    private class MultipartFileResource extends ByteArrayResource {

        private final String filename;

        public MultipartFileResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }
        @Override
        public String getFilename() {
            return this.filename;
        }
    }



}