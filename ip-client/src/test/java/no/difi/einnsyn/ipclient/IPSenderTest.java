package no.difi.einnsyn.ipclient;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = IPSender.class)
public class IPSenderTest {

    @MockBean
    IPClient ipClient;

    @MockBean
    HttpEntity<MultiValueMap<String,Object>> entity;


    HashMap<String,String> payloadMap;
    String rawContent;

    @Autowired
    IPSender ipSender;



    MockRestServiceServer server;
    String senderId;
    String receiverId;
    String baseUrl;
    private String transaksjonsId;

    @Before
    public void setUp(){
        receiverId = "111222333";
        senderId = "444555666";
        baseUrl = "http://localhost:9093";
        transaksjonsId = "transaksjonsId";

        rawContent = null;

        try {

            rawContent = new String(Files.readAllBytes(Paths.get("src/test/resources/expected.json")),"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        payloadMap = new HashMap<>();
        payloadMap.put(IPClientProp.PAYLOAD,rawContent);



    }

    @Test
    public void packageHttpEntityWithMetadataTest(){

        String rawContent = null;
        try {

            rawContent = new String(Files.readAllBytes(Paths.get("src/test/resources/expected.json")),"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("payload.jsonld",rawContent);


        HttpEntity<MultiValueMap<String, Object>> entity = ipSender.packHttpEntity(payload);

        Assert.assertEquals(entity.getHeaders().getContentType(), MediaType.MULTIPART_FORM_DATA);
        Assert.assertTrue(entity.getBody().containsKey("payload.jsonld"));
    }

    @Test
    public void testPackageHttpEntityMultipleFiles(){
        String address = null;
        String order = null;
        try {
            address = new String(Files.readAllBytes(Paths.get("src/test/resources/message/address")),"UTF-8");
            order = new String(Files.readAllBytes(Paths.get("src/test/resources/message/order.xml")),"UTF-8");

        } catch (IOException io){
            io.printStackTrace();
        }

        Map<String,String> payload = new HashMap<>();
        payload.put("address",address);
        payload.put("order.xml",order);

        HttpEntity<MultiValueMap<String,Object>> entity = ipSender.packHttpEntity(payload);

        System.out.println(entity.getBody().keySet());
        ByteArrayResource addressResource = (ByteArrayResource)entity.getBody().get("address").get(0);
        System.out.println(new String(addressResource.getByteArray()));
        Assert.assertEquals(2,entity.getBody().keySet().size());
        Assert.assertEquals("test@example.no",new String(addressResource.getByteArray()));

    }

    @Test
    public void testSenderSouldBeInCustomProperty(){
        Map<String,String> customPropeties = new HashMap<>();
        customPropeties.put(IPClientProp.ORGNUMBER,senderId);
        JsonObject message = ipSender.createMessageMetadata("test", "DPE_INNSYN",customPropeties,receiverId);
        System.out.println(message.toString());
        Assert.assertEquals(message.get("customProperties").getAsJsonObject().get("orgnumber").getAsString(),"444555666");
    }

    @Test
    public void testSetTransactionId(){
        String rawContent = null;
        try {

            rawContent = new String(Files.readAllBytes(Paths.get("src/test/resources/expected.json")),"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("payload.jsonld",rawContent);

        Map<String,String> customPropeties = new HashMap<>();
        customPropeties.put(IPClientProp.ORGNUMBER,senderId);

        JsonObject message = ipSender.createMessageMetadata("test", "DPE_DATA",customPropeties,receiverId);

        System.out.println("Message " + message);




        when(ipClient.createResource(anyString(),any(JsonObject.class))).thenReturn(message.get(IPClientProp.CONVERSATION_ID).toString());

        String conversationID = ipSender.send(payload,"test",baseUrl,"DPE_DATA",customPropeties,receiverId,true);

        Assert.assertEquals(message.get(IPClientProp.CONVERSATION_ID).toString(),conversationID.toString());

        System.out.println("conversationID " +  conversationID);
    }

    @Test
    public void sendJournalData(){


        Map<String,String> customProperties = new HashMap<>();
        customProperties.put("orgnumber",senderId);
        customProperties.put(IPClientProp.DATA_TYPE,IPClientProp.JOURNALDATA);


        IPSender ipSenderSpy = spy(ipSender);

        doReturn(transaksjonsId).when(ipSenderSpy).initTransactionId(transaksjonsId);
        JsonObject messageMetadata = ipSenderSpy.createMessageMetadata(transaksjonsId,IPClientProp.DPE_DATA,customProperties,receiverId);
        doReturn(messageMetadata).when(ipSenderSpy).createMessageMetadata(transaksjonsId,IPClientProp.DPE_DATA,customProperties,receiverId);
        doReturn(entity).when(ipSenderSpy).packHttpEntity(payloadMap);
        when(ipClient.createResource(baseUrl,messageMetadata)).thenReturn(transaksjonsId);

        String conversationId = ipSenderSpy.sendJournaldata(rawContent, transaksjonsId,baseUrl,receiverId,senderId,false);

        verify(ipClient).createResource(baseUrl,messageMetadata);
        verify(ipClient).uploadFiles(entity,baseUrl,transaksjonsId,false);


        System.out.println(conversationId);
    }

}
