from django.contrib import admin
from website.models import EventData

class EventDataAdmin(admin.ModelAdmin):
	list_display = (
					'username',
					'title',
					'description',
					'location',
					'allday',
					'startdate',
					'starttime',
					'enddate',
					'endtime',
					'private'
					)

admin.site.register(EventData, EventDataAdmin)