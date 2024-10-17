import SwiftUI

struct PhotoEditView: View {
    var image: UIImage?
    @Binding var isActive: Bool
    @State private var selectedTab: Int = 0
    @State private var selectedItem: String? = nil // Store selected item
    
    var body: some View {
        VStack(spacing: 0) {
            // Top Menu Bar
            HStack {
                Button(action: {
                    // Action for going back
                    isActive = false
                }) {
                    Image(systemName: "arrow.left")
                        .foregroundColor(.white)
                }
                Spacer()
                Text("Photo Edit")
                    .foregroundColor(.white)
                    .font(.headline)
                Spacer()
            }
            .padding(.top, 10) // Control the amount of padding at the top
            .padding(.bottom, 10)
            .padding(.leading, 16)
            .padding(.trailing, 16)
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
            
            // Bottom control panel
            VStack {
                // Small Item List (Icon + Text)
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 20) {
                        if selectedTab == 0 {
                            // First Tab items
                            ForEach(beautifyOptions, id: \.self) { item in
                                itemButton(item: item)
                            }
                        } else {
                            // Second Tab items
                            ForEach(adjustOptions, id: \.self) { item in
                                itemButton(item: item)
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.top, 10)
                
                // Bottom Tab Bar
                HStack {
                    Button(action: {
                        selectedTab = 0
                    }) {
                        Text("Beautify")
                            .fontWeight(selectedTab == 0 ? .bold : .regular)
                            .foregroundColor(selectedTab == 0 ? .green : .gray)
                    }
                    .frame(maxWidth: .infinity)
                    
                    Button(action: {
                        selectedTab = 1
                    }) {
                        Text("Adjust")
                            .fontWeight(selectedTab == 1 ? .bold : .regular)
                            .foregroundColor(selectedTab == 1 ? .green : .gray)
                    }
                    .frame(maxWidth: .infinity)
                }
                .padding(.top, 10)
            }
            .background(Color(hex: "#212121"))
        }
        .navigationBarBackButtonHidden(true) // Hide the default back button
    }
    
    // Helper function to create item buttons with selection state
    func itemButton(item: String) -> some View {
        Button(action: {
            // Toggle selection
            if selectedItem == item {
                selectedItem = nil // Deselect if tapped again
            } else {
                selectedItem = item
            }
        }) {
            VStack {
                Image(systemName: iconForItem(item: item))
                    .resizable()
                    .frame(width: 30, height: 30)
                    .foregroundColor(selectedItem == item ? .green : .white)
                Text(item)
                    .font(.caption)
                    .foregroundColor(selectedItem == item ? .green : .white)
            }
            .padding(10)
            .background(Color.clear)
            .cornerRadius(10)
        }
    }
    
    // Helper function to get the icon name for each item
    func iconForItem(item: String) -> String {
        return "circle"
    }
    
    // Mock data for beautify and adjust options
    let beautifyOptions = ["Smooth", "White", "Lipstick", "Blusher", "Eye Zoom", "Face Slim"]
    let adjustOptions = ["Contrast", "Exposure", "Saturation", "Sharpen", "Brightness"]
}
