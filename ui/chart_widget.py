import matplotlib
matplotlib.use('Qt5Agg') # Ensure backend is Qt-compatible

from PySide6.QtWidgets import QWidget, QVBoxLayout
from matplotlib.backends.backend_qt5agg import FigureCanvasQTAgg as FigureCanvas
from matplotlib.figure import Figure

class MatplotlibWidget(QWidget):
    def __init__(self, parent=None, title="Chart"):
        super().__init__(parent)
        self.figure = Figure(figsize=(5, 3), facecolor='#1E1E1E')
        self.canvas = FigureCanvas(self.figure)

        layout = QVBoxLayout(self)
        layout.setContentsMargins(0,0,0,0)
        layout.addWidget(self.canvas)

        self.ax = self.figure.add_subplot(111)
        self.ax.set_title(title, color='white')

        # Style axes for dark mode
        self.ax.set_facecolor('#1E1E1E')
        self.ax.tick_params(colors='white')
        for spine in self.ax.spines.values():
            spine.set_color('#333333')

    def plot_form(self, x_labels, y_data):
        self.ax.clear()
        self.ax.set_title("Evolution de la forme", color='white')
        self.ax.plot(x_labels, y_data, marker='o', color='#00E676', linewidth=2)

        # Restore styles after clear
        self.ax.set_facecolor('#1E1E1E')
        self.ax.tick_params(colors='white')
        for spine in self.ax.spines.values():
            spine.set_color('#333333')

        self.canvas.draw()
