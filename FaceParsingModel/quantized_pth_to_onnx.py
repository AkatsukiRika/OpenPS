import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.utils.model_zoo as modelzoo
import onnx
import os

# 预训练模型 URL
resnet18_url = 'https://download.pytorch.org/models/resnet18-5c106cde.pth'

def conv3x3(in_planes, out_planes, stride=1):
    """3x3 convolution with padding"""
    return nn.Conv2d(in_planes, out_planes, kernel_size=3, stride=stride,
                     padding=1, bias=False)

class BasicBlock(nn.Module):
    def __init__(self, in_chan, out_chan, stride=1):
        super(BasicBlock, self).__init__()
        self.conv1 = conv3x3(in_chan, out_chan, stride)
        self.bn1 = nn.BatchNorm2d(out_chan)
        self.conv2 = conv3x3(out_chan, out_chan)
        self.bn2 = nn.BatchNorm2d(out_chan)
        self.relu = nn.ReLU(inplace=True)
        self.downsample = None
        if in_chan != out_chan or stride != 1:
            self.downsample = nn.Sequential(
                nn.Conv2d(in_chan, out_chan,
                          kernel_size=1, stride=stride, bias=False),
                nn.BatchNorm2d(out_chan),
            )

    def forward(self, x):
        residual = self.conv1(x)
        residual = F.relu(self.bn1(residual))
        residual = self.conv2(residual)
        residual = self.bn2(residual)

        shortcut = x
        if self.downsample is not None:
            shortcut = self.downsample(x)

        out = shortcut + residual
        out = self.relu(out)
        return out

class Resnet18(nn.Module):
    def __init__(self):
        super(Resnet18, self).__init__()
        self.conv1 = nn.Conv2d(3, 64, kernel_size=7, stride=2, padding=3, bias=False)
        self.bn1 = nn.BatchNorm2d(64)
        self.maxpool = nn.MaxPool2d(kernel_size=3, stride=2, padding=1)
        self.layer1 = self.create_layer_basic(64, 64, bnum=2, stride=1)
        self.layer2 = self.create_layer_basic(64, 128, bnum=2, stride=2)
        self.layer3 = self.create_layer_basic(128, 256, bnum=2, stride=2)
        self.layer4 = self.create_layer_basic(256, 512, bnum=2, stride=2)
        self.init_weight()

    def create_layer_basic(self, in_chan, out_chan, bnum, stride=1):
        layers = [BasicBlock(in_chan, out_chan, stride=stride)]
        for i in range(bnum-1):
            layers.append(BasicBlock(out_chan, out_chan, stride=1))
        return nn.Sequential(*layers)

    def forward(self, x):
        x = self.conv1(x)
        x = F.relu(self.bn1(x))
        x = self.maxpool(x)

        x = self.layer1(x)
        feat8 = self.layer2(x)  # 1/8
        feat16 = self.layer3(feat8)  # 1/16
        feat32 = self.layer4(feat16)  # 1/32
        return feat8, feat16, feat32

    def init_weight(self):
        state_dict = modelzoo.load_url(resnet18_url)
        self_state_dict = self.state_dict()
        for k, v in state_dict.items():
            if 'fc' in k: continue
            self_state_dict.update({k: v})
        self.load_state_dict(self_state_dict)

class ConvBNReLU(nn.Module):
    def __init__(self, in_chan, out_chan, ks=3, stride=1, padding=1):
        super(ConvBNReLU, self).__init__()
        self.conv = nn.Conv2d(in_chan, out_chan, kernel_size=ks,
                             stride=stride, padding=padding, bias=False)
        self.bn = nn.BatchNorm2d(out_chan)
        self.relu = nn.ReLU(inplace=True)

    def forward(self, x):
        x = self.conv(x)
        x = self.bn(x)
        x = self.relu(x)
        return x

class AttentionRefinementModule(nn.Module):
    def __init__(self, in_chan, out_chan):
        super(AttentionRefinementModule, self).__init__()
        self.conv = ConvBNReLU(in_chan, out_chan, ks=3, stride=1, padding=1)
        self.conv_atten = nn.Conv2d(out_chan, out_chan, kernel_size=1, bias=False)
        self.bn_atten = nn.BatchNorm2d(out_chan)
        self.sigmoid_atten = nn.Sigmoid()

    def forward(self, x):
        feat = self.conv(x)
        atten = F.adaptive_avg_pool2d(feat, 1)
        atten = self.conv_atten(atten)
        atten = self.bn_atten(atten)
        atten = self.sigmoid_atten(atten)
        out = torch.mul(feat, atten)
        return out

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
        feat8, feat16, feat32 = self.resnet(x)
        
        avg = F.adaptive_avg_pool2d(feat32, 1)
        avg = self.conv_avg(avg)
        
        feat32_arm = self.arm32(feat32)
        feat32_sum = feat32_arm + F.interpolate(avg, size=feat32.shape[2:], mode='nearest')
        
        feat32_up = F.interpolate(feat32_sum, size=feat16.shape[2:], mode='nearest')
        feat32_up = self.conv_head32(feat32_up)

        feat16_arm = self.arm16(feat16)
        feat16_sum = feat16_arm + feat32_up
        feat16_up = F.interpolate(feat16_sum, size=feat8.shape[2:], mode='nearest')
        feat16_up = self.conv_head16(feat16_up)

        return feat8, feat16_up, feat32_up

