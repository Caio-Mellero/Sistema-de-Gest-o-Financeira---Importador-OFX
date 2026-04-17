function renderizarGraficoDonut(nomes, valores) {
    const ctx = document.getElementById('graficoCategoria');

    if (!ctx) return; // Evita erro se o elemento não existir na página

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
            cutout: '70%' // Deixa a rosca mais fina e moderna
        }
    });
}

function inicializarDashboard(dadosDonut, dadosBarra, dadosGauge) {
    // Configuração Global do Chart.js para Tema Escuro
    Chart.defaults.color = '#71717a';
    Chart.defaults.borderColor = '#27272a';

    // 1. Gráfico de Rosca (Categorias)
    new Chart(document.getElementById('donutChart'), {
        type: 'doughnut',
        data: {
            labels: dadosDonut.labels,
            datasets: [{
                data: dadosDonut.valores,
                backgroundColor: ['#ef4444', '#3b82f6', '#10b981', '#f59e0b', '#8b5cf6'],
                borderWidth: 0
            }]
        },
        options: { responsive: true, maintainAspectRatio: false, cutout: '70%' }
    });

    // 2. Gráfico de Barras (Histórico)
    new Chart(document.getElementById('histChart'), {
        type: 'bar',
        data: {
            labels: dadosBarra.labels,
            datasets: [
                { label: 'Receitas', data: dadosBarra.receitas, backgroundColor: '#10b981' },
                { label: 'Despesas', data: dadosBarra.despesas, backgroundColor: '#ef4444' }
            ]
        },
        options: { responsive: true, maintainAspectRatio: false }
    });

    // 3. Gráfico Gauge (Orçamento)
    new Chart(document.getElementById('gaugeChart'), {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [dadosGauge.gasto, Math.max(0, dadosGauge.limite - dadosGauge.gasto)],
                backgroundColor: [dadosGauge.gasto > dadosGauge.limite ? '#ef4444' : '#3b82f6', '#27272a'],
                circumference: 180, rotation: 270, borderWidth: 0
            }]
        }
    });
}