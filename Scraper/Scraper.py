from webdriver_manager.chrome import ChromeDriverManager
from selenium.common.exceptions import NoSuchElementException
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.options import Options
from selenium import webdriver
from bs4 import BeautifulSoup
from threading import Thread
from multiprocessing import Process
from datetime import date, timedelta, datetime
import json
import requests
import pandas as pd
import numpy as np
import re
import time
import sys
from creds import username, password


class Scraper():

    def __init__(self, headless=True, bot_init=False, semester=None, index=0):
        self.options = Options()
        self.options.add_argument("--headless") if headless else None
        self.options.add_argument('--ignore-ssl-errors=yes')
        self.options.add_argument('--ignore-certificate-errors')
        self.bot = self.get_bot() if bot_init else None
        self.semester = semester
        self.index = index

        self.groups = None
        self.url = {'letni': None, 'zimowy': None}
        self.data = {}
        self.in_groups = False

        self.pool_size = 1
        self.queue = None

        self.current_group = None
        self.groups_scraped = 0
        self.timer = get_timer()

    def get_bot(self):
        return webdriver.Chrome(ChromeDriverManager().install(), options=self.options)

    def set_up(self, semester=None, url=None, groups=None, pool_size=None):
        self.semester = semester if semester else self.semester
        self.url[self.semester] = url if url else self.url[self.semester]
        self.groups = groups if groups else self.groups
        self.pool_size = pool_size if pool_size else self.pool_size

    def run(self):
        try:
            self.bot = self.bot if self.bot else self.get_bot()
            self.get(self.url[self.semester])
            self.in_groups = True

            if not self.groups:
                self.get_groups()
            self.groups = np.array_split(
                self.groups, self.pool_size)[self.index]
            for group in self.groups:
                self.scrape_semester(group)

            finish = self.timer['finish'] = datetime.now()-self.timer['born']
            printf(f'Scraper-{self.index} finished within {finish}')
            self.close(message='finished')

        except Exception as e:
            if e not in (NoSuchElementException, ValueError):
                printf(
                    f'Scrapper-{self.index}: unexpected Exception: \n{type(e)}:{e}\n')
                self.close()

    # method -> 'class name' 'id' 'xpath' 'link text' 'name' 'css selector'
    def find(self, method, element: str):
        el = []
        attempts = 0
        sleep_time = 0.1
        time_out = 20
        while not len(el):
            try:
                el = self.bot.find_elements(method, element)
                if not len(el):
                    attempts += 1
                    if attempts*sleep_time == time_out:
                        raise ValueError('Finding element timeout')
                    elif not attempts*sleep_time % 5:
                        printf(f'Trying to find {element}')
                    time.sleep(sleep_time)
            except ValueError:
                printf(
                    f'Element not found:{element} in group {self.current_group}')
                return None
        return el if len(el) > 1 else el[0]

    def login(self):
        self.bot.get('https://s1.wcy.wat.edu.pl/ed1/')
        try:
            inputs = self.find('class name', 'inputChaLog')
            if inputs is None:
                raise ValueError('Failed to log in')
            inputs[0].send_keys(username)
            inputs[1].send_keys(password)
            self.find('css selector', 'input.inputLogL').click()

        except (ValueError, IndexError) as e:
            printf(f'Scraper-{self.index}: login failed Exception: {e}\n')
            self.close()
            return

    def get_to_groups(self, semester=None):
        self.in_groups = False
        self.semester = semester if semester else self.semester
        assert self.semester, 'a Semester must be assigned to Scraper'

        menus = self.find('class name', 'ThemeIEMainFolderText')
        schedules = self.find('class name', 'ThemeIEMenuFolderText')
        semesters = self.find('class name', 'ThemeIEMenuItemText')

        year = date.today().year
        semester = f'{year - 1}/{year} {self.semester}'

        sems = []
        for sem in semesters:
            if semester in sem.get_attribute('innerHTML'):
                sems.append(sem)
        for button in [menus[1], schedules[2], sems[2]]:
            button.click()

        self.url[self.semester] = self.curl
        self.in_groups = True

    def get_groups(self):
        """saves all available groups of a given semester"""
        assert self.in_groups, 'Groups page must be open in order to get groups'
        groups = []
        aMenus = self.find('class name', "aMenu")
        for aMenu in aMenus:
            groups.append(aMenu.get_attribute('innerText'))

        self.groups = groups
        return groups

    def scrape_semester(self, group_name):
        assert self.in_groups, 'Groups page must be open in order to scrape'

        self.current_group = group_name
        scrape_start = datetime.now()
        group_link = self.find('link text', group_name)
        if group_link == None:
            printf(f'Failed to find group')
            return
        group_link.click()
        page_loaded = datetime.now()
        delta_loading = page_loaded-scrape_start

        soup = BeautifulSoup(self.bot.page_source, features='lxml')
        cells = soup.find_all('td', class_='tdFormList1DSheTeaGrpHTM3')
        if not len(cells):
            printf(f'Plan of {self.current_group} is empty')
            self.data[self.semester] = {group_name: None}
        else:
            roman_notation = {
                'I': 1, 'II': 2, 'III': 3, 'IV': 4, 'V': 5, 'VI': 6, 'VII': 7, 'VIII': 8, 'IX': 9, 'X': 10, 'XI': 11, 'XII': 12
            }
            day = self.find('css selector', '.thFormList1HSheTeaGrpHTM3 nobr')
            day, month = day[0].get_attribute('innerText').split('\n')
            month = roman_notation[month]
            day = date(date.today().year, month, int(day))

            data = {}
            for plan_day in self.scrape_day(cells=cells):
                data[str(day)] = plan_day
                day += timedelta(days=1)
            self.data[self.semester] = {group_name: data.copy()}

        delta_scraping = datetime.now() - page_loaded
        post_result(
            self.current_group,
            self.semester,
            self.data[self.semester][group_name]
        )
        delta_posting = datetime.now() - page_loaded - delta_scraping
        self.notify(delta_loading, delta_scraping, delta_posting)

    def scrape_day(self, soup=None, cells=None):
        """
        scrape_day is a generator function yelding one day of whole plan
        """
        assert soup or cells, 'No data to scrape'
        # finding all the block cells in a plan & rearanging them for easier manipulation
        if not cells:
            cells = soup.find_all('td', class_='tdFormList1DSheTeaGrpHTM3')

        cells = pd.Series(cells)
        days, blocks_per_day = 7, 7
        cells = np.array_split(cells.values, days*blocks_per_day)
        df = pd.DataFrame(cells).T
        day_data = {}
        for week in df.values:
            for block, i in zip(week, range(len(week))):
                if block == None or len(block.text) == 1:
                    day_data[i % 7 + 1] = None
                else:
                    block_data = {}
                    data = block.find_all('nobr')
                    block_data['title'] = block['title']
                    block_data['teacher'] = data[1].text
                    block_data['class_index'] = data[2].text.replace(
                        '[', '').replace(']', '')
                    data = str(data[0]).replace('<br/>', '|')
                    data = BeautifulSoup(
                        data, features="lxml").nobr.text.split('|')
                    block_data['subject'] = data[0]
                    block_data['class_type'] = data[1].replace(
                        '(', '').replace(')', '')
                    block_data['place'] = data[2:]
                    day_data[i % 7 + 1] = block_data

                if i and (i+1) % 7 == 0:
                    yield day_data.copy()

    @property
    def curl(self):
        return self.bot.current_url

    def notify(self, loading, scraping, posting):
        total = loading + scraping + posting
        self.timer['loading'] += loading
        self.timer['scraping'] += scraping
        self.timer['posting'] += posting
        self.timer['total'] += total
        self.groups_scraped += 1
        print('THATS POSTING:', posting, ' ppp ', self.timer['posting'])

        if self.queue:
            self.queue.put({
                'loading': loading,
                'scraping': scraping,
                'posting': posting,
                'total': total,
                'index': self.index,
                'semester': self.semester,
                'groups_scraped': self.groups_scraped,
                'group': self.current_group
            })
        else:
            loading = self.timer['loading']
            scraping = self.timer['scraping']
            posting = self.timer['posting']
            total = self.timer['total']
            born = self.timer['born']
            printf(f'Average time for {self.semester}-{self.index}')
            printf(
                f'Loading: {loading/self.groups_scraped}')
            printf(
                f'Scraping: {scraping/self.groups_scraped}')
            printf(
                f'Posting: {posting/self.groups_scraped}')
            printf(
                f'Total: {total/self.groups_scraped}')
            printf(f'Time alive: {datetime.now()-born}')
            printf('')

    def get(self, url):
        self.bot.get(url)

    def close(self, message='failure'):
        if message and self.queue:
            self.queue.put({message: True})
        self.bot.close()


def post_result(group, semester, data):
    data = {
        'group': group,
        'semester': semester,
        'plan': data
    }
    url = "https://watplan.eu.pythonanywhere.com/Plan/update_plan/"
    headers = {'Content-type': 'application/json'}
    r = requests.post(url, data=json.dumps(data), headers=headers)
    printf(
        f'Request status for group {group} {semester}: {r.status_code}')
    pass


def get_timer():
    timer = {}
    for name in ['born', 'alive', 'loading', 'scraping', 'posting', 'total', 'finish']:
        if name == 'born':
            timer[name] = datetime.now()
        else:
            timer[name] = timedelta(seconds=0)
    return timer.copy()


def printf(content):
    print(content, flush=True)


def test():
    s = Scraper(headless=False, bot_init=True, semester='letni')
    s.login()
    s.get_to_groups()
    s.scrape_semester('WCY')
    s.scrape_semester('WCY18IY5S1')
