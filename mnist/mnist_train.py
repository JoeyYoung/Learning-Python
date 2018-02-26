# -*- coding: utf-8 -*-
"""
Created on Mon Feb 26 15:55:54 2018

@author: xiaoy
"""
import tensorflow as tf
import os
from tensorflow.examples.tutorials.mnist import input_data

# readin the variables set & forword boardcast process function
import mnist_inference

# set the nn pars
BATCH_SIZE = 100
LEARNING_RATE_BASE = 0.8
LEARNING_RATE_DECAY = 0.99
REGULARIZTION_RATE = 0.0001
TRAINING_STEPS = 30000
MOVING_AVERAGE_DECAY = 0.99

# save the model
MODEL_SAVE_PATH = "C:/Users/xiaoy"
MODEL_NAME = "model.ckpt"

def train(mnist):
    # define the input&output place holder
    x = tf.placeholder(tf.float32, [None, mnist_inference.INPUT_NODE], name='x-input')
    y_ = tf.placeholder(tf.float32, [None, mnist_inference.OUTPUT_NODE], name='y-input')
    
    # define the regularizer function
    regularizer = tf.contrib.layers.l2_regularizer(REGULARIZTION_RATE)
    
    # use the forward function to get predict y
    y = mnist_inference.inference(x, regularizer)
    
    '''
    below define loss function, learning rate, moving average & train process
    '''
    #define moving averages
    global_step = tf.Variable(0, trainable=False)
    variable_averages = tf.train.ExponentialMovingAverage(
            MOVING_AVERAGE_DECAY, global_step)
    variable_averages_op = variable_averages.apply(
            tf.trainable_variables())
    
    # define loss: cross_entropy
    cross_entropy = tf.nn.sparse_softmax_cross_entropy_with_logits(
            y, tf.argmax(y_, 1))
    cross_entropy_mean = tf.reduce_mean(cross_entropy)
    loss = cross_entropy_mean + tf.add_n(tf.get_collection('losses'))
    
    # define learning function 
    learning_rate = tf.train.exponential_decay(
            LEARNING_RATE_BASE,
            global_step,
            mnist.train.num_examples / BATCH_SIZE,
            LEARNING_RATE_DECAY)
    
    # define train step
    train_step = tf.train.GradientDescentOptimizer(learning_rate).minimize(
            loss, global_step)
    
    # dependence of ops 
    # the dependence control represents every train step need op: t_s & v_a_op
    # based the two steps, tf.no_op means no other operations to be done
    # every op in tf.control_dependencies([a, b, ...]) should be created outside
    with tf.control_dependencies([train_step, variable_averages_op]):
        train_op = tf.no_op(name = 'train')
        
    saver = tf.train.Saver()
    with tf.Session() as sess:
        init = tf.initialize_all_variables()
        sess.run(init)
        
        # train process is seperate from test
        for i in range(TRAINING_STEPS):
            xs, ys = mnist.train.next_batch(BATCH_SIZE)
            _, loss_value, step = sess.run([train_op, loss, global_step],
                                           feed_dict={x:xs, y_:ys})
            
            # every 1000 times save the module
            if i%1000 == 0:
                print("After %d training step(s), loss on training"
                      "batch is %g." % (step, loss_value))
                saver.save(sess, os.path.join(MODEL_SAVE_PATH, MODEL_NAME),
                           global_step)

def main(argv=None):
    mnist = input_data.read_data_sets("C:/Users/xiaoy/traindata", one_hot=True)
    train(mnist)

if __name__ == '__main__':
    tf.app.run()
            