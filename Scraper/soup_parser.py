from typing import List, Dict, Iterable, TypeVar
import types
import inspect

from bs4 import BeautifulSoup
import pandas as pd
import datetime

from setting import Setting

ROMAN_NOTATION = {
    'I': 1, 'II': 2, 'III': 3, 'IV': 4,
    'V': 5, 'VI': 6, 'VII': 7, 'VIII': 8,
    'IX': 9, 'X': 10, 'XI': 11, 'XII': 12
}

DAYS_PER_WEEK = 7
BLOCKS_PER_DAY = 7


class SoupParser():
    """

    """

    class Block():

        def __init__(self):
            self.title = None
            self.teacher = None
            self.class_index = None
            self.subject = None
            self.class_type = None
            self.place = None

    @staticmethod
    @Setting.requires_setting(Setting.SEMESTER, Setting.YEAR, Setting.GROUP)
    def get_group_schedule(setting: Setting, soup: BeautifulSoup) -> Dict:
        """
         :param url -
         :return
        """
        days_soup = SoupParser.__soup_to_sorted_days_soup(soup)
        if days_soup is None:
            return None

        start_date = SoupParser.__get_start_date(soup, setting.year, setting.semester)

        group_schedule = SoupParser.__parse_semester(days_soup, start_date)
        group_schedule_formatted = {
            'year': setting.year,
            'semester': setting.semester,
            'group': setting.group,
            'schedule': group_schedule
        }
        return group_schedule_formatted

    @staticmethod
    def __soup_to_sorted_days_soup(soup: List[BeautifulSoup]) -> List[List[BeautifulSoup]]:
        blocks = soup.find_all('td', class_='tdFormList1DSheTeaGrpHTM3')
        if not len(blocks):
            return None

        columns_count = int(len(blocks) / (DAYS_PER_WEEK*BLOCKS_PER_DAY))
        row_blocks = SoupParser.array_split(blocks, columns_count)
        week_blocks = pd.DataFrame(row_blocks).T.values
        sorted_blocks = [
            block for column in week_blocks for block in column]
        sorted_days = SoupParser.array_split(sorted_blocks, DAYS_PER_WEEK)
        return sorted_days

    @staticmethod
    def __get_start_date(soup: BeautifulSoup, year: str, semester: str) -> datetime.date:
        day_month = soup.find_all(class_='thFormList1HSheTeaGrpHTM3')[0]
        day, month = SoupParser.array_split(day_month.nobr.text, 2)
        month = ROMAN_NOTATION[month]
        year = int(year) if semester == Setting.WINTER else int(year)+1
        return datetime.date(year, month, int(day))

    @staticmethod
    def __parse_semester(days_soup: List[BeautifulSoup], start_date: datetime.date):

        semester_schedule = {}
        for day_data, date in SoupParser.enumerate_date(days_soup, start_date):
            day_schedule = SoupParser.__parse_day(day_data)
            date = str(date)
            semester_schedule[date] = day_schedule

        return semester_schedule

    @staticmethod
    def __parse_day(day_blocks_soup):
        assert len(day_blocks_soup) == BLOCKS_PER_DAY, (
            f'Invalid data format at {inspect.stack()[0][3]}')

        day_schedule = [SoupParser.__parse_block(block_soup)
                        for block_soup in day_blocks_soup]
        return day_schedule

    @staticmethod
    def __parse_block(block_soup):
        if block_soup == None or len(block_soup.text) == 1:
            return None

        block = SoupParser.Block()
        data = block_soup.find_all('nobr')

        block.title = block_soup['title']
        block.teacher = data[1].text
        block.class_index = data[2].text.replace('[', '').replace(']', '')

        data = str(data[0]).replace('<br/>', '|')
        data = BeautifulSoup(data, features="lxml").nobr.text.split('|')

        block.subject = data[0]
        block.class_type = data[1].replace('(', '').replace(')', '')
        block.place = data[2:]
        return block.__dict__

    @staticmethod
    def array_split(iterable: Iterable[TypeVar], chunk_length: int) -> List[List[TypeVar]]:
        if isinstance(iterable, types.GeneratorType):
            iterable = list(iterable)
        return [iterable[i:i+chunk_length]if len(iterable)-i != 1 else iterable[-1] for i in range(0, len(iterable), chunk_length)]

    @staticmethod
    def enumerate_date(iterable: Iterable[TypeVar], start_date: datetime.date, days_step: int = 1):
        for element in iterable:
            yield element, start_date
            start_date += datetime.timedelta(days=days_step)
