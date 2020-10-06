from typing import List, Dict, Iterable, TypeVar
from bs4 import BeautifulSoup

from Decorators import timer
from Scraper import Scraper
from Setting import Setting
from SoupParser import SoupParser


def test():
    ALL_SEMESTERS_SETTING = [
        {Setting.YEAR: year, Setting.SEMESTER: semester}
        for year in ['2019', '2020']
        for semester in [Setting.WINTER, Setting.SUMMER, Setting.RETAKE]
    ]

    TEST_SETTING = {
        Setting.YEAR: '2020',
        Setting.SEMESTER: Setting.WINTER,
        Setting.GROUP: 'WCY18IJ6S1'
    }
    sid = None
    sid = Scraper.authenticate()

    # with open('sid.sid', 'r') as f:
    #     sid = f.read()
    # with open('sid.sid', 'w')as f:
    #     f.write(sid)
    # return
    setting = Setting(sid=sid, **TEST_SETTING)
    url = Scraper.get_group_url(setting)
    print(url)
    soup = Scraper.get_soup(url)

    res, time = measure(soup, setting)
    print(res)
    print(time)
    return

    setting = Setting(sid=sid, **TEST_SETTING)
    group_names = Scraper.get_group_names(setting)
    start = time.time()

    for group_name in group_names[:1]:
        setting.set_settings(group=group_name)
        url = Scraper.get_group_url(setting)
        soup = Scraper.get_soup(url)
        res = SoupParser.get_group_schedule(soup, setting)

    end = time.time()
    total_scrape_time = end-start
    print(f'Time Scraping: {total_scrape_time}')


class Runner():

    def __init__(self):

        pass

    def run(self, setting_list: Setting, post=True):
        pass

    def __parse(self):
        pass

    def __mark_failed(self):
        pass

    def __log(self):
        pass

    def __store_group_schedule_soup(self, schedule_soup: BeautifulSoup):
        pass

    def __get_group_schedule_soup(self,):
        pass

    def __estimate_time(self):
        pass

    @staticmethod
    def __post_group_schedule(group_schedule: Dict):
        pass


@timer(1)
def measure(soup, setting):
    url = Scraper.get_group_url(setting)
    soup = Scraper.get_soup(url)


if __name__ == '__main__':
    test()
