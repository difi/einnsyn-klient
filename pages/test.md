---
title: test
description: test
summary: "test"
permalink: test.html

layout: page
sidebar: main_sidebar
---

<details>
	<summary>Brannmuråpninger i testmiljø</summary><p>

### Generelle
Sentrale tjenester(Adresseoppslag, sentral konfigurasjon mm.) 
* beta-meldingsutveksling.difi.no
	- 93.94.10.30:443 
	- 93.94.10.45:443 
	- 93.94.10.5:443

Id-portens autentiseringstjeneste 
* oidc-ver2.difi.no
	- 146.192.252.152:443
* oidc-ver1.difi.no 
	- 146.192.252.121:443

Logging 
+ 93.94.10.18:8300/5343

### einnsyn 
Meldingsformidler eInnsyn
* move-dpe.servicebus.windows.net -> *.cloudapp.net[Hva er dette? les mer her]()

### Meldingsformidler DPO og DPV
* www.altinn.no 
	- 79.171.86.33:443
	
### Meldingsformidler DPI

qaoffentlig.meldingsformidler.digipost.no
	- 146.192.168.18:443
	- 146.192.168.19:443
	
Meldingsformidler KS SvarUt/SvarInn

test.svarut.ks.no -> 193.161.160.165:443

</p>
</details>


Also attempting to include
{% include custom/reuse.html %}

>Did it work? yup!

<button data-toggle="collapse" data-target="#demo">Brannmuråpninger testmiljø: DPE</button>

<div id="demo" class="collapse">

	### Generelle
Sentrale tjenester(Adresseoppslag, sentral konfigurasjon mm.) 
* beta-meldingsutveksling.difi.no
	- 93.94.10.30:443 
	- 93.94.10.45:443 
	- 93.94.10.5:443

Id-portens autentiseringstjeneste 
* oidc-ver2.difi.no
	- 146.192.252.152:443
* oidc-ver1.difi.no 
	- 146.192.252.121:443

Logging 
+ 93.94.10.18:8300/5343

### einnsyn 
Meldingsformidler eInnsyn
* move-dpe.servicebus.windows.net -> *.cloudapp.net[Hva er dette? les mer her]()

### Meldingsformidler DPO og DPV
* www.altinn.no 
	- 79.171.86.33:443
	
### Meldingsformidler DPI

qaoffentlig.meldingsformidler.digipost.no
	- 146.192.168.18:443
	- 146.192.168.19:443
	
Meldingsformidler KS SvarUt/SvarInn

test.svarut.ks.no -> 193.161.160.165:443


</div>
