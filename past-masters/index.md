---
title: Past Masters
layout: page
---

The honored title of Past Master is given to those Brothers who have served their Lodge as Worshipful Master. The following Worshipful Brothers have served Esoterika Lodge №227 in this capacity, and are here listed in recognition of their service.

<div class="pm-nav">
{% for item in site.bios %}
  <a href="#{{ item.years }}">{{ item.years }}</a>{% unless forloop.last %}&nbsp;| {% endunless %}
{% endfor %}
</div>

{% for item in site.bios %}
  <div class="pm-block">
    <h2 id="{{ item.years }}">{{ item.title }} ({{ item.years }})</h2>
    {% if item.image %}<img src="{{ item.image }}" alt="{{ item.title }}" class="pm-headshot">{% endif %}
    {{ item.content }}
  </div>
{% endfor %}
