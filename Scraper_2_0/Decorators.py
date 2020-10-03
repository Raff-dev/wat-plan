class HasRequiredAttribute():

    def not_allowed_attributes(self, *attribute_names):
        return set(attribute_names).difference(self.__dict__.keys())

    @staticmethod
    def requires_attribute(*required_attribute):
        def wrap(func):
            def wrapper(self, *args, **kwargs):

                # replace with sets

                not_allowed = self.not_allowed_attributes(*required_attribute)
                assert not len(not_allowed), (
                    f'Required data error, '
                    f'requested attribute: {not_allowed} '
                    f'are not allowed in {func.__name__} ')

                missing = [
                    s for s in required_attribute if self.__dict__[s] is None]
                assert not len(missing), (
                    f'Required data error, '
                    f'{missing} is missing in {func.__name__}.')

                return func(self, *args, **kwargs)
            return wrapper
        return wrap
