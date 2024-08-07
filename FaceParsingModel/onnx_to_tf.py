from onnx_tf.backend import prepare
import onnx

onnx_model = onnx.load('output/79999_iter.onnx')
tf_rep = prepare(onnx_model)
tf_rep.export_graph('output/79999_iter.pb')
