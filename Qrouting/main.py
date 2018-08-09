import env as simulator
import agent as q_agent
import numpy as np
import heapq
import util
# import DQNAgent

# 1.0load for low, 3.0load for high

# Q-Routing 方法
def qlearn():
    network_loads = [1.0, 3.0]
    avg_delivery_time_dic = {}
    avg_hop_dic = {}
    for network_load in network_loads:
        env = simulator.NetworkSimulatorEnv()
        env.init()
        env.network_load = network_load
        agent = q_agent.networkTabularQAgent(env.num_nodes, env.num_edges, env.distance, env.nlinks, q_agent.LEARN)
        time_limit = 10000
        now = 0.0

        while now < time_limit:
            # 从event_queue中获取当前最先需要处理的event（其中包含了current_state）
            current_event = heapq.heappop(env.event_queue)[1]
            current_time = current_event.etime
            now = current_time

            # 这是inject信号
            if current_event.source == simulator.INJECT:
                current_event.etime += env.poisson(network_load)
                env.events += 1
                heapq.heappush(env.event_queue, ((current_event.etime, env.events), current_event))
                current_event = env.start_packet(current_time)

            # 这是report信号
            if current_event != q_agent.NIL and current_event.source == q_agent.REPORT:
                env.interactive_report(current_time, current_event, q_agent.LEARN)

            # 这是个需要转发的packet
            elif current_event != q_agent.NIL:
                # 当前state
                current_state = (current_event.node, current_event.dest)
                current_node = current_state[0]
                dest = current_state[1]

                # Full echo 将当前路由器的下一跳都执行一遍并更新Q表
                if env.learn_type is simulator.VL:
                    for action in range(env.nlinks[current_node]):
                        reward, next_state, done = env.pseudostep(current_event, action)
                        agent.learn(current_state, next_state, reward, action, done, env.nlinks)

                action = agent.act(current_state, env.nlinks, env.links, env.distance, env.shortest, q_agent.LEARN)
                next_state, reward, done, _ = env.step(current_event, action)

                agent.learn(current_state, next_state, reward, action, done, env.nlinks)

        avg_delivery_time_dic[network_load] = env.avg_delivery_time
        avg_hop_dic[network_load] = env.avg_hop

    return avg_delivery_time_dic, avg_hop_dic


# 短路径优先方法
def bestatic():
    network_loads = [1.0, 3.0]

    avg_delivery_time_dic = {}
    avg_hop_dic = {}

    for network_load in network_loads:
        env = simulator.NetworkSimulatorEnv()
        env.init()
        env.network_load = network_load
        agent = q_agent.networkTabularQAgent(env.num_nodes, env.num_edges, env.distance, env.nlinks,
                                             q_agent.DEFAULTINIT)
        time_limit = 10000
        now = 0.0

        while now < time_limit:
            # 从event_queue中获取当前最先需要处理的event（其中包含了current_state）
            current_event = heapq.heappop(env.event_queue)[1]
            current_time = current_event.etime
            now = current_time

            # 这是inject信号
            if current_event.source == simulator.INJECT:
                current_event.etime += env.poisson(network_load)
                env.events += 1
                heapq.heappush(env.event_queue, ((current_event.etime, env.events), current_event))
                current_event = env.start_packet(current_time)

            # 这是report信号
            if current_event != q_agent.NIL and current_event.source == q_agent.REPORT:
                env.interactive_report(current_time, current_event, q_agent.BESTATIC)

            # 这是个需要转发的packet
            elif current_event != q_agent.NIL:
                # 当前state
                current_state = (current_event.node, current_event.dest)

                action = agent.act(current_state, env.nlinks, env.links, env.distance, env.shortest, q_agent.BESTATIC)
                env.step(current_event, action)

        avg_delivery_time_dic[network_load] = env.avg_delivery_time
        avg_hop_dic[network_load] = env.avg_hop

    return avg_delivery_time_dic, avg_hop_dic


# Deep Q Routing方法
# def learn(time):
#     network_load = 1.0  # 网络负载级别
#     network_load_max = 4.0
#     avg_delivery_time_dic = {}
#     avg_hop_dic = {}
#     env = simulator.NetworkSimulatorEnv()
#     env.init()
#     env.network_load = network_load
#     agents = []
#     for i in range(env.num_nodes):
#         agents.append(DQNAgent.DQNAgent(2, env.nlinks[i]))
#     # agent = DQNAgent.DQNAgent(2, 36)
#     time_limit = time
#     now = 0.0
#     batch_size = 32
#     while now < time_limit:
#         # 从event_queue中获取当前最先需要处理的event（其中包含了current_state）
#         current_event = heapq.heappop(env.event_queue)[1]
#         current_time = current_event.etime
#         now = current_time
#
#         # 这是inject信号
#         if current_event.source == simulator.INJECT:
#             current_event.etime += env.poisson(network_load)
#             env.events += 1
#             heapq.heappush(env.event_queue, ((current_event.etime, env.events), current_event))
#             current_event = env.start_packet(current_time)
#
#         # 这是report信号
#         if current_event != DQNAgent.NIL and current_event.source == DQNAgent.REPORT:
#             env.interactive_report(current_time, current_event, DQNAgent.LEARN)
#
#         # 这是个需要转发的packet
#         elif current_event != DQNAgent.NIL:
#             # 当前state
#             current_state = [current_event.node, current_event.dest]
#             current_state = np.reshape(current_state, [1, 2])
#
#             # Full echo 将当前路由器的下一跳都执行一遍并更新Q表
#             # if env.learn_type is simulator.VL:
#             #     for action in range(env.nlinks[current_node]):
#             #         reward, next_state, done = env.pseudostep(current_event, action)
#             #         agent.learn(current_state, next_state, reward, action, done, env.nlinks)
#
#             action = agents[current_state[0][0]].act(current_state, env.nlinks, env.links, env.distance, env.shortest,
#                                                      now)
#             # action_index = util.router_to_index(env.links, current_state[0][0], action)
#             # next_state, reward, done, _ = env.step(current_event, action_index)
#
#             next_state, reward, done, _ = env.step(current_event, action)
#
#             next_state = np.reshape(next_state, [1, 2])
#             future = agents[next_state[0][0]].model.predict(next_state)[0]
#
#             agents[current_state[0][0]].remember(current_state, action, reward, next_state, done, future)
#             if done:
#                 # print('memory length:', len(agents[current_event.node].memory))
#                 for agent in agents:
#                     if len(agent.memory) > batch_size:
#                         agent.replay(batch_size)
#                     # if len(agents[current_state[0][0]].memory) > batch_size:
#                     #     agents[current_state[0][0]].replay(batch_size)
#
#     avg_delivery_time_dic[network_load] = env.avg_delivery_time
#     avg_hop_dic[network_load] = env.avg_hop


if __name__ == '__main__':
    avg_delivery_time_dic_bestatic, avg_hop_dic_bestatic = bestatic()

    # q-routing with shortest, in high and low situations
    avg_delivery_time_dic_qlearn, avg_hop_dic_qlearn = qlearn()


    # plot

    # util.plot_avg_delivery_time(1.0, avg_delivery_time_dic_qlearn[1], avg_delivery_time_dic_bestatic[1],
    #                             title='Low Network Load')
    # util.plot_avg_delivery_time(3.0, avg_delivery_time_dic_qlearn[3], avg_delivery_time_dic_bestatic[3],
    #                             title='High Network Load')
