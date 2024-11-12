import torch
import torch.nn as nn
import torch.nn.functional as F
import tensorflow as tf
import numpy as np
import onnx
from onnx_tf.backend import prepare
import os
from pth_to_onnx import Resnet18, AttentionRefinementModule, ConvBNReLU, BiSeNet

def convert_pytorch_to_tflite(pytorch_model, input_shape, output_path):
    """Convert PyTorch model to TFLite with FP16 quantization"""
    
    print("Step 1: Exporting to ONNX...")
    # 1. Export PyTorch model to ONNX with static shapes
    dummy_input = torch.randn(input_shape)
    temp_onnx = 'output/temp.onnx'
    
    # Set model to eval mode
    pytorch_model.eval()
    
    # Trace the model with static shapes
    traced_model = torch.jit.trace(pytorch_model, dummy_input)
    
    # Export the traced model to ONNX
    torch.onnx.export(
        traced_model,               # 使用traced模型替代原始模型
        dummy_input,
        temp_onnx,
        export_params=True,
        opset_version=11,
        do_constant_folding=True,
        input_names=['input'],
        output_names=['output', 'output_aux1', 'output_aux2'],
        dynamic_axes=None,         # 移除动态轴，使用静态shape
        verbose=True,              # 启用详细日志
        keep_initializers_as_inputs=True,
        training=torch.onnx.TrainingMode.EVAL,
    )

    print("Step 2: Loading and checking ONNX model...")
    onnx_model = onnx.load(temp_onnx)
    onnx.checker.check_model(onnx_model)
    
    print("Step 3: Converting ONNX to TensorFlow...")
    tf_rep = prepare(onnx_model)
    
    saved_model_path = 'output/saved_model'
    print(f"Step 4: Saving TensorFlow model to {saved_model_path}...")
    tf_rep.export_graph(saved_model_path)
    
    print("Step 5: Converting to TFLite...")
    converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_path)
    
    # Configure the converter
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    converter.target_spec.supported_types = [tf.float16]
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS]
    converter.allow_custom_ops = False
    converter.experimental_new_converter = True
    
    try:
        tflite_model = converter.convert()
        print(f"Step 6: Saving TFLite model to {output_path}...")
        with open(output_path, 'wb') as f:
            f.write(tflite_model)
            
        print("Conversion completed successfully!")
            
        # Cleanup
        if os.path.exists(temp_onnx):
            os.remove(temp_onnx)
        if os.path.exists(saved_model_path):
            import shutil
            shutil.rmtree(saved_model_path)
            
        return tflite_model
        
    except Exception as e:
        print(f"Error during conversion: {str(e)}")
        print("Attempting conversion with TF ops included...")
        converter.target_spec.supported_ops = [
            tf.lite.OpsSet.TFLITE_BUILTINS,
            tf.lite.OpsSet.SELECT_TF_OPS
        ]
        
        tflite_model = converter.convert()
        print(f"Saving TFLite model (with TF ops) to {output_path}...")
        with open(output_path, 'wb') as f:
            f.write(tflite_model)
            
        print("Conversion completed with TF ops included.")
        return tflite_model

# 修改 ContextPath 的 forward 方法，使用固定的大小操作
class ContextPath(nn.Module):
    def __init__(self):
        super(ContextPath, self).__init__()
        self.resnet = Resnet18()
        self.arm16 = AttentionRefinementModule(256, 128)
        self.arm32 = AttentionRefinementModule(512, 128)
        self.conv_head32 = ConvBNReLU(128, 128, ks=3, stride=1, padding=1)
        self.conv_head16 = ConvBNReLU(128, 128, ks=3, stride=1, padding=1)
        self.conv_avg = ConvBNReLU(512, 128, ks=1, stride=1, padding=0)

    def forward(self, x):
        # 使用固定尺寸的操作
        feat8, feat16, feat32 = self.resnet(x)
        
        # 计算 feat32 的平均池化
        avg = torch.mean(feat32, dim=(2, 3), keepdim=True)
        avg = self.conv_avg(avg)
        
        # 使用固定大小的上采样
        avg_up = F.interpolate(avg, size=feat32.shape[2:], mode='nearest')
        
        feat32_arm = self.arm32(feat32)
        feat32_sum = feat32_arm + avg_up
        feat32_up = F.interpolate(feat32_sum, size=feat16.shape[2:], mode='nearest')
        feat32_up = self.conv_head32(feat32_up)
        
        feat16_arm = self.arm16(feat16)
        feat16_sum = feat16_arm + feat32_up
        feat16_up = F.interpolate(feat16_sum, size=feat8.shape[2:], mode='nearest')
        feat16_up = self.conv_head16(feat16_up)
        
        return feat8, feat16_up, feat32_up

if __name__ == '__main__':
    print("Initializing model...")
    n_classes = 19
    model = BiSeNet(n_classes)
    
    print("Loading model weights...")
    model.load_state_dict(torch.load('79999_iter.pth', weights_only=True))
    model.eval()
    
    input_shape = (1, 3, 512, 512)
    output_path = 'output/79999_iter_fp16.tflite'
    
    print(f"\nStarting conversion process for BiSeNet model...")
    print(f"Input shape: {input_shape}")
    print(f"Output path: {output_path}\n")
    
    tflite_model = convert_pytorch_to_tflite(model, input_shape, output_path)
    print("\nModel conversion completed!")