import time


class HasRequiredAttribute():

    def not_found_attributes(self, *attribute_names):
        return set(attribute_names).difference(self.__dict__.keys())

    def set_attributes(self, **kwargs) -> None:
        not_found = self.not_found_attributes(*kwargs.keys())
        assert kwargs.keys() <= self.__dict__.keys(), (
            f'Following attributes were not found: {not_found}\n')

        self.__dict__.update(kwargs)

    @staticmethod
    def requires_attribute(*required_attribute):
        def wrap(func):
            def wrapper(self, *args, **kwargs):

                # replace with sets

                not_found = self.not_found_attributes(*required_attribute)
                assert not len(not_found), (
                    f'Required data error, '
                    f'requested attribute: {not_found} '
                    f'are not found in {func.__name__} ')

                missing = [
                    s for s in required_attribute if self.__dict__[s] is None]
                assert not len(missing), (
                    f'Required data error, '
                    f'{missing} is missing in {func.__name__}.')

                return func(self, *args, **kwargs)
            return wrapper
        return wrap


class TimeMeasure():

    def __init__(self, func):
        self.func = func

    def __call__(self, *args):
        start = time.time()
        result = self.func(*args)
        end = time.time()
        return end-start, result
