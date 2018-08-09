# Deep Q-learning Agent
import random
import numpy as np
from collections import deque
from keras.models import Sequential
from keras.layers import Dense
from keras.optimizers import Adam
from keras.utils import plot_model

import util

NIL = -1
# strategies
DEFAULTINIT = 0
LEARN = 1
BEST = 2
BESTATIC = 3
PRIOR = 4

# /* Special events. */
INJECT = -1   # 注入
REPORT = -2   # 报告
END_SIM = -3  # 模拟结束
UNKNOWN = -4


class DQNAgent:
    def __init__(self, state_size, action_size):
        self.state_size = state_size
        self.action_size = action_size
        self.memory = deque(maxlen=5000)
        self.gamma = 1  # discount rate
        self.epsilon = 1.0  # exploration rate
        self.epsilon_min = 0.01
        self.epsilon_decay = 0.995
        self.learning_rate = 0.001
        self.model = self._build_model()

    def _build_model(self):
        # Neural Net for Deep-Q learning Model
        model = Sequential()

        model.add(Dense(32, input_dim=self.state_size, activation='relu'))
        model.add(Dense(32, activation='relu'))
        model.add(Dense(self.action_size, activation='linear'))

        model.compile(loss='mse',
                      optimizer=Adam(lr=self.learning_rate))
        return model

    def remember(self, state, action, reward, next_state, done, future):
        self.memory.append((state, action, reward, next_state, done, future))

    # 前2000时间步长采用基于距离矢量的短路径优先，利用这段时间积累的经验训练
    # 神经网络，后面的时间利用神经网络预测action
    def act(self, state, nlinks, links, distance, shortest, now):
        n = state[0][0]
        dest = state[0][1]
        if now < 2000:
            best = distance[n][dest]
            bes_count = 0
            for action in range(nlinks[n]):
                if 1 + distance[links[n][action]][dest] == best:
                    bes_count += 1
                    if util.one_in(bes_count):
                        best_action = action
        else:
            act_values = self.model.predict(state)
            best_action = np.argmax(act_values[0])
        return best_action

    def replay(self, batch_size):
        minibatch = random.sample(self.memory, batch_size)
        for state, action, reward, next_state, done, future in minibatch:

            target = reward
            if not done:
                target = reward + self.gamma * \
                                  np.amax(future)
            target_f = self.model.predict(state)

            target_f[0][action] = target
            history = self.model.fit(state, target_f, epochs=1, verbose=0)
            # print('loss:', history.history['loss'])
        # if self.epsilon > self.epsilon_min:
        #     self.epsilon *= self.epsilon_decay

    # 将模型权重保存到指定路径，文件类型是HDF5（后缀是.h5）
    def load(self, name):
        self.model.load_weights(name)

    # 从HDF5文件中加载权重到当前模型中, 默认情况下模型的结构将保持不变。
    def save(self, name):
        self.model.save_weights(name)