# gentrans

## Prerequisites

To run `gentrans`, you need to have Java installed on your system. Please ensure you have Java Development Kit (JDK) 17 or newer.

## Installation

To install `gentrans`, follow these steps:

1. **Download the Release:**
   Download the `gentrans-1.0-SNAPSHOT.zip` file from
   the [GitHub Releases](https://github.com/hiroaki404/gentrans/releases) page.

2. **Extract the Archive:**
   Extract the contents of the downloaded `gentrans-1.0-SNAPSHOT.zip` file to your desired location.

3. **Run the Application:**
   Navigate to the extracted directory. You can run the application from the command line.
   For example:
   ```bash
   ./gentrans
   ```
   (Note: You might need to adjust the command based on your operating system and how you set up the executable.)

4. **Add to PATH**
   To run `gentrans` from any directory, you can add its extracted directory to your system's PATH environment variable.

   **For Linux/macOS:**
   Open your shell configuration file (e.g., `~/.bashrc`, `~/.zshrc`, or `~/.profile`) and add the following line,
   replacing `/path/to/gentrans` with the actual path to the extracted `gentrans` directory:
   ```bash
   export PATH="/path/to/gentrans:$PATH"
   ```
   After saving the file, apply the changes by running:
   ```bash
   source ~/.bashrc  # or ~/.zshrc, ~/.profile
   ```

   **For Windows:**
    1. Search for "Environment Variables" in the Start menu and select "Edit the system environment variables".
    2. Click "Environment Variables..."
    3. Under "System variables" or "User variables", find the "Path" variable and click "Edit...".
    4. Click "New" and add the full path to the extracted `gentrans` directory (e.g., `C:\Users\YourUser\gentrans`).
    5. Click "OK" on all windows to save the changes.

   After adding to PATH, you can run `gentrans` from any directory by simply typing:
   ```bash
   gentrans
   ```
