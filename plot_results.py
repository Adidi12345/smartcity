import csv
from pathlib import Path
import matplotlib.pyplot as plt

root = Path(__file__).resolve().parent
with (root / 'results/tables/averages.csv').open() as f:
    rows = list(csv.DictReader(f))

for column, label, filename in [('V', 'Number of vertices', 'time_vertices.png'),
                                ('E', 'Number of edges', 'time_edges.png')]:
    fig, ax = plt.subplots(figsize=(8, 5))
    for name in dict.fromkeys(row['algorithm'] for row in rows):
        data = [row for row in rows if row['algorithm'] == name]
        ax.plot([int(row[column]) for row in data],
                [float(row['average_ns']) / 1000 for row in data], marker='o', label=name)
    ax.set(xlabel=label, ylabel='Average time (microseconds)', title='Smart City graph algorithms')
    ax.grid(alpha=0.25)
    ax.legend()
    fig.tight_layout()
    fig.savefig(root / 'results/plots' / filename, dpi=160)
    plt.close(fig)
