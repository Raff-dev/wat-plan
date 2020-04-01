from webdriver_manager.chrome import ChromeDriverManager
from selenium.common.exceptions import NoSuchElementException
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.options import Options
from selenium import webdriver
from bs4 import BeautifulSoup
from threading import Thread
from multiprocessing import Process
from datetime import date, timedelta, datetime
import pandas as pd
import numpy as np
import re
import time
import sys
from creds import username, password

class Scraper():

    def __init__(self,headless=True,bot_init=False,semester=None, index = None):
        self.options = Options()
        self.options.add_argument("--headless") if headless else None
        self.bot = self.get_bot() if bot_init == True else None
        self.semester = semester
        self.index = index

        self.groups = None
        self.url = {'letni':None,'zimowy':None}
        self.data = {}

        self.pool_size = None
        self.queue=None

        self.current_group = None
        self.groups_scraped = 0
        self.timer = get_timer()

    def get_bot(self):
        return webdriver.Chrome(ChromeDriverManager().install(),options=self.options)

    def set_up(self,semester = None, url = None, groups = None, pool_size=None,):
        self.semester = semester if semester else self.semester
        self.url[self.semester] = url if url else self.url[self.semester]
        self.groups = groups if groups else self.groups
        self.pool_size = pool_size if pool_size else self.pool_size
        assert self.url[self.semester] or url, 'Scraper must have an url'

    def run(self):
        self.bot = self.get_bot() if not self.bot else None
        self.get(self.url[self.semester])
        if not self.groups:
            self.get_groups()
        if self.index and self.pool_size:
            self.groups = np.array_split(self.groups,self.pool_size)[self.index]
        for group in self.groups:
            self.scrape_semester(group)
        self.time_finish = datetime.now()-self.time_born
        print(f'Scraper-{self.index} finished within {self.time_finish}')

    # method -> 'class name' 'id' 'xpath' 'link text' 'name' 'css selector'
    def find(self, method, element: str):   
        el = []
        send_msg = 0
        while not len(el):
            el = self.bot.find_elements(method, element)
            if not len(el):
                send_msg += 1
                if not send_msg % 20:
                    print('Trying to find the element')
                    if send_msg > 10:
                        print('Could not find element: ' + element)
                        print('Refreshing page')
                        self.get(self.curl)
                    if send_msg > 30:
                        print('Could not find element: ' + element)
                        return None
                time.sleep(0.5)
        return el if len(el) > 1 else el[0]

    def login(self):
        bot = self.bot
        bot.get('https://s1.wcy.wat.edu.pl/ed1/')

        inputs = self.find('class name', 'inputChaLog')
        for i, k in zip(inputs, [username, password]):
            i.send_keys(k)

        self.find('css selector', 'input.inputLogL').click()

    def get_to_groups(self, semester):
        self.in_groups=False
        self.semester = semester
        menus = self.find('class name', 'ThemeIEMainFolderText')
        schedules = self.find('class name', 'ThemeIEMenuFolderText')
        semesters = self.find('class name', 'ThemeIEMenuItemText')

        year = date.today().year
        semester = f'{year - 1}/{year} {semester}'

        sems = []
        for sem in semesters:
            if semester in sem.get_attribute('innerHTML'):
                sems.append(sem)

        for button in [menus[1], schedules[2], sems[2]]:
            button.click()
        self.in_groups = True
        self.url[self.semester] = self.curl()

    def get_groups(self):
        groups = []
        aMenus = self.find('class name', "aMenu")
        for aMenu in aMenus:
            groups.append(aMenu.get_attribute('innerText'))

        self.groups = groups
        return groups

    def scrape_semester(self,group_name):
        self.current_group = group_name
        scrape_start = datetime.now()
        group_link = self.find('link text', group_name)
        if group_link==None:
            self.data[group_name] = None
            return
        group_link.click()
        page_loaded = datetime.now()
        delta_loading = (page_loaded-scrape_start).microseconds

        soup = BeautifulSoup(self.bot.page_source, features='lxml')
        roman_notation={
            'I':1,'II':2,'III':3,'IV':4,'V':5,'VI':6,'VII':7,'VIII':8,'IX':9,'X':10,'XI':11,'XII':12
        }
        day = self.find('css selector', '.thFormList1HSheTeaGrpHTM3 nobr')
        day,month = day[0].get_attribute('innerText').split('\n')
        month = roman_notation[month]
        day = date(date.today().year,month,int(day))
        data={}

        for plan_day in self.scrape_day(soup):
            data[str(day)] = plan_day
            day+=timedelta(days=1)

        delta_scraping = (datetime.now()-page_loaded).microseconds
        delta_total = delta_loading + delta_scraping
        self.notify(delta_loading,delta_scraping,delta_total)
        self.data[group_name] = data.copy()

    def scrape_day(self,soup):
        """this is a generator function yelding one day of whole plan"""
        assert self.scrape_ready, 'Plan of a group must be opened in order to scrape'

        # finding all the block cells in a plan & rearanging them for easier manipulation
        days,blocks_per_day =7,7
        cells = soup.find_all('td',class_='tdFormList1DSheTeaGrpHTM3')
        cells = pd.Series(cells)
        cells = np.array_split(cells.values,days*blocks_per_day)
        df = pd.DataFrame(cells).T
        day_data = {}
        for week in df.values:
            for block, i in zip(week,range(len(week))):
                if block==None or len(block.text) == 1:
                    day_data[ i%7 +1 ] = None
                else:
                    block_data = {}
                    data = block.find_all('nobr')
                    block_data['title'] = block['title']
                    block_data['teacher'] = data[1].text
                    block_data['index'] = data[2].text.replace('[','').replace(']','')
                    data = str(data[0]).replace('<br/>','|')
                    data = BeautifulSoup(data,features="lxml").nobr.text.split('|')
                    block_data['subject'] = data[0]
                    block_data['class_type'] = data[1].replace('(','').replace(')','')
                    block_data['place'] = data[2:]
                    day_data[ i%7 +1 ] = block_data

                if i and (i+1) % 7 == 0 :
                    yield day_data.copy()
    

    def curl(self):
        return self.bot.current_url

    def notify(self,loading,scraping,total):
        self.timer['loading'] += loading
        self.timer['scraping'] += scraping
        self.timer['total'] += total
        self.groups_scraped += 1

        if self.queue:
            self.queue.put({
                'loading' : loading,
                'scraping' : scraping,
                'total' : total,
                'index' : self.index,
                'semester' : self.semester,
                'groups_scraped' : self.groups_scraped,
                'group' : self.current_group
                })
        else:
            loading = self.timer['loading']
            scraping = self.timer['scraping']
            total = self.timer['total']
            born = self.timer['born']
            print(f'Average time for {self.semester}-{self.index}')
            print(f'Loading: {round(loading/self.groups_scraped/100000,2)} seconds')
            print(f'Scraping: {round(scraping/self.groups_scraped/100000,2)} seconds')
            print(f'Total: {round(total/self.groups_scraped/100000,2)} seconds')
            print(f'Time alive: {datetime.now()-born}')
            print('')

    
    def get(self,url):
        self.in_groups=True
        self.bot.get(url)

    def close(self):
        self.bot.close()

def get_timer():
        timer ={}
        for name in ['born','alive','loading','scraping','total','finish']:
            if name =='born': timer[name] = datetime.now() 
            else: timer[name] = 0
        return timer.copy()

def post_result(data):
        print('posting')