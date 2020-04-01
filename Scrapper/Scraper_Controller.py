import Scraper
import numpy as np
import time
from threading import Thread
from multiprocessing import Process, Queue
from datetime import datetime

class Scrape_Squad():
    def __init__(self,pool_size,semesters=None):
        self.semesters = semesters if semesters else ['letni','zimowy']
        self.pool_size = pool_size
        self.leader = Scraper.Scraper(bot_init=True)
        self.squad=None
        self.url = {}
        self.groups = {}
        self.queue = Queue()
        self.groups_scraped = 0
        self.groups_count = 0
        self.scrapers_finished =0
        self.timer = Scraper.get_timer()

    def create_squad(self,*args):
        self.squad = [[Scraper.Scraper(semester = sem, index = index,headless=False)
            for index in range(self.pool_size)] for sem in self.semesters]

    def set_up(self):
        self.leader.login()
        self.create_squad()
        for semester in self.semesters:
            self.leader.get_to_groups(semester)
            self.leader.get_groups()
            self.url[semester] = self.leader.url[semester]
            self.groups[semester] = self.leader.groups
            self.groups_count += len(self.leader.groups)
        self.leader.close()

    def begin(self):
        for semester_squad in self.squad:
            for scraper in semester_squad:
                scraper.set_up(url = self.url[scraper.semester], pool_size=self.pool_size)
                scraper.queue = self.queue
                Process(target=scraper.run).start()    

    def gather_data(self):
        while True:
            msg = self.queue.get()
            if 'finished' in msg.keys():
                self.scrapers_finished+=1
                if self.scrapers_finished == self.pool_size*len(self.semesters):
                    break
            self.groups_scraped+=1
            self.timer['loading'] += msg['loading']
            self.timer['scraping'] += msg['scraping']
            self.timer['total'] += msg['total']
            av_loading = self.timer['loading']/self.groups_scraped
            av_scraping= self.timer['scraping']/self.groups_count
            av_total = self.timer['total']/self.groups_count
            group = msg['group']
            total = msg['total']
            index = msg['index']
            print(f'Scraper {index} finished {group} in {total}')
            print(f'Groups scraped {self.groups_scraped}/{self.groups_count}')
            print('Average Time:')
            print(f'Loading: {av_loading}')
            print(f'Scraping: {av_scraping}')
            print(f'Total: {av_total}')
            print('')
        finished = self.timer['finish'] = datetime.now() - self.timer['born']
        print(f'Scraping completed in {finished}')

if __name__ == '__main__':
    squad = Scrape_Squad(pool_size = 10, semesters=['letni'])
    squad.set_up()
    squad.begin()
    squad.gather_data()