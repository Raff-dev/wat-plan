from django.db import models

class Group(models.Model):
    name = models.CharField(max_length=30)

class Semester(models.Model):
    group = models.ForeignKey(Group, on_delete=models.CASCADE)
    name = models.PositiveSmallIntegerField(choices=[(1,'Zimowy'),(2,'Letni')])

class Day(models.Model):
    semester = models.ForeignKey(Semester,on_delete=models.CASCADE)
    date = models.DateField()

class Block(models.Model):
    day = models.ForeignKey(Day, on_delete=models.CASCADE)
    subject = models.CharField(max_length=30)
    teacher = models.CharField(max_length=30)
    place = models.CharField(max_length=30)
    class_type = models.CharField(max_length=30)
    number = models.IntegerField()


