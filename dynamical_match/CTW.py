# -*- coding: utf-8 -*-
"""
Created on Wed Oct 18 17:07:18 2017

@author: xiaoy
"""
import numpy as np
from fastdtw import fastdtw
from functools import reduce
from scipy.linalg import eigh

def mm(*args):
    return reduce(np.dot, args, 1)

def ctw(A, B, z, tol=1e-8):
    '''
    Params:
    -------
        A: m-by-l matrix
        B: n-by-k matrix

    Returns:
    --------
        Qa: m-by-h matrix
        Qb: n-by-h matrix
        Va: l-by-z matrix
        Vb: k-by-z matrix
    '''
    # Va.T x A.T x Qa: h x z
    # Vb.T x B.T x Qb: h x z

    m = A.shape[0]
    n = B.shape[0]

    h = max(m, n)
    Qa = np.eye(m, h)
    Qb = np.eye(n, h)

    last_err = 0

    while True:
        Ah = np.dot(Qa.T, A)
        Bh = np.dot(Qb.T, B)
        # Ah: h-by-l
        # Bh: h-by-k

        Va, Vb = cca(Ah, Bh, z)

        Ac = np.dot(A, Va)
        Bc = np.dot(B, Vb)

        Qa[:] = 0
        Qb[:] = 0

        _, path = fastdtw(Ac, Bc)
        # path: 2-by-h

        h = len(path)
        Qa = np.zeros((m, h))
        Qb = np.zeros((n, h))
        Qa[tuple(x[0] for x in path), range(0, h)] = 1
        Qb[tuple(x[1] for x in path), range(0, h)] = 1

        err = np.linalg.norm(mm(Qa.T, A, Va) - mm(Qb.T, B, Vb), 2)
        print(err)

        if abs(err - last_err) <= tol:
            break

        last_err = err

    return Qa, Qb, Va, Vb

def cca(A, B, n_components):
    '''
    Params:
    ------
        A: h-by-l matrix
        B: h-by-k matrix
        n_components: int, should be less than min(l, k)

    Returns:
    -------
        Va: l-by-z matrix
        Vb: k-by-z matrix

        Va, Vb satisfy the following equation:
            A.T B Vb = lbd * A.T A Va
            B.T A Vb = lbd * B.T B Vb
    '''

    h, l = A.shape
    k = B.shape[1]

    t = l + k

    LHS = np.zeros((t, t))
    RHS = np.zeros((t, t))

    LHS[:l, l:] = np.dot(A.T, B)
    LHS[l:, :l] = np.dot(B.T, A)

    RHS[:l, :l] = np.dot(A.T, A)
    RHS[l:, l:] = np.dot(B.T, B)

    W, V = eigh(LHS, RHS, eigvals=(t-n_components, t-1))

    Va = V[:l]
    Vb = V[l:]

    return Va, Vb