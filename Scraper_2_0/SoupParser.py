import requests
from bs4 import BeautifulSoup
from typing import List, Dict, Iterable, TypeVar

import inspect

import pandas as pd
import numpy as np
import datetime

from Decorators import TimeMeasure


class SoupParser():
    """

    """
    ROMAN_NOTATION = {
        'I': 1, 'II': 2, 'III': 3, 'IV': 4,
        'V': 5, 'VI': 6, 'VII': 7, 'VIII': 8,
        'IX': 9, 'X': 10, 'XI': 11, 'XII': 12
    }

    BLOCKS_PER_DAY = 7
    DAYS_PER_WEEK = 7

    class Block():

        def __init__(self):
            self.title = None
            self.teacher = None
            self.class_index = None
            self.subject = None
            self.class_type = None
            self.place = None

    @staticmethod
    def get_soup(url: str) -> BeautifulSoup:
        res = requests.get(url, verify=False)
        # --- TEST PRINT ---
        print(res)
        soup = BeautifulSoup(res.text, features="lxml")
        return soup

    @TimeMeasure
    @staticmethod
    def get_group_schedule(url: str):
        """
         :param url -
         :return
        """
        soup = SoupParser.get_soup(url)
        schedule_cells = soup.find_all(
            'td', class_='tdFormList1DSheTeaGrpHTM3')

        if not len(schedule_cells):
            # the schedule is empty
            return None

        days_soup = SoupParser.__soup_to_sorted_days_soup(soup)
        start_date = SoupParser.__get_start_date(soup)
        print(f'START_DATE: {start_date}')

        group_schedule = SoupParser.__parse_semester(days_soup, start_date)
        return group_schedule

    @staticmethod
    def __soup_to_sorted_days_soup(soup: BeautifulSoup) -> pd.DataFrame:
        data_frame = None
        return data_frame

    @staticmethod
    def __get_start_date(soup: BeautifulSoup) -> datetime.date:
        day_month = soup.find_all(
            'nobr', class_='thFormList1HSheTeaGrpHTM3')[0]
        day_month = day_month.get_attribute('innerText')
        day, month = day_month.split('\n')
        month = SoupParser.ROMAN_NOTATION[month]
        year = datetime.date.today().year

        return datetime.date(year, month, int(day))

    @staticmethod
    def __parse_semester(
            days_soup: List[BeautifulSoup],
            start_date: datetime.date):

        semester_schedule = {}
        for day_data, date in enumerate_date(days_soup, start_date):
            day_schedule = SoupParser.__parse_day(day_data)
            date = str(date)
            semester_schedule[date] = day_schedule

        return semester_schedule.copy()

    @staticmethod
    def __parse_day(day_blocks_soup):
        assert len(day_blocks_soup) == SoupParser.BLOCKS_PER_DAY, (
            f'Invalid data format at {inspect.stack()[0][3]}')

        day_schedule = {}
        for block_soup, block_index in enumerate(day_blocks_soup, start=1):
            day_schedule[block_index] = SoupParser.__parse_block(block_soup)

    @staticmethod
    def __parse_block(block_soup):
        if block_soup == None or len(block_soup.text) == 1:
            return None

        block = SoupParser.Block()
        data = block_soup.find_all('nobr')

        block.title = block_soup['title']
        block.teacher = data[1].text
        block.class_index = data[2].text.replace(
            '[', '').replace(']', '')

        data = str(data[0]).replace('<br/>', '|')
        data = BeautifulSoup(data, features="lxml").nobr.text.split('|')

        block.subject = data[0]
        block.class_type = data[1].replace(
            '(', '').replace(')', '')
        block.place = data[2:]

        return block.__dict__


def enumerate_date(iterable: Iterable[TypeVar], start_date, days_step=1):
    for element in iterable:
        yield element, start_date
        start_date += datetime.timedelta(days=days_step)


schedule = {
    'year': '20202',
    'semester': 'winter',
    'group': 'WCY18IY5S1',
    'plan': {
        '2020.10.01': {
            '1': {
                'title': 'lorem ipsum',
                'teacher': 'lorem ipsum',
                'class_index': 'lorem ipsum',
                'subject': 'lorem ipsum',
                'class_type': 'lorem ipsum',
                'place': 'lorem ipsum',
            },
        }
    }
}
