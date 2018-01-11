---
title: test
description: test
summary: "test"
permalink: test.html

layout: page
sidebar: main_sidebar
---

## Brannmuråpninger testmiljø

<button data-toggle="collapse" data-target="#demo">Brannmuråpninger testmiljø: DPE</button>
<div id="demo" class="collapse">
  {% include custom/reuse.html %} 
  {% include custom/test_dpe.html %}
</div>

<button data-toggle="collapse" data-target="#demo2">Brannmuråpninger testmiljø: DPO og DPV</button>
<div id="demo2" class="collapse">
  {% include custom/reuse.html %} 
  {% include custom/test_dpo.html %}
</div>

## Brannmuråpninger produksjon

<button data-toggle="collapse" data-target="#demo4">Brannmuråpninger produksjonsmiljø: DPE</button>
<div id="demo4" class="collapse">
  {% include custom/reuse.html %}
</div>

<button data-toggle="collapse" data-target="#demo5">Brannmuråpninger produksjonsmiljø: DPO og DPV</button>
<div id="demo5" class="collapse">
  {% include custom/prod_generell.html %}
</div>

