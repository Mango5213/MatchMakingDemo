package org.example.matchmakingdemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public enum MatchMaker {

    INSTANCE;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final ConcurrentHashMap<Long, PlayerInfo> playerPool = new ConcurrentHashMap<>();

    private final Integer NUM_MATCHING_PLAYERS = 2;

    MatchMaker() {
        scheduler.scheduleWithFixedDelay(() -> matchProcess(), 1L, 1L, TimeUnit.SECONDS);
    }

    public void putPlayerIntoPool(Long playerId, Integer rank) {
        if (playerPool.containsKey(playerId)) {
            log.warn("Found duplicated playerId [{}] when trying to put player into pool", playerId);
        }
        playerPool.put(playerId, new PlayerInfo(playerId, rank));
    }

    public void removePlayerFromPool(Long playerId) {
        if (!playerPool.containsKey(playerId)) {
            log.warn("Cannot find playerId [{}] when removing player from pool", playerId);
        }
        playerPool.remove(playerId);
    }

    private void matchProcess() {
        if (playerPool.size() < NUM_MATCHING_PLAYERS) {
            log.info("Not enough players in matchmaking, skip");
            return;
        }
        Map<Integer, SortedSet<PlayerInfo>> rankedMap = new HashMap<>();
        for (PlayerInfo playerInfo : playerPool.values()) {
            rankedMap.putIfAbsent(playerInfo.getRank(), new TreeSet<>(new Comparator<PlayerInfo>() {
                @Override
                public int compare(PlayerInfo o1, PlayerInfo o2) {
                    return o1.getStartMatchingTime().compareTo(o2.getStartMatchingTime()) == 0 ? Long.compare(o1.getPlayerId(), o2.getPlayerId()) : o1.getStartMatchingTime().compareTo(o2.getStartMatchingTime());
                }
            }));
            Set<PlayerInfo> set = rankedMap.get(playerInfo.getRank());
            set.add(playerInfo);
        }

        Set<Long> matchedIds = new HashSet<>();
        for (SortedSet<PlayerInfo> playerInfos : rankedMap.values()) {
            if (playerInfos.isEmpty()) {
                continue;
            }
            PlayerInfo longestWaiting = playerInfos.first();
            RankWrapper wrapper = calculateRank(longestWaiting);

            for (int searchRankUp = wrapper.getMiddle() + 1, searchRankDown = wrapper.getMiddle(); searchRankUp <= wrapper.getMax() || searchRankDown >= wrapper.getMin(); searchRankUp++, searchRankDown--) {
                if (NUM_MATCHING_PLAYERS.equals(matchedIds.size())) {
                    log.info("Found enough players for matchmaking");
                    break;
                }
                TreeSet<PlayerInfo> downSet = (TreeSet<PlayerInfo>) rankedMap.get(searchRankDown);
                TreeSet<PlayerInfo> upSet = (TreeSet<PlayerInfo>) rankedMap.get(searchRankUp);
                if (downSet != null) {
                    for (int i = matchedIds.size(); i < NUM_MATCHING_PLAYERS; i++) {
                        if (!downSet.isEmpty()) {
                            matchedIds.add(downSet.pollFirst().getPlayerId());
                        }
                    }
                }
                if (upSet != null) {
                    for (int i = matchedIds.size(); i < NUM_MATCHING_PLAYERS; i++) {
                        if (!upSet.isEmpty()) {
                            matchedIds.add(upSet.pollFirst().getPlayerId());
                        }
                    }
                }
                if (NUM_MATCHING_PLAYERS.equals(matchedIds.size())) {
                    log.info("Match Made!");
                    // doSomething()
                    for (Long playerId : matchedIds) {
                        playerPool.remove(playerId);
                    }
                    matchedIds.clear();
                }
            }
        }

    }

    private RankWrapper calculateRank (PlayerInfo longestWaiting) {
        int waitingTimeInSecond = LocalTime.now().getSecond() - longestWaiting.getStartMatchingTime().getSecond();

        float c2 = 1.5f;
        int c3 = 5;
        int c4 = 100;

        float u = (float) Math.pow(waitingTimeInSecond, c2);
        u = u + c3;
        u = (float) Math.round(u);
        u = Math.min(u, c4);

        int min = (longestWaiting.getRank() - (int) u) < 0 ? 0 : (longestWaiting.getRank() - (int) u);
        int max = longestWaiting.getRank() + (int) u;

        int middle = longestWaiting.getRank();

        return new RankWrapper(min, max, middle);
    }

    @Data
    @AllArgsConstructor
    private class RankWrapper {
        private int min;
        private int max;
        private int middle;
    }


}
