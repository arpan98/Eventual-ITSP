from django.conf.urls import url, include

from website import views

urlpatterns = [
    url(r'^$', views.landing, name='landing'),
    url(r'^create/?$', views.create, name='create'),
    url(r'^validate/web/?$',
        views.validate_web, name='web'),
    url(r'^search/?', views.search, name='search'),
]
