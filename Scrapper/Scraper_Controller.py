import Scraper

scraper = Scraper.Scraper()
scraper.login()
scraper.get_to_groups('letni')
group_names = scraper.get_group_names()
scraper.find('link text', next(group_names)).click()
scraper.scrape()
