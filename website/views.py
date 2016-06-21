from django.http import HttpResponse
from django.shortcuts import render, redirect
from django.core import serializers
from website.models import EventData
import json
import datetime
import urllib
from copy import deepcopy

def homepage(request):
    return render(request, 'landing.html', {})

def event_get_or_create(params):
    event, created = EventData.objects.get_or_create(
        username=params['username'],
        title=params['title'],
        description=params['description'],
        location=params['location'],
        startdate=datetime.datetime.strptime(params['startdate'], '%d/%m/%Y'),
        enddate=datetime.datetime.strptime(params['enddate'], '%d/%m/%Y'),
        allday=bool(params['allday']),
        starttime=datetime.time(int(params['starttime'].split(':')[0]),
                                int(params['starttime'].split(':')[1])
                                ),
        endtime=datetime.time(int(params['endtime'].split(':')[0]),
                              int(params['endtime'].split(':')[1])
                              ),
        )
    if created:
        return HttpResponse(event.id)
    else:
        return HttpResponse("Duplicate")

def create(request):
    if request.method == 'POST':
        params = json.loads(request.body)
        response = event_get_or_create(params)
        return response
    elif request.method == 'GET':
        return render(request, 'create.html', {})

# Validation method for web form 
def create_web(request):
    if request.method == 'POST':
        params = deepcopy(request.POST)
        params['username'] = "web"
        params.method = "POST"
        if request.POST.get('allday', 'off') == 'off':
            params['allday'] = "False"
            params['startdate'] = request.POST['start'].split()[0].replace('-', '/')
            params['enddate'] = request.POST['end'].split()[0].replace('-', '/')
            params['starttime'] = request.POST['start'].split()[1]
            params['endtime'] = request.POST['end'].split()[1]
        elif request.POST.get('allday', 'on') == 'on':
            params['allday'] = "True"
            params['startdate'] = request.POST['start'].split()[0].replace('-', '/')
            params['enddate'] = request.POST['end'].split()[0].replace('-', '/')
            params['starttime'] = '00:00'
            params['endtime'] = '00:00'
        response = event_get_or_create(params)
        return response
    else:
    #     return redirect('/create')
        return HttpResponse("Hello")

def search(request):
    if request.method == 'POST':
        json_dict = json.loads(request.body)
        kwargs = {}
        for key, value in json_dict.iteritems():
            kwargs[key] = urllib.unquote(value).replace('+', ' ')
            if 'date' in key:
                kwargs[key] = datetime.datetime.strptime(value, '%d/%m/%Y')
            if 'time' in key:
                kwargs[key] = datetime.time(int(value.split(':')[0]),
                                      int(value.split(':')[1])
                                     )
        event = EventData.objects.filter(**kwargs)
        return HttpResponse(serializers.serialize("json", event))
    return HttpResponse("Nothing here")