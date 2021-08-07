import multiprocessing


def _wrapper(func, dic, *args, **kwargs):
    dic['result'] = func(*args, **kwargs)


def invoke_timeout(func, seconds, *args, **kwargs):
    m = multiprocessing.Manager()
    dic = m.dict()
    dic['result'] = None
    p = multiprocessing.Process(target=_wrapper, args=(func, dic) + args, kwargs=kwargs)
    p.start()
    p.join(seconds)
    p.kill()
    return dic['result']
