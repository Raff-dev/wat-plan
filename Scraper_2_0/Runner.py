from typing import List, Dict, Tuple, Iterable, TypeVar
from bs4 import BeautifulSoup

from threading import Thread
from requests import Session

from Decorators import timer
from Scraper import Scraper
from Reporter import Reporter
from Setting import Setting
from SoupParser import SoupParser
from SharedList import SharedList
SCRAPING = 'scraping'
PARSING = 'parsing'
POSTING = 'posting'


class Runner():

    def __init__(self, max_scraping_workers=50, max_posting_workers=5):
        self.max_scraping_workers = max_scraping_workers
        self.max_posting_workers = max_posting_workers

        self.reporter = Reporter()
        self.setting_list = SharedList(name='setting')
        self.soup_data = SharedList(name='soup')
        self.schedule_data = SharedList(name='schedule')

    def prepare(self):
        TEST_SETTING = {
            Setting.YEAR: '2020',
            Setting.SEMESTER: Setting.WINTER,
            Setting.GROUP: 'WCY18IJ6S1'
        }
        sid = Scraper.authenticate()
        setting = Setting(sid=sid, **TEST_SETTING)
        self.run([setting]*5)

    def run(self, setting_list: List[Setting]):
        self.reset(setting_list)
        scraping_workers = min(self.max_scraping_workers,
                               self.setting_list.length)
        posting_workers = min(self.max_posting_workers,
                              self.setting_list.length)

        scrape_threads = [Thread(target=Runner.repeat,
                                 args=(self, self.__scrape, lambda: self.keep_scraping
                                       )) for _ in range(scraping_workers)]
        parse_thread = Thread(target=Runner.repeat,
                              args=(self, self.__parse, lambda: self.keep_parsing))
        post_threads = [Thread(target=Runner.repeat,
                               args=(self, self.__post, lambda: self.keep_posting
                                     )) for _ in range(posting_workers)]

        all_threads = [*scrape_threads, parse_thread, *post_threads]
        for thread in all_threads:
            thread.start()
        for thread in all_threads:
            thread.join()

        print(f'LENGTH: {self.schedule_data.length}')
        # print(f'FINAL DATA: {self.schedule_data}')

        self.reporter.final_report()

    def reset(self, setting_list: List[Setting]):
        self.reporter.reset()
        self.soup_data.reset()
        self.schedule_data.reset()
        self.setting_list.reset(setting_list)
        self.settings_count = len(setting_list)

    @staticmethod
    def repeat(self, func, predicate, *args, **kwargs):
        while predicate():
            func(self, *args, **kwargs)

    @Reporter.report_on(SCRAPING)
    def __scrape(self):
        setting = self.setting_list.pop()
        print(f'SETTING: {setting.__dict__}')
        url = Scraper.get_group_url(setting)
        print(f'URL {url}')
        soup = Scraper.get_soup(url)
        self.soup_data.append((setting, soup))

    @Reporter.report_on(PARSING)
    def __parse(self) -> None:
        setting, soup = self.soup_data.pop()
        print(f'SETTING: {setting.__dict__}')
        group_schedule = SoupParser.get_group_schedule(
            setting=setting, soup=soup)
        self.schedule_data.append(group_schedule)

    @Reporter.report_on(POSTING)
    def __post(self) -> None:
        # group_schedule = self.schedule_data.pop()
        pass
        # print(f'SCHEDULE DADTA: {group_schedule}')

    @property
    def keep_scraping(self):
        return self.setting_list.length > 0

    @property
    def keep_parsing(self):
        return (self.settings_count -
                self.reporter.get(SCRAPING).failed -
                self.reporter.get(PARSING).finished > 0)

    @property
    def keep_posting(self):
        return (self.settings_count -
                self.reporter.get(SCRAPING).failed -
                self.reporter.get(PARSING).failed -
                self.reporter.get(POSTING).finished > 0)


if __name__ == '__main__':
    ALL_SEMESTERS_SETTING = [
        {Setting.YEAR: year, Setting.SEMESTER: semester}
        for year in ['2019', '2020']
        for semester in [Setting.WINTER, Setting.SUMMER, Setting.RETAKE]
    ]
    runner = Runner()
    runner.prepare()
