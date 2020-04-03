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
import json
from .models import Block, Group, Semester


class Plan(ViewSet):

    @action(methods=['post'], detail=False)
    def update_plan(self, request, *args, **kwargs):
        print('GOT REQUEST')
        group = request.data['group']
        semester = request.data['semester']
        plan = request.data['plan']
        outdated = False

        group, gc = Group.objects.get_or_create(name=group)
        semester, sc = Semester.objects.get_or_create(
            name=semester, group=group)
        outdated = not semester.empty == (plan == None)
        semester.empty = plan == None
        semester.save()
        if semester.empty:
            return Response({"That's": 'Tosted'}, status=status.HTTP_201_CREATED)

        for date, plan_day in plan.items():
            for index, data in plan_day.items():
                block, created = Block.objects.get_or_create(
                    semester=semester, date=date, index=index)

                if data == None and not block.empty:
                    block.empty = True
                    block.save()
                    outdated = True
                else:
                    check = data.copy()
                    check.pop('place')
                    if not check.items() <= block.__dict__.items():
                        outdated = True
                        Block.objects.filter(id=block.id).update(**data)
        print(
            F'Plan of group {group.name}for semester {semester.name} Updated')
        semester.version += 1 if outdated else 0
        return Response({"That's": 'Tosted'}, status=status.HTTP_201_CREATED)

    @action(methods=['get'], detail=False)
    def get_semester(self, request, *args, **kwargs):
        print(f'headers {request.headers}\n')
        try:
            group = request.headers['group']
            group = Group.objects.get(name=group)
            semester = request.headers['semester']
            semester = Semester.objects.get(name=semester, group=group)
        except (ObjectDoesNotExist, EmptyResultSet):
            return Response(status=status.HTTP_204_NO_CONTENT)

        blocks = Block.objects.filter(
            semester=semester).order_by('date', 'index')

        values = [f.name for f in Block._meta.get_fields() if f.name not in [
            'semester', 'id']] + ['group', 'semester_name']

        result = Block.objects.annotate(
            semester_name=F('semester__name'),
            group=F('semester__group__name')).values(*values)
        return Response(result)

    @action(methods=['get'], detail=False)
    def get_versions(self, request, *args, **kwargs):
        vals = Semester.objects.all().values()
        result = [{
            'group': Group.objects.get(id=v['group_id']).name,
            'name': v['name'],
            'version': v['version']
        } for v in vals]
        return Response(result)

    @action(methods=['get', 'post'], detail=False)
    def test(self, request, *args, **kwargs):
        print(f'headers {request.headers}\n')
        group, created = Group.objects.get_or_create(
            name=request.headers['group'])
        print(group)
        groups = list(Group.objects.all().values('name'))
        print(groups)

        return Response(groups)
