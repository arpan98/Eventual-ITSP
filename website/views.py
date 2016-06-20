from django.http import HttpResponse
from django.shortcuts import render
from django.core import serializers
from website.models import EventData
import json
import datetime
import urllib

def homepage(request):
    return render(request, 'homepage.html', {})

def create(request):
    if request.method == 'POST':
        json_dict = json.loads(request.body)
        event = EventData(
            username=json_dict['username'],
            title=json_dict['title'],
            description=json_dict['description'],
            location=json_dict['location'],
            startdate=datetime.datetime.strptime(json_dict['startdate'], '%d/%m/%Y'),
            enddate=datetime.datetime.strptime(json_dict['enddate'], '%d/%m/%Y'),
            allday=json_dict['allday'],
            starttime=datetime.time(int(json_dict['starttime'].split(':')[0]),
                                    int(json_dict['starttime'].split(':')[1])
                                    ),
            endtime=datetime.time(int(json_dict['endtime'].split(':')[0]),
                                  int(json_dict['endtime'].split(':')[1])
                                  ),
            )
        event.save()
        return HttpResponse(event.id)
    elif request.method == 'GET':
        return render(request, 'create.html', {})

def search(request):
    if request.method == 'POST':
        json_dict = json.loads(request.body)
        kwargs = {}
        for key, value in json_dict.iteritems():
            kwargs[key] = urllib.unquote(value).replace('+', ' ')
        event = EventData.objects.filter(**kwargs)
        return HttpResponse(serializers.serialize("json", event))
    return HttpResponse("Nothing here")