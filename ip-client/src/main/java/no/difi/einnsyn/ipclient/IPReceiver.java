package no.difi.einnsyn.ipclient;



import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;


@Service
public class IPReceiver {

    private final Logger logger = LoggerFactory.getLogger(IPReceiver.class);

    private RestTemplate restTemplate;

    @Autowired
    private IPClient ipClient;

    public Map<String,Object> getMessage(String baseUrl, String serviceIdentifier,List<String> fileNames) {

        Map<String,Object> messageMap = new HashMap();



        //peek
        JSONParser jsonParser = new JSONParser();
        String responseString = null;

        responseString = ipClient.peek(baseUrl, serviceIdentifier);

        if(responseString!=null){
                    Object responseObject = null;
            try {
                responseObject = jsonParser.parse(responseString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
                    JSONObject response = (JSONObject)responseObject;
            String conversationId = response.get(IPClientProp.CONVERSATION_ID).toString();
            logger.debug("Peek conversatioId " + conversationId);

            Map<String,Object> responseMap = setMetadata(response);
            messageMap.put("metadata",responseMap);
            logger.debug("Get metadata " +  responseMap);


            //pop
            byte[] asic = ipClient.pop(baseUrl,serviceIdentifier,conversationId);
            System.out.println("Asic " + asic.length);

            Map<String,String>  files = getPayload(asic,fileNames);
            logger.debug("Get files " + files);

            messageMap.put("files",files);

        }
        return messageMap;
    }

    public Map<String,Object> setMetadata(JSONObject responseObject){
        Map<String,Object> responseMap = new HashMap<>();
        logger.debug("Responseobject " + responseObject);
        responseMap.put("transactionId",responseObject.get(IPClientProp.CONVERSATION_ID).toString());
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject customProperties = (JSONObject)jsonParser.parse(responseObject.get(IPClientProp.CUSTOM_PROPERTIES).toString());
            customProperties.forEach((k,v) -> responseMap.put(k.toString(),v.toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Object properties = responseObject.get(IPClientProp.CUSTOM_PROPERTIES);
        JSONObject customProperties = (JSONObject) properties;
        logger.debug("Custom properties " + customProperties);

//        customProperties.forEach((k,v) -> responseMap.put(k.toString(),v.toString()));
        return responseMap;
    }

    public Map<String, String> getPayload(byte[] asic, List<String> entries){
        Map<String,String>  files = new HashMap<>();
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(asic));
        ZipEntry zipEntry = null;
        try {
            while ((zipEntry = zipInputStream.getNextEntry())!=null){
                if(entries.contains(zipEntry.getName())){
                    files.put(zipEntry.getName(),new String(IOUtils.toByteArray(zipInputStream)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                zipInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return files;
    }

}