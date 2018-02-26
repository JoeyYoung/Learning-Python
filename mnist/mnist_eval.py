# -*- coding: utf-8 -*-
"""
Created on Mon Feb 26 19:57:26 2018

@author: xiaoy
"""
import time
import tensorflow as tf
from tensorflow.examples.tutorials.mnist import input_data

import mnist_inference 
import mnist_train

# load new module every 10 seconds, test the accuracy 
EVAL_INTERVAL_SECS = 10

def evaluate(mnist):
    with tf.Graph().as_default() as g:
        x = tf.placeholder(tf.float32, [None, mnist_inference.INPUT_NODE], name='x-input')
        y_ = tf.placeholder(tf.float32, [None, mnist_inference.OUTPUT_NODE], name='y-input')
        validate_feed = {x:mnist.validation.images,
                         y_:mnist.validation.labels}
        
        # we don't need to add the regularizer loss when test
        y = mnist_inference.inference(x, None)
        
        # calculate the acc
        # arg_max returns the idx of the maxnum in tensor (so get the belonging label)
        # when the 2rd par is 1, represents the line axis, 0 represents the row axis
        # the axis chose is the same in reduce_mean
        correct_prediction = tf.equal(tf.arg_max(y, 1), tf.arg_max(y_, 1))
        accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
        
        # load the module using rename method
        # so that can use the forward function defined in inference
        # note: restore is to extra
        
        '''
        * tf uses dictionary to connect the variable name saved and the v n loaded when save
          this process can be done in the function variables_to_restore
        
        * v = tf.Variable(0, dtype=tf.float32, name='v')
          saver = tf.train.Saver("v/ema":v)
          with ....:
              saver.restore(sess, "/path/model.ckpt")
              # so the v is just the value of previous module
        
        * use variables_to_restore to do the process
        
        * here ema is a tool to maintain the shadow variable
          that means 
              - variable_averages_op = variable_averages.apply(tf.all_variables())
                # when run this, a shadow variable will be created automatically
        
        * below is a std example used for variables of ema module
          variable_averages is the same var name as previous module (rename)
          apply v_t_r function to blind by (dictionary)
          then can save it, so when next restore(extract), can just get the variable_averages of the whole process
          
        '''
        variable_averages = tf.train.ExponentialMovingAverage(
                mnist_train.MOVING_AVERAGE_DECAY)
        variables_to_restore = variable_averages.variables_to_restore()
        saver = tf.train.Saver(variables_to_restore)
        
        # every EVAL_INTERVAL_SECS time to test acc
        while True:
            with tf.Session() as sess:
                # according to the checkpoint file
                # find the newest model file name
                ckpt = tf.train.get_checkpoint_state(
                        mnist_train.MODEL_SAVE_PATH)
                if ckpt and ckpt.model_checkpoint_path:
                    # laod model
                    saver.restore(sess, ckpt.model_checkpoint_path)
                    # get the steps saved 
                    global_step = ckpt.model_checkpoint_path.split('/')[-1].split('-')[-1]
                    accuracy_score = sess.run(accuracy, feed_dict=validate_feed)
                    print("After %s training step(s), validation "
                          "accuracy = %g" % (global_step, accuracy_score))
                else:
                    print("No checkpoint file found")
                    return
            time.sleep(EVAL_INTERVAL_SECS)
            
def main(argv=None):
    mnist = input_data.read_data_sets("C:/Users/xiaoy/evalset", one_hot=True)
    evaluate(mnist)
    
if __name__ == '__main__':
    tf.app.run()
        
