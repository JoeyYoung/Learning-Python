import random
import numpy as np
import matplotlib.pyplot as plt

plt.rcParams['font.sans-serif'] = ['SimHei']  # 用来正常显示中文标签
plt.rcParams['axes.unicode_minus'] = False  # 用来正常显示负号


def create(maxnum):
    return random.randint(0, 2 ** 31) % max(1, maxnum)


def one_in(n):
    if create(n) == 0:
        return 1
    else:
        return 0


# 画平均延迟时间-仿真时间图
def plot_avg_delivery_time(network_load, avg_delivery_time_learn, avg_delivery_time_bestatic, title):
    report_time = np.arange(100, 10001, 100)
    plt.figure(figsize=(5, 5))
    plt.plot(report_time, avg_delivery_time_learn, label='Q Routing', color='r', linestyle='-')
    plt.plot(report_time, avg_delivery_time_bestatic, label='Shortest Path', color='k', linestyle='--')
    if network_load == 1.0:
        plt.ylim(0, 30)
    else:
        plt.ylim(0, 600)
    plt.legend(loc='upper right')
    plt.xlabel('time series')
    plt.ylabel('average delay')
    plt.title(title)
    plt.savefig('./' + str(network_load) + 'time.pdf')
    plt.show()


# 画平均跳数-仿真时间图
def plot_avg_hop(network_load, avg_delivery_hop_learn, avg_delivery_hop_bestatic):
    report_time = np.arange(100, 10001, 100)
    plt.figure(figsize=(5, 5))
    plt.plot(report_time, avg_delivery_hop_learn, label='Q Routing', linestyle='-', grid='true')
    plt.plot(report_time, avg_delivery_hop_bestatic, label='Shortest Path', linestyle='--', grid='true')
    plt.ylim(0, 30)
    plt.legend(loc='upper right')
    plt.xlabel('time series')
    plt.ylabel('average transmit')
    plt.title('Situation: Network Load ' + str(network_load))
    plt.savefig('./' + str(network_load) + 'hop.pdf')
    plt.show()
