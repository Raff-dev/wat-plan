from Scraper import Scraper
import unittest


class TestScrapper(unittest.TestCase):

    def test_required_setting(self):
        scraper = Scraper()


if __name__ == '__main__':
    unittest.main()
