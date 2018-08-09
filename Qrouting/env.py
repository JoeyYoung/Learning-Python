import numpy as np
import heapq
import collections
import math
import random

try:
    import Queue as Q  # ver. < 3.0
except ImportError:
    import queue as Q

# /* Event structure. */
class event:
    def __init__(self, time, dest):
        # /* Initialize new event. */
        self.dest = dest         # 目的地节点
        self.source = UNKNOWN    # 创建packet的节点
        self.node = UNKNOWN      # packet当前所在节点
        self.birth = time        # packet创建时间
        self.hops = 0
        self.etime = time        # packet remove时间
        self.qtime = time        # packet insert时间


# /* Special events. */
INJECT = -1   # 注入
REPORT = -2   # 报告
END_SIM = -3  # 模拟结束
UNKNOWN = -4

QL = 5
VL = 6

# /* Define. */
NIL = Nil = -1

# strategies
DEFAULTINIT = 0
LEARN = 1
BEST = 2
BESTATIC = 3
PRIOR = 4


class NetworkSimulatorEnv:
    # We init the network simulator here
    def __init__(self):
        self.viewer = None
        self.graphname = './dataNet/6x6.net'           # 加载拓扑文件
        self.done = False
        self.num_nodes = 0                             # 拓扑中节点数
        self.num_edges = 0                             # 拓扑中边数
        self.nlinks = {}                            # 每个节点的链路数
        self.links = collections.defaultdict(dict)  # 每个节点上的所有相邻节点(defaultdict方法初始化dict,value 类型也是dict)

        self.enqueued = {}                   # Time of last in line.
        self.nenqueued = {}                  # 队列中packet个数
        self.event_queue = []                # 事件队列
        self.internode = 1.0                 # packet在传输时需要的时间
        self.interqueue = 1.0
        self.interqueuen = []                # 在队列中需要等待的时间
        self.max_packets = 1000              # 网络中packet限制

        # Report variables.
        self.inter_report = 100  # report interval
        self.total_routing_time = 0.0
        self.routed_packets = 0
        self.total_hops = 0      # report interval中完成transfer的hop数
        self.node_activity = []
        self.send_fail = 0
        self.queue_full = 0      # 统计节点队列已满的数目
        self.active_packets = 0

        # before quitting?
        self.queue_packets_limit = 1000       # 节点队列中packet个数限制
        self.network_load = 1  # network load

        # Represent shortest path
        self.distance = []      # S-D距离
        self.shortest = []      # S-D最短路径对应的下一跳

        self.learn_type = VL
        self.injections = 0  # 注入
        self.events = 0

        # plot variable
        self.avg_delivery_time = []
        self.avg_hop = []
        self.report_time = []

    # 初始化仿真环境
    def init(self):
        self.readin_graph()
        self.distance = np.zeros((self.num_nodes, self.num_nodes))
        self.shortest = np.zeros((self.num_nodes, self.num_nodes))
        self.compute_best()
        self.done = False

        self.interqueuen = [self.interqueue] * self.num_nodes
        self.event_queue = []  # Q.PriorityQueue()

        # Empty all node queues.
        self.enqueued = [0.0] * self.num_nodes     # time of last in line
        self.nenqueued = [0] * self.num_nodes
        self.node_activity = [0] * self.num_nodes

        self.events = 0

        # 放入inject信号
        inject_event = event(0.0, 0)
        inject_event.source = REPORT
        inject_event.etime = 0.0
        inject_event.qtime = 0.0
        self.events += 1
        heapq.heappush(self.event_queue, ((inject_event.etime, self.events), inject_event))

        # 放入report信号
        report_event = event(0.0, 0)
        report_event.source = INJECT
        report_event.etime = self.poisson(self.network_load)
        report_event.qtime = 0.0
        self.events += 1
        heapq.heappush(self.event_queue, ((report_event.etime, self.events), report_event))

        # 重置inject信号出现次数
        self.injections = 0

    def step(self, current_event, action):

        current_time = current_event.etime
        current_node = current_event.node

        time_in_queue = current_time - current_event.qtime - self.internode        # 在队列里的时间

        # 链路有问题
        if action < 0 or action >= self.nlinks[current_node]:
            next_node = current_node
        else:
            next_node = self.links[current_node][action]

        # 处理下一跳是des的情况
        if next_node == current_event.dest:

            current_event.node = next_node  # 到达dest
            self.node_activity[current_event.node] += 1

            self.routed_packets += 1
            self.nenqueued[current_node] -= 1
            self.total_routing_time += current_time - current_event.birth + self.internode
            self.total_hops += current_event.hops + 1

            self.active_packets -= 1

            reward = time_in_queue + self.internode  # possibly change? totally random currently

            return (current_event.node, current_event.dest), reward, True, {}

        # 下一跳不是dest
        if self.nenqueued[next_node] >= self.queue_packets_limit:
            self.send_fail += 1
            next_node = current_node

        reward = time_in_queue + self.internode

        current_event.node = next_node  # do the send!
        self.node_activity[current_event.node] += 1
        current_event.hops += 1
        next_time = max(self.enqueued[next_node] + self.interqueuen[next_node],
                        current_time + self.internode)
        current_event.etime = next_time
        self.enqueued[next_node] = next_time

        current_event.qtime = current_time
        if type(current_event) == int:
            print("this is current_event:{}".format(current_event))
        self.events += 1
        heapq.heappush(self.event_queue, ((current_event.etime, self.events), current_event))
        self.events += 1

        self.nenqueued[next_node] += 1
        self.nenqueued[current_node] -= 1

        return (current_event.node, current_event.dest), reward, False, {}

    # ##########helper functions############################
    # 读取拓扑图
    def readin_graph(self):
        self.num_nodes = 0
        self.num_edges = 0

        graph_file = open(self.graphname, "r")

        for line in graph_file:
            line_contents = line.split()     # 读入一行字符，默认以空格分隔

            if line_contents[0] == '1000':  # node declaration 节点创建命令
                self.nlinks[self.num_nodes] = 0
                self.num_nodes += 1

            if line_contents[0] == '2000':  # link declaration  链路创建命令
                node1 = int(line_contents[1])
                node2 = int(line_contents[2])

                # 增加双向链路
                self.links[node1][self.nlinks[node1]] = node2
                self.nlinks[node1] += 1
                self.links[node2][self.nlinks[node2]] = node1
                self.nlinks[node2] += 1
                self.num_edges += 1

    # 在随机节点产生一个packet,随机指定目的地，并返回相应event
    def start_packet(self, time):

        if self.active_packets >= self.max_packets:
            return NIL

        source = np.random.random_integers(0, self.num_nodes - 1)       # 随机 源节点
        dest = np.random.random_integers(0, self.num_nodes - 1)         # 随机 目的地节点

        # 确保目的地节点和源节点不同
        while source == dest:
            dest = np.random.random_integers(0, self.num_nodes - 1)

        # 如果源节点队列已满的话，则不注入这个packet，返回值是Nil
        if self.nenqueued[source] > self.queue_packets_limit - 1:
            self.queue_full += 1
            return (Nil)

        self.nenqueued[source] += 1     # 将包注入queue，相当于这个节点的队列上产生了一个packet

        self.active_packets += 1
        current_event = event(time, dest)           # 产生新的packet

        # Now that we know where this packet began, set source.
        current_event.source = current_event.node = source
        return current_event

    # 仿照C语言版本中的pseudosend（预发送）
    def pseudostep(self, current_event, action):

        # 当前节点
        current_time = current_event.etime
        current_node = current_event.node
        dest = current_event.dest
        done = False

        # 计算reward
        time_in_queue = current_time - current_event.qtime - self.internode
        reward = time_in_queue + self.internode     # 排队延迟+传输延迟

        # 如果选择的action有问题
        if action < 0 or action >= self.nlinks[current_node]:
            next_node = current_node
        else:
            next_node = self.links[current_node][action]

        if next_node is dest:
            done = True
            return reward, (next_node, dest), done

        if self.nenqueued[next_node] >= self.queue_packets_limit:
            next_node = current_node
        return reward, (next_node, dest), done

    # shortest path
    def compute_best(self):

        changing = True

        for i in range(self.num_nodes):
            for j in range(self.num_nodes):
                if i == j:
                    self.distance[i][j] = 0
                else:
                    self.distance[i][j] = self.num_nodes + 1
                self.shortest[i][j] = -1

        while changing:
            changing = False
            for i in range(self.num_nodes):
                for j in range(self.num_nodes):
                    # /* Update our estimate of distance for sending from i to j. */
                    if i != j:
                        for k in range(self.nlinks[i]):
                            if self.distance[i][j] > 1 + self.distance[self.links[i][k]][j]:
                                self.distance[i][j] = 1 + self.distance[self.links[i][k]][j]  # /* Better. */
                                self.shortest[i][j] = k
                                changing = True

    # 产生符合负指数随机分布的时间间隔序列
    def poisson(self, network_load):
        while True:
            rand = random.random()
            if rand != 0 and rand != 1:
                break
        return -math.log(rand) / network_load

    # report路由效率评价
    def interactive_report(self, time, current_event, strat):
        if time > 0.0 and self.routed_packets:
            self.avg_delivery_time.append(float(self.total_routing_time) / float(self.routed_packets))
            self.avg_hop.append(float(self.total_hops) / float(self.routed_packets))
            self.report_time.append(time)

        self.routed_packets = 0
        self.total_hops = 0
        self.total_routing_time = 0
        self.node_activity = [0] * self.num_nodes

        current_event.etime += self.inter_report
        self.events += 1
        heapq.heappush(self.event_queue, ((current_event.etime, self.events), current_event))




