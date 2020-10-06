import requests
from requests.packages.urllib3.exceptions import InsecureRequestWarning

from typing import List, Dict
from bs4 import BeautifulSoup
import time

from form_data import FORM_DATA
from Decorators import timer
from SoupParser import SoupParser
from Setting import Setting

BASE_URL = 'https://s1.wcy.wat.edu.pl/ed1/'

LOGGED_URL_ADDON = 'logged_inc.php?'
GROUPS_URL_ADDON = '&mid=328'
GROUP_URL_ADDON = '&exv='
SEMESTER_URL_ADDON = '&iid='


class Scraper():
    requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

    """
    Contains all the needed functionalities to
    obtain access to the schedule website,
    find, parse and post the schedule data
    to the WAT Plan API
    """

    @staticmethod
    def get_soup(url: str) -> BeautifulSoup:
        res = requests.get(url, verify=False)
        assert res.status_code == requests.codes.ok, (
            f'Failed to obtain soup, request status: {res.status_code}')

        # --- TEST PRINT ---
        print(res)
        soup = BeautifulSoup(res.text, features="lxml")
        return soup

    @staticmethod
    @Setting.requires_setting(Setting.SID, Setting.SEMESTER, Setting.YEAR)
    def get_groups_url(setting: Setting) -> str:
        """
        Returns an url path of a site containing all groups
        of current year and semester.
        """

        return BASE_URL + LOGGED_URL_ADDON + setting.sid + GROUPS_URL_ADDON +\
            SEMESTER_URL_ADDON + setting.year + setting.semester

    @staticmethod
    @Setting.requires_setting(Setting.SID, Setting.SEMESTER, Setting.YEAR, Setting.GROUP)
    def get_group_url(setting: Setting) -> str:
        """
        Returns an url path of a site containing current group's plan.
        """
        return Scraper.get_groups_url(setting) + GROUP_URL_ADDON + setting.group

    @staticmethod
    def authenticate() -> str:
        """ Logs into website and obtains sid """

        # make sure the website isnt offline
        form = Scraper.get_soup(BASE_URL).form
        login_url = BASE_URL + form['action']
        res = requests.post(login_url, data=FORM_DATA, verify=False)

        sid = form['action'][10:]
        return sid
        # make sure it has logged in succesfully
        # check the title

    @staticmethod
    @Setting.requires_setting(Setting.SID, Setting.SEMESTER, Setting.YEAR)
    def get_group_names(setting: Setting) -> List[str]:
        """gets all available groups of a given semester"""

        url = Scraper.get_groups_url(setting)
        soup = Scraper.get_soup(url)
        aMenus = soup.find_all("a", class_='aMenu')

        groups = []
        for aMenu in aMenus:
            groups.append(aMenu.text)

        return groups

    @staticmethod
    def get_all_group_names(settings_list: List[Dict]):
        """
        """
        sid = Scraper.authenticate()
        setting = Setting(sid=sid)

        all_groups = {}
        for settings in settings_list:
            setting.set_settings(**settings)
            groups = Scraper.get_group_names(setting)
            all_groups[f'{settings.year}_{settings.semester}'] = groups

        return all_groups
