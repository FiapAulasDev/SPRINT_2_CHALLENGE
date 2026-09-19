package com.aguia_branca.app.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aguia_branca.app.data.remote.dto.IdeiaResponse
import com.aguia_branca.app.data.remote.dto.StatusIdeia
import com.aguia_branca.app.viewmodel.AuthViewModel
import com.aguia_branca.app.viewmodel.IdeiaViewModel
import com.aguia_branca.app.viewmodel.LiderViewModel

@Composable
fun OperadorHomeContent(
    authViewModel: AuthViewModel,
    ideiaViewModel: IdeiaViewModel,
    liderViewModel: LiderViewModel,
    onShowMessage: (String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var beneficioEsperado by remember { mutableStateOf("") }
    val ideias by ideiaViewModel.minhasIdeias.collectAsState()
    val erro by ideiaViewModel.errorMessage.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val ranking by authViewModel.ranking.collectAsState()
    var ideiaParaEditar by remember { mutableStateOf<IdeiaResponse?>(null) }
    var ideiaParaExcluir by remember { mutableStateOf<IdeiaResponse?>(null) }

    LaunchedEffect(Unit) {
        ideiaViewModel.carregar()
        authViewModel.recarregarUsuario()
        authViewModel.carregarRanking()
    }
    LaunchedEffect(erro) { erro?.let(onShowMessage) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { EstrategiasSection(liderViewModel, onShowMessage = onShowMessage) }
        item { Spacer(Modifier.height(24.dp)) }

        item {
            Text("Ranking de Inovação 🏆", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CorPrimaria)
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(12.dp)) {
                    if (ranking.isEmpty()) {
                        Text("Ainda não há ideias aprovadas para ranquear.", fontSize = 13.sp, color = Color.Gray)
                    }
                    ranking.forEachIndexed { index, usuario ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${index + 1}º", Modifier.width(30.dp), fontWeight = FontWeight.Bold, color = CorPrimaria)
                            Text(
                                usuario.nome, Modifier.weight(1f),
                                fontWeight = if (usuario.email == currentUser?.email) FontWeight.Bold else FontWeight.Normal
                            )
                            Text("${usuario.pontos} pts", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
        item {
            Text("Sugira uma Inovação", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = CorPrimaria)
            OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título da Ideia") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("O que pode ser melhorado?") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            OutlinedTextField(value = beneficioEsperado, onValueChange = { beneficioEsperado = it }, label = { Text("Benefício esperado") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

            Button(
                onClick = {
                    if (titulo.isNotBlank() && descricao.isNotBlank() && beneficioEsperado.isNotBlank()) {
                        ideiaViewModel.criar(titulo, descricao, beneficioEsperado,
                            onSuccess = { onShowMessage("Ideia enviada! +100 pontos se aprovada."); titulo = ""; descricao = ""; beneficioEsperado = "" },
                            onError = onShowMessage)
                    } else onShowMessage("Preencha todos os campos.")
                },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(CorPrimaria)
            ) { Text("Enviar para Gestão", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(24.dp))
            Text("Minhas Contribuições", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (ideias.isEmpty()) { item { Text("Você ainda não enviou ideias.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp)) } }

        items(ideias) { ideia ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(ideia.titulo, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (ideia.status == StatusIdeia.PENDENTE) {
                            Row {
                                IconButton(onClick = { ideiaParaEditar = ideia }) { Icon(Icons.Default.Edit, "Editar", tint = Color.Gray, modifier = Modifier.size(18.dp)) }
                                IconButton(onClick = { ideiaParaExcluir = ideia }) { Icon(Icons.Default.Delete, "Excluir", tint = Color.Red, modifier = Modifier.size(18.dp)) }
                            }
                        }
                    }
                    Text(ideia.descricao, fontSize = 13.sp, color = Color.DarkGray)
                    val corStatus = when (ideia.status) {
                        StatusIdeia.PENDENTE -> CorPendente
                        StatusIdeia.APROVADA -> CorAprovada
                        StatusIdeia.REJEITADA -> CorRejeitada
                    }
                    Text("Status: ${ideia.status}", color = corStatus, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (ideia.status == StatusIdeia.REJEITADA && !ideia.comentarioAvaliacao.isNullOrBlank()) {
                        Text("Motivo: ${ideia.comentarioAvaliacao}", fontSize = 12.sp, color = Color.Gray)
                    }
                    if (ideia.pontuacaoViabilidade != null) {
                        Text("Nota da IA: ${ideia.pontuacaoViabilidade}/100", fontSize = 12.sp, color = CorPrimaria)
                    }
                }
            }
        }
    }

    if (ideiaParaEditar != null) {
        val original = ideiaParaEditar!!
        var tempTitulo by remember(original) { mutableStateOf(original.titulo) }
        var tempDescricao by remember(original) { mutableStateOf(original.descricao) }
        var tempBeneficio by remember(original) { mutableStateOf(original.beneficioEsperado) }

        AlertDialog(
            onDismissRequest = { ideiaParaEditar = null },
            title = { Text("Editar Ideia") },
            text = {
                Column {
                    OutlinedTextField(value = tempTitulo, onValueChange = { tempTitulo = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = tempDescricao, onValueChange = { tempDescricao = it }, label = { Text("Descrição") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = tempBeneficio, onValueChange = { tempBeneficio = it }, label = { Text("Benefício esperado") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    ideiaViewModel.atualizar(original.id, tempTitulo, tempDescricao, tempBeneficio,
                        onSuccess = { onShowMessage("Ideia atualizada!"); ideiaParaEditar = null },
                        onError = onShowMessage)
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { ideiaParaEditar = null }) { Text("Cancelar") } }
        )
    }

    if (ideiaParaExcluir != null) {
        val alvo = ideiaParaExcluir!!
        AlertDialog(
            onDismissRequest = { ideiaParaExcluir = null },
            title = { Text("Excluir Ideia") },
            text = { Text("Tem certeza que deseja excluir \"${alvo.titulo}\"?") },
            confirmButton = {
                Button(onClick = {
                    ideiaViewModel.excluir(alvo.id,
                        onSuccess = { onShowMessage("Ideia excluída."); ideiaParaExcluir = null },
                        onError = onShowMessage)
                }) { Text("Excluir") }
            },
            dismissButton = { TextButton(onClick = { ideiaParaExcluir = null }) { Text("Cancelar") } }
        )
    }
}
