from Scraper import Scraper
from Decorators import HasRequiredAttribute
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


class TestHasRequiredAttribute(unittest.TestCase):
    TEST1 = 'test1'
    TEST2 = 'test2'

    def test_not_allowed_attributes(self):

        class TestNotAllowedAttributes(HasRequiredAttribute):
            def __init__(self):
                self.test1 = None

        naa = TestNotAllowedAttributes()
        not_allowed = naa.not_allowed_attributes(self.TEST1)
        self.assertEqual(not_allowed, set())

        not_allowed = naa.not_allowed_attributes(
            self.TEST1, *[self.TEST1 for _ in range(21)])
        self.assertEqual(not_allowed, set())

        not_allowed = naa.not_allowed_attributes(self.TEST1, self.TEST2)
        self.assertEqual(not_allowed, set([self.TEST2]))

        not_allowed = naa.not_allowed_attributes(self.TEST2, self.TEST2)
        self.assertEqual(not_allowed, set([self.TEST2, ]))

        not_allowed = naa.not_allowed_attributes(self.TEST2)
        self.assertEqual(not_allowed, set([self.TEST2]))

    def test_requires_attribute(self):

        class TestRequiresAttribute(HasRequiredAttribute):

            def __init__(self):
                self.test1 = None
                self.test2 = None

            @HasRequiredAttribute.requires_attribute(self.TEST1, self.TEST2)
            def allowed_attributes_func(self): ...

            @HasRequiredAttribute.requires_attribute('asd', 'dss')
            def not_allowed_attributes_func(self): ...

        ra = TestRequiresAttribute()
        with self.assertRaises(AssertionError):
            ra.allowed_attributes_func()

        with self.assertRaises(AssertionError):
            ra.not_allowed_attributes_func()

        ra.test1 = self.TEST1
        ra.test2 = self.TEST2
        try:
            ra.allowed_attributes_func()
        except AssertionError as e:
            self.fail("requires_attribute raised AssertionError unexpectedly")
        except Exception as e:
            self.fail(
                f'{ra.allowed_attributes_func.__name__} ' +
                f'raised {type(e).__name__} unexpectedly')


if __name__ == '__main__':
    unittest.main()
