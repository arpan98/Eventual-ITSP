from django.http import HttpResponse
from django.shortcuts import render, redirect
from website.models import EventData
import json
import datetime
import urllib
from copy import deepcopy


def landing(request):
    return render(request, 'website/landing.html', {})


def about(request):
    return render(request, 'website/about.html', {})


def create(request):
    if request.method == 'POST':
        params = json.loads(request.body)
        ukey = event_get_or_create(params)
        return HttpResponse(str(ukey))
    elif request.method == 'GET':
        return render(request, 'website/create.html', {})


# Validation method for web form
def create_web(request):
    if request.method == 'POST':
        params = deepcopy(request.POST)
        params['username'] = "web"
        if request.POST.get('allday', 'off') == 'off':
            params['allday'] = "false"
            params['startdate'] = request.POST['start'].split()[0].replace('-',
                                                                           '/')
            params['enddate'] = request.POST['end'].split()[0].replace('-',
                                                                       '/')
            params['starttime'] = request.POST['start'].split()[1]
            params['endtime'] = request.POST['end'].split()[1]
        elif request.POST.get('allday', 'off') == 'on':
            params['allday'] = "true"
            params['startdate'] = request.POST['start'].split()[0].replace('-',
                                                                           '/')
            params['enddate'] = request.POST['end'].split()[0].replace('-',
                                                                       '/')
            params['starttime'] = '00:00'
            params['endtime'] = '00:00'
        if request.POST.get('private', 'off') == 'off':
            params['private'] = "false"
        elif request.POST.get('private', 'on') == 'on':
            params['private'] = "true"
        ukey = event_get_or_create(params)
        return HttpResponse('/event/' + str(ukey))
    elif request.method == 'GET':
        return redirect('/create')


def searchHandler(kwargs):
    events = EventData.objects.filter(**kwargs)
    cleaned_events = format_query_data(events)
    return cleaned_events


def search(request):
    if request.method == 'POST':
        json_dict = json.loads(request.body)
        kwargs = {}
        for key, value in json_dict.iteritems():
            if key == "private" or key == "allday":
                kwargs[key] = value
            else:
                kwargs[key] = urllib.unquote(value).replace('+', ' ')
            if 'date' in key:
                kwargs[key] = datetime.datetime.strptime(value, '%d/%m/%Y')
            if 'time' in key:
                kwargs[key] = datetime.time(
                    int(value.split(':')[0]), int(value.split(':')[1]))
        cleaned_events = searchHandler(kwargs)
        return HttpResponse(json.dumps(cleaned_events))
    elif request.method == 'GET':
        return render(request, 'website/search.html', {})


def search_web(request):
    if request.method == 'POST':
        params = deepcopy(request.POST)
        keys_to_be_removed = []
        for key in params:
            if params[key] == "":
                keys_to_be_removed.append(key)
        for key in keys_to_be_removed:
            params.pop(key, None)
        kwargs = deepcopy(params)
        kwargs.pop('start', None)
        kwargs.pop('end', None)
        if (('allday' in params and params['allday'] == 'off') or
            ('allday' not in params)):
            kwargs["allday"] = "false"
            if 'start' in params:
                kwargs['startdate'] = params['start'].split()[0]
                kwargs['starttime'] = params['start'].split()[1]
            if 'end' in params:
                kwargs['enddate'] = params['end'].split()[0]
                kwargs['endtime'] = params['end'].split()[1]
        elif 'allday' in params and params['allday'] == 'on':
            kwargs["allday"] = "true"
            if 'start' in params:
                kwargs['startdate'] = params['start'].split()[0]
            if 'end' in params:
                kwargs['enddate'] = params['end'].split()[0]
        for key, value in kwargs.iteritems():
            if 'date' in key:
                kwargs[key] = datetime.datetime.strptime(value, '%d-%m-%Y')
            if 'time' in key:
                kwargs[key] = datetime.time(
                    int(value.split(':')[0]), int(value.split(':')[1]))
        # return HttpResponse(json.dumps(kwargs))
        # if not check_if_any_field_present(kwargs):
        #     return HttpResponse("No fields entered.")

        cleaned_events = searchHandler(dict(kwargs.iteritems()))
        return HttpResponse(json.dumps(cleaned_events))


def event_get_or_create(params):
    event, created = EventData.objects.get_or_create(
        username=params['username'],
        title=params['title'],
        description=params['description'],
        location=params['location'],
        startdate=datetime.datetime.strptime(params['startdate'], '%d/%m/%Y'),
        enddate=datetime.datetime.strptime(params['enddate'], '%d/%m/%Y'),
        allday=params['allday'],
        starttime=datetime.time(
            int(params['starttime'].split(':')[0]),
            int(params['starttime'].split(':')[1])),
        endtime=datetime.time(
            int(params['endtime'].split(':')[0]),
            int(params['endtime'].split(':')[1])),
        private=params['private'])
    return event.ukey


def format_query_data(events):
    cleaned_events = []
    for event in events:
        cleaned_event = {}
        cleaned_event["ukey"] = event.ukey
        cleaned_event["username"] = event.username.encode('ascii', 'ignore')
        cleaned_event["title"] = event.title.encode('ascii', 'ignore')
        cleaned_event["description"] = event.description.encode('ascii',
                                                                'ignore')
        cleaned_event["location"] = event.location.encode('ascii', 'ignore')

        cleaned_event["startdate"] = event.startdate.strftime('%d/%m/%Y')
        cleaned_event["enddate"] = event.enddate.strftime('%d/%m/%Y')
        cleaned_event["allday"] = event.allday.encode('ascii', 'ignore')
        cleaned_event["starttime"] = event.starttime.strftime('%H:%M')
        cleaned_event["endtime"] = event.endtime.strftime('%H:%M')
        cleaned_event["isPrivate"] = event.private.encode('ascii', 'ignore')
        cleaned_event["ukey"] = event.ukey.encode('ascii', 'ignore')
        cleaned_events.append(cleaned_event)
    return cleaned_events


def check_if_any_field_present(params):
    if 'allday' in params and params['allday'] == False:
        params.pop('allday', None)
    if all(params[key] == "" for key in params):
        return False
    else:
        return True
