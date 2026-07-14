import sys
import json
from PySide6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QStackedWidget, QListWidget, QFrame,
    QProgressBar, QTabWidget, QTextEdit
)
from PySide6.QtCore import Qt, QThread, Signal
from PySide6.QtGui import QFont

from utils.logger import logger

class WorkerThread(QThread):
    finished = Signal(str)
    progress = Signal(int)

    def run(self):
        # Simulate loading data
        import time
        for i in range(1, 101):
            time.sleep(0.01)
            self.progress.emit(i)
        self.finished.emit("Data loaded successfully!")

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("BETPRO Analyst - Windows Desktop")
        self.resize(1200, 800)
        self.apply_theme()

        # Main Layout
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        main_layout = QHBoxLayout(central_widget)
        main_layout.setContentsMargins(0, 0, 0, 0)
        main_layout.setSpacing(0)

        # Sidebar
        self.sidebar = QFrame()
        self.sidebar.setFixedWidth(250)
        self.sidebar.setObjectName("Sidebar")
        sidebar_layout = QVBoxLayout(self.sidebar)

        title_label = QLabel("BETPRO Analyst")
        title_label.setFont(QFont("Segoe UI", 16, QFont.Bold))
        title_label.setAlignment(Qt.AlignCenter)
        title_label.setStyleSheet("color: #00E676; padding: 20px 0;")
        sidebar_layout.addWidget(title_label)

        self.nav_list = QListWidget()
        self.nav_list.setObjectName("NavList")
        self.nav_list.addItems(["Dashboard", "Calendrier", "Statistiques", "Rapports IA", "Historique", "Paramètres"])
        self.nav_list.currentRowChanged.connect(self.switch_page)
        sidebar_layout.addWidget(self.nav_list)

        # Content Area
        self.content_area = QStackedWidget()

        # Create Pages
        self.page_dashboard = self.create_dashboard_page()
        self.page_calendar = QLabel("Calendrier (Work In Progress)")
        self.page_stats = QLabel("Statistiques (Work In Progress)")
        self.page_reports = self.create_reports_page()
        self.page_history = QLabel("Historique (Work In Progress)")
        self.page_settings = QLabel("Paramètres (Work In Progress)")

        self.content_area.addWidget(self.page_dashboard)
        self.content_area.addWidget(self.page_calendar)
        self.content_area.addWidget(self.page_stats)
        self.content_area.addWidget(self.page_reports)
        self.content_area.addWidget(self.page_history)
        self.content_area.addWidget(self.page_settings)

        main_layout.addWidget(self.sidebar)
        main_layout.addWidget(self.content_area)

        self.nav_list.setCurrentRow(0)

    def create_dashboard_page(self):
        page = QWidget()
        layout = QVBoxLayout(page)
        layout.setContentsMargins(30, 30, 30, 30)

        title = QLabel("Dashboard")
        title.setFont(QFont("Segoe UI", 24, QFont.Bold))
        layout.addWidget(title)

        self.status_label = QLabel("Prêt")
        layout.addWidget(self.status_label)

        self.progress_bar = QProgressBar()
        self.progress_bar.setValue(0)
        layout.addWidget(self.progress_bar)

        refresh_btn = QPushButton("Actualiser les données")
        refresh_btn.setFixedWidth(200)
        refresh_btn.clicked.connect(self.refresh_data)
        layout.addWidget(refresh_btn)

        layout.addStretch()
        return page

    def create_reports_page(self):
        page = QWidget()
        layout = QVBoxLayout(page)
        layout.setContentsMargins(30, 30, 30, 30)

        title = QLabel("Rapports IA")
        title.setFont(QFont("Segoe UI", 24, QFont.Bold))
        layout.addWidget(title)

        self.report_text = QTextEdit()
        self.report_text.setReadOnly(True)
        self.report_text.setText("Sélectionnez un match pour générer l'analyse IA...")
        layout.addWidget(self.report_text)

        return page

    def switch_page(self, index):
        self.content_area.setCurrentIndex(index)

    def refresh_data(self):
        self.status_label.setText("Chargement en cours...")
        self.progress_bar.setValue(0)

        self.worker = WorkerThread()
        self.worker.progress.connect(self.progress_bar.setValue)
        self.worker.finished.connect(self.on_refresh_finished)
        self.worker.start()

    def on_refresh_finished(self, msg):
        self.status_label.setText(msg)

    def apply_theme(self):
        # Dark Theme definition
        dark_stylesheet = """
        QMainWindow {
            background-color: #121212;
        }
        #Sidebar {
            background-color: #1E1E1E;
            border-right: 1px solid #333333;
        }
        #NavList {
            background-color: transparent;
            border: none;
            color: #FFFFFF;
            font-size: 14px;
            font-family: 'Segoe UI';
        }
        #NavList::item {
            padding: 15px 20px;
            border-left: 3px solid transparent;
        }
        #NavList::item:selected {
            background-color: #2A2A2A;
            border-left: 3px solid #00E676;
            color: #00E676;
        }
        QLabel {
            color: #FFFFFF;
            font-family: 'Segoe UI';
        }
        QPushButton {
            background-color: #00E676;
            color: #121212;
            border: none;
            border-radius: 4px;
            padding: 10px 15px;
            font-weight: bold;
        }
        QPushButton:hover {
            background-color: #00C853;
        }
        QProgressBar {
            border: 1px solid #333333;
            border-radius: 4px;
            text-align: center;
            color: white;
            background-color: #1E1E1E;
        }
        QProgressBar::chunk {
            background-color: #00E676;
            width: 10px;
        }
        QTextEdit {
            background-color: #1E1E1E;
            color: #E0E0E0;
            border: 1px solid #333333;
            border-radius: 4px;
            padding: 10px;
            font-family: 'Segoe UI';
            font-size: 14px;
        }
        """
        self.setStyleSheet(dark_stylesheet)
