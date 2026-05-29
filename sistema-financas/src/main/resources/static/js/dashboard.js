// Configuração global do Chart.js para tema escuro
Chart.defaults.color = '#71717a';
Chart.defaults.borderColor = '#27272a';

/**
 * Gráfico de rosca (donut) por categoria de gastos.
 */
function renderizarGraficoDonut(nomes, valores) {
    const ctx = document.getElementById('graficoCategoria');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: nomes,
            datasets: [{
                data: valores,
                borderWidth: 0,
                backgroundColor: [
                    '#ef4444', '#3b82f6', '#10b981', '#f59e0b',
                    '#8b5cf6', '#ec4899', '#6366f1', '#64748b'
                ],
                hoverOffset: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { color: '#a1a1aa', font: { family: 'Inter' } }
                }
            },
            cutout: '70%'
        }
    });
}

/**
 * Gráfico de barras agrupadas com o histórico mensal real (últimos 6 meses).
 */
function renderizarGraficoHistorico(labels, entradas, saidas) {
    const ctx = document.getElementById('graficoHistorico');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Entradas',
                    data: entradas,
                    backgroundColor: 'rgba(16, 185, 129, 0.75)',
                    borderColor: '#10b981',
                    borderWidth: 1,
                    borderRadius: 4
                },
                {
                    label: 'Saídas',
                    data: saidas,
                    backgroundColor: 'rgba(239, 68, 68, 0.75)',
                    borderColor: '#ef4444',
                    borderWidth: 1,
                    borderRadius: 4
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                    labels: { color: '#a1a1aa', font: { family: 'Inter' } }
                }
            },
            scales: {
                x: { ticks: { color: '#71717a' }, grid: { color: '#27272a' } },
                y: { ticks: { color: '#71717a' }, grid: { color: '#27272a' } }
            }
        }
    });
}