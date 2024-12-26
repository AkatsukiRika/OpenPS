from onnx_tf.backend import prepare
import onnx

onnx_model = onnx.load('migan_pipeline_v2.onnx')
tf_rep = prepare(onnx_model)
tf_rep.export_graph('output/migan_pipeline_v2.pb')
