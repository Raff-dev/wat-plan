from django.db import models


class Group(models.Model):
    name = models.CharField(unique=True, max_length=30)

    def __str__(self):
        return self.name


class Semester(models.Model):
    group = models.ForeignKey(
        Group, on_delete=models.CASCADE, related_name='semesters')
    name = models.CharField(max_length=30)
    empty = models.BooleanField(default=False)
    version = models.IntegerField(default=0)


class Block(models.Model):
    semester = models.ForeignKey(
        Semester, on_delete=models.CASCADE, related_name='blocks')
    date = models.DateField(max_length=10)
    index = models.IntegerField()
    empty = models.BooleanField(default=False)

    title = models.CharField(
        default=None, max_length=100, null=True, blank=True)
    subject = models.CharField(
        default=None, max_length=30, null=True, blank=True)
    teacher = models.CharField(
        default=None, max_length=30, null=True, blank=True)
    place = models.CharField(
        default=None, max_length=30, null=True, blank=True)
    class_type = models.CharField(
        default=None, max_length=30, null=True, blank=True)
    class_index = models.CharField(
        max_length=1, default=None, null=True, blank=True)

    class Meta():
        unique_together = ['semester', 'date', 'index']
        ordering = ['semester__group__name', 'semester__name', 'date', 'index']

    # pervious_values = None

    # def __init__(self, *args, **kwargs):
    #     super(Block, self).__init__(*args, **kwargs)
    #     self.pervious_values = kwargs

    # def save(self, force_insert=False, force_update=False, *args, **kwargs):
    #     super(Block, self).save(force_insert, force_update, *args, **kwargs)
    #     if self.pervious_values != kwargs:
    #         group = Group.objects.get(name=self.group)
    #         group.version += 1
    #         group.save()
    #     self.pervious_values = kwargs
