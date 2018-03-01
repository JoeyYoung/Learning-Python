## Some Simple Concepts to Notice
### learning rate


### loss function


### operation dependency 


### regularizer
* This is used to avoid overfitting. That means add the std into loss function to measure the complextity of the model using **L1** or **L2**
``` python
tf.contrib.layers.l2_regularizer(REGULARIZTION_RATE)
tf.contrib.layers.l1_regularizer(REGULARIZTION_RATE)
```

* So when we are trying to optimize the loss function, we actually optimize **J(θ)+λR(w)**, R(w) is the function representes the complextity, **lambda** means the rate of **complextity loss/whole loss**. One simple way in tensorflow is like below:
``` python
loss = tf.reduce_mean(tf.square(y_ - y))+tf.contrib.layer.l2_regularize(lambda)(w) 
```

* However, this is not good for code understanding. As we can regard reguarlize as a way to limit weights. So usually add the regularize loss of weights into a set named 'losses'
``` python
def get_weight_variable(shape, regularizer):
    weights = tf.get_variable("weights", shape,
                              initializer = tf.truncated_normal_initializer(stddev=0.1))
    # when regularizer is given
    if regularizer != None:
        tf.add_to_collection('losses', regularizer(weights))
    return weights
```
> **in the inference defination**
``` python
weights = get_weight_variable([INPUT_NODE, LAYER1_NODE], regularizer)
```
> **in the train defination**
``` python
loss = tf.reduce_mean(...) + tf.add_n(tf.get_collection('losses'))
```
> **or we can choose also add normal loss into collection**
> **example**
``` python
mse_loss = tf.reduce_mean(tf.square(y-cur_layer))
tf.add_to_collection('losses', mse_loss)
loss = tf.add_n(tf.get_collection('losses'))
```


### variant management
* *tf.get_variable("v", shape=[1], initializer.constant_initializer(1.0))* **&** *tf.variable_scope(name, reuse=None)*
>
> when scope's reuse set **False**, the variable shouldn't be reused, which means you can only create a new variant, but not choose the name used.
> when scope's reuse set **True**, the variant name must be the one has declared before.(occupy the memory). New create will go wrong.

* So particularily, when we are defining the process of forward boardcast, *def inference* for example, we needn't pass all the pars(weight1, biase1, weight2 ....) to the function. Using variable_scope allows us to use theses pars everywhere in the program. *seems like global?*
```python
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
```

* Need to notice that scope can be nested. Like the code above, weights in layer1 is totally seperated from weights in layer2. This can be understood as memory management:
> /layer1/weights
> /layer2/weights


### exponential moving average (add data robust)
* **tf.train.ExponentialMovingAverage** gives the function to use a decay to control the rate of module update. Specificly, the variale. For every variable, a shadow variable is being maintain.
> shadow_variable = decay \* shadow_variable + (1-decay) \* variable

* The steps of moving average always combines with persistence model. Talk later.
> global_step is widly used as pars while v_a is maintained though every save and restore.
``` python
global_step = tf.Variable(0, trainable=False)
variable_averages = tf.train.ExponentialMovingAverage(MOVING_AVERAGE_DECAY, global_step)
variable_averages_op = variable_averages.apply(tf.trainable_variables())
```


### persistence model
* when a model trained halfway, we always need to save it(pars particularly) and restore(extract and retrain) it later from the break point.
> tf.trainSaver is a class to save model, its function *restore* used to restor model. The way to use like below:
``` python
v1 = ...
v2 = ...
result = v1+v2
init = tf.initialize_all_variables()
saver = tf.train.Saver()
with tf.Session() as sess:
    sess.run(init)
    saver.save(sess, "/path/model.ckpt")
    # saver.restore(sess, "/path/model.ckpt")
    # print(sess.run(result))
```

* apply it on the moving average using **rename**. tf uses dictionary to connect the variable name saved and the v n loaded when save this process can be done in the function variables_to_restore. 
> Here ema is a tool to maintain the shadow variable that means
``` python
variable_averages_op = variable_averages.apply(tf.all_variables())
# when run this, a shadow variable will be created automatically
```
> below is a std example used for variables of ema module variable_averages is the same var name as previous module (rename), apply v_t_r function to blind by (dictionary), then can save it, so when next restore(extract), can just get the variable_averages saved previously.
> 
``` python
variable_averages = tf.train.ExponentialMovingAverage(mnist_train.MOVING_AVERAGE_DECAY)
variables_to_restore = variable_averages.variables_to_restore()
saver = tf.train.Saver(variables_to_restore)
# later saver.restore(sess, ckpt.model_checkpoint_path)
```

