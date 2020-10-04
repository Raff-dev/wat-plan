import requests
from typing import List, Dict

from form_data import FORM_DATA
from Decorators import HasRequiredAttribute, TimeMeasure
from SoupParser import SoupParser

BASE_URL = 'https://s1.wcy.wat.edu.pl/ed1/'

LOGGED_URL_ADDON = 'logged_inc.php?'
GROUPS_URL_ADDON = '&mid=328'
GROUP_URL_ADDON = '&exv='
SEMESTER_URL_ADDON = '&iid='

WINTER, SUMMER, RETAKE = '1', '2', '3'
SID, SEMESTER, YEAR, GROUP = 'SID', 'SEMESTER', 'YEAR', 'GROUP'


class Scraper(HasRequiredAttribute):
    """
    Contains all the needed functionalities to
    obtain access to the schedule website,
    find, parse and post the schedule data
    to the WAT Plan API
    """

    def __init__(self, **kwargs):
        self.SID = None
        self.YEAR = None
        self.SEMESTER = None
        self.GROUP = None
        self.set_attributes(**kwargs)

    @property
    @HasRequiredAttribute.requires_attribute(SID, SEMESTER, YEAR)
    def groups_url(self) -> str:
        """
        Returns an url path of a site containing all groups
        of current year and semester.
        """

        return BASE_URL + LOGGED_URL_ADDON + self.SID + GROUPS_URL_ADDON +\
            SEMESTER_URL_ADDON + self.YEAR + self.SEMESTER

    @property
    @HasRequiredAttribute.requires_attribute(SID, SEMESTER, YEAR, GROUP)
    def group_url(self) -> str:
        """
        Returns an url path of a site containing current group's plan.
        """
        return self.groups_url + GROUP_URL_ADDON + self.GROUP

    @staticmethod
    def authenticate() -> str:
        """ Logs into website and obtains sid """

        # make sure the website isnt offline
        form = SoupParser.get_soup(BASE_URL).form
        login_url = BASE_URL + form['action']
        res = requests.post(login_url, data=FORM_DATA, verify=False)

        sid = form['action'][10:]
        return sid
        # make sure it has logged in succesfully
        # check the title

    @HasRequiredAttribute.requires_attribute(SID, SEMESTER, YEAR)
    def get_groups(self) -> List[str]:
        """gets all available groups of a given semester"""

        soup = SoupParser.get_soup(self.groups_url)
        aMenus = soup.find_all("a", class_='aMenu')

        groups = []
        for aMenu in aMenus:
            groups.append(aMenu.text)

        return groups

    @classmethod
    def get_all_group_names(cls, settings_list: List[Dict]):
        """
        """
        scraper = cls()
        sid = cls.authenticate()
        scraper.set_attributes(SID=sid)

        all_groups = {}
        for settings in settings_list:
            print(settings)
            scraper.set_attributes(**settings)
            groups = scraper.get_groups()
            all_groups[f'{settings[YEAR]}_{settings[SEMESTER]}'] = groups

        return all_groups


settings_list = [
    {YEAR: year, SEMESTER: semester}
    for year in ['2019', '2020']
    for semester in [WINTER, SUMMER, RETAKE]
]

if __name__ == '__main__':
    all_group_names = Scraper.get_all_group_names(settings_list)
    print(all_group_names)
