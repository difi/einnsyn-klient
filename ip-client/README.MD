# Forklaring på bruk at integrasjonspunktet
Metodekalla som blir utført er i henhold til api https://difi.github.io/move-api
# Metodekall for sending

## Opprett melding /out/messages
 Post med jsonformatert metadata
 ``` json
 "{
   //Standard/obligatoriske attributt
   "conversationId":"", //transaksjonsid. Valgfritt parameter
   "serviceIdentifier":"DPE_DATA", //Skal vere DPE_DATA for eInnsynsmeldingar som skal sendast til eInnsyn sentralt. Dvs journaldata og møtedata
   "receiverId":"991825827", //Id/adresse til mottakar av meldinga. Dette skal vere organisasjonsnummeret til Difi i produksjon
   //Ekstra attributt/forretningsmetadata
   "customProperties": {
     "orgnumber":"",//organisasjonsnummeret til innhaldsleverandør
     "data_type":"journaldata", // Indikerar kva type data det er. Journaldata vil vere gjeldande for journal-/møteleveransar. Andre type meldingar fil blir definert for t.d slettemeldingar
   }
 }" ```

## Last opp data /out/messages/{conversation-id}
Set http header
Content-Type: MediaType.MULTIPART_FORM_DATA
Lag til fil med navn "payload.jsonld"
POST fil til /out/messages/{conversation-id}


# Metodekall for uthenting

## Peek på kø /in/messages/peek
Requestparameter serviceIdentifier=DPE_INNSYN
DPE_INNSYN er identifikator for alle meldinar som vert sent frå eInnsyn sentralt ut til innhaldsleverandørar som innsynsbegjæringar.

Metadata ved innsynsbegjæring vil vere


``` jsonformatert
{
  "conversationId":"",
  "serviceIdentifier":"DPE_INNSYN",
  "senderId":"991825827", //Avsendar. Vil vere generert ut i frå virksomhetssertifikat
  "senderName":"DIREKTORATET FOR FORVALTNING OG IKT",
  "receiverId":"",//Verdiar henta ut frå mottakars verksemdssertifikat
  "receiverName":"",//Verdiar henta ut frå mottakars verksemdssertifikat
  "fileRefs":{ //Filer som ligg i meldinga. Vil vere metadatafiler for transport, samt sjølve datafila som vart lasta opp av anvsendar
    "0":"message.zip",
    "1":"mimtype",
    "2":"manifest.xml",
    "3":"order.xml", // Dette er sjølve innsynsbegjærniga ihh til dagens format
    "4":"META-INF/ascimanifest.xml",
    "4":"signature-",
    "6":"META-INF/manifest.xml",
  },
  "customProperties":{
    "orgnumber":"991825827",
    "email":"test@example.com", //Epostadresse som er registrert i einnsyn som innsynskravmottak for innhaldsleverandør
    "data_type":"innsynskrav",
  }
}
```
