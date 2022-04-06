from datetime import date
from threading import Thread, Lock
from typing import List
import json
import logging
import requests
import sys
import time
import os

from dotenv import load_dotenv
from distutils.util import strtobool

import soup_parser
from reporter import Reporter
from scraper import Scraper
from setting import Setting
from shared_list import SharedList


if logging.getLogger().handlers:
    # The Lambda environment pre-configures a handler logging to stderr. If a handler is already configured,
    # `.basicConfig` does not execute. Thus we set the level directly.
    logging.getLogger().setLevel(logging.INFO)
else:
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format='%(asctime)s %(levelname)s: %(message)s', datefmt='%Y-%m-%d %I:%M:%S')

_logger = logging.getLogger(__name__)
load_dotenv()

DEV = strtobool(os.getenv('DEV'))

UPDATE_PLAN_ROUTE = 'Plan/update_schedule/'
UPDATE_PLAN_URL = ('http://127.0.0.1:8000/' if DEV else os.getenv('UPDATE_PLAN_URL')) + UPDATE_PLAN_ROUTE

KEEP_CONNECTION_DELAY = 0.25
SECOND_SEMESTER_START = 3
FIRST_SEMESTER_START = 9


class InvalidResponseError(Exception):
    pass


dic = {}


def repeat(func, predicate, *args, **kwargs):
    dic[func] = 0
    def predicate(): return dic[func] < 3
    while predicate():
        dic[func] += 1
        _logger.info(f'Running {func.__name__} ')
        func(*args, **kwargs)

    _logger.info(f'Finished {func.__name__} ')


class Runner():

    def __init__(self, max_scraping_workers=3):
        self.max_scraping_workers = max_scraping_workers

        self.session = None
        self.scrapeLock = Lock()
        self.reporter = Reporter()
        self.setting_list = SharedList()
        self.soup_data = SharedList()
        self.schedule_data = SharedList()

    @classmethod
    def run_for_semester(cls, year: str, semester: str) -> None:
        sid = Scraper.authenticate()
        SETTING = {
            Setting.SID: sid,
            Setting.YEAR: year,
            Setting.SEMESTER: semester,
        }

        group_names = Scraper.get_group_names(Setting(**SETTING))
        settings = [Setting(group=group, **SETTING) for group in group_names]

        runner = cls()
        with requests.Session() as session:
            runner.session = session
            runner.run(settings)
        runner.reporter.report()

    @Reporter.time_measure
    def run(self, setting_list: List[Setting]) -> None:
        self.reset(setting_list)
        scraping_workers = min(self.max_scraping_workers, self.setting_list.length)

        jobs = [
            (self.__scrape, lambda: self.keep_scraping, scraping_workers),
            (self.__parse, lambda: self.keep_parsing, 1),
            (self.__post, lambda: self.keep_posting, 1)
        ]

        threads = [
            Thread(target=repeat, args=args)
            for *args, count in jobs for _ in range(count)
        ]

        for thread in threads:
            thread.start()
        for thread in threads:
            thread.join()

    def reset(self, setting_list: List[Setting]):
        self.reporter.reset()
        self.soup_data.reset()
        self.schedule_data.reset()
        self.setting_list.reset(setting_list)
        self.settings_count = len(setting_list)

    @Reporter.observe
    def __scrape(self) -> None:
        with self.scrapeLock:
            setting = self.setting_list.pop()
            time.sleep(KEEP_CONNECTION_DELAY)
        url = Scraper.get_group_url(setting)
        soup = Scraper.get_soup(url, self.session)

        assert soup and soup.title and 'e-Dziekanat' in soup.title.text, 'Invalid soup'
        self.soup_data.append((setting, soup))

    @Reporter.observe
    def __parse(self) -> None:
        setting, soup = self.soup_data.pop()
        group_schedule = soup_parser.get_group_schedule(
            setting=setting, soup=soup)
        self.schedule_data.append(group_schedule)

    @Reporter.observe
    def __post(self) -> None:
        group_schedule = self.schedule_data.pop()
        json_data = json.dumps(group_schedule, indent=4)

        with open(f"{group_schedule['group']}.py", 'w') as group_file:
            group_file.write(str(json_data))

        res = requests.post(
            url=UPDATE_PLAN_URL,
            headers={'Content-type': 'application/json'},
            data=json_data
        )
        if 200 >= res.status_code > 300:
            raise InvalidResponseError(f'Invalid status: {res.status_code}')

    @property
    def keep_scraping(self):
        return self.setting_list.length > 0

    @property
    def keep_parsing(self):
        return (self.settings_count -
                self.reporter.get(self.__scrape).failed -
                self.reporter.get(self.__parse).finished > 0)

    @property
    def keep_posting(self):
        return (self.settings_count -
                self.reporter.get(self.__scrape).failed -
                self.reporter.get(self.__parse).failed -
                self.reporter.get(self.__post).finished > 0)


def main():
    today = date.today()
    semester = (SECOND_SEMESTER_START <= today.month <= FIRST_SEMESTER_START) + 1
    year = today.year - (semester == 2)
    Runner.run_for_semester(year=str(year), semester=str(semester))


if __name__ == '__main__':
    main()
