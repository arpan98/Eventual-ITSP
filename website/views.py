from django.http import HttpResponse
from django.shortcuts import render

def homepage(request):
    return render(request, 'homepage.html', {})

def create(request):
	if request.method == 'POST':
		return HttpResponse("Event saved")
	return HttpResponse("Na ho paega")
