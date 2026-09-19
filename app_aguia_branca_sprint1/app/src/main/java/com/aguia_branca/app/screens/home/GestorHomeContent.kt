package com.aguia_branca.app.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aguia_branca.app.data.remote.dto.EtapaProjeto
import com.aguia_branca.app.data.remote.dto.IdeiaResponse
import com.aguia_branca.app.data.remote.dto.ProjetoRequest
import com.aguia_branca.app.data.remote.dto.ProjetoResponse
import com.aguia_branca.app.data.remote.dto.StatusIdeia
import com.aguia_branca.app.data.remote.dto.StatusProjeto
import com.aguia_branca.app.viewmodel.GestorViewModel
import com.aguia_branca.app.viewmodel.LiderViewModel
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

private fun String.paraBigDecimalOuNulo(): BigDecimal? =
    runCatching { BigDecimal(replace(",", ".")) }.getOrNull()

private fun String.paraDataOuNula(): LocalDate? =
    try { LocalDate.parse(this) } catch (e: DateTimeParseException) { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestorHomeContent(
    gestorViewModel: GestorViewModel,
    liderViewModel: LiderViewModel,
    onShowMessage: (String) -> Unit
) {
    val ideiasPendentes by gestorViewModel.ideiasPendentes.collectAsState()
    val rankingIdeias by gestorViewModel.rankingIdeias.collectAsState()
    val ideiasAprovadas by gestorViewModel.ideiasAprovadas.collectAsState()
    val projetos by gestorViewModel.projetos.collectAsState()
    val erro by gestorViewModel.errorMessage.collectAsState()

    var ideiaParaAvaliar by remember { mutableStateOf<IdeiaResponse?>(null) }
    var projetoParaEditar by remember { mutableStateOf<ProjetoResponse?>(null) }
    var projetoParaAndamento by remember { mutableStateOf<ProjetoResponse?>(null) }
    var mostrarNovoProjeto by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { gestorViewModel.carregar() }
    LaunchedEffect(erro) { erro?.let(onShowMessage) }

    val idsComProjeto = projetos.mapNotNull { it.ideiaId }.toSet()
    val ideiasDisponiveis = ideiasAprovadas.filter { it.id !in idsComProjeto }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { EstrategiasSection(liderViewModel, onShowMessage = onShowMessage) }
        item { Spacer(Modifier.height(24.dp)) }
        item { Text("Triagem de Sugestões", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CorPrimaria) }

        if (ideiasPendentes.isEmpty()) { item { Text("Nenhuma ideia aguardando avaliação.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp)) } }

        items(ideiasPendentes) { ideia ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(12.dp)) {
                    Text(ideia.titulo, fontWeight = FontWeight.Bold)
                    Text(ideia.descricao, fontSize = 13.sp)
                    Text("Benefício esperado: ${ideia.beneficioEsperado}", fontSize = 12.sp, color = Color.Gray)
                    if (ideia.pontuacaoViabilidade != null) {
                        Text("Nota da IA: ${ideia.pontuacaoViabilidade}/100 — ${ideia.recomendacaoIa ?: ""}", fontSize = 12.sp, color = CorPrimaria)
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            gestorViewModel.analisarComIa(ideia.id,
                                onSuccess = { onShowMessage("Análise da IA concluída.") },
                                onError = onShowMessage)
                        }) { Text("Analisar com IA") }
                        Button(onClick = { ideiaParaAvaliar = ideia }, modifier = Modifier.weight(1f)) {
                            Text("Avaliar")
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        item { Text("Ranking de Viabilidade (IA)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CorPrimaria) }
        if (rankingIdeias.isEmpty()) { item { Text("Nenhuma ideia no ranking.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp)) } }
        items(rankingIdeias) { ideia ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(ideia.titulo, fontWeight = FontWeight.Bold)
                        Text(ideia.status.name, fontSize = 11.sp, color = Color.Gray)
                    }
                    Text(
                        if (ideia.pontuacaoViabilidade != null) "${ideia.pontuacaoViabilidade}/100" else "Sem análise",
                        fontWeight = FontWeight.ExtraBold, color = CorPrimaria
                    )
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Projetos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CorPrimaria)
                TextButton(onClick = { mostrarNovoProjeto = true }) { Text("Novo Projeto") }
            }
        }

        if (projetos.isEmpty()) { item { Text("Nenhum projeto cadastrado.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp)) } }

        items(projetos) { projeto ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(projeto.titulo, fontWeight = FontWeight.Bold)
                            Text("Etapa: ${projeto.etapa} · Status: ${projeto.status}", fontSize = 12.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { projetoParaEditar = projeto }) { Icon(Icons.Default.Edit, "Editar", tint = CorPrimaria) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("ROI: ${projeto.roiPercentual ?: BigDecimal.ZERO}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Retorno: ${formatarMoeda(projeto.retornoEsperado)}", fontSize = 12.sp, color = CorAprovada)
                    }
                    TextButton(onClick = { projetoParaAndamento = projeto }) { Text("Atualizar andamento") }
                }
            }
        }
    }

    if (ideiaParaAvaliar != null) {
        val ideia = ideiaParaAvaliar!!
        var statusEscolhido by remember(ideia) { mutableStateOf<StatusIdeia?>(null) }
        var comentario by remember(ideia) { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { ideiaParaAvaliar = null },
            title = { Text("Avaliar Ideia") },
            text = {
                Column {
                    Text(ideia.titulo, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { statusEscolhido = StatusIdeia.APROVADA }) { Text("Aprovar") }
                        Button(onClick = { statusEscolhido = StatusIdeia.REJEITADA }) { Text("Rejeitar") }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comentario, onValueChange = { comentario = it },
                        label = { Text(if (statusEscolhido == StatusIdeia.REJEITADA) "Comentário (obrigatório)" else "Comentário (opcional)") },
                        minLines = 2, modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val status = statusEscolhido
                    if (status == null) {
                        onShowMessage("Escolha Aprovar ou Rejeitar.")
                        return@Button
                    }
                    gestorViewModel.avaliar(ideia.id, status, comentario.ifBlank { null },
                        onSuccess = { onShowMessage("Ideia avaliada!"); ideiaParaAvaliar = null },
                        onError = onShowMessage)
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { ideiaParaAvaliar = null }) { Text("Cancelar") } }
        )
    }

    if (projetoParaAndamento != null) {
        val projeto = projetoParaAndamento!!
        var etapa by remember(projeto) { mutableStateOf(projeto.etapa) }
        var status by remember(projeto) { mutableStateOf(projeto.status) }

        AlertDialog(
            onDismissRequest = { projetoParaAndamento = null },
            title = { Text("Atualizar Andamento") },
            text = {
                Column {
                    EnumDropdown("Etapa", EtapaProjeto.entries, etapa) { etapa = it }
                    Spacer(Modifier.height(8.dp))
                    EnumDropdown("Status", StatusProjeto.entries, status) { status = it }
                }
            },
            confirmButton = {
                Button(onClick = {
                    gestorViewModel.atualizarAndamento(projeto.id, etapa, status,
                        onSuccess = { onShowMessage("Andamento atualizado!"); projetoParaAndamento = null },
                        onError = onShowMessage)
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { projetoParaAndamento = null }) { Text("Cancelar") } }
        )
    }

    if (mostrarNovoProjeto || projetoParaEditar != null) {
        ProjetoFormDialog(
            projetoExistente = projetoParaEditar,
            ideiasDisponiveis = ideiasDisponiveis,
            onDismiss = { mostrarNovoProjeto = false; projetoParaEditar = null },
            onSalvar = { dados ->
                val editando = projetoParaEditar
                if (editando != null) {
                    gestorViewModel.atualizarProjeto(editando.id, dados,
                        onSuccess = { onShowMessage("Projeto atualizado!"); projetoParaEditar = null },
                        onError = onShowMessage)
                } else {
                    gestorViewModel.criarProjeto(dados,
                        onSuccess = { onShowMessage("Projeto criado!"); mostrarNovoProjeto = false },
                        onError = onShowMessage)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Enum<T>> EnumDropdown(label: String, opcoes: List<T>, selecionado: T, onSelecionar: (T) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }) {
        OutlinedTextField(
            value = selecionado.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(text = { Text(opcao.name) }, onClick = { onSelecionar(opcao); expandido = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjetoFormDialog(
    projetoExistente: ProjetoResponse?,
    ideiasDisponiveis: List<IdeiaResponse>,
    onDismiss: () -> Unit,
    onSalvar: (ProjetoRequest) -> Unit
) {
    var titulo by remember(projetoExistente) { mutableStateOf(projetoExistente?.titulo ?: "") }
    var descricao by remember(projetoExistente) { mutableStateOf(projetoExistente?.descricao ?: "") }
    var etapa by remember(projetoExistente) { mutableStateOf(projetoExistente?.etapa ?: EtapaProjeto.PLANEJAMENTO) }
    var status by remember(projetoExistente) { mutableStateOf(projetoExistente?.status ?: StatusProjeto.NAO_INICIADO) }
    var investimento by remember(projetoExistente) { mutableStateOf(projetoExistente?.investimentoPrevisto?.toPlainString() ?: "") }
    var retorno by remember(projetoExistente) { mutableStateOf(projetoExistente?.retornoEsperado?.toPlainString() ?: "") }
    var prazoInicio by remember(projetoExistente) { mutableStateOf(projetoExistente?.prazoInicio?.toString() ?: "") }
    var prazoFim by remember(projetoExistente) { mutableStateOf(projetoExistente?.prazoFim?.toString() ?: "") }
    var ideiaSelecionadaId by remember(projetoExistente) { mutableStateOf(projetoExistente?.ideiaId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (projetoExistente != null) "Editar Projeto" else "Novo Projeto") },
        text = {
            LazyColumn(Modifier.heightIn(max = 460.dp)) {
                item { OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth()) }
                item { Spacer(Modifier.height(8.dp)) }
                item { OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, minLines = 2, modifier = Modifier.fillMaxWidth()) }
                item { Spacer(Modifier.height(8.dp)) }
                item { EnumDropdown("Etapa", EtapaProjeto.entries, etapa) { etapa = it } }
                item { Spacer(Modifier.height(8.dp)) }
                item { EnumDropdown("Status", StatusProjeto.entries, status) { status = it } }
                item { Spacer(Modifier.height(8.dp)) }
                item { OutlinedTextField(value = investimento, onValueChange = { investimento = it }, label = { Text("Investimento previsto (R$)") }, modifier = Modifier.fillMaxWidth()) }
                item { Spacer(Modifier.height(8.dp)) }
                item { OutlinedTextField(value = retorno, onValueChange = { retorno = it }, label = { Text("Retorno esperado (R$)") }, modifier = Modifier.fillMaxWidth()) }
                item { Spacer(Modifier.height(8.dp)) }
                item { OutlinedTextField(value = prazoInicio, onValueChange = { prazoInicio = it }, label = { Text("Prazo início (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth()) }
                item { Spacer(Modifier.height(8.dp)) }
                item { OutlinedTextField(value = prazoFim, onValueChange = { prazoFim = it }, label = { Text("Prazo fim (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth()) }
                if (projetoExistente == null) {
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        Text("Vincular a ideia aprovada (opcional)", fontSize = 12.sp, color = Color.Gray)
                        Column {
                            ideiasDisponiveis.forEach { ideia ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(ideia.titulo, fontSize = 13.sp)
                                    TextButton(onClick = {
                                        ideiaSelecionadaId = if (ideiaSelecionadaId == ideia.id) null else ideia.id
                                    }) { Text(if (ideiaSelecionadaId == ideia.id) "Selecionada ✓" else "Selecionar") }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val investimentoValor = investimento.paraBigDecimalOuNulo()
                val retornoValor = retorno.paraBigDecimalOuNulo()
                val inicioValor = prazoInicio.paraDataOuNula()
                val fimValor = prazoFim.paraDataOuNula()

                if (titulo.isBlank() || descricao.isBlank()) return@Button
                if (investimentoValor == null || retornoValor == null) return@Button
                if (inicioValor == null || fimValor == null) return@Button

                onSalvar(
                    ProjetoRequest(
                        titulo = titulo,
                        descricao = descricao,
                        etapa = etapa,
                        status = status,
                        investimentoPrevisto = investimentoValor,
                        retornoEsperado = retornoValor,
                        prazoInicio = inicioValor,
                        prazoFim = fimValor,
                        ideiaId = ideiaSelecionadaId
                    )
                )
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
