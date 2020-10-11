from typing import List, TypeVar
from threading import Lock, Semaphore


class SharedList():
    """
    Implements thread safe
    """

    def __init__(self, initial_values: List[TypeVar] = [], name: str = ''):
        self.name = name
        self.reset(initial_values)

    def __repr__(self):
        with self.lock:
            return str(self.list)

    def reset(self, initial_values: List[TypeVar] = []) -> None:
        self.list = initial_values
        self.semaphore = Semaphore(len(initial_values))
        self.lock = Lock()

    def append(self, value: TypeVar) -> None:
        with self.lock:
            print(f'appending {self.name}')
            self.list.append(value)
            self.semaphore.release()

    def pop(self, index: int = 0) -> TypeVar:
        self.semaphore.acquire()
        with self.lock:
            print(f'poping {self.name}')
            value = self.list.pop(index)
            return value

    @property
    def length(self):
        return len(self.list)
