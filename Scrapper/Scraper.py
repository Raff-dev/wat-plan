from webdriver_manager.chrome import ChromeDriverManager
from selenium.common.exceptions import NoSuchElementException
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.options import Options
from selenium import webdriver
from bs4 import BeautifulSoup
from threading import Thread
from datetime import date, timedelta, datetime
import pandas as pd
import numpy as np
import re
import time
import sys
from creds import username, password

class Scraper():
    def __init__(self):
        options = Options()
        # options.add_argument("--headless")
        self.bot = webdriver.Chrome(ChromeDriverManager().install(),options=options)
        self.in_groups=False
        self.scrape_ready=False
        self.test = False

    def __repr__(self):
        return self.bot

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
                    assert send_msg < 100, 'Could not find element: ' + element
                time.sleep(0.1)
        return el if len(el) > 1 else el[0]

    def login(self):
        bot = self.bot
        bot.get('https://s1.wcy.wat.edu.pl/ed1/')

        inputs = self.find('class name', 'inputChaLog')
        for i, k in zip(inputs, [username, password]):
            i.send_keys(k)

        self.find('css selector', 'input.inputLogL').click()

    def get_group_names(self):
        group_names = []
        aMenus = self.find('class name', "aMenu")
        for aMenu in aMenus:
            group_names.append(aMenu.get_attribute('innerText'))
        return group_names

    def get_to_groups(self, semester):
        self.in_groups=False
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

    def scrape_semester(self,group_name):
        scrape_start = datetime.now()
        assert self.in_groups, 'Groups page must be opened in order to scrape'
        group_link = self.find('link text', group_name)
        if group_link:
            group_link.click()
            self.scrape_ready = True
        page_loaded = datetime.now()
        print(f'Loading Page took {(page_loaded-scrape_start).seconds} seconds')

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

        print(f'Scraping {group_name} took {(datetime.now()-page_loaded).seconds} seconds')
        print(f'Total time: {(datetime.now()-scrape_start).seconds} seconds')
        return data

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

    def get_current_url(self):
        return self.bot.current_url

    def close(self):
        self.bot.close()