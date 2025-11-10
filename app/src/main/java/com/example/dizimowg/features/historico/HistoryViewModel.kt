package com.example.dizimowg.features.historico.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dizimowg.core.network.ApiClient
import com.example.dizimowg.features.history.Lancamento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Define a "forma" do nosso estado da UI
data class HistoryUiState(
    val groupedLancamentos: Map<String, List<Lancamento>> = emptyMap(),
    val totalSum: Double = 0.0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userName: String? = null,
    val userId: Int? = null
)

class HistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchHistory()
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. Chama a API (que já retorna { userName, lancamentos, userId })
                val historyData = ApiClient.apiService.getHistory()

                // 2. Separa os dados
                val lancamentos = historyData.lancamentos
                val userName = historyData.userName
                val userId = historyData.userId

                // 3. Agrupa a lista por ano
                val grouped = lancamentos.groupBy { it.ano }.toSortedMap(compareByDescending { it })
                val total = lancamentos.sumOf { it.valor }

                // 4. Atualiza o estado com todos os dados
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userName = userName,
                        userId = userId, // Salva o ID do usuário (coddiz)
                        groupedLancamentos = grouped,
                        totalSum = total
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Falha ao buscar histórico.") }
            }
        }
    }
}