class FeatureFusionModule(nn.Module):
    def __init__(self, in_chan, out_chan):
        super(FeatureFusionModule, self).__init__()
        self.convblk = ConvBNReLU(in_chan, out_chan, ks=1, stride=1, padding=0)
        self.conv1 = nn.Conv2d(out_chan, out_chan//4, kernel_size=1, stride=1, padding=0, bias=False)
        self.conv2 = nn.Conv2d(out_chan//4, out_chan, kernel_size=1, stride=1, padding=0, bias=False)
        self.relu = nn.ReLU(inplace=True)
        self.sigmoid = nn.Sigmoid()

    def forward(self, fsp, fcp):
        fcat = torch.cat([fsp, fcp], dim=1)
        feat = self.convblk(fcat)
        atten = F.adaptive_avg_pool2d(feat, 1)
        atten = self.conv1(atten)
        atten = self.relu(atten)
        atten = self.conv2(atten)
        atten = self.sigmoid(atten)
        feat_atten = torch.mul(feat, atten)
        feat_out = feat_atten + feat
        return feat_out

class BiSeNetOutput(nn.Module):
    def __init__(self, in_chan, mid_chan, n_classes):
        super(BiSeNetOutput, self).__init__()
        self.conv = ConvBNReLU(in_chan, mid_chan, ks=3, stride=1, padding=1)
        self.conv_out = nn.Conv2d(mid_chan, n_classes, kernel_size=1, bias=False)

    def forward(self, x):
        x = self.conv(x)
        x = self.conv_out(x)
        return x

class BiSeNet(nn.Module):
    def __init__(self, n_classes):
        super(BiSeNet, self).__init__()
        self.cp = ContextPath()
        self.ffm = FeatureFusionModule(256, 256)
        self.conv_out = BiSeNetOutput(256, 256, n_classes)
        self.conv_out16 = BiSeNetOutput(128, 64, n_classes)
        self.conv_out32 = BiSeNetOutput(128, 64, n_classes)

    def forward(self, x):
        H, W = x.size()[2:]
        feat_res8, feat_cp8, feat_cp16 = self.cp(x)
        feat_sp = feat_res8
        feat_fuse = self.ffm(feat_sp, feat_cp8)

        feat_out = self.conv_out(feat_fuse)
        feat_out16 = self.conv_out16(feat_cp8)
        feat_out32 = self.conv_out32(feat_cp16)

        feat_out = F.interpolate(feat_out, (H, W), mode='bilinear', align_corners=True)
        feat_out16 = F.interpolate(feat_out16, (H, W), mode='bilinear', align_corners=True)
        feat_out32 = F.interpolate(feat_out32, (H, W), mode='bilinear', align_corners=True)

        return feat_out, feat_out16, feat_out32

def convert_fp16_to_onnx(model_path, onnx_path, input_shape=(1, 3, 512, 512)):
    """
    将 FP16 PyTorch 模型转换为 ONNX 格式
    """
    # 加载模型
    checkpoint = torch.load(model_path, weights_only=True)
    n_classes = checkpoint['model_config']['n_classes']
    
    # 初始化模型
    model = BiSeNet(n_classes=n_classes)
    
    # 加载权重并设置为评估模式
    model.load_state_dict(checkpoint['model_state_dict'])
    model.eval()

    # 转换为 CUDA 和 FP16
    if torch.cuda.is_available():
        model = model.cuda()
        model = model.half()
    
    # 创建示例输入
    dummy_input = torch.randn(input_shape, dtype=torch.float16)
    if torch.cuda.is_available():
        dummy_input = dummy_input.cuda()
    
    # 导出为 ONNX
    torch.onnx.export(
        model,
        dummy_input,
        onnx_path,
        export_params=True,
        opset_version=13,
        do_constant_folding=True,
        input_names=['input'],
        output_names=['output', 'output16', 'output32'],
        dynamic_axes={
            'input': {0: 'batch_size'},
            'output': {0: 'batch_size'},
            'output16': {0: 'batch_size'},
            'output32': {0: 'batch_size'}
        }
    )
    
    # 验证导出的 ONNX 模型
    onnx_model = onnx.load(onnx_path)
    onnx.checker.check_model(onnx_model)
    
    print(f"PyTorch model size: {os.path.getsize(model_path) / (1024 * 1024):.2f} MB")
    print(f"ONNX model size: {os.path.getsize(onnx_path) / (1024 * 1024):.2f} MB")
    print("ONNX model is valid!")
    
    return onnx_model

if __name__ == '__main__':
    if not torch.cuda.is_available():
        print("Warning: CUDA is not available. This script requires GPU support.")
        exit(1)
    
    input_model = 'output/79999_iter_fp16.pth'
    output_model = 'output/79999_iter_fp16.onnx'
    
    os.makedirs('output', exist_ok=True)
    
    try:
        onnx_model = convert_fp16_to_onnx(input_model, output_model)
        print(f"Successfully converted {input_model} to {output_model}")
    except Exception as e:
        print(f"Error during conversion: {e}")