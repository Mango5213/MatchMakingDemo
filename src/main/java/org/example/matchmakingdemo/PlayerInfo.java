package org.example.matchmakingdemo;

import lombok.Data;

import java.time.LocalTime;

@Data
public class PlayerInfo {

    private final Long playerId;

    private final Integer rank;

    private final LocalTime startMatchingTime;

    public PlayerInfo(Long playerId, Integer rank) {
        this.playerId = playerId;
        this.rank = rank;
        this.startMatchingTime = LocalTime.now();
    }

}
