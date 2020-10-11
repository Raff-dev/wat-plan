from __future__ import annotations
import inspect
from threading import Lock


class Reporter():
    """
    Stores statuses, and errors of methods that are decorated as reported.
    """

    class ReportableValue():
        def __init__(self):
            self.lock = Lock()
            self.ongoing = 0
            self.succedeed = 0
            self.failed = 0
            self.errors = []

        def lock_required(self, func):
            def wrapper(self, *args, **kwargs):
                with self.lock:
                    res = func(*args, **kwargs)
                    return res
            return wrapper

        @property
        def finished(self):
            with self.lock:
                return self.ongoing + self.succedeed + self.failed

    def reset(self):
        self.__dict__.clear()

    def report(self):
        message = ''
        for reportable_value in self.__dict__.items():
            name, value = reportable_value
            with value.lock:
                message += (
                    f'\nReport on: {name} \n'
                    f'Ongoing: {value.ongoing}: \n'
                    f'Succededd: {value.succedeed}: \n'
                    f'Failed: {value.failed}: \n'
                )
                if value.failed > 0:
                    message += f'Errors: {[error for error in value.errors]}: \n'

        print(message)
        return message

    def get(self, reportable_value_name: str) -> Reporter.ReportableValue:
        if not reportable_value_name in self.__dict__.keys():
            self.__dict__.update(
                {reportable_value_name: Reporter.ReportableValue()})

        reportable_value = self.__dict__[reportable_value_name]
        return reportable_value

    @staticmethod
    def report_on(reportable_value_name):
        def wrapper(func):
            def wrapper(instance, *args, **kwargs):
                reporter = instance.reporter
                assert reporter and isinstance(
                    reporter, Reporter), "Reporter not found"

                reportable_value = reporter.get(reportable_value_name)

                try:
                    with reportable_value.lock:
                        print(f'ongoing {reportable_value_name}')
                        reportable_value.ongoing += 1
                    res = func(*args, **kwargs)
                    with reportable_value.lock:
                        reportable_value.succedeed += 1
                        reportable_value.ongoing -= 1
                        print(f'succeded {reportable_value_name}')
                    return res
                except Exception as e:
                    with reportable_value.lock:
                        reportable_value.failed += 1
                        reportable_value.ongoing -= 1
                        print(f'\nfailed {reportable_value_name}\n'
                              f'with {e}\n'
                              F'at {inspect.stack()}\n')
                        reportable_value.errors.append(e)
                    return None
            return wrapper
        return wrapper
