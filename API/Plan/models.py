from django.db import models


class Semester(models.Model):
    name = models.CharField(max_length=30)

    class Meta():
        ordering = ['name']

    def __str__(self):
        return self.name


class Group(models.Model):
    semester = models.ForeignKey(
        Semester, on_delete=models.CASCADE, related_name='groups')
    name = models.CharField(max_length=30)
    version = models.IntegerField(default=0)

    class Meta():
        ordering = ['semester__name', 'name']

    def __str__(self):
        return self.name


class Day(models.Model):
    group = models.ForeignKey(
        Group, on_delete=models.CASCADE, related_name='days')
    date = models.DateField(max_length=10)

    class Meta():
        unique_together = ['group', 'date']
        ordering = [
            'group__semester__name',
            'group__name',
            'date']


class Block(models.Model):
    index = models.IntegerField()
    day = models.ForeignKey(
        Day, on_delete=models.CASCADE, related_name='blocks')

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
        unique_together = ['day', 'index']
        ordering = [
            'day__group__semester__name',
            'day__group__name',
            'day__date',
            'index']

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
