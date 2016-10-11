from django.conf.urls import url, include
from django.views.generic import DetailView
from django.contrib import admin
from website import views
from website.models import EventData
from django.contrib.sitemaps.views import sitemap
from website.sitemaps import StaticSitemap, DynamicSitemap
from website.views import landing

sitemaps = {
    'static': StaticSitemap(),
    'dynamic': DynamicSitemap()
}

urlpatterns = [
    url(r'^admin/', include(admin.site.urls)),
    url(r'^$', landing, name='landing'),
    url(r'^create/?$', views.create, name='create'),
    url(r'^event/(?P<pk>\S+)/$',
        DetailView.as_view(model=EventData,
                           template_name="website/event.html"),
        name='event'),
    url(r'^create/web/?$',
        views.create_web,
        name='create_web'),
    url(r'^search/?$', views.search, name='search'),
    url(r'^search/web/?$',
        views.search_web,
        name='search_web'),
    url(r'about/?$', views.about, name='about'),
    url(r'^sitemap.xml/?$', sitemap, {'sitemaps': sitemaps}),
    url(r'^robots.txt/?', include('robots.urls')),
]
