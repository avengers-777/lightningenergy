package com.alameda.lightningenergy.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BlockchainPlatform {
    ETHEREUM("Ethereum",16),
    BINANCE_SMART_CHAIN("Binance Smart Chain",16),
    TRON("Tron",4),
    POLKADOT("Polkadot",4),
    CARDANO("Cardano",4),
    SOLANA("Solana",4),
    AVALANCHE("Avalanche",4),
    ALGORAND("Algorand",4),
    TEZOS("Tezos",4),
    COSMOS("Cosmos",4);

    private final String name;
    private final Integer zeros;
}
