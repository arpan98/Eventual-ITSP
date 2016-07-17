from django.db import models


def pkgen():
    from base64 import b32encode
    from hashlib import sha1
    from random import random
    rude = ('lol', 'fuck', 'bitch', 'dog')
    bad_pk = True
    pk = ""
    while bad_pk:
        pk = b32encode(sha1(str(random())).digest()).lower()[:6]
        bad_pk = False
        for rw in rude:
            if pk.find(rw) >= 0:
                bad_pk = True
    return pk


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
    ukey = models.CharField(max_length=6, primary_key=True, default=pkgen())

    def get_absolute_url(self):
        return '/event/' + str(self.id)

