import numpy as np
import tensorflow as tf
from tensorflow.python.framework.graph_util import convert_variables_to_constants
import os

# Model parameters
W = tf.Variable([.3], tf.float32)
b = tf.Variable([-.3], tf.float32)
# Model input and output
x = tf.placeholder(tf.float32)
linear_model = W * x + b
y = tf.placeholder(tf.float32)
# loss
loss = tf.reduce_sum(tf.square(linear_model - y)) # sum of the squares
# optimizer
optimizer = tf.train.GradientDescentOptimizer(0.01)
train = optimizer.minimize(loss)
# training data
x_train = [1,2,3,4]
y_train = [0,-1,-2,-3]
# training loop
init = tf.global_variables_initializer()
sess = tf.Session()
sess.run(init) # reset values to wrong
for i in range(1000):
    sess.run(train, {x:x_train, y:y_train})

# evaluate training accuracy
#curr_W, curr_b, curr_loss  = sess.run([W, b, loss], {x:x_train, y:y_train})
#print("W: %s b: %s loss: %s"%(curr_W, curr_b, curr_loss))

output = sess.run(linear_model, {x:0.7})
print(output)

# for p in tf.global_variables():
#     print(p)

for n in tf.get_default_graph().as_graph_def().node:
   print(n.name)

RUN_DIR = os.path.abspath(os.path.curdir)
minimal_graph = convert_variables_to_constants(sess, sess.graph_def, ['add'])
tf.train.write_graph(minimal_graph, RUN_DIR, 'linear_regression_graph.proto', as_text=False)
tf.train.write_graph(minimal_graph, RUN_DIR, 'linear_regression.txt', as_text=True)
