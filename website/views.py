from django.http import HttpResponse

def homepage(request):
    return HttpResponse("Eventual rocks yo!")
