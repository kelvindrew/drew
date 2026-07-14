import sys
from PySide6.QtWidgets import QApplication
from ui.main_window import MainWindow
from utils.logger import logger
from controller import AppController

def main():
    logger.info("Starting BETPRO Analyst application...")

    app = QApplication(sys.argv)

    # Modern Windows style
    app.setStyle("Fusion")

    controller = AppController()
    window = MainWindow(controller)
    # Wiring signals
    controller.data_updated.connect(lambda d: window.status_label.setText(d.get("message", "Actualisé")))
    controller.report_generated.connect(window.receive_chat_response)

    window.show()

    logger.info("GUI loaded successfully.")
    sys.exit(app.exec())

if __name__ == "__main__":
    main()
