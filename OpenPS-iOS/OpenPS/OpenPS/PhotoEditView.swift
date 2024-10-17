import SwiftUI

struct PhotoEditView: View {
    var image: UIImage?
    
    var body: some View {
        VStack {
            // Top Menu Bar
            HStack {
                Button(action: {
                    // Action for going back
                }) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(.white)
                }
                Spacer()
                Text("Photo Edit")
                    .foregroundColor(.white)
                    .font(.headline)
                Spacer()
                Button(action: {
                    // Action for saving or settings
                }) {
                    Image(systemName: "gear")
                        .foregroundColor(.white)
                }
            }
            .padding()
            .frame(maxWidth: .infinity)
            .background(Color.green) // Ensure the background fills the entire width
            
            // Display the selected image in full screen
            if let uiImage = image {
                Image(uiImage: uiImage)
                    .resizable()
                    .scaledToFit()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black)
            } else {
                // Placeholder if no image is selected
                Text("No image selected")
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color.black)
            }
        }
        .edgesIgnoringSafeArea(.top) // Extend the green bar to the top
    }
}
