import numpy as np
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


class networkTabularQAgent(object):
    """
    Agent implementing tabular Q-learning for the NetworkSimulatorEnv.
    """

    def __init__(self, num_nodes, num_actions, distance, nlinks, strat):
        self.config = {
            "init_mean": 0.0,      # Initialize Q values with this mean（本实验未使用）
            "init_std": 0.0,       # Initialize Q values with this standard deviation（本实验未使用）
            "learning_rate": 0.7,
            "eps": 0.1,             # Epsilon in epsilon greedy policies（本实验未使用）
            "discount": 1,
            "n_iter": 10000000}        # Number of iterations（本实验未使用）

        self.q = np.zeros((num_nodes, num_nodes, num_actions))   # Q 表（需要学习的变量）

        if strat is PRIOR:
            for src in range(num_nodes):                            # 初始化Q 表
                for dest in range(num_nodes):
                    for action in range(nlinks[src]):
                        self.q[src][dest][action] = distance[src][dest]

    # 根据策略选择action，返回当前节点的链路索引号，而非结点号
    def act(self, state, nlinks, links, distance, shortest, strategy):
        n = state[0]
        dest = state[1]
        best_action = 0
        # Q-learning 从Q表中选出符合条件的link
        if (strategy is PRIOR) or (strategy is LEARN):
            best = min(self.q[n][dest][:nlinks[n]])
            bes_count = 0
            for action in range(nlinks[n]):
                if self.q[n][dest][action] == best:
                    bes_count += 1
                    if util.one_in(bes_count):
                        best_action = action
        # 任意最短路径
        elif strategy is BEST:
            best = distance[n][dest]
            bes_count = 0
            for action in range(nlinks[n]):
                if 1 + distance[links[n][action]][dest] == best:
                    bes_count += 1
                    if util.one_in(bes_count):
                        best_action = action
        # 固定的最短路径
        elif strategy is BESTATIC:
            best_action = shortest[n][dest]
        return best_action

    # 更新Q表
    def learn(self, current_event, next_event, reward, action, done, nlinks):

        n = current_event[0]    # 当前的router
        dest = current_event[1]  # 目的地

        n_next = next_event[0]   # 下一跳|当前router下action对应的值
        dest_next = next_event[1]   # 目的地保持不变

        if done:
            future = 0
        else:  # 寻找最小future
            future = min(self.q[n_next][dest][:nlinks[n_next]])
        # Q 值更新
        self.q[n][dest][action] += (reward + self.config["discount"]*future - self.q[n][dest][action]) \
                                    * self.config["learning_rate"]
