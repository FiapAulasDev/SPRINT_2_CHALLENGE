package com.aguia_branca.app.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aguia_branca.app.data.remote.dto.EstrategiaResponse
import com.aguia_branca.app.viewmodel.LiderViewModel
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Composable
fun EstrategiasSection(
    liderViewModel: LiderViewModel,
    podeGerenciar: Boolean = false,
    onShowMessage: (String) -> Unit = {}
) {
    val estrategias by liderViewModel.estrategias.collectAsState()
    var estrategiaParaEditar by remember { mutableStateOf<EstrategiaResponse?>(null) }
    var mostrarAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { liderViewModel.carregarEstrategias() }

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Diretrizes Estratégicas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CorPrimaria)
            if (podeGerenciar) {
                IconButton(onClick = { mostrarAddDialog = true }) { Icon(Icons.Default.AddCircle, "Novo", tint = CorPrimaria) }
            }
        }

        if (estrategias.isEmpty()) {
            Text("Nenhuma diretriz estratégica disponível.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
        }

        estrategias.forEach { estrategia ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(estrategia.titulo, fontWeight = FontWeight.Bold)
                            if (estrategia.vigente) {
                                Text("VIGENTE", color = CorAprovada, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (podeGerenciar) {
                            Row {
                                IconButton(onClick = { estrategiaParaEditar = estrategia }) { Icon(Icons.Default.Edit, "Editar", tint = Color.Gray, modifier = Modifier.size(18.dp)) }
                                IconButton(onClick = {
                                    liderViewModel.excluirEstrategia(estrategia.id, onSuccess = {}, onError = onShowMessage)
                                }) { Icon(Icons.Default.Delete, "Excluir", tint = Color.Red, modifier = Modifier.size(18.dp)) }
                            }
                        }
                    }
                    Text(estrategia.descricao, fontSize = 13.sp, color = Color.DarkGray)
                    Text("Início: ${estrategia.dataInicio}${estrategia.dataFim?.let { " · Fim: $it" } ?: ""}", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }

    if (mostrarAddDialog || estrategiaParaEditar != null) {
        val editando = estrategiaParaEditar
        var tempTitulo by remember(editando) { mutableStateOf(editando?.titulo ?: "") }
        var tempDescricao by remember(editando) { mutableStateOf(editando?.descricao ?: "") }
        var tempDataInicio by remember(editando) { mutableStateOf(editando?.dataInicio?.toString() ?: "") }
        var tempDataFim by remember(editando) { mutableStateOf(editando?.dataFim?.toString() ?: "") }
        var tempVigente by remember(editando) { mutableStateOf(editando?.vigente ?: false) }

        fun fechar() { mostrarAddDialog = false; estrategiaParaEditar = null }

        AlertDialog(
            onDismissRequest = ::fechar,
            title = { Text(if (editando != null) "Editar Diretriz" else "Nova Diretriz") },
            text = {
                Column {
                    OutlinedTextField(value = tempTitulo, onValueChange = { tempTitulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = tempDescricao, onValueChange = { tempDescricao = it }, label = { Text("Descrição") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = tempDataInicio, onValueChange = { tempDataInicio = it }, label = { Text("Data início (yyyy-MM-dd)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = tempDataFim, onValueChange = { tempDataFim = it }, label = { Text("Data fim (opcional)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Vigente")
                        Switch(checked = tempVigente, onCheckedChange = { tempVigente = it })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (tempTitulo.isBlank() || tempDescricao.isBlank()) {
                        onShowMessage("Preencha título e descrição.")
                        return@Button
                    }
                    val dataInicio = try { LocalDate.parse(tempDataInicio) } catch (e: DateTimeParseException) {
                        onShowMessage("Data de início inválida (use yyyy-MM-dd).")
                        return@Button
                    }
                    val dataFim = if (tempDataFim.isBlank()) null else try {
                        LocalDate.parse(tempDataFim)
                    } catch (e: DateTimeParseException) {
                        onShowMessage("Data de fim inválida (use yyyy-MM-dd).")
                        return@Button
                    }

                    if (editando != null) {
                        liderViewModel.atualizarEstrategia(
                            editando.id, tempTitulo, tempDescricao, dataInicio, dataFim, tempVigente,
                            onSuccess = ::fechar, onError = onShowMessage
                        )
                    } else {
                        liderViewModel.criarEstrategia(
                            tempTitulo, tempDescricao, dataInicio, dataFim, tempVigente,
                            onSuccess = ::fechar, onError = onShowMessage
                        )
                    }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = ::fechar) { Text("Cancelar") } }
        )
    }
}
