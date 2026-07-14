import sys
import json
from PySide6.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QPushButton, QStackedWidget, QListWidget, QFrame,
    QProgressBar, QTabWidget, QTextEdit, QTableWidget, QTableWidgetItem,
    QCalendarWidget, QHeaderView, QSystemTrayIcon, QMenu, QStyle,
    QLineEdit, QFormLayout, QMessageBox
)
from PySide6.QtCore import Qt, QThread, Signal
from PySide6.QtGui import QFont, QIcon
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
    def __init__(self, controller=None):
        self.controller = controller
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
        self.page_stats = self.create_stats_page()
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

        main_layout.addWidget(self.content_area)

        self.nav_list.setCurrentRow(0)

        # Setup System Tray
        self.setup_tray()

    def setup_tray(self):
        self.tray_icon = QSystemTrayIcon(self)
        self.tray_icon.setIcon(self.style().standardIcon(QStyle.SP_ComputerIcon))

        tray_menu = QMenu()
        show_action = tray_menu.addAction("Afficher BETPRO")
        show_action.triggered.connect(self.show)

        quit_action = tray_menu.addAction("Quitter")
        quit_action.triggered.connect(QApplication.instance().quit)

        self.tray_icon.setContextMenu(tray_menu)
        self.tray_icon.show()

    def send_notification(self, title, message):
        self.tray_icon.showMessage(title, message, QSystemTrayIcon.Information, 3000)

    def closeEvent(self, event):
        # Minimize to tray instead of closing
        event.ignore()
        self.hide()
        self.send_notification("BETPRO Analyst", "L'application continue de tourner en arrière-plan.")

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
        refresh_btn.clicked.connect(self.controller.refresh_data if self.controller else self.refresh_data)
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

    def create_stats_page(self):
        from ui.chart_widget import MatplotlibWidget
        page = QWidget()
        layout = QVBoxLayout(page)
        layout.setContentsMargins(30, 30, 30, 30)

        title = QLabel("Statistiques Interactives")
        title.setFont(QFont("Segoe UI", 24, QFont.Bold))
        layout.addWidget(title)

        self.chart = MatplotlibWidget(title="Forme récente")
        layout.addWidget(self.chart)

        btn = QPushButton("Charger les données du graphique")
        btn.clicked.connect(self.load_mock_chart)
        layout.addWidget(btn)

        return page

    def load_mock_chart(self):
        self.chart.plot_form(['J-4', 'J-3', 'J-2', 'J-1', 'Aujourdhui'], [40, 50, 70, 85, 90])

    def create_calendar_page(self):
        page = QWidget()
        layout = QVBoxLayout(page)
        layout.setContentsMargins(30, 30, 30, 30)

        title = QLabel("Calendrier des Matchs")
        title.setFont(QFont("Segoe UI", 24, QFont.Bold))
        layout.addWidget(title)

        self.calendar = QCalendarWidget()
        self.calendar.setStyleSheet("QCalendarWidget { background-color: #1E1E1E; color: white; }")
        self.calendar.selectionChanged.connect(self.on_date_selected)
        layout.addWidget(self.calendar)

        self.matches_list = QListWidget()
        self.matches_list.setStyleSheet("background-color: #1E1E1E; color: white; border: 1px solid #333;")
        layout.addWidget(self.matches_list)

        return page

    def on_date_selected(self):
        date = self.calendar.selectedDate().toString("yyyy-MM-dd")
        self.matches_list.clear()
        self.matches_list.addItem(f"Chargement des matchs pour le {date}...")

    def create_history_page(self):
        page = QWidget()
        layout = QVBoxLayout(page)
        layout.setContentsMargins(30, 30, 30, 30)

        title = QLabel("Historique des Analyses")
        title.setFont(QFont("Segoe UI", 24, QFont.Bold))
        layout.addWidget(title)

        self.history_table = QTableWidget, QLineEdit, QFormLayout, QMessageBox(5, 4)
        self.history_table.setHorizontalHeaderLabels(["Date", "Match", "Score", "Confiance IA"])
        self.history_table.horizontalHeader().setSectionResizeMode(QHeaderView.Stretch)
        self.history_table.setStyleSheet("QTableWidget, QLineEdit, QFormLayout, QMessageBox { background-color: #1E1E1E; color: white; gridline-color: #333; } QHeaderView::section { background-color: #2A2A2A; color: #00E676; font-weight: bold; }")

        # Populate mock data
        self.history_table.setItem(0, 0, QTableWidget, QLineEdit, QFormLayout, QMessageBoxItem("2024-05-10"))
        self.history_table.setItem(0, 1, QTableWidget, QLineEdit, QFormLayout, QMessageBoxItem("Paris SG vs Marseille"))
        self.history_table.setItem(0, 2, QTableWidget, QLineEdit, QFormLayout, QMessageBoxItem("2 - 1"))
        self.history_table.setItem(0, 3, QTableWidget, QLineEdit, QFormLayout, QMessageBoxItem("85%"))

        layout.addWidget(self.history_table)

        return page

    def create_settings_page(self):
        page = QWidget()
        layout = QVBoxLayout(page)
        layout.setContentsMargins(30, 30, 30, 30)

        title = QLabel("Paramètres")
        title.setFont(QFont("Segoe UI", 24, QFont.Bold))
        layout.addWidget(title)

        form_layout = QFormLayout()
        form_layout.setLabelAlignment(Qt.AlignRight)

        # We load actual values from config.json
        import json
        try:
            with open("config.json", "r") as f:
                self.config_data = json.load(f)
        except:
            self.config_data = {"api": {}, "ai": {}, "app": {}}

        self.api_football_input = QLineEdit(self.config_data.get("api", {}).get("api_football_key", ""))
        self.the_odds_input = QLineEdit(self.config_data.get("api", {}).get("the_odds_api_key", ""))
        self.ai_endpoint_input = QLineEdit(self.config_data.get("ai", {}).get("endpoint", ""))
        self.ai_model_input = QLineEdit(self.config_data.get("ai", {}).get("model", ""))

        for w in [self.api_football_input, self.the_odds_input, self.ai_endpoint_input, self.ai_model_input]:
            w.setStyleSheet("background-color: #2A2A2A; color: white; border: 1px solid #444; padding: 5px;")
            w.setEchoMode(QLineEdit.Password) # Mask keys visually

        # Model endpoint shouldn't be masked
        self.ai_endpoint_input.setEchoMode(QLineEdit.Normal)
        self.ai_model_input.setEchoMode(QLineEdit.Normal)

        form_layout.addRow(QLabel("API-Football Key:"), self.api_football_input)
        form_layout.addRow(QLabel("The-Odds API Key:"), self.the_odds_input)
        form_layout.addRow(QLabel("Ollama API Endpoint:"), self.ai_endpoint_input)
        form_layout.addRow(QLabel("Modèle IA (Local):"), self.ai_model_input)

        layout.addLayout(form_layout)

        save_btn = QPushButton("Sauvegarder")
        save_btn.clicked.connect(self.save_settings)
        layout.addWidget(save_btn, alignment=Qt.AlignRight)

        layout.addStretch()
        return page

    def save_settings(self):
        import json
        self.config_data["api"]["api_football_key"] = self.api_football_input.text()
        self.config_data["api"]["the_odds_api_key"] = self.the_odds_input.text()
        self.config_data["ai"]["endpoint"] = self.ai_endpoint_input.text()
        self.config_data["ai"]["model"] = self.ai_model_input.text()

        try:
            with open("config.json", "w") as f:
                json.dump(self.config_data, f, indent=4)
            QMessageBox.information(self, "Succès", "Paramètres sauvegardés avec succès !")
        except Exception as e:
            QMessageBox.critical(self, "Erreur", f"Impossible de sauvegarder: {e}")

    def trigger_betting_bot(self):
        if not hasattr(self, 'controller') or not self.controller:
            QMessageBox.warning(self, "Erreur", "Le contrôleur n'est pas connecté.")
            return

        reply = QMessageBox.question(self, "Confirmer", "Voulez-vous vraiment lancer l'automatisation via le Bot Node.js ?", QMessageBox.Yes | QMessageBox.No)
        if reply == QMessageBox.Yes:
            self.controller.place_automated_bet(12345, "Home Win", 1.95, 50)
            self.send_notification("Bot Activé", "Ordre envoyé au backend Node.js.")
