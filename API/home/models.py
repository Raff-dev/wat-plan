from django.db import models


class Apk(models.Model):
    version = models.CharField(unique=True, max_length=100)
    release_date = models.DateField(blank=True, null=True)
    apk = models.FileField(upload_to='apk')

    def __str__(self):
        return f'WatPlan {self.version}'

    class Meta():
        ordering = ['release_date']


class Data(models.Model):
    name = models.CharField(max_length=100)
    value = models.CharField(max_length=1000)


class ReleaseNote(models.Model):
    apk = models.ForeignKey(Apk, on_delete=models.CASCADE,
                            related_name='release_notes')
    content = models.CharField(max_length=300)

    def __str__(self):
        return f'Release note {self.apk.version}'
