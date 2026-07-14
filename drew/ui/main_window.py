import sys
import json
from PySide6.QtWidgets import (QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
                               QPushButton, QLabel, QLineEdit, QListWidget, QListWidgetItem,
                               QMessageBox, QFrame, QScrollArea, QProgressBar)
from PySide6.QtCore import Qt, QTimer, QThread, Signal
from PySide6.QtGui import QFont, QColor, QPalette

from drew.engine.coordinator import Coordinator

# Use neon green for accents
NEON_GREEN = "#00E676"
DARK_BG = "#121212"
CARD_BG = "#1E1E1E"

class RefreshWorker(QThread):
    finished = Signal()

    def __init__(self, coordinator):
        super().__init__()
        self.coordinator = coordinator

    def run(self):
        self.coordinator.refresh_data()
        self.finished.emit()


class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Drew - IA d'Analyse Sportive")
        self.resize(1000, 700)

        self.coordinator = Coordinator()

        # Load config
        with open("drew/config.json", 'r') as f:
            self.config = json.load(f)

        self.setup_ui()
        self.apply_theme()

        # Setup auto refresh timer (1 hour)
        self.timer = QTimer(self)
        self.timer.timeout.connect(self.start_refresh)
        # Convert hours to ms
        interval_ms = self.config.get("update_interval_hours", 1) * 3600 * 1000
        self.timer.start(interval_ms)

        # Initial data load
        self.start_refresh()

    def apply_theme(self):
        # Dark Theme as specified in memory
        self.setStyleSheet(f"""
            QMainWindow, QWidget {{
                background-color: {DARK_BG};
                color: white;
                font-family: 'Segoe UI', Arial, sans-serif;
            }}
            QPushButton {{
                background-color: {CARD_BG};
                border: 1px solid #333;
                padding: 10px;
                border-radius: 5px;
                font-weight: bold;
            }}
            QPushButton:hover {{
                background-color: #2a2a2a;
                border: 1px solid {NEON_GREEN};
            }}
            QPushButton#ActionBtn {{
                background-color: {NEON_GREEN};
                color: black;
            }}
            QPushButton#ActionBtn:hover {{
                background-color: #00c853;
            }}
            QLineEdit {{
                background-color: {CARD_BG};
                border: 1px solid #333;
                padding: 8px;
                border-radius: 5px;
                color: white;
            }}
            QFrame#Card {{
                background-color: {CARD_BG};
                border-radius: 8px;
            }}
            QScrollArea {{
                border: none;
                background-color: transparent;
            }}
            QScrollArea > QWidget > QWidget {{
                background-color: transparent;
            }}
        """)

    def setup_ui(self):
        main_widget = QWidget()
        self.setCentralWidget(main_widget)
        main_layout = QHBoxLayout(main_widget)

        # Sidebar
        sidebar = QFrame()
        sidebar.setFixedWidth(200)
        sidebar.setStyleSheet(f"background-color: {CARD_BG}; border-right: 1px solid #333;")
        sidebar_layout = QVBoxLayout(sidebar)

        title = QLabel("DREW")
        title.setFont(QFont("Arial", 24, QFont.Bold))
        title.setStyleSheet(f"color: {NEON_GREEN}; padding-bottom: 20px;")
        title.setAlignment(Qt.AlignCenter)

        btn_dashboard = QPushButton("🏠 Tableau de bord")
        btn_history = QPushButton("📊 Historique")
        self.btn_refresh = QPushButton("🔄 Actualiser Manuellement")
        self.btn_refresh.clicked.connect(self.start_refresh)

        sidebar_layout.addWidget(title)
        sidebar_layout.addWidget(btn_dashboard)
        sidebar_layout.addWidget(btn_history)
        sidebar_layout.addStretch()
        sidebar_layout.addWidget(self.btn_refresh)

        main_layout.addWidget(sidebar)

        # Main Content
        content = QWidget()
        content_layout = QVBoxLayout(content)

        # Search area
        search_frame = QFrame()
        search_frame.setObjectName("Card")
        search_layout = QHBoxLayout(search_frame)

        search_layout.addWidget(QLabel("Cote cible minimum :"))
        self.input_min = QLineEdit("5.80")
        self.input_min.setFixedWidth(80)
        search_layout.addWidget(self.input_min)

        search_layout.addWidget(QLabel("Maximum :"))
        self.input_max = QLineEdit("6.20")
        self.input_max.setFixedWidth(80)
        search_layout.addWidget(self.input_max)

        btn_search = QPushButton("🔍 Chercher Combinaisons")
        btn_search.setObjectName("ActionBtn")
        btn_search.clicked.connect(self.find_combinations)
        search_layout.addWidget(btn_search)
        search_layout.addStretch()

        content_layout.addWidget(search_frame)

        # Progress bar
        self.progress = QProgressBar()
        self.progress.setTextVisible(False)
        self.progress.setFixedHeight(4)
        self.progress.hide()
        content_layout.addWidget(self.progress)

        # Results area
        self.results_area = QScrollArea()
        self.results_area.setWidgetResizable(True)
        self.results_widget = QWidget()
        self.results_layout = QVBoxLayout(self.results_widget)
        self.results_layout.setAlignment(Qt.AlignTop)
        self.results_area.setWidget(self.results_widget)

        content_layout.addWidget(self.results_area)
        main_layout.addWidget(content)

    def start_refresh(self):
        self.btn_refresh.setEnabled(False)
        self.btn_refresh.setText("Actualisation...")
        self.progress.setRange(0, 0) # Indeterminate
        self.progress.show()

        self.worker = RefreshWorker(self.coordinator)
        self.worker.finished.connect(self.end_refresh)
        self.worker.start()

    def end_refresh(self):
        self.btn_refresh.setEnabled(True)
        self.btn_refresh.setText("🔄 Actualiser Manuellement")
        self.progress.hide()
        QMessageBox.information(self, "Actualisation", "Les données ont été mises à jour avec succès.")

    def find_combinations(self):
        try:
            target_min = float(self.input_min.text())
            target_max = float(self.input_max.text())
        except ValueError:
            QMessageBox.warning(self, "Erreur", "Veuillez entrer des valeurs numériques valides.")
            return

        # Clear previous results
        for i in reversed(range(self.results_layout.count())):
            self.results_layout.itemAt(i).widget().setParent(None)

        combos = self.coordinator.get_combinations(target_min, target_max)

        if not combos:
            lbl = QLabel("Aucune combinaison trouvée pour cette plage de cotes.")
            lbl.setAlignment(Qt.AlignCenter)
            self.results_layout.addWidget(lbl)
            return

        for i, combo in enumerate(combos):
            card = self.create_combo_card(i+1, combo)
            self.results_layout.addWidget(card)

    def create_combo_card(self, index, combo):
        card = QFrame()
        card.setObjectName("Card")
        layout = QVBoxLayout(card)

        # Header
        header_layout = QHBoxLayout()
        title = QLabel(f"Combinaison {chr(64+index)}") # A, B, C...
        title.setFont(QFont("Arial", 16, QFont.Bold))
        title.setStyleSheet(f"color: {NEON_GREEN};")

        stats = QLabel(f"Cote Totale: <b>{combo['total_odds']}</b> | Confiance: <b>{combo['avg_confidence']}%</b>")

        header_layout.addWidget(title)
        header_layout.addStretch()
        header_layout.addWidget(stats)
        layout.addLayout(header_layout)

        # Items
        for odd in combo['items']:
            item_frame = QFrame()
            item_frame.setStyleSheet("background-color: #252525; border-radius: 5px; padding: 5px;")
            item_layout = QHBoxLayout(item_frame)

            match_lbl = QLabel(f"{odd['home_team']} vs {odd['away_team']}")
            match_lbl.setFont(QFont("Arial", 11, QFont.Bold))

            selection_lbl = QLabel(f"{odd['selection_name']} @ {odd['price']}")

            conf_lbl = QLabel(f"Score: {odd['confidence_score']}")

            item_layout.addWidget(match_lbl)
            item_layout.addStretch()
            item_layout.addWidget(selection_lbl)
            item_layout.addWidget(conf_lbl)

            layout.addWidget(item_frame)

            if odd.get('risk_flags'):
                try:
                    import json
                    flags = json.loads(odd['risk_flags'])
                    if flags:
                        flags_lbl = QLabel("⚠️ Risques: " + ", ".join(flags))
                        flags_lbl.setStyleSheet("color: #FF5252; font-size: 10px;")
                        layout.addWidget(flags_lbl)
                except:
                    pass

        return card

if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = MainWindow()
    window.show()
    sys.exit(app.exec())
