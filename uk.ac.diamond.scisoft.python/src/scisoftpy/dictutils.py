###
# Copyright 2011 Diamond Light Source Ltd.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
###

# coding=utf-8

def _maketranslator():
    '''
    Make a translator to sanitise names for python variable by replacing characters that are not
    ASCII letters, digits or '_' (underscore) with an underscore
    '''
    from string import ascii_letters, digits, printable, maketrans
    safechars = '_' + ascii_letters + digits
    trans = list(printable)
    for i,t in enumerate(trans):
        if not t in safechars:
            trans[i] = '_'
    tostrs = ''.join(trans)
    return maketrans(printable, tostrs)

_translator = _maketranslator()

_method_names = ['metadata', 'clear', 'popitem', 'has_key', 'keys', 'fromkeys', 'get', 'copy', 'setdefault', 'update', 'pop',
                 'values', 'items', 'iterkeys', 'itervalues', 'iteritems', 'append', 'extend', 'index', 'remove']

def sanitise_name(text):
    '''
    Sanitise name:
        if text is metadata then prepend with an underscore
        translate dodgy characters to underscores
        if text starts with a digit then prepend with an underscore
        if text is a reserved method name then prepend with an underscore
        if text starts with double underscores then raise error
    '''
    from string import digits
    sane = text.translate(_translator)
    if sane[0] in digits:
        print "First character of '%s' is a digit so prepending an underscore" % sane
        sane = '_' + sane
    if sane in _method_names:
        sane = '_' + sane
        print "'%s' is a reserved method name so prepending an underscore" % sane
    if sane.startswith('__'):
        raise ValueError, "Cannot use a name that starts with double underscores: %s" % sane

    return sane

def make_safe(items):
    '''
    Make a list of key/value tuples safe by sanitising keys
    and make them unique
    '''
    if items is None or len(items) == 0:
        return []

    items = zip(*items) # transform to two lists
    keys = [sanitise_name(str(k)) for k in items[0]]
    vals = items[1]

    lk = len(keys)
    sk = set(keys)
    ls = len(sk)
    if ls < lk:
        # some headers are not unique so append a number
        uk = dict(zip(sk, [0]*ls))
        for i,k in enumerate(keys):
            n = uk[k]
            if n > 0:
                newkey = keys[i] + str(n)
                while newkey in uk:
                    n += 1
                    newkey = keys[i] + str(n)
                print "Replacing duplicate key '%s' with '%s'" % (k, newkey)
                keys[i] = newkey
            uk[k] = n+1
    
    return zip(keys, vals)

from _external.ordereddict import OrderedDict as _odict

class ListDict(_odict):
    '''
    Combined list/ordered dictionary class. Keys to the dictionary are exposed as attributes.
    This supports all dictionary methods, pop, append, extend, index, remove and del
    '''
    def __init__(self, data=None, lock=False):
        '''
        A dictionary or list of tuples of key/value pairs. If lock=True,
        keys cannot be reassigned without first deleting the item
        '''
