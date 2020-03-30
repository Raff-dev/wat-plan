from django.contrib import admin
from django.urls import include, path

from Plan.router import router



urlpatterns = [
    path('admin/', admin.site.urls),
    path('', include((router.urls, 'Plan'),namespace='Plan')),
]

for url in router.urls:
     print(url)