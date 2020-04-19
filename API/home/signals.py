from datetime import date
from django.dispatch import receiver
from django.db.models.signals import pre_save, post_save
from .models import Apk


@receiver(pre_save, sender=Apk)
def insert_date(sender, instance, **kwargs):
    print(f'saving {date.today()}')
    if instance.release_date == None:
        instance.release_date = date.today()
