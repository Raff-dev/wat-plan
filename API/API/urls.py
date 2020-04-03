from django.contrib import admin
from django.urls import include, path

from rest_framework import routers
from Plan.views import Plan

router = routers.DefaultRouter()
router.register('Plan', Plan, basename='Plan')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', include((router.urls, 'Plan'), namespace='Plan')),
]

for url in router.urls:
    print(url)
