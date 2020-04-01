import Scraper

scraper = Scraper.Scraper()
scraper.login()
scraper.get_to_groups('letni')
group_names = scraper.get_group_names()
data = {}
for group in group_names:
    data[group] = scraper.scrape_semester(group)

def get(group,month,day):
    for k,v in data[group][f'2020-{month}-{day}'].items():
        print(k,v)
