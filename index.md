---
title: Installasjonsguide for einnsyn-klienten
description: Dokumentasjon for einnsyn-klient
summary: "Dokumentasjon for einnsyn-klient"
permalink: index.html

layout: page
sidebar: main_sidebar
isHome: true
---

## [Nyeste versjon av einnsyn-klienten finner du her](https://github.com/difi/einnsyn-klient/releases/)
Work in progress... (14.12.17)
## Hvordan fungerer einnsyn-klienten i helhet.. working title

I de fleste tilfeller vil det være naturlig å sette opp både integrasjonspunktet og einnsyn-klient på samme server. Denne veiledningen tar utgangspunkt i det. 


*skriv om til bokmål...*
Integrasjonspunktet er kanalen til omverdenen og må ha portåpning gjennom brannmuren. Samtidig må det ligge innanfor brannmuren sidan kommunikasjonen går ukryptert til einnsyn-klienten og arkivsystemet. Integrasjonspunktet må også ha tilgang til arkivsystemet for å kunne fungere for alternativ 1, samt i dei tilfelle ein skal sende andre meldingar enn eInnsyn-meldingar.
eInnsyn-klienten kommuniserar over http med integrasjonspunktet og treng såleis tilgang til det. Sidan innsynskrav vert sendt på epost frå eInnsyn-klient til arkivsystem eller anna epostboks treng klienten også tilgang til ein mailserver.
Filkatalogen som vert grensesnittet for arkivarar må eInnsyn-klienten kunne nå for å overvåke og skrive til. Samtidig som at arkivar må ha tilgang til den for å laste opp filer og lese loggfiler. Dette føreset at filene ligg på ein server som både arkivar har tilgang til og som kan kommunisere med dei andre komponentane
Kort forklart, korleis fungerer dette? E-postadressa for innsynskrav blir spesifisert frå einnsyn admin, der kan ein legge inn den e-postadressa ein ønsker. E-postserveren som blir satt opp i einnsyn-klient.xml er for kva SMTP-server som skal motta innsynskrav-xml  igjennom integrasjonspunktet. Deretter bruker bruker klienten SMTP-serveren til å sende e-posten.
