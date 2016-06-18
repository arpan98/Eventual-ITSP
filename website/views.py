from django.http import HttpResponse
from django.shortcuts import render
from website.models import EventData
from django.views.decorators.csrf import csrf_exempt
import json
import datetime

def homepage(request):
    return render(request, 'homepage.html', {})

@csrf_exempt
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
