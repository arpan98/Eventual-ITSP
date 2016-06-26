from django.db import models


class EventData(models.Model):
    username = models.CharField(max_length=255, blank=True)
    title = models.CharField(max_length=255)
    description = models.CharField(max_length=255, blank=True)
    location = models.CharField(max_length=255, blank=True)
    startdate = models.DateField()
    enddate = models.DateField()
    allday = models.CharField(max_length=5)
    starttime = models.TimeField()
    endtime = models.TimeField()
    private = models.CharField(max_length=5, default="false")
