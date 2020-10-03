
def requires_settings(cls, *required_settings):
    def wrap(func):
        def wrapper(self, *args, **kwargs):

            # replace with sets

            not_allowed = self.is_allowed(*required_settings)
            assert not len(not_allowed), (
                f'Required data error, '
                f'requested settings: {not_allowed} '
                f'are not allowed in {func.__name__} ')

            missing = [
                s for s in required_settings if self.__dict__[s] is None]
            assert not len(missing), (
                f'Required data error, '
                f'{missing} is missing in {func.__name__}.')

            return func(self, *args, **kwargs)
        return wrapper
    return wrap
