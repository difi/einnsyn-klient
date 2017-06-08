package no.difi.einnsyn.ipclient;

import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by oho on 23.05.2017.
 */
@Service
public class IPClient {

    private RestTemplate restTemplate;

    private final String PEEK_URL="/in/messages/peek?serviceIdentifier=";
    private final String POP_URL="/in/messages/pop";

    Logger errorLogger = LoggerFactory.getLogger(IPClient.class);
    Logger transactionLogger  = LoggerFactory.getLogger("directorylistener_transactionlogger");

    public IPClient(RestTemplateBuilder restTemplateBuilder){
        restTemplate =  restTemplateBuilder.build();
    }

    public String createResource(String url, JsonObject resource){
        String conversationId = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> metadataEntity = new HttpEntity<String>(resource.toString(),headers);

            transactionLogger.info("Create message " +  url + metadataEntity.toString());

            restTemplate.postForObject(url + "out/messages", metadataEntity, String.class);


        }catch (RestClientException rce){
            errorLogger.error("Failed createMessage " + rce.getMessage());
        }
        return resource.get(IPClientProp.CONVERSATION_ID).getAsString();
    }

    public void uploadFiles(HttpEntity<MultiValueMap<String, Object>> files, String baseUrl,String correlationId,boolean devMode){
        try {

            transactionLogger.info("Upload message " +  baseUrl + "/out/messages/" + correlationId);

            restTemplate.exchange(baseUrl + "/out/messages/" + correlationId, HttpMethod.POST, files, String.class);

        } catch (RestClientException rce){
            if(devMode){
                transactionLogger.debug("Transfer locally");
                restTemplate.getForEntity(baseUrl+"/transferqueue/" + correlationId,String.class);
            }
            errorLogger.error("Failed uploadFiles " + rce.getMessage());
        }
    }

    public String peek(String baseUrl, String serviceIdentifier){
        transactionLogger.debug("Peek url " + baseUrl+PEEK_URL+serviceIdentifier);

//        String response = restTemplate.getForObject(baseUrl+PEEK_URL+serviceIdentifier, String.class);
        return restTemplate.getForObject(baseUrl+PEEK_URL+serviceIdentifier, String.class);

    }

    public byte[] pop(String baseUrl, String serviceIdentifier, String conversationId){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl+POP_URL)
                .queryParam("serviceIdentifier",serviceIdentifier)
                .queryParam("conversationId",conversationId);



        ResponseEntity<byte[]> response =   restTemplate.getForEntity(builder.build().encode().toUri(),byte[].class);

        return response.getBody();
    }


}
