from django.urls import path
from .views import home, download_apk
app_name = 'home'

urlpatterns = [
    path('home/', home, name='home'),
    path('download/', download_apk, name='download'),
]