#        super(ListDict, self).__setattr__('__lock', lock)
        _odict.__setattr__(self, '__lock', lock)
        #self.__lock = lock #setattr__(self, '__lock', lock)
        if isinstance(data, dict):
            data = [ i for i in data.items() ]
        data = make_safe(data)
        _odict.__init__(self, data)
        if data:
            self.__dict__.update(data)

    def _replacedata(self, data):
        _odict.clear(self)
        if isinstance(data, dict):
            data = [ i for i in data.items() ]
        data = make_safe(data)
        _odict.update(self, data)
        if data:
            self.__dict__.update(data)

    def __getitem__(self, key):
        '''
        Key can be a number (integer) in which case return value at index of key list
        '''
        from types import StringType, IntType
        if type(key) is IntType:
            if key > len(self):
                raise IndexError, 'Key was too large'
            key = self.keys()[key]

        if type(key) is StringType:
            return super(_odict, self).__getitem__(key)
        else:
            raise KeyError, 'Key was not a string or integer'

    def __setitem__(self, key, value):
        '''
        Key can be a number (integer) in which case set value at index of key list
        '''
        from types import StringType, IntType
        if type(key) is IntType:
            if key > len(self):
                raise IndexError, 'Key was too large'
            key = _odict.keys(self)[key]

        if type(key) is StringType:
            key = sanitise_name(key)
            if key in self and _odict.__getattribute__(self, '__lock'):
                raise KeyError, 'Dictionary is locked, delete item to reassign to key'
            _odict.__setitem__(self, key, value)
            _odict.__setattr__(self, key, value)
        else:
            raise KeyError, 'Key was not a string or integer'

    def __delitem__(self, key):
        '''
        Key can be a number (integer) in which case set value at index of key list
        '''
        from types import StringType, IntType
        if type(key) is IntType:
            if key > len(self):
                raise IndexError, 'Key was too large'
            key = _odict.keys(self)[key]

        if type(key) is StringType:
            _odict.__delitem__(self, key)
            _odict.__delattr__(self, key)
        else:
            raise KeyError, 'Key was not a string or integer'

    def __setattr__(self, key, value):
        from types import StringType
        if type(key) is not StringType:
            raise KeyError, 'Key was not a string or integer'

        if key in self and _odict.__getattribute__(self, '__lock'):
            raise KeyError, 'Dictionary is locked, delete item to reassign to key'

        _odict.__setattr__(self, key, value)
        if key in ['_OrderedDict__end', '_OrderedDict__map'] or key.startswith('__'):
            return # ignore internal attributes used by ordereddict implementation

        _odict.__setitem__(self, key, value)

    def __delattr__(self, key):
        self.__delitem__(key)

    def __str__(self):
        s = ""
        for k in self: #_odict.__iter__(self):
            s += "('" + k + "', " + str(self[k]) + "), "
        if len(s) > 0:
            s = s[:-2]
        return _odict.__class__(_odict(self)).__name__ + "([" + s + "])"

    def pop(self, i=-1):
        '''
        Remove item at i-th index and return it
        '''
        from types import IntType
        if type(i) is not IntType:
            raise IndexError, "Index must be an integer"
        r = self.__getitem__(i)
        self.__delitem__(i)
        return r

    def append(self, item):
        '''
        Add item on to end of list. Item must be one of following:
            * a dictionary (only first key/value pair used)
            * a list or tuple with more than one item (otherwise single item is used as an object for next criterion)
            * an object with a 'name' attribute
        '''
        from types import TupleType, ListType, DictType
        if type(item) is DictType:
            if len(item) > 1:
                print "Only adding first item in dictionary"
            if len(item) > 0:
                item = item.popitem() # fall through with key/value pair
        if type(item) is TupleType or type(item) is ListType:
            if len(item) > 1:
                self[item[0]] = item[1:]
                return
            else:
                item = item[0]
        if hasattr(item, 'name'):
            self[item.name] = item
            return
        raise ValueError, "Item is not a sequence or has no 'name' attribute"

    def extend(self, items):
        '''
        Add items in iterable to end of list
        '''
        from types import DictType
        if type(items) is DictType:
            for i in items.items():
                self.append(i)
        else:
            for i in items:
                self.append(i)

    def index(self, value):
        '''
        Return index of first occurrence of value  
        '''
        for k,v in self.items():
            if v == value:
                return self.keys().index(k)
        raise ValueError, "Value not found"

    def remove(self, value):
        '''
        Remove first occurrence of value
        '''
        for k,v in self.items():
            if v == value:
                del self[k]
                return
        raise ValueError, "Value not found"

class DataHolder(ListDict):
    '''
    This class holds data and metadata from a single data file
    
    Arguments:
    data        -- a list of tuples where each tuple is a string, NumPy array pair
    metadata    -- a list of tuples where each tuple is a string, object pair

    Data can be accessed in three ways:
        1. as an attribute
        2. as a dictionary value
        3. as a list item
    Metadata can be accessed in a similar manner though from an attribute called metadata
    '''
    def __init__(self, data=None, metadata=None):
        ListDict.__init__(self, data)
        self.metadata = ListDict(metadata)

def _test_make_safe():
    # test make_safe
    data = [ ('a', 0), ('a', 1), ('a.b', -2), ('a$asd923', -4), (u'a£b£.123', -7) ]
    safe_data = make_safe(data)
    expected = [ ('a', 0), ('a1', 1), ('a_b', -2), ('a_asd923', -4), ('a_b_.123', -7) ]
    for a, b in zip(safe_data, expected):
        if a[0] != b[0] and a[1] != b[1]:
            print("Actual %s, %d does not match expected %s, %d" % (a, b))

def _test_setting_listdict():
#    d = ListDict({'a': 1, 'c':-2})
    d = ListDict([('a', 1), ('c',-2)])
    print d.a
    print len(d)
    d['d'] = 0.7
    d.b = 0.5
#    d['b'] = 0.5
    print d.b
    print len(d)
    print d.keys()
    print d
    print d.items()
    d[1] = 2.2
    print d
    d[1] = 2.5
#    print d[2]
    del d['d']
    print d.pop()
    print d
    del d.c
    print d
    print d.keys()
    d.append({'e': 2.3})
    d.append(['f', 2.3])
    d.append(['g', 1])
    print d.index(1)
    d.remove(1)
    class testObj():
        def __str__(self):
            return '"test obj"'

    try:
        g = testObj()
        d.append(g)
    except:
        print 'Exception raised successfully'

    g.name = 'blah'
    d.append(g)
    print d

    try:
        d.extend(2.3)
    except Exception, e:
        print 'Exception raised successfully ' + str(e)

    d.extend({'h': -2})
    d.extend([('i',-2.5)])
    print d

    ld = ListDict([('a', 1), ('c',-2)], lock=True)
    try:
        ld['c'] = 3
    except Exception, e:
        print 'Exception raised successfully ' + str(e)

    try:
        ld.c = 3
    except Exception, e:
        print 'Exception raised successfully ' + str(e)

    try:
        ld[0] = 3
    except Exception, e:
        print 'Exception raised successfully ' + str(e)

    del ld.c
    ld.c = 3
    print ld
    ld.__c = 3
    print ld

#    from pprint import pprint
#    pprint(dir(ld)); print
    ld._replacedata([('a', -1), ('c',-2.5)])
    print ld

if __name__ == "__main__":
    _test_setting_listdict()

