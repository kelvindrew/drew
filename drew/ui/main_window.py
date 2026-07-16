import sys
import json
import os
from PySide6.QtWidgets import (QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
                               QPushButton, QLabel, QLineEdit, QListWidget, QListWidgetItem,
                               QMessageBox, QFrame, QScrollArea, QProgressBar, QSystemTrayIcon,
                               QMenu, QTabWidget, QTableWidget, QTableWidgetItem, QHeaderView, QFileDialog)
from PySide6.QtCore import Qt, QTimer, QThread, Signal
from PySide6.QtGui import QFont, QColor, QPalette, QIcon, QAction

from drew.engine.coordinator import Coordinator
from drew.utils.export import export_to_csv

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
        self.setWindowTitle("Drew - Analyse Sportive & Value")
        self.resize(1200, 800)

        self.coordinator = Coordinator()

        # Load config
        with open("drew/config.json", 'r') as f:
            self.config = json.load(f)

        self.setup_ui()
        self.apply_theme()
        self.setup_tray_icon()

        # Setup auto refresh timer
        self.timer = QTimer(self)
        self.timer.timeout.connect(self.start_refresh)
        interval_ms = self.config.get("update_interval_hours", 1) * 3600 * 1000
        self.timer.start(interval_ms)

        # Initial data load
        self.start_refresh()

    def setup_tray_icon(self):
        self.tray_icon = QSystemTrayIcon(self)
        # Create a simple transparent/color icon if no real icon exists
        icon = QIcon.fromTheme("applications-internet")
        if icon.isNull():
            from PySide6.QtGui import QPixmap
            pixmap = QPixmap(32, 32)
            pixmap.fill(QColor(NEON_GREEN))
            icon = QIcon(pixmap)

        self.tray_icon.setIcon(icon)
        self.tray_icon.setToolTip("Drew - Analyse Sportive")

        tray_menu = QMenu()
        show_action = QAction("Ouvrir Drew", self)
        show_action.triggered.connect(self.showNormal)

        refresh_action = QAction("Actualiser", self)
        refresh_action.triggered.connect(self.start_refresh)

        quit_action = QAction("Quitter", self)
        quit_action.triggered.connect(QApplication.instance().quit)

        tray_menu.addAction(show_action)
        tray_menu.addAction(refresh_action)
        tray_menu.addSeparator()
        tray_menu.addAction(quit_action)

        self.tray_icon.setContextMenu(tray_menu)
        self.tray_icon.show()

    def apply_theme(self):
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
            QTabWidget::pane {{
                border: 1px solid #333;
                background-color: {DARK_BG};
            }}
            QTabBar::tab {{
                background-color: {CARD_BG};
                color: #aaa;
                padding: 10px 20px;
                margin-right: 2px;
                border-top-left-radius: 4px;
                border-top-right-radius: 4px;
            }}
            QTabBar::tab:selected {{
                background-color: {DARK_BG};
                color: {NEON_GREEN};
                border-bottom: 2px solid {NEON_GREEN};
            }}
            QTableWidget {{
                background-color: {CARD_BG};
                gridline-color: #333;
                border: none;
            }}
            QHeaderView::section {{
                background-color: #111;
                color: white;
                padding: 5px;
                border: 1px solid #333;
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
        title.setFont(QFont("Arial", 28, QFont.Bold))
        title.setStyleSheet(f"color: {NEON_GREEN}; padding-bottom: 10px;")
        title.setAlignment(Qt.AlignCenter)

        subtitle = QLabel("AI Analytics")
        subtitle.setFont(QFont("Arial", 10))
        subtitle.setStyleSheet("color: #888; padding-bottom: 20px;")
        subtitle.setAlignment(Qt.AlignCenter)

        self.btn_refresh = QPushButton("🔄 Rafraîchir API")
        self.btn_refresh.clicked.connect(self.start_refresh)

        btn_export = QPushButton("📤 Exporter CSV")
        btn_export.clicked.connect(self.export_data)

        sidebar_layout.addWidget(title)
        sidebar_layout.addWidget(subtitle)
        sidebar_layout.addWidget(self.btn_refresh)
        sidebar_layout.addWidget(btn_export)
        sidebar_layout.addStretch()

        main_layout.addWidget(sidebar)

        # Main Content area (Tabs)
        content_layout = QVBoxLayout()

        self.tabs = QTabWidget()
        self.tab_combos = QWidget()
        self.tab_value_bets = QWidget()

        self.setup_combo_tab()
        self.setup_value_bets_tab()

        self.tabs.addTab(self.tab_combos, "🎯 Générateur de Combinaisons")
        self.tabs.addTab(self.tab_value_bets, "💎 Value Bets & Alertes")

        content_layout.addWidget(self.tabs)

        # Progress bar
        self.progress = QProgressBar()
        self.progress.setTextVisible(False)
        self.progress.setFixedHeight(4)
        self.progress.hide()
        content_layout.addWidget(self.progress)

        main_layout.addLayout(content_layout)

    def setup_combo_tab(self):
        layout = QVBoxLayout(self.tab_combos)

        # Search area
        search_frame = QFrame()
        search_frame.setObjectName("Card")
        search_layout = QHBoxLayout(search_frame)

        search_layout.addWidget(QLabel("Cote cible : De"))
        self.input_min = QLineEdit("5.00")
        self.input_min.setFixedWidth(60)
        search_layout.addWidget(self.input_min)

        search_layout.addWidget(QLabel("à"))
        self.input_max = QLineEdit("7.00")
        self.input_max.setFixedWidth(60)
        search_layout.addWidget(self.input_max)

        btn_search = QPushButton("Rechercher la combinaison idéale")
        btn_search.setObjectName("ActionBtn")
        btn_search.clicked.connect(self.find_combinations)
        search_layout.addWidget(btn_search)
        search_layout.addStretch()

        layout.addWidget(search_frame)

        # Results area
        self.results_area = QScrollArea()
        self.results_area.setWidgetResizable(True)
        self.results_widget = QWidget()
        self.results_layout = QVBoxLayout(self.results_widget)
        self.results_layout.setAlignment(Qt.AlignTop)
        self.results_area.setWidget(self.results_widget)

        layout.addWidget(self.results_area)

    def setup_value_bets_tab(self):
        layout = QVBoxLayout(self.tab_value_bets)

        info_lbl = QLabel("Événements présentant un écart important (Value / EV+) entre la cote bookmaker et notre estimation.")
        info_lbl.setStyleSheet("color: #888; margin-bottom: 10px;")
        layout.addWidget(info_lbl)

        self.table_value = QTableWidget(0, 6)
        self.table_value.setHorizontalHeaderLabels(["Match", "Marché", "Cote Bookmaker", "Probabilité (Nous)", "Cote Implicite", "Expected Value (EV)"])
        self.table_value.horizontalHeader().setSectionResizeMode(0, QHeaderView.Stretch)
        self.table_value.setEditTriggers(QTableWidget.NoEditTriggers)

        layout.addWidget(self.table_value)

    def start_refresh(self):
        self.btn_refresh.setEnabled(False)
        self.btn_refresh.setText("Actualisation...")
        self.progress.setRange(0, 0)
        self.progress.show()

        self.worker = RefreshWorker(self.coordinator)
        self.worker.finished.connect(self.end_refresh)
        self.worker.start()

    def end_refresh(self):
        self.btn_refresh.setEnabled(True)
        self.btn_refresh.setText("🔄 Rafraîchir API")
        self.progress.hide()

        # Show Windows Notification
        self.tray_icon.showMessage(
            "Drew - Analyse Terminée",
            "Les nouvelles données ont été analysées avec succès.",
            QSystemTrayIcon.Information,
            3000
        )

        self.populate_value_bets()

    def populate_value_bets(self):
        self.table_value.setRowCount(0)
        # Fetch Top EV odds
        value_bets = self.coordinator.get_value_bets()
        for row, bet in enumerate(value_bets):
            self.table_value.insertRow(row)
            self.table_value.setItem(row, 0, QTableWidgetItem(f"{bet['home_team']} vs {bet['away_team']}"))
            self.table_value.setItem(row, 1, QTableWidgetItem(bet['selection_name']))

            price_item = QTableWidgetItem(str(bet['price']))
            self.table_value.setItem(row, 2, price_item)

            prob_item = QTableWidgetItem(f"{bet['true_probability']}%")
            self.table_value.setItem(row, 3, prob_item)

            implied_item = QTableWidgetItem(str(bet['implied_odds']))
            self.table_value.setItem(row, 4, implied_item)

            ev_val = bet['expected_value']
            ev_item = QTableWidgetItem(f"{ev_val}%")
            if ev_val > 5:
                ev_item.setForeground(QColor(NEON_GREEN))
            elif ev_val < 0:
                ev_item.setForeground(QColor("#FF5252"))
            self.table_value.setItem(row, 5, ev_item)

    def find_combinations(self):
        try:
            target_min = float(self.input_min.text())
            target_max = float(self.input_max.text())
        except ValueError:
            QMessageBox.warning(self, "Erreur", "Veuillez entrer des cotes valides.")
            return

        for i in reversed(range(self.results_layout.count())):
            self.results_layout.itemAt(i).widget().setParent(None)

        combos = self.coordinator.get_combinations(target_min, target_max)

        if not combos:
            lbl = QLabel("Aucune combinaison trouvée respectant nos critères de sécurité.")
            lbl.setAlignment(Qt.AlignCenter)
            self.results_layout.addWidget(lbl)
            return

        for i, combo in enumerate(combos):
            self.results_layout.addWidget(self.create_combo_card(i+1, combo))

    def create_combo_card(self, index, combo):
        card = QFrame()
        card.setObjectName("Card")
        layout = QVBoxLayout(card)

        header_layout = QHBoxLayout()
        title = QLabel(f"Ticket #{index} - Combo {len(combo['items'])} événements")
        title.setFont(QFont("Arial", 14, QFont.Bold))
        title.setStyleSheet(f"color: {NEON_GREEN};")

        stats = QLabel(f"Cote Totale: <b>{combo['total_odds']}</b> | Confiance: <b>{combo['avg_confidence']}%</b>")

        header_layout.addWidget(title)
        header_layout.addStretch()
        header_layout.addWidget(stats)
        layout.addLayout(header_layout)

        for odd in combo['items']:
            item_frame = QFrame()
            item_frame.setStyleSheet("background-color: #252525; border-radius: 4px; padding: 5px; margin-top: 5px;")
            item_layout = QHBoxLayout(item_frame)

            match_lbl = QLabel(f"⚽ {odd['home_team']} - {odd['away_team']}")
            match_lbl.setFixedWidth(250)

            selection_lbl = QLabel(f"{odd['selection_name']}")
            selection_lbl.setStyleSheet("color: #ccc;")

            price_lbl = QLabel(f"@{odd['price']}")
            price_lbl.setFont(QFont("Arial", 10, QFont.Bold))
            price_lbl.setStyleSheet(f"color: {NEON_GREEN};")

            item_layout.addWidget(match_lbl)
            item_layout.addWidget(selection_lbl)
            item_layout.addStretch()
            item_layout.addWidget(price_lbl)

            layout.addWidget(item_frame)

            if odd.get('risk_flags'):
                try:
                    flags = json.loads(odd['risk_flags'])
                    if flags:
                        flags_lbl = QLabel("⚠️ Alerte IA: " + ", ".join(flags))
                        flags_lbl.setStyleSheet("color: #FF5252; font-size: 10px;")
                        layout.addWidget(flags_lbl)
                except:
                    pass

        return card

    def export_data(self):
        file_path, _ = QFileDialog.getSaveFileName(self, "Exporter les données", "", "CSV Files (*.csv)")
        if file_path:
            try:
                export_to_csv(self.coordinator.db, file_path)
                QMessageBox.information(self, "Export", "Données exportées avec succès.")
            except Exception as e:
                QMessageBox.critical(self, "Erreur", f"Erreur lors de l'exportation: {str(e)}")

    def closeEvent(self, event):
        # Instead of closing immediately, hide to system tray
        event.ignore()
        self.hide()
        self.tray_icon.showMessage(
            "Drew en arrière-plan",
            "Le moteur d'analyse continue de tourner. Double-cliquez sur l'icône pour l'ouvrir.",
            QSystemTrayIcon.Information,
            2000
        )
