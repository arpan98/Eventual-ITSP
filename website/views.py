from django.http import HttpResponse
from django.shortcuts import render
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
        return HttpResponse(event.startdate)
    return HttpResponse("Na ho paega")

def search(request):
    if request.method == 'POST':
        json_dict = json.loads(request.body)
        params = {}
        for key, value in json_dict.iteritems():
            params[key] = urllib.unquote(value).replace('+', ' ')
        event = EventData.objects.filter(
                    # username=params["username"],
                    title=params["title"],
                    description=params["description"],
                    location=params["location"],
                    startdate=datetime.datetime.strptime(params["startdate"], '%d/%m/%Y'),
                    enddate=datetime.datetime.strptime(params["enddate"], '%d/%m/%Y'),
                    allday=params["allday"],
                    starttime=datetime.time(int(params["starttime"].split(':')[0]),
                                            int(params["starttime"].split(':')[1])
                                           ),
                    endtime=datetime.time(int(params["endtime"].split(':')[0]),
                                          int(params["endtime"].split(':')[1])
                                         ),
                    )
        return HttpResponse(len(event))
    return HttpResponse("Nothing here")