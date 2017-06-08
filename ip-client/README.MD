* Metodekall for sending

** Opprett melding /out/messages
 Post med jsonformatert metadata
 ````{
 //Standard/obligatoriske attributt
 conversationId: //transaksjonsid. Valgfritt parameter
 serviceIdentifier:"DPE_DATA" //Skal vere DPE_DATA for eInnsynsmeldingar som skal sendast til eInnsyn sentralt. Dvs journaldata og møtedata
 receiverId:991825827  //Id/adresse til mottakar av meldinga. Dette skal vere organisasjonsnummeret til Difi i produksjon
 //Ekstra attributt/forretningsmetadata
 customProperties{
 orgnummer: //organisasjonsnummeret til innhaldsleverandør
 data_type: "journaldata".  Indikerar kva type data det er. Journaldata vil vere gjeldande for journal/møte leveransar. Andre type meldingar fil blir definert for t.d slettemeldingar
 }
 
 }```

* Metodekall for uthenting
