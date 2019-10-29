from __future__ import absolute_import, division, print_function, unicode_literals
import tensorflow as tf
import numpy as np
import time
import functools

# Function to load an image from a file, and add a batch dimension.
def load_img(path_to_img):
  img = tf.io.read_file(path_to_img)
  img = tf.image.decode_image(img, channels=3)
  img = tf.image.convert_image_dtype(img, tf.float32)
  img = img[tf.newaxis, :]

  return img
