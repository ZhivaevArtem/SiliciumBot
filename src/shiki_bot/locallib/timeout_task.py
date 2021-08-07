from typing import Callable
import multiprocessing
import threading
import ctypes
import time


class TimeoutTask(object):
    def __init__(self, timeout, func):
        self._timeout = timeout
        self._func = func
        self._result = None

    def invoke(self, arg):
        p = multiprocessing.Process(target=self._func, args=(arg,))
        p.start()
        p.join(2)
        p.terminate()
        ret = self._result
        self._result = None
        return ret


def timeout(seconds: float):
    def decorator(func):
        def wrapper(*args, **kwargs):
            print('start')
            p = multiprocessing.Process(target=func, args=args, kwargs=kwargs)
            p.start()
            p.join(seconds)
            p.terminate()
            print('end')
            return None
        return wrapper
    return decorator


def eval_(expr, value):
    n = eval(expr)
    if type(n) in (int, float):
        value['result'] = n



# @timeout(2)
def eval_timeout(expr):
    print('start')
    m = multiprocessing.Manager()
    value = m.dict()
    value['result'] = None
    p = multiprocessing.Process(target=eval_, args=(expr, value))
    p.start()
    p.join(.3)
    p.terminate()
    print('end')
    return value['result']


def invoke_timeout(func, seconds, *args, **kwargs):
    m = multiprocessing.Manager()
    dic = m.dict()
    dic['result'] = None

    def wrapper(fun, d, *args, **kwargs):
        d['result'] = fun(*args, **kwargs)

    p = multiprocessing.Process(target=wrapper, args=(func, dic) + args, kwargs=kwargs)
    p.start()
    p.join(seconds)
    p.terminate()
    return dic['result']


if __name__ == '__main__':
    n = invoke_timeout(eval, .5, '123-3')
    print(n)
