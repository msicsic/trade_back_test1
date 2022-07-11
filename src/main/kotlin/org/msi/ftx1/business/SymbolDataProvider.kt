package org.msi.ftx1.business

// entréé, branchée sur le WS ou bien data histo pour le replay
interface SymbolDataProvider {
    fun setListener(listener: SymbolDataConsumer)
    fun start()
    fun listenSymbol(symbol: String)
}
