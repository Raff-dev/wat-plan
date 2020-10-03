from Scraper import *
from Decorators import requires_settings
import unittest


class TestScrapper(unittest.TestCase):

    def test_set_settings(self):
        scraper = Scraper()
        string = 'string'

        scraper.set_settings(GROUP=string)
        self.assertEqual(scraper.GROUP, string)

        scraper = Scraper()
        string = 'string2'
        scraper.set_settings(YEAR=string, SEMESTER=string, GROUP=string)
        self.assertEqual(scraper.YEAR, string)
        self.assertEqual(scraper.SEMESTER, string)
        self.assertEqual(scraper.GROUP, string)

    def test_invalid_argument_set_settings(self):
        with self.assertRaises(AssertionError):
            scraper = Scraper()
            scraper.set_settings(test='test')

    def test_requires_settings(self):

        class RequiresSettingTest(Scraper):

            @requires_settings(YEAR, GROUP)
            def test_allowed_func(self):
                pass

            @requires_settings('test', 'test')
            def test_not_allowed_func(self):
                pass

        scraper = RequiresSettingTest()
        with self.assertRaises(AssertionError):
            scraper.test_allowed_func()

        with self.assertRaises(AssertionError):
            scraper.test_not_allowed_func()

        scraper.set_settings(YEAR='string', GROUP='string')
        try:
            scraper.test_allowed_func()
        except AssertionError as e:
            self.fail("requires_setting raised AssertionError unexpectedly")
        except Exception as e:
            self.fail(
                f'{scraper.test_allowed_func.__name__} ' +
                f'raised {type(e).__name__} unexpectedly')


if __name__ == '__main__':
    unittest.main()
