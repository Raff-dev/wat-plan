import requests
from bs4 import BeautifulSoup
from form_data import FORM_DATA

BASE_URL = 'https://s1.wcy.wat.edu.pl/ed1/'

LOGGED_URL_ADDON = 'logged_inc.php?'
GROUPS_URL_ADDON = '&MID=328'
GROUP_URL_ADDON = '&exv='
SEMESTER_URL_ADDON = '&iid='

WINTER, SUMMER, RETAKE = 1, 2, 3
SID, SEMESTER, YEAR, GROUP = 'sid', 'semester', 'year', 'group'


class Scraper:
    """


    """

    def requires_data(self, func, *required):
        for setting in required:
            assert self.settings[setting] is not None, setting + ' is missing'

        def wrapper(*args, **kwargs):
            return func(*args, **kwargs)
        return wrapper

    def __init__(self):
        self.sid = None
        self.settings = {
            SID: None,
            SEMESTER: None,
            YEAR: None,
            GROUP: None,
        }

    def set_settings(self, **kwargs):
        for key, value in kwargs.items():
            self.settings[key] = value

    @requires_data(SID, SEMESTER, YEAR)
    @property
    def groups_url(self) -> str:
        """
        Returns an url path of a site containing all groups
        of current year and semester.
        """

        return BASE_URL + LOGGED_URL_ADDON + self.settings[SID] + GROUPS_URL_ADDON +\
            SEMESTER_URL_ADDON + self.settings[YEAR] + self.settings[SEMESTER]

    @requires_data(SID, SEMESTER, YEAR, GROUP)
    @property
    def group_url(self) -> str:
        """
        Returns an url path of a site containing current group's plan.

        """
        return self.groups_url + GROUP_URL_ADDON + self.settings[GROUP]

    @requires_data(SID, SEMESTER, YEAR)
    def get_groups(self):
        """gets all available groups of a given semester"""
        soup = get_soup(self.groups_url)
        aMenus = soup.find_all(class_='aMenu')

        groups = []
        for aMenu in aMenus:
            groups.append(aMenu.get_attribute('innerText'))

        return groups

    def log_in(self):
        # make sure the website isnt offline
        res = requests.get(BASE_URL, verify=False)
        form = BeautifulSoup(res.text).form
        login_url = BASE_URL + form['action']
        res = requests.post(login_url, data=FORM_DATA, verify=False)

        self.sid = form['action'][10:]
        # make sure it has logged in succesfully
        # check the title


def get_soup(url):
    res = requests.get(url, verify=False)
    soup = BeautifulSoup(res.text)
    return soup


if __name__ == '__main__':
    s = Scraper()
    s.log_in()
    s.set_settings(YEAR=2020, SEMESTER=WINTER)
    groups = s.get_groups()
