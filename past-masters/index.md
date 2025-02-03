---
title: Past Masters
layout: page
---

The honored title of Past Master is given to those Brothers who have served their Lodge as Worshipful Master. The following Worshipful Brothers have served Esoterika Lodge №227 in this capacity, and are here listed in recognition of their service.

{% for item in site.bios %}
  <h2>{{ item.title }} ({{ item.years }})</h2>
  {{ item.content }}
{% endfor %}
