from collections import namedtuple
from inspect import BlockFinder
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.viewsets import ViewSet
from rest_framework import status
from django.core.exceptions import ObjectDoesNotExist, EmptyResultSet
from .models import Block, Day, Group, Semester
from home.models import Apk
import datetime

from django.db import transaction


class Plan(ViewSet):
    BlockFinder = namedtuple('BlockFinder', ['date', 'index'])

    @action(methods=['post'], detail=False)
    def update_schedule(self, request, *args, **kwargs):
        if not request.data:
            return Response(status=status.HTTP_200_OK)

        group = request.data['group']
        semester = request.data['semester']
        schedule = request.data['schedule']

        semester, screated = Semester.objects.get_or_create(name=semester)
        group, gcreated = Group.objects.get_or_create(
            name=group,
            semester=semester
        )
        increment_version = screated or gcreated

        days = Day.objects.filter(group=group)
        blocks = Block.objects.filter(day__in=days)
        blocks_map = {self.BlockFinder(block.day.date, block.index): block for block in blocks}

        days_to_create = []
        blocks_to_delete = []
        blocks_to_create = []
        blocks_to_update: dict[BlockFinder, dict] = {}

        if schedule is None and days:  # the group was removed from plan
            increment_version = True
            days.delete()

        elif schedule:
            for date, schedule_day in schedule.items():
                date = datetime.date(*[int(d) for d in date.split('-')])

                day = None
                for _day in days:
                    if _day.date == date:
                        day = _day
                        break

                if not day:
                    day = Day(group=group, date=date)
                    days_to_create.append(day)

                for block_index, block_data in enumerate(schedule_day, start=1):
                    block_finder = self.BlockFinder(date, block_index)

                    if block_finder in blocks_map:  # such a block already exists
                        if block_data is None:
                            blocks_to_delete.append(blocks_map[block_finder])
                            continue

                        block = blocks_map[block_finder]
                        values = {v for v in block_data.items() if v[0] != 'place'}

                        # checks whether new values differ from existing ones
                        if not values <= block.__dict__.items():
                            blocks_to_update[block_finder] = block_data
                    elif block_data:
                        blocks_to_create.append(Block(**block_data, index=block_index, day=day))

        increment_version |= any([days_to_create, blocks_to_delete, blocks_to_create, blocks_to_update])

        with transaction.atomic():
            if days_to_create:
                days= Day.objects.bulk_create(days_to_create)
                for day in days:
                    day.save()

                # assign created days to the blocks
                days = Day.objects.filter(group=group, date__in=[block.day.date for block in blocks_to_create])
                days = {day.date: day for day in days}
                for block in blocks_to_create:
                    block.day = days[block.date]

            if blocks_to_delete:
                Block.objects.all().filter(pk__in=[block.pk for block in blocks_to_delete]).delete()

            if blocks_to_create:
                Block.objects.bulk_create(blocks_to_create)

            if blocks_to_update:
                for block, block_data in blocks_to_update.items():
                    block.update(**block_data)

        print(f'{group.name} Semester: {semester.name} Updated: {increment_version}')
        if increment_version:
            group.version = group.version + 1
            group.save()

        return Response(status=status.HTTP_201_CREATED)

    @action(methods=['get'], detail=False)
    def get_group(self, request, *args, **kwargs):
        try:
            semester_name = request.headers['semester']
            semester = Semester.objects.get(name=semester_name)

            group_name = request.headers['group']
            group = Group.objects.get(name=group_name, semester=semester)

            days = Day.objects.all().filter(group=group)
            result = {
                'version': group.version,
                'first_day': days and days.first().date,
                'last_day': days and days.last().date,
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
