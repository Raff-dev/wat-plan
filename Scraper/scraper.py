from requests.packages.urllib3.exceptions import InsecureRequestWarning
from typing import List
import logging
import requests
import os

from bs4 import BeautifulSoup

from setting import Setting

_logger = logging.getLogger(__name__)

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
    find and gather desired date
    """

    @staticmethod
    def get_soup(url: str, session: requests.Session = None) -> BeautifulSoup:
        """Gets page content and converts it into beautifulsoup object"""

        get = session.get if session else requests.get
        res = get(url, verify=False)
        assert res.ok and res.text, (
            f'Failed to obtain soup\n'
            f'request status: {res.status_code}\n'
            f'request url: {res.url}\n'
            f'result length: {len(res.text)}')

        _logger.info(res)
        soup = BeautifulSoup(res.text, features="lxml")
        return soup

    @staticmethod
    @Setting.requires_setting(Setting.SID, Setting.SEMESTER, Setting.YEAR)
    def get_groups_url(setting: Setting) -> str:
        """
        Returns an url path of a site containing all groups
        of year and semester specified within Setting object.
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
        """Logs into website and obtains sid"""

        soup = Scraper.get_soup(BASE_URL)
        error = soup.find_all('b', class_='bError')
        assert not error, 'The service is unavailable'
        assert soup.form, 'The form was not found'

        login_url = BASE_URL + soup.form['action']

        res = requests.post(login_url, verify=False, data={
            'userid': os.getenv('SCRAPER_USER_ID'),
            'password': os.getenv('SCRAPER_PASSWORD'),
            'formname': 'login',
            'default_fun': 1,
        })
        sid = soup.form['action'][10:]
        return sid

    @staticmethod
    @Setting.requires_setting(Setting.SID, Setting.SEMESTER, Setting.YEAR)
    def get_group_names(setting: Setting) -> List[str]:
        """gets all available groups of a given semester"""

        url = Scraper.get_groups_url(setting)
        soup = Scraper.get_soup(url)
        group_nodes = soup.find_all("a", class_='aMenu')
        group_names = [node.text for node in group_nodes]
        return group_names
