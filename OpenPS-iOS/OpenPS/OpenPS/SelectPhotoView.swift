import SwiftUI
import PhotosUI

struct SelectPhotoView: View {
    @State private var selectedImage: UIImage? = nil
    @State private var showPhotoPicker = false
    @State private var navigateToEditor = false
    
    var body: some View {
        NavigationView {
            VStack {
                Spacer()
                
                Button(action: {
                    showPhotoPicker = true
                }) {
                    HStack {
                        Image(systemName: "plus")
                        Text("Select Photo")
                    }
                    .padding()
                    .frame(maxWidth: .infinity)
                    .background(Color.green)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
                .padding(.horizontal, 32)
                .sheet(isPresented: $showPhotoPicker) {
                    PhotoPicker(selectedImage: $selectedImage, navigateToEditor: $navigateToEditor)
                }
                
                Spacer()
                
                // When the image is selected, navigate to the editor
                NavigationLink(destination: PhotoEditView(image: selectedImage), isActive: $navigateToEditor) {
                    EmptyView()
                }
            }
            .background(Color(hex: "#212121"))
            .navigationBarTitle("Open Photo Studio", displayMode: .inline)
            .navigationBarItems(trailing: Button(action: {
                // Action for settings
            }) {
                Image(systemName: "gear")
                    .foregroundColor(.white)
            })
            .toolbarBackground(Color.green, for: .navigationBar)
            .toolbarBackground(.visible, for: .navigationBar)
        }
    }
}

struct PhotoPicker: UIViewControllerRepresentable {
    @Binding var selectedImage: UIImage?
    @Binding var navigateToEditor: Bool

    func makeUIViewController(context: Context) -> PHPickerViewController {
        var configuration = PHPickerConfiguration()
        configuration.filter = .images
        configuration.selectionLimit = 1
        
        let picker = PHPickerViewController(configuration: configuration)
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: PHPickerViewController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, PHPickerViewControllerDelegate {
        var parent: PhotoPicker
        
        init(_ parent: PhotoPicker) {
            self.parent = parent
        }
        
        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            picker.dismiss(animated: true)
            
            guard let provider = results.first?.itemProvider else { return }
            
            if provider.canLoadObject(ofClass: UIImage.self) {
                provider.loadObject(ofClass: UIImage.self) { (image, error) in
                    if let uiImage = image as? UIImage {
                        DispatchQueue.main.async {
                            self.parent.selectedImage = uiImage
                            self.parent.navigateToEditor = true
                        }
                    }
                }
            }
        }
    }
}

extension Color {
    // Helper function to create Color from hex string
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

#Preview {
    SelectPhotoView()
}
