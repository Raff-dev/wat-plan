from __future__ import annotations
from functools import wraps
import inspect
from threading import Lock
import time


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
            self.time = []

        @property
        def finished(self) -> int:
            with self.lock:
                return self.ongoing + self.succedeed + self.failed

    def reset(self):
        self.__dict__.clear()

    def report(self) -> str:
        message = ''
        for reportable_value in self.__dict__.items():
            name, value = reportable_value
            with value.lock:
                message += (
                    f'\nReport on: {name} \n'
                    f'Ongoing: {value.ongoing}: \n'
                    f'Succededd: {value.succedeed}: \n'
                    f'Failed: {value.failed}: \n'
                    f'Total time: {sum(value.time)}: \n'
                    f'Average time: {sum(value.time)/len(value.time)}: \n'
                )
                if value.failed > 0:
                    message += f'Errors: {[error for error in value.errors]}: \n'

        print(message)
        return message

    def get(self, func: str) -> Reporter.ReportableValue:
        reportable_value_name = func.__name__
        if not reportable_value_name in self.__dict__.keys():
            self.__dict__.update(
                {reportable_value_name: Reporter.ReportableValue()})

        reportable_value = self.__dict__[reportable_value_name]
        return reportable_value

    @staticmethod
    def observe(func):
        @wraps(func)
        def wrapper(instance, *args, **kwargs):
            reporter = instance.reporter
            assert reporter and isinstance(
                reporter, Reporter), "Reporter not found"

            reportable_value = reporter.get(func)
            start = time.time()
            try:
                with reportable_value.lock:
                    reportable_value.ongoing += 1
                res = func(instance, *args, **kwargs)
                with reportable_value.lock:
                    reportable_value.succedeed += 1
                return res
            except Exception as e:
                with reportable_value.lock:
                    reportable_value.failed += 1
                    print(f'\nfailed {func.__name__}\n'
                          f'with {e}\n'
                          F'at {func.__name__}\n')
                    reportable_value.errors.append(e)
                return None
            finally:
                with reportable_value.lock:
                    reportable_value.ongoing -= 1
                    reportable_value.time.append(time.time()-start)
        return wrapper
