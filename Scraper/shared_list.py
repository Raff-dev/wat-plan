from typing import List, TypeVar
from copy import copy
from threading import Lock, Semaphore


class SharedList():
    """    Implements thread safe list    """

    def __init__(self, initial_values: List = None):
        self.reset(initial_values if initial_values else [])

    def __repr__(self) -> List:
        with self.lock:
            return str(self.list)

    def reset(self, initial_values: List = None) -> None:
        self.list = initial_values if initial_values else []
        self.semaphore = Semaphore(len(self.list))
        self.lock = Lock()

    def append(self, value: TypeVar) -> None:
        with self.lock:
            self.list.append(copy(value))
            self.semaphore.release()

    def pop(self, index: int = 0) -> TypeVar:
        self.semaphore.acquire()
        with self.lock:
            value = self.list.pop(index)
            return copy(value)

    @property
    def length(self) -> int:
        with self.lock:
            return len(self.list)
