import tensorflow as tf

converter = tf.lite.TFLiteConverter.from_saved_model('output/79999_iter.pb')
tflite_model = converter.convert()
with open('output/79999_iter.tflite', 'wb') as f:
    f.write(tflite_model)
