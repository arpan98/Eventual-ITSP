from django.contrib.sitemaps import Sitemap
from django.core.urlresolvers import reverse

from website.models import EventData

class StaticSitemap(Sitemap):
    """Reverse 'static' views for XML sitemap."""
    changefreq = "daily"
    priority = 0.5

    def items(self):
        # Return list of url names for views to include in sitemap
        return ['website:landing', 'website:about', 'website:create', 'website:search']

    def location(self, item):
        return reverse(item)

class DynamicSitemap(Sitemap):
    changefreq = "daily"
    priority = 0.5

    def items(self):
        return EventData.objects.all()
