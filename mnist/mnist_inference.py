# -*- coding: utf-8 -*-
"""
Created on Sun Feb 25 19:46:20 2018

@author: xiaoy
"""
import tensorflow as tf

INPUT_NODE = 784
OUTPUT_NODE = 10
LAYER1_NODE = 500

# use this way to get var,
# var weights will be created when train network
def get_weight_variable(shape, regularizer):
    weights = tf.get_variable("weights", shape,
                              initializer = tf.truncated_normal_initializer(stddev=0.1))
    # when regularizer is given
    if regularizer != None:
        tf.add_to_collection('losses', regularizer(weights))
    return weights

# define the forward boardcast process
def inference(input_tensor, regularizer):
    # here in this program, it was not called many times
    # later when layer1 was multi used, scope shoud set true
    with tf.variable_scope('layer1'):
        weights = get_weight_variable([INPUT_NODE, LAYER1_NODE], regularizer)
        biases = tf.get_variable("biases", [LAYER1_NODE],
                                 initializer=tf.constant_initializer(0.0))
        layer1 = tf.nn.relu(tf.matmul(input_tensor, weights) + biases)
    
    with tf.variable_scope('layer2'):
        weights = get_weight_variable([LAYER1_NODE, OUTPUT_NODE], regularizer)
        biases = tf.get_variable("biases", [OUTPUT_NODE],
                                 initializer=tf.constant_initializer(0.0))
        layer2 = tf.matmul(layer1, weights) + biases
    return layer2

        