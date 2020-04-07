from rest_framework.serializers import ModelSerializer
from rest_framework.decorators import action
from rest_framework.authentication import TokenAuthentication
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework.decorators import permission_classes, authentication_classes
from rest_framework.viewsets import ViewSet
from rest_framework import status
from django.core.exceptions import ObjectDoesNotExist, EmptyResultSet
from django.core import serializers
from django.db.models import F
from django.db.models import Count
import json
from .models import Block, Day, Group, Semester
import datetime


class Plan(ViewSet):

    @action(methods=['post'], detail=False)
    def update_plan(self, request, *args, **kwargs):
        print('GOT REQUEST')
        group = request.data['group']
        semester = request.data['semester']
        plan = request.data['plan']
        outdated = False
        plan_exists = plan is not None

        semester, screated = Semester.objects.get_or_create(
            name=semester)
        group, gcreated = Group.objects.get_or_create(
            name=group, semester=semester)

        if Day.objects.filter(group=group).exists() != plan_exists:
            outdated = not gcreated
            if plan is None and not gcreated:
                Day.objects.filter(group=group).delete()

        if plan_exists:
            for date, plan_day in plan.items():
                date = [int(d) for d in date.split('-')]
                date = datetime.date(*date)
                day, dcreated = Day.objects.get_or_create(
                    group=group, date=date)

                for index, data in plan_day.items():
                    if data is None:
                        outdated = bool(Block.objects.filter(
                            day=day, index=index
                        ).delete()[0]) or outdated
                    else:
                        block, bcreated = Block.objects.get_or_create(
                            day=day, index=index)
                        check = data.copy()
                        check.pop('place')
                        if not check.items() <= block.__dict__.items():
                            outdated = not bcreated
                            Block.objects.filter(id=block.id).update(**data)

        print(F'{group.name} : {semester.name} Updated')
        group.version += 1 if outdated else 0
        return Response({"That's": 'Tosted'}, status=status.HTTP_201_CREATED)

    @action(methods=['get'], detail=False)
    def get_group(self, request, *args, **kwargs):
        try:
            semester = request.headers['semester']
            semester = Semester.objects.get(name=semester)
            group = request.headers['group']
            group = Group.objects.get(name=group, semester=semester)

            days = Day.objects.filter(group=group)
            result = {
                'version': group.version,
                'first_day': group.days.first().date,
                'last_day': group.days.last().date,
                'data': [{
                    'date': str(d.date),
                    'blocks': d.blocks.values()}
                    for d in days if d.blocks.exists()]
            }
        except (ObjectDoesNotExist, EmptyResultSet):
            return Response(status=status.HTTP_204_NO_CONTENT)
        except KeyError as e:
            result = f'{e} not specified'
            return Response(result, status=status.HTTP_204_NO_CONTENT)
        return Response(result)

    @action(methods=['get'], detail=False)
    def get_versions(self, request, *args, **kwargs):
        groups = Group.objects.all().values().order_by('name')
        semesters = Semester.objects.all().values()
        result = {
            'versions': [{
                'semester': s['name'],
                'groups': [{'group': g['name'], 'version': g['version']} for g in groups]
            } for s in semesters]
        }
        return Response(result, status=status.HTTP_200_OK)

    @action(methods=['get', 'post'], detail=False)
    def get_semesters(self, request, *args, **kwargs):
        result = [s.name for s in Semester.objects.all()]
        return Response(result, status=status.HTTP_200_OK)

    @action(methods=['get', 'post'], detail=False)
    def get_groups(self, request, *args, **kwargs):
        try:
            semester = request.headers['semester']
            semester = Semester.objects.get(name=semester)
            result = [g.name for g in Group.objects.filter(semester=semester)]

            return Response(result, status=status.HTTP_200_OK)

        except (ObjectDoesNotExist, EmptyResultSet):
            return Response(status=status.HTTP_204_NO_CONTENT)
        except KeyError as e:
            result = f'{e} not specified'
            return Response(result, status=status.HTTP_204_NO_CONTENT)
