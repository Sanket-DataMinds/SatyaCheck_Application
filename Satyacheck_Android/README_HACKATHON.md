# SatyaCheck - Misinformation Detection App

## Google Hackathon Prototype

SatyaCheck is an Android application that uses AI to detect misinformation in text content. This prototype demonstrates the core functionality and has been optimized for hackathon demonstrations.

## Features

- **Text Analysis**: Analyze text content for misinformation markers
- **Multiple Languages**: Support for English, Hindi, and Marathi
- **Self-Contained**: Works offline with embedded processing
- **Educational Resources**: Articles on digital literacy and misinformation detection

## Installation Instructions

### Option 1: Direct APK Installation

1. Download the APK file from the provided link or USB drive
2. On your Android device, go to Settings → Security
3. Enable "Unknown Sources" or "Install unknown apps" for your file manager
4. Open the APK file and tap "Install"
5. Once installed, tap "Open" to launch SatyaCheck

### Option 2: Install via ADB (For Technical Evaluators)

If you prefer using ADB:

1. Connect your Android device to your computer via USB
2. Enable USB debugging in Developer Options on your device
3. Open a terminal/command prompt and run:
   ```
   adb install SatyaCheck.apk
   ```
4. The app will be installed automatically

## Using the App

1. **Launch the App**: Open SatyaCheck from your app drawer
2. **Navigate to "Analyze"**: Use the bottom navigation to go to the Analyze screen
3. **Enter or Paste Text**: Type or paste the text you want to analyze
4. **Analyze**: Tap "Analyze" to process the text
5. **View Results**: See the analysis result with verdict and explanation

## Demo Scenarios

For the best demonstration experience, try analyzing these example texts:

1. **Credible Information**:
   "Regular exercise helps improve cardiovascular health and reduces the risk of heart disease."

2. **Potentially Misleading**:
   "Scientists are shocked by this one weird trick that cures all diseases instantly!"

3. **High Misinformation Risk**:
   "5G towers are directly responsible for spreading COVID-19 through electromagnetic radiation."

4. **Scam Alert**:
   "Congratulations! You've won $10,000,000 in the international lottery. Send $500 processing fee to claim your prize immediately!"

## Technical Notes for Evaluators

- The app uses an embedded server to simulate backend functionality, making it completely self-contained
- All analysis is performed locally, enabling offline operation
- The prototype includes the complete UI and core functionality for demonstration purposes
- The app stores analysis results locally for quick access

## Troubleshooting

If you encounter any issues:

1. **App Crashes**: Try clearing app data in Settings → Apps → SatyaCheck
2. **Slow Analysis**: For long text samples, please allow additional processing time
3. **Language Issues**: Ensure your device supports the selected language

## Contact Information

For any questions or assistance during the hackathon:

- Email: [your-email@example.com]
- Phone: [your contact number]

Thank you for evaluating SatyaCheck!