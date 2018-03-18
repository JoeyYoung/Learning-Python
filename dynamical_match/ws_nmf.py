import numpy as np
from scipy.special import lambertw
from functools import reduce

def mm(*args):
  return reduce(np.dot, args, 1)

# input S, K
def ws_nmf(S, K, epsilon=1e-10, l=3e-3, tol=1e-3):
  last_err = 0
  N = S.shape[0]    # row / line
  B = np.random.randint(1, 10000, (N, K)) / 10000
  W = np.random.randint(1, 10000, (K, K)) / 10000

  while True:
    BTB = mm(B.T, B)
    # hadmard mul == 两者对应位置的积 ---- A*B
    W = W * (mm(B.T, S, B) / (mm(BTB, W, BTB) + epsilon))
    B_tilde = B * 0.5 * (1 + (mm(S, B, W) / (mm(B, W, BTB, W) + epsilon)))

    #for i in range(N):
    #  B[i] = (- B_tilde[i] / l) / np.real(lambertw(- B_tilde[i] * np.exp(1 + 0.1 / l) / l))
    B = B_tilde

    err = np.linalg.norm(S - mm(B, W, B.T), 2)
    if abs(last_err - err) <= tol:
      print(err)
      break
    last_err = err

  return B, W

if __name__ == '__main__':  # the module name, convenienet for others to import and test for itself
                            # only in this module , its' true
                            # others is false, not do the following things.
  S = 1000 * (np.random.randn(10, 10) - 0.5)
  K = 5
  B, W = ws_nmf(S, K)

  S_ = mm(B, W, B.T)
  print(S)
  print(S_)
  print(np.linalg.norm(S - S_, 2))
