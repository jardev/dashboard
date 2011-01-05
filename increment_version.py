#!/usr/bin/python
import sys

vl = open(sys.argv[1], 'r').read().split('-')
vl[2] = str(int(vl[2])+1)
open(sys.argv[1], 'w').write('-'.join(vl))
