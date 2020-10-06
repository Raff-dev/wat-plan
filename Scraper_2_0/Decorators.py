import time


def timer(repeat):
    def wrapper(func):
        def wrapper(*args, **kwargs):
            start = time.time()
            runs = [func(*args, **kwargs) for _ in range(repeat)]
            end = time.time()
            avg_time = (end-start)/len(runs)
            return runs[0], avg_time
        return wrapper
    return wrapper
