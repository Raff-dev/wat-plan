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
from home.models import Apk
import datetime
import time


class Plan(ViewSet):

    @action(methods=['post'], detail=False)
    def update_schedule(self, request, *args, **kwargs):
        try:
            start = time.time()
            group = request.data['group']
            semester = request.data['semester']
            schedule = request.data['schedule']

            semester, screated = Semester.objects.get_or_create(
                name=semester)
            group, gcreated = Group.objects.get_or_create(
                name=group, semester=semester)

            should_update = screated or gcreated

            schedule_days = Day.objects.filter(group=group)
            if schedule is None and schedule_days:
                schedule_days.delete()

            elif schedule:
                for date, schedule_day in schedule.items():
                    date = datetime.date(*[int(d) for d in date.split('-')])
                    day, dcreated = Day.objects.get_or_create(
                        group=group, date=date)

                    for block_index, block_data in enumerate(schedule_day, start=1):
                        if block_data is None:
                            should_update |= Block.objects.filter(
                                day=day, index=block_index).delete()[0]
                        else:
                            block, bcreated = Block.objects.get_or_create(
                                day=day, index=block_index)
                            check = {v for v in block_data.items()
                                     if v[0] != 'place'}

                            if not check <= block.__dict__.items():
                                Block.objects.filter(
                                    id=block.id).update(**block_data)
                                should_update = True
            end = time.time()
            print(
                f'{group.name} Semester: {semester.name} Updated: {should_update} time: {round(end-start,2)}')
            if should_update:
                group.version += 1
                group.save()
            return Response(group, status=status.HTTP_201_CREATED)
        except Exception as e:
            print(f'Exception {e}')
            return Response('konia', status=status.HTTP_400_BAD_REQUEST)

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

    @action(methods=['get'], detail=False)
    def get_app_version(self, request, *args, **kwargs):
        version = Apk.objects.all().order_by('-release_date').first().version
        return Response({'version': version}, status=status.HTTP_200_OK)
