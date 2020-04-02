import Scraper
import numpy as np
import time
from threading import Thread
from multiprocessing import Process, Queue
from datetime import datetime, timedelta
import atexit

class ScrapeSquad():
    def __init__(self, pool_size = 1,semesters = ['letni']):
        self.semesters = semesters
        self.pool_size = pool_size
        self.leader = Scraper.Scraper(bot_init = True, headless = False)
        self.squad= None
        self.squad_handle= {}
        self.url = {}
        self.groups = {}
        self.queue = Queue()
        self.groups_scraped = 0
        self.groups_count = 0
        self.scrapers_finished = 0
        self.failures = 0
        self.timer = Scraper.get_timer()
        atexit.register(self.finish)

    def run(self):
        try:
            self.set_up()
            self.begin()
            self.gather_data()
        except (SystemExit, KeyboardInterrupt) as e:
            printf(f'Squad: Exception occured \n{e}')
            self.finish()

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

    def create_squad(self,*args):
        self.squad = [[Scraper.Scraper(semester = sem, index = index,headless=False)
            for index in range(self.pool_size)] for sem in self.semesters]

    def begin(self):
        for semester_squad in self.squad:
            for scraper in semester_squad:
                scraper.set_up(url = self.url[scraper.semester], pool_size=self.pool_size)
                scraper.queue = self.queue
                process =  Process(target=scraper.run)
                self.squad_handle[scraper] = process
                process.start()

    def gather_data(self):
        while True:
            msg = self.queue.get()
            if 'finished' in msg.keys() or 'failure' in msg.keys():
                if 'failure' in msg.keys():
                    self.failures+=1
                self.scrapers_finished+=1
                if self.scrapers_finished == self.pool_size*len(self.semesters):
                    break
            else:
                self.groups_scraped+=1
                self.timer['loading'] += msg['loading']
                self.timer['scraping'] += msg['scraping']
                self.timer['total'] += msg['total']
                av_loading = self.timer['loading']/self.groups_scraped
                av_scraping = self.timer['scraping']/self.groups_scraped
                av_total = self.timer['total']/self.groups_scraped
                group = msg['group']
                total =  msg['total']
                index = msg['index']
                estimated = av_total*self.groups_count/self.pool_size
                printf(f'Scraper {index} finished {group} in {total}')
                printf(f'Groups scraped {self.groups_scraped}/{self.groups_count}')
                printf(f'Loading average: {av_loading}')
                printf(f'Scraping average: {av_scraping}')
                printf(f'Total average: {av_total}')
                printf(f'Estimated Scraping time: {int(estimated.seconds/60)}m {estimated.seconds%60}s')
                printf(f'Finished: {self.scrapers_finished}/{self.pool_size} Failures: {self.failures}\n')
        finished = self.timer['finish'] = datetime.now() - self.timer['born']
        printf(f'Scraping completed in {finished}')
    
    def finish(self):
        printf(f'Groups scraped {self.groups_scraped}/{self.groups_count}')
        printf(f'Finished: {self.scrapers_finished}/{self.pool_size} Failures: {self.failures}\n')



def printf(content):
    print(content,flush=True)

if __name__ == '__main__':
    squad = ScrapeSquad(pool_size = 4)
    squad.run()
    