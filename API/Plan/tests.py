from datetime import date
from django.test import TestCase

from django.test import TestCase, Client
from rest_framework.status import HTTP_201_CREATED

from .models import Semester, Group, Day, Block

MAX_BLOCK_INDEX = 7


class TestUpdateSchedule(TestCase):
    def setUp(self):
        self.url_update_schedule = "/Plan/update_schedule/"
        self.headers = {'Content-type': 'application/json'}
        self.block_data = {
            "title": "test",
            "teacher": "test",
            "class_index": "1",
            "subject": "test",
            "class_type": "test",
            "place": "test",
        }
        self.schedule_data = {
            "group": "test",
            "semester": "test",
            "schedule": {
                "2022-03-03": [self.block_data] + [None]*(MAX_BLOCK_INDEX - 1)
            }
        }

    def do_schedule_update(self):
        return Client().post(self.url_update_schedule, self.schedule_data, content_type="application/json", **self.headers)

    def test_add_schedule(self):
        res = self.do_schedule_update()
        self.assertEquals(res.status_code, HTTP_201_CREATED)
        for model in [Semester, Group, Day, Block]:
            self.assertTrue(model.objects.exists())

    def test_update_schedule(self):
        semester = Semester.objects.create(name='test')
        group = Group.objects.create(semester=semester, name='test')
        day = Day.objects.create(group=group, date=date(2022, 3, 3))
        Block.objects.bulk_create([
            Block(**self.block_data, index=i, day_id=day.id) for i in range(1, MAX_BLOCK_INDEX + 1)
        ])
        self.assertTrue(Semester.objects.exists())
        self.assertTrue(Group.objects.exists())
        self.assertTrue(Day.objects.exists())
        self.assertTrue(Block.objects.all().count(), 7)

        res = self.do_schedule_update()
        self.assertTrue(Block.objects.all().count(), 1)
