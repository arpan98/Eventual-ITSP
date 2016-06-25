from django.conf.urls import url, include
from django.views.generic import DetailView
from website import views
from website.models import EventData

urlpatterns = [
    url(r'^$', views.landing, name='landing'),
    url(r'^create/?$', views.create, name='create'),
    url(r'^event/(?P<pk>\d+)/$', DetailView.as_view(model=EventData, template_name="website/event.html"), name='event'),
    url(r'^create/web/?$',
        views.create_web, name='create_web'),
    url(r'^search/?$', views.search, name='search'),
    url(r'^search/web/?$', views.search_web, name='search_web'),
]
