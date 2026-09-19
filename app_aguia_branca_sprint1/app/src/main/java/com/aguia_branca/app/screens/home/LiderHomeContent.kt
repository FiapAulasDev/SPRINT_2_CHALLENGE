package com.aguia_branca.app.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aguia_branca.app.viewmodel.LiderViewModel
import java.math.BigDecimal

@Composable
fun LiderHomeContent(liderViewModel: LiderViewModel, onShowMessage: (String) -> Unit) {
    val dashboard by liderViewModel.dashboard.collectAsState()
    val erro by liderViewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        liderViewModel.carregarDashboard()
    }
    LaunchedEffect(erro) { erro?.let(onShowMessage) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { EstrategiasSection(liderViewModel, podeGerenciar = true, onShowMessage = onShowMessage) }
        item { Spacer(Modifier.height(24.dp)) }

        val projetos = dashboard?.projetos
        val ideias = dashboard?.ideias

        item {
            Text("Dashboard Águia Branca 📈", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = CorPrimaria)
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                MetricCard("ROI Total", formatarPercentual(projetos?.roiTotalPercentual?.toDouble()), CorPrimaria, Modifier.weight(1f))
                MetricCard("Lucro Total", formatarMoeda(projetos?.lucroTotal), CorAprovada, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                MetricCard("Investimento Total", formatarMoeda(projetos?.investimentoTotal), Color(0xFF673AB7), Modifier.weight(1f))
                MetricCard("Retorno Total", formatarMoeda(projetos?.retornoTotal), Color(0xFFE65100), Modifier.weight(1f))
            }
        }

        item {
            Text("Funil de Ideias", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CorPrimaria)
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                MetricCard("Total", "${ideias?.total ?: 0}", CorPrimaria, Modifier.weight(1f))
                MetricCard("Pendentes", "${ideias?.pendentes ?: 0}", CorPendente, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                MetricCard("Aprovadas", "${ideias?.aprovadas ?: 0}", CorAprovada, Modifier.weight(1f))
                MetricCard("Rejeitadas", "${ideias?.rejeitadas ?: 0}", CorRejeitada, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                MetricCard("% Aprovação", formatarPercentual(ideias?.percentualAprovacao), CorPrimaria, Modifier.weight(1f))
                MetricCard("Média Nota IA", "${ideias?.mediaPontuacaoIa?.let { String.format("%.1f", it) } ?: "-"}", Color(0xFF673AB7), Modifier.weight(1f))
            }
        }

        item { Text("Top Projetos por ROI", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        val topProjetos = projetos?.topProjetosPorRoi ?: emptyList()
        if (topProjetos.isEmpty()) { item { Text("Nenhum projeto cadastrado.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp)) } }

        items(topProjetos) { projeto ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(12.dp)) {
                    Text(projeto.titulo, fontWeight = FontWeight.Bold)
                    Text("Etapa: ${projeto.etapa} · Status: ${projeto.status}", color = CorPrimaria, fontSize = 12.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ROI: ${projeto.roiPercentual ?: BigDecimal.ZERO}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Retorno: ${formatarMoeda(projeto.retornoEsperado)}", fontSize = 11.sp, color = CorAprovada)
                    }
                }
            }
        }
    }
}
