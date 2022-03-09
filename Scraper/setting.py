class Setting():
    WINTER, SUMMER, RETAKE = '1', '2', '3'
    SID, SEMESTER, YEAR, GROUP = 'sid', 'semester', 'year', 'group'

    def __init__(self, **kwargs):
        self.sid = None
        self.year = None
        self.semester = None
        self.group = None
        self.set_settings(**kwargs)

    def not_found_settings(self, *setting_names):
        return set(setting_names).difference(self.__dict__.keys())

    def set_settings(self, **kwargs) -> None:
        not_found = self.not_found_settings(*kwargs.keys())
        assert kwargs.keys() <= self.__dict__.keys(), (
            f'Following settings were not found: {not_found}\n')

        self.__dict__.update(kwargs)

    @staticmethod
    def requires_setting(*required_setting):
        def wrap(func):
            def wrapper(*args, **kwargs):

                setting = Setting.__find_setting(*args, **kwargs)
                assert setting is not None, (
                    f'failed to find settings within '
                    f'{func.__name__} arguments.')
                # replace with sets

                not_found = setting.not_found_settings(*required_setting)
                assert not len(not_found), (
                    f'Required data error, '
                    f'requested setting: {not_found} '
                    f'are not found in {func.__name__} ')

                missing = [
                    s for s in required_setting if setting.__dict__[s] is None]
                assert not len(missing), (
                    f'Required data error, '
                    f'{missing} is missing in {func.__name__}.')

                return func(*args, **kwargs)
            return wrapper
        return wrap

    @staticmethod
    def __find_setting(*args, **kwargs):
        try:
            setting = kwargs['setting'] if isinstance(
                kwargs['setting'], Setting) else None
            return setting
        except KeyError:
            for arg in [*args, *kwargs.values()]:
                if isinstance(arg, Setting):
                    return arg
            return None
