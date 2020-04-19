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
    apks = Apk.objects.all().order_by('-release_date')
    file_dir = apks.first().apk.name
    file_name = file_dir.split('/')[-1]
    response = HttpResponse(content_type='application/force-download')
    response['Content-Disposition'] = f'attachment; filename={smart_str(file_name)}'
    response['X-Sendfile'] = smart_str(file_dir)
    return response
