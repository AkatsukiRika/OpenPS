import torch
import torch.nn as nn
from torch.cuda.amp import autocast
import os

# 导入模型定义
from pth_to_onnx import BiSeNet

def quantize_to_fp16(model_path, output_path, n_classes=19):
    # 加载模型
    model = BiSeNet(n_classes=n_classes)
    state_dict = torch.load(model_path)
    model.load_state_dict(state_dict)
    model.eval()

    # 将模型转换为 float16
    model_fp16 = model.half()

    # 创建一个示例输入进行测试
    dummy_input = torch.randn(1, 3, 512, 512, dtype=torch.float16)

    # 使用 autocast 确保推理过程中使用 float16
    with autocast():
        # 测试模型
        try:
            outputs = model_fp16(dummy_input)
            print("Model successfully converted to FP16!")
            print(f"Original model size: {os.path.getsize(model_path) / (1024 * 1024):.2f} MB")
        except Exception as e:
            print(f"Error during inference: {e}")
            return

    # 保存量化后的模型
    torch.save({
        'model_state_dict': model_fp16.state_dict(),
        'model_config': {
            'n_classes': n_classes
        }
    }, output_path)
    
    print(f"Quantized model size: {os.path.getsize(output_path) / (1024 * 1024):.2f} MB")

def load_fp16_model(model_path):
    """
    加载 float16 模型并返回可用于推理的模型
    """
    checkpoint = torch.load(model_path)
    model_config = checkpoint['model_config']
    
    model = BiSeNet(n_classes=model_config['n_classes'])
    model = model.half()
    model.load_state_dict(checkpoint['model_state_dict'])
    model.eval()
    
    return model

if __name__ == '__main__':
    # 设置输入和输出路径
    input_model_path = '79999_iter.pth'
    output_model_path = 'output/79999_iter_fp16.pth'
    
    # 执行量化
    quantize_to_fp16(input_model_path, output_model_path)
    
    # 测试加载量化后的模型
    try:
        model_fp16 = load_fp16_model(output_model_path)
        print("FP16 model loaded successfully!")
        
        # 测试推理
        with autocast():
            dummy_input = torch.randn(1, 3, 512, 512, dtype=torch.float16)
            outputs = model_fp16(dummy_input)
            print("Inference test passed!")
    except Exception as e:
        print(f"Error during model loading or inference: {e}")