---
title: Konfigurasjon av einnsyn-klient
description: Konfigurasjon av einnsyn-klient
summary: "Konfigurasjon av einnsyn-klient ++"
permalink: install_.html

layout: page
sidebar: main_sidebar
---

## Konfigurasjon av einnsyn-klient.xml filen

Det første som må gjøres er å legge inn korrekte verdier i einnsyn-klient.xml-filen. I utgangspunktet ser denne slik ut:

```
<service>
	<id>einnsyn-klient</id>
	<name>einnsyn-klient</name>
	<description>Klient for parsing og sending av journaldata</description>
	<env name="USE_IP" value="true"/>
	<env name="RECEIVER_ORGNUMMER" value="991825827" />
	<argument>-jar</argument>
	<argument>-Dapplication.moveUrl=</argument>
	<argument>-Dapplication.inputDirectory=</argument>
	<argument>-Dapplication.orgnummer=</argument>
	<argument>-Dspring.mail.host=</argument>
	<argument>-Dspring.mail.port</argument>
	<argument>-Dspring.mail.username=</argument>
	<argument>-Dspring.mail.password=</argument>
	<argument>sender-2017-11-29T10_48.jar</argument>
	<logpath>%BASE%/logs</logpath>
	<executable>javaw</executable>
</service>
```

### Disse fyller du inn

Det som må gjøres her er å fylle inn følgende(ikke fjerne <argument> og </argument>. Du skal fylle inn innimellom de) :
* "-Dapplication.moveUrl=http://localhost:9093"
  * Denne linker einnsyn-klienten til integrasjonspunktet. Her må du ha med navnet på integrasjonspunktet og porten det kjører på. ```http://localhost:9093``` er standardnavnet.
* "-Dapplication.inputDirectory=sti til katalog"
  * Dette er mappen som einnsyn-klienten vil lese filer fra. Filer som skal lastes opp til eInnsyn. Etter at en fil er lest herifra vil den forsvinne frå inputDirectory-mappa. Feks: ```-Dapplication.inputDirectory=C:\einnsyn\opplasting``` 
* -Dapplication.orgnummer=
  * Her skal du fylle inn organisasjonsnummeret til din organisasjon. Feks: ```-Dapplication.orgnummer=123456789```
* -Dspring.mail.host=
  * Her må du fylle inn navnet til din e-postserver. Denne skal sende ut e-posten med innsynskravet. Deres integrasjonspunkt vil motta innsynskravet hos seg for og så deretter fortelle einnsyn-klienten at den må sende e-posten.
  * standardport for -Dspring.mail.port er 25. Så denne trenger du ikke fylle inn om du bruker port 25.
  * br







Legg til lenke til integrasjonspunktet som verdi for moveUrl. Her må du og ha protokoll med

<argument>-Dapplication.moveUrl=http://localhost:9093</argument>
 

Katalogen klienten vil lese filer frå

<argument>-Dapplication.inputDirectory=Sti til katalog</argument>
 

Organisasjonsnummeret

<argument>-Dapplication.orgnummer=Her legg du inn organisasjonsnummeret ditt</argument>
 

Logkatalog

<logpath>%BASE%/Loggkatalog</logpath>

Stien her er relativt til installasjonsmappa. %BASE% variabelen peikar på installasjonsmappa.

 

Epostinstillingar
<argument>-Dspring.mail.host=</argument>

<argument>-Dspring.mail.port</argument>

<argument>-Dspring.mail.username=</argument>

<argument>-Dspring.mail.password=</argument>
