from django.shortcuts import render, redirect
from django.utils.encoding import smart_str
from django.http import HttpResponse

from .models import Apk


def home(request, *args, **kwargs):
    apks = Apk.objects.all().order_by('-release_date')
    context = {
        'apks': apks,
    }
    return render(request, 'home/home.html', context)


def download_apk(request, *args, **kwargs):
    apk = Apk.objects.all().order_by('-release_date').first().apk
    file_name = apk.name.split('/')[-1]
    file = apk.file
    response = HttpResponse(file, content_type='application/force-download')
    response['Content-Disposition'] = f'attachment; filename={smart_str(file_name)}'
    response['X-Sendfile'] = smart_str(apk.name)
    return response
