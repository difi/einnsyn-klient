package no.difi.einnsyn.ipclient;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IPReceiver.class)
public class IPReceiverTest {

    @Autowired
    IPReceiver ipReceiver;

    @MockBean
    IPClient ipClient;

    String messageMetadata;
    String innsynskravMetadata;


    @Before
    public void setUp(){
        String jsonString =
                "  {\n" +
                "    \"conversationId\": \"testtransaction1\",\n" +
                "    \"serviceIdentifier\": \"DPE_DATA\",\n" +
                "    \"senderId\": \"991825827\",\n" +
                "    \"senderName\": \"DIREKTORATET FOR FORVALTNING OG IKT\",\n" +
                "    \"receiverId\": \"991825827\",\n" +
                "    \"receiverName\": \"DIREKTORATET FOR FORVALTNING OG IKT\",\n" +
                "    \"lastUpdate\": \"2017-06-01T08:56:35.001\",\n" +
                "    \"fileRefs\": {\n" +
                "      \"0\": \"message.zip\",\n" +
                "      \"1\": \"mimetype\",\n" +
                "      \"2\": \"manifest.xml\",\n" +
                "      \"3\": \"payload.jsonld\",\n" +
                "      \"4\": \"META-INF/asicmanifest.xml\",\n" +
                "      \"5\": \"META-INF/signature-a77c5623-04da-4400-a9a5-f93d44685af3.p7s\",\n" +
                "      \"6\": \"META-INF/manifest.xml\"\n" +
                "    },\n" +
                "    \"customProperties\": {\n" +
                "      \"orgnumber\": \"222333444\",\n" +
                "      \"data_type\": \"journaldata\"\n" +
                "    }\n" +
                "  }\n";

        String testInnsynskrav = "{\n" +
                "    \"conversationId\": \"testinnsynkrav\",\n" +
                "    \"serviceIdentifier\": \"DPE_INNSYN\",\n" +
                "    \"senderId\": \"991825827\",\n" +
                "    \"senderName\": \"DIREKTORATET FOR FORVALTNING OG IKT\",\n" +
                "    \"receiverId\": \"991825827\",\n" +
                "    \"receiverName\": \"DIREKTORATET FOR FORVALTNING OG IKT\",\n" +
                "    \"lastUpdate\": \"2017-06-01T12:43:15.987\",\n" +
                "    \"fileRefs\": {\n" +
                "      \"0\": \"message.zip\",\n" +
                "      \"1\": \"mimetype\",\n" +
                "      \"2\": \"manifest.xml\",\n" +
                "      \"3\": \"order.xml\",\n" +
                "      \"4\": \"META-INF/asicmanifest.xml\",\n" +
                "      \"5\": \"META-INF/signature-cf5db533-7a41-4fa1-b0ff-1e584c20e0d6.p7s\",\n" +
                "      \"6\": \"META-INF/manifest.xml\"\n" +
                "    },\n" +
                "    \"customProperties\": {\n" +
                "      \"orgnumber\": \"222333444\",\n" +
                "      \"email\": \"test@example.com\",\n" +
                "      \"data_type\": \"innsynskrav\"\n" +
                "    }\n" +
                "  }";




        JSONParser jsonParser = new JSONParser();
        messageMetadata = jsonString;
        innsynskravMetadata = testInnsynskrav;

    }

    @Test
    public void getPayloadTest() throws IOException, URISyntaxException {
        Path path = Paths.get(ClassLoader.getSystemResource("message.zip").toURI());
        byte[] asic = Files.readAllBytes(path);
        List<String> fileNames = Arrays.asList("payload.jsonld");

        Map<String,String>  files = ipReceiver.getPayload(asic,fileNames);
        System.out.println("Files " + files);

        Assert.assertTrue(files.containsKey("payload.jsonld"));
        Assert.assertNotEquals(files.get("payload.jsonld").length(),0);

    }

    @Test
    public void testMultipleCustomProperties() throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject metadataObject = (JSONObject) jsonParser.parse(messageMetadata);
        Map<String,Object> responseMap = ipReceiver.setMetadata(metadataObject);
        Assert.assertEquals(responseMap.get(IPClientProp.ORGNUMBER),"222333444");
        Assert.assertEquals(responseMap.get(IPClientProp.DATA_TYPE),IPClientProp.JOURNALDATA);


    }

    @Test
    public void testGetDataMessage() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("message.zip").toURI());
        byte[] asic = Files.readAllBytes(path);
        List<String> fileNames = Arrays.asList("payload.jsonld");

        when(ipClient.peek("http://somehost","DPE_DATA")).thenReturn(messageMetadata);
        when(ipClient.pop("http://somehost","DPE_DATA","testtransaction1")).thenReturn(asic);
        Map<String,Object> responseMap = ipReceiver.getMessage("http://somehost","DPE_DATA",fileNames);
        Map<String,String> metadata = (Map<String, String>) responseMap.get(IPClientProp.RECEIVER_RESPONSE_METADATA);
        Map<String,String> files = (Map<String,String>) responseMap.get(IPClientProp.RECEIVER_RESPONSE_FILES);

        Assert.assertEquals(metadata.get(IPClientProp.ORGNUMBER),"222333444");
        Assert.assertEquals(metadata.get(IPClientProp.DATA_TYPE),IPClientProp.JOURNALDATA);
        Assert.assertNotEquals(files.get(fileNames.get(0).length()),0);

        System.out.println(responseMap);

    }

    @Test
    public void testGetInnsynkravMessage() throws URISyntaxException, IOException {
        Path path = Paths.get(ClassLoader.getSystemResource("message_innsynkrav.zip").toURI());
        byte[] asic = Files.readAllBytes(path);
        List<String> fileNames = Arrays.asList("order.xml");

        Map<String,Object> emptyMap = ipReceiver.getMessage("http://somehost","DPE_DATA",fileNames);
        Assert.assertEquals(emptyMap.size(),0);

        when(ipClient.peek("http://somehost","DPE_INNSYN")).thenReturn(innsynskravMetadata);
        when(ipClient.pop("http://somehost","DPE_INNSYN","testinnsynkrav")).thenReturn(asic);

        Map<String,Object> responseMap = ipReceiver.getMessage("http://somehost","DPE_INNSYN",fileNames);
        Map<String,String> metadata = (Map<String, String>) responseMap.get(IPClientProp.RECEIVER_RESPONSE_METADATA);
        Map<String,String> files = (Map<String,String>) responseMap.get(IPClientProp.RECEIVER_RESPONSE_FILES);
        System.out.println("ResponseMap " + responseMap);

        Assert.assertEquals(metadata.get(IPClientProp.EMAIL),"test@example.com");
        Assert.assertEquals(metadata.get(IPClientProp.DATA_TYPE),IPClientProp.INNSYNSKRAV);
        Assert.assertNotEquals(files.get(fileNames.get(0).length()),0);

    }
}
