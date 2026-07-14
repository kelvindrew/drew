import sys
from PySide6.QtWidgets import QApplication
from ui.main_window import MainWindow
from utils.logger import logger

def main():
    logger.info("Starting BETPRO Analyst application...")

    # Enable High DPI support
    # QApplication.setHighDpiScaleFactorRoundingPolicy(Qt.HighDpiScaleFactorRoundingPolicy.PassThrough)

    app = QApplication(sys.argv)

    # Modern Windows style
    app.setStyle("Fusion")

    window = MainWindow()
    window.show()

    logger.info("GUI loaded successfully.")
    sys.exit(app.exec())

if __name__ == "__main__":
    main()
