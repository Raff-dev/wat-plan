from typing import List, Dict, Tuple, Iterable, TypeVar
from bs4 import BeautifulSoup

import time
from threading import Thread, Lock

from Decorators import timer
from Scraper import Scraper
from Reporter import Reporter
from Setting import Setting
from SoupParser import SoupParser
from SharedList import SharedList


KEEP_CONNECTION_DELAY = 0.2


class Runner():

    def __init__(self, max_scraping_workers=5, max_posting_workers=5):
        self.max_scraping_workers = max_scraping_workers
        self.max_posting_workers = max_posting_workers

        self.scrapeLock = Lock()
        self.reporter = Reporter()
        self.setting_list = SharedList()
        self.soup_data = SharedList()
        self.schedule_data = SharedList()

    def run(self, setting_list: List[Setting]):
        self.reset(setting_list)
        scraping_workers = min(self.max_scraping_workers,
                               self.setting_list.length)
        posting_workers = min(self.max_posting_workers,
                              self.setting_list.length)

        scrape_threads = [Thread(target=Runner.repeat,
                                 args=(self.__scrape, lambda: self.keep_scraping
                                       )) for _ in range(scraping_workers)]
        parse_thread = Thread(target=Runner.repeat,
                              args=(self.__parse, lambda: self.keep_parsing))
        post_threads = [Thread(target=Runner.repeat,
                               args=(self.__post, lambda: self.keep_posting
                                     )) for _ in range(posting_workers)]

        all_threads = [*scrape_threads, parse_thread, *post_threads]
        for thread in all_threads:
            thread.start()

        for thread in all_threads:
            thread.join()

        self.reporter.report()

    def reset(self, setting_list: List[Setting]):
        self.reporter.reset()
        self.soup_data.reset()
        self.schedule_data.reset()
        self.setting_list.reset(setting_list)
        self.settings_count = len(setting_list)

    @staticmethod
    def repeat(func, predicate, *args, **kwargs):
        while predicate():
            func(*args, **kwargs)
        print(f'FINISHED {func.__name__} ')

    @Reporter.observe
    def __scrape(self):
        setting = self.setting_list.pop()
        with self.scrapeLock:
            time.sleep(KEEP_CONNECTION_DELAY)
        url = Scraper.get_group_url(setting)
        soup = Scraper.get_soup(url)

        assert soup is not None and soup.title and 'e-Dziekanat' in soup.title.text, 'Invalid soup'
        self.soup_data.append((setting, soup))

    @Reporter.observe
    def __parse(self) -> None:
        setting, soup = self.soup_data.pop()
        group_schedule = SoupParser.get_group_schedule(
            setting=setting, soup=soup)
        self.schedule_data.append(group_schedule)

    @Reporter.observe
    def __post(self) -> None:
        group_schedule = self.schedule_data.pop()

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


if __name__ == '__main__':
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
    COUNT = 20
    sid = Scraper.authenticate()
    setting = Setting(sid=sid, **TEST_SETTING)
    start = time.time()
    runner = Runner()
    runner.run([setting]*COUNT)
    print(f'Total Time: {time.time()-start}')
    print(f'Time per Setting: {(time.time()-start)/COUNT}')